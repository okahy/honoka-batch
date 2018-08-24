package jp.terasoluna.batch.honoka.b001.blogic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import jp.terasoluna.batch.honoka.cons.Param;
import jp.terasoluna.batch.honoka.cons.Path;
import jp.terasoluna.batch.honoka.cons.TsStatus;
import jp.terasoluna.batch.honoka.dto.EncData;
import jp.terasoluna.batch.honoka.dto.ProgramCountData;
import jp.terasoluna.batch.honoka.dto.ProgramData;
import jp.terasoluna.batch.honoka.dto.RecData;
import jp.terasoluna.batch.honoka.dto.TitleCountData;
import jp.terasoluna.batch.honoka.dto.TitleData;
import jp.terasoluna.batch.honoka.util.AccessShoboCal;
import jp.terasoluna.batch.honoka.util.DBUtil;
import jp.terasoluna.batch.honoka.util.StringReplacementUtil;
import jp.terasoluna.fw.batch.blogic.BLogic;
import jp.terasoluna.fw.batch.blogic.vo.BLogicParam;
import jp.terasoluna.fw.dao.QueryRowHandleDAO;
import jp.terasoluna.fw.dao.UpdateDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * B001 録画情報登録処理
 * @author hyite
 *
 */
@Component
public class B001BLogic implements BLogic {

	private static final Log log = LogFactory.getLog(B001BLogic.class);

	@Autowired
	protected UpdateDAO updateDAO;

	@Autowired
	protected QueryRowHandleDAO queryRowHandleDAO;

	// DBUtilの定義
	private DBUtil dBUtil = new DBUtil(log);

	@Override
	public int execute(BLogicParam param) {

		log.info("B001 録画情報登録処理 を開始します。");

		File dataFolder = new File(Path.PGDATA_FOLDER);
		File[] dataFiles = dataFolder.listFiles();

		// 登録数のカウント
		int fileCount = 0;

		for(File dataFile : dataFiles) {


			// JSONファイル以外はスキップ
			if (!dataFile.isFile() || !dataFile.getName().endsWith(".json")) {
				continue;
			}

			/*
			 * １．録画ファイルのチェック
			 */
			File jsonFile = this.checkRecordedFile(dataFile);
			if (jsonFile == null) {
				continue;
			}
			log.info("録画ファイル／JSONファイルが見つかりました。"
					+ jsonFile.getName() +"の登録処理を開始します。");

			/*
			 * ２．録画情報をファイルから取得
			 */
			RecData inputData = this.getRecParam(jsonFile);

			/*
			 * ３．Recへの登録
			 */
			int ret_insert_rec = dBUtil.updateDB(updateDAO, "B001.insertRecData", inputData);
			if (ret_insert_rec != 0) {
				return 255;
			}

			/*
			 * ４．登録データの取得
			 */
			RecData recData = null;
			Object recObject = dBUtil.getDBData(queryRowHandleDAO, "B001.selectRecData", inputData);
			if (recObject instanceof RecData) {
				recData = (RecData) recObject;
			}
			if (recData == null) {
				return 255;
			}

			/*
			 * ５．番組情報の取得
			 */
			AccessShoboCal asc = new AccessShoboCal(log);
			JSONObject rss2Result = asc.getShoboCalRss2(recData);
			log.info("取得番組情報：" + rss2Result);
			JSONObject dbResult = null;

			/*
			 * ６．番組情報／作品情報の登録
			 */
			if (rss2Result != null) {

				/*
				 * ６．１． 番組情報の登録
				 */
				int ret_registerProgramData = this.registerProgramData(rss2Result);
				if (ret_registerProgramData != 0) {
					return 255;
				}

				/*
				 * ６．２．作品情報の取得
				 */
				dbResult = asc.getShoboCalDB(rss2Result.getString("TID"));
				log.info("取得作品情報：" + dbResult);

				if (dbResult != null) {

					/*
					 * ６．２．１．RecDataのTID・PIDを更新する
					 */
					this.appendProgramInfo(recData, rss2Result);

					/*
					 * ６．２．２．作品情報の登録
					 */
					this.registerTitleData(dbResult);

				}

			// 番組情報が取得できなかった場合
			} else {
				log.warn("しょぼいカレンダーから番組データが取得できませんでした。");

			}

			/*
			 * ７．エンコード予約情報の登録
			 */
			EncData encData = insertEncData(recData, rss2Result);
			if (encData == null) {
				return 255;
			}

			// ts_statusの更新
			recData.setTs_status(TsStatus.ENCODE_RESERVED);
			dBUtil.updateDB(updateDAO, "B001.updateTsStatus", recData);

			fileCount ++;

		}

		log.info("B001 録画情報登録処理 を終了します。（登録数：" + fileCount + "）");
		return 0;

	}


// サブルーチン

	/**
	 * １．録画ファイルのチェック
	 * @param dataFile
	 * @return
	 */
	private File checkRecordedFile(File dataFile) {

		// m2tsファイルを定義
		File recordedFile = new File(Path.REC_FOLDER + "\\" + dataFile.getName().replace(".json", ".m2ts"));

		// m2tsファイルの存在確認
		if (!recordedFile.exists()) {
			boolean ret_move_m2ts = false;
			ret_move_m2ts = dataFile.renameTo(
					new File(Path.MOVED_UNENC_PGDATA_FOLDER + "\\" + dataFile.getName()));
			if (ret_move_m2ts) {
				log.error("動画ファイルが見つかりませんでした。JSONファイルを移動しました。");
			} else {
				log.error("動画ファイルが見つかりませんでした。また、JSONファイルの移動に失敗しました。");
			}
			return null;
		}

		// ファイル名に置換対象文字があるかチェック
		StringReplacementUtil sru = new StringReplacementUtil(log);
		String renamedRecordedFileName = sru.windowsFileName(recordedFile.getName());

		// 置換対象文字がない場合、元のデータを返却
		if (recordedFile.getName().equals(renamedRecordedFileName)) {
			return dataFile;
		}

		//置換対象文字があった場合、ファイルをリネーム
		boolean ret_rename_m2ts = false;
		ret_rename_m2ts = recordedFile.renameTo(
				new File(Path.REC_FOLDER + "\\" + renamedRecordedFileName));

		boolean ret_rename_json = false;
		File jsonFile = new File(Path.PGDATA_FOLDER + "\\" + renamedRecordedFileName.replace(".m2ts", ".json"));
		ret_rename_json = dataFile.renameTo(jsonFile);

		if (!ret_rename_m2ts || !ret_rename_json) {
			log.error("ファイルのリネームに失敗しました。"
					+ "ファイルが既に存在していないか、権限があるかを確認してください。");
		} else {
			log.info("録画ファイルに置換対象文字が含まれていたため、リネームしました。");
		}

		return jsonFile;
	}


	/**
	 * ２．録画情報をファイルから取得
	 * @param
	 * @return
	 */
	private RecData getRecParam(File dataFile) {

		JSONObject recParam = null;

		try {
			// 入力ストリームの生成（文字コード指定）
			FileInputStream fis = new FileInputStream(dataFile);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);
			// テキストファイルからの読み込み
			String json = null;
			String msg = null;;
			while ( ( msg = br.readLine()) != null ) {
				if(json == null) {
					json = msg;
				} else {
					json = json + msg + " ";
				}
			}
			br.close();

			recParam = new JSONObject(json);
			log.info("取得した録画情報：" + recParam);

		} catch(Exception e) {
			log.error("録画情報の取得に失敗しました。", e);
	    	return null;
		}

	    RecData inputData = new RecData();
		inputData.setTs_name(dataFile.getName().replace(".json", ".m2ts"));
		inputData.setTitle(recParam.getString("title"));
		if (!recParam.isNull("subtitle")) {
			inputData.setSubtitle(recParam.getString("subtitle"));
		}

		// 時刻処理
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		log.debug(sdf.format(cal.getTime()));
		TimeZone tz = TimeZone.getTimeZone("GMT");
		cal.setTimeZone(tz);
		cal.set(1970, 0, 1, 0, 0, 0);
		long start_seconds = recParam.getLong("start") / 1000;
		cal.add(Calendar.SECOND, (int)start_seconds);
		inputData.setStart_time(sdf.format(cal.getTime()));
		cal.set(1970, 0, 1, 0, 0, 0);
		long end_seconds = recParam.getLong("end") / 1000;
		cal.add(Calendar.SECOND, (int)end_seconds);
		inputData.setEnd_time(sdf.format(cal.getTime()));
		log.debug("start_time:" + inputData.getStart_time() + "/end_time:" + inputData.getEnd_time());
		inputData.setCh_name(recParam.getJSONObject("channel").getString("name"));
		inputData.setTs_status(TsStatus.RECORDED);

		boolean ret_move = false;
		ret_move = dataFile.renameTo(
				new File(Path.MOVED_PGDATA_FOLDER + "\\" + dataFile.getName())
				);
		if (!ret_move) {
			log.warn(dataFile.getName() + "の移動に失敗しました。"
					+ "同名のファイルが存在しているか、SMBの状態が悪い可能性があります。");
		}

		return inputData;

	}


	/**
	 * ６．１．番組情報の登録
	 * @param rss2Result
	 * @return
	 */
	private int registerProgramData(JSONObject rss2Result) {

		// 同じPIDのデータ登録数を取得する
		ProgramCountData programCountData = new ProgramCountData();
		programCountData.setProgram_id(Integer.parseInt(rss2Result.getString("PID")));
		Object data = dBUtil.getDBData(queryRowHandleDAO, "B001.countProgram", programCountData);
		if(data != null && data instanceof ProgramCountData) {
			programCountData = (ProgramCountData) data;
		}

		// 番組情報が登録されていない場合
		if (programCountData == null || programCountData.getProgram_count() == 0) {

			//StringReplacementUtil
			StringReplacementUtil sru = new StringReplacementUtil(log);

			// 変数の格納
			ProgramData programData = new ProgramData();
			programData.setProgram_id(rss2Result.getInt("PID"));
			programData.setTitle_id(rss2Result.getInt("TID"));
			try {
				programData.setSubtitle(sru.windowsFileName(rss2Result.getString("SubTitle")));
			} catch (JSONException e) {
				log.warn("サブタイトルの取得に失敗しました。", e);
			}

			try {
				programData.setCount(rss2Result.getString("Count"));
			} catch (Exception e) {
				log.warn("Countが取得できませんでした。", e);
			}

			programData.setCh_id(rss2Result.getInt("ChID"));

			// 時刻処理
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			log.debug(sdf.format(cal.getTime()));
			TimeZone tz = TimeZone.getTimeZone("GMT");
			cal.setTimeZone(tz);
			cal.set(1970, 0, 1, 0, 0, 0);
			cal.add(Calendar.SECOND, rss2Result.getInt("StTime"));
			programData.setStart_time(sdf.format(cal.getTime()));
			cal.set(1970, 0, 1, 0, 0, 0);
			cal.add(Calendar.SECOND, rss2Result.getInt("EdTime"));
			programData.setEnd_time(sdf.format(cal.getTime()));
			log.debug(programData.getStart_time());

			// DBへ登録
			int ret_insertProgramData = dBUtil.updateDB(updateDAO, "B001.insertProgramData", programData);
			if(ret_insertProgramData == -1) return -1;

		// 番組情報が既に登録されていた場合
		} else {
			log.warn("既に番組情報が登録されています。");
		}

		return 0;
	}


	/**
	 * ６．２．１．RecDataのTID・PIDを更新する
	 * @param recData
	 * @param rss2Result
	 * @return
	 */
	private int appendProgramInfo(RecData recData, JSONObject rss2Result) {

		int program_id = rss2Result.getInt("PID");
		int title_id = rss2Result.getInt("TID");

		recData.setProgram_id(program_id);
		recData.setTitle_id(title_id);
		int ret = dBUtil.updateDB(updateDAO, "B001.appendProgramInfo", recData);

		if (ret != 0) {
			return 255;
		} else {
			return 0;
		}
	}


	/**
	 * ６．２．２．作品情報の登録
	 * @param dbResult
	 * @return
	 */
	private int registerTitleData(JSONObject dbResult) {

		// 作品登録状況の確認
		TitleCountData titleCountData = new TitleCountData();
		titleCountData.setTitle_id(dbResult.getInt("TID"));
		Object ret_TitleCount = dBUtil.getDBData(queryRowHandleDAO, "B001.countTitle", titleCountData);
		if(ret_TitleCount != null && ret_TitleCount instanceof TitleCountData) {
			titleCountData = (TitleCountData) ret_TitleCount;
		} else {
			log.error("作品情報登録状況をDBから取得できませんでした。");
			return 255;
		}
		if (titleCountData != null && titleCountData.getTitle_count() > 0) {
			log.info("既に作品情報が登録されています。");
			return 0;
		}

		// 変数の格納
		TitleData titleData = new TitleData();

		//StringReplacementUtil
		StringReplacementUtil sru = new StringReplacementUtil(log);

		// TID
		titleData.setTitle_id(dbResult.getInt("TID"));

		// Title
		String title = sru.windowsFileName(dbResult.getString("Title"));
		titleData.setTitle(title);

		// Title_kana
		String title_kana = null;
		if (!dbResult.isNull("TitleYomi")){
			title_kana = dbResult.getString("TitleYomi");
		}
		if (title_kana != null && !title_kana.equals("")) {
			titleData.setTitle_kana(title_kana);
			titleData.setDestination(title_kana.substring(0, 1) + "-" + title);
		} else if (title.substring(0, 1).matches("^[\\u3040-\\u309F]+$")){
			titleData.setDestination(title.substring(0, 1) + "-" + title);
		} else {
			titleData.setDestination("＃-" + title);
		}

		// Short_title
		if(!dbResult.getString("ShortTitle").equals("")){
			titleData.setShort_title(
					sru.windowsFileName(dbResult.getString("ShortTitle")));
		}

		// Frist_broadcast
		String first_month = String.valueOf(dbResult.getInt("FirstMonth"));
		String first_year = String.valueOf(dbResult.getInt("FirstYear"));
		if(first_month.length() == 2) {
			titleData.setFirst_broadcast(first_year + first_month);
		} else {
			titleData.setFirst_broadcast(first_year + "0" + first_month);
		}

		//登録
		int ret_insertTitleData = dBUtil.updateDB(updateDAO, "B001.insertTitleData", titleData);
		if(ret_insertTitleData == -1) return -1;

		return 0;
	}


	/**
	 * ７．エンコード予約情報の登録
	 * @param recData
	 * @param rss2Result
	 * @return
	 */
	private EncData insertEncData(RecData recData, JSONObject rss2Result) {

		// 変数の格納
		EncData encData = new EncData();
		encData.setSource(Path.REC_FOLDER + "\\" + recData.getTs_name());
		encData.setRec_id(recData.getRec_id());
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd HH:mm:ss");
		encData.setPreserved_date(sdf.format(cal.getTime()));
		encData.setEnc_format("0");

		if(rss2Result != null) {

			// StringReplacementUtil
			StringReplacementUtil sru = new StringReplacementUtil(log);

			// PIDの設定
			encData.setProgram_id(rss2Result.getInt("PID"));

			// titleの設定
			String title = sru.windowsFileName(rss2Result.getString("Title"));

			// countの設定
			boolean hasCount = !rss2Result.isNull("Count");
			String count = null;
			if (hasCount) {
				count = rss2Result.getString("Count");
				// 1桁のときは0埋めして2桁にする
				if(count.length() == 1) {
					count = "0" + count;
				}
			}

			// shortTitleの設定
			boolean hasShortTitle = false;
			String shortTitle = null;
			if (title.length() > Param.SHORT_TITLE_LENGTH
					&& !rss2Result.getString("ShortTitle").equals("")) {
				hasShortTitle = true;
				shortTitle = sru.windowsFileName(rss2Result.getString("ShortTitle"));
			}

			// subTitleの設定
			boolean hasSubtitle = false;
			String subTitle = null;
			if (!rss2Result.isNull("SubTitle") && !rss2Result.get("SubTitle").equals("")) {
				hasSubtitle = true;
				subTitle = sru.windowsFileName(rss2Result.getString("SubTitle"));
			}

			// mp4_nameの決定
			if(hasShortTitle) {

				if(hasCount && hasSubtitle) {
					encData.setMp4_name(shortTitle + " #" + count + "「" + subTitle + "」.mp4");
				} else if (hasCount) {
					encData.setMp4_name(shortTitle + " #" + count + ".mp4");
				} else if (hasSubtitle){
					encData.setMp4_name(shortTitle + "「" + subTitle + "」.mp4");
				} else {
					encData.setMp4_name(title + ".mp4");
				}
			} else {
				if(hasCount && hasSubtitle) {
					encData.setMp4_name(title + " #" + count + "「" + subTitle + "」.mp4");
				} else if (hasCount) {
					encData.setMp4_name(title + " #" + count +  ".mp4");
				} else if (hasSubtitle){
					encData.setMp4_name(title + "「" + subTitle  + "」.mp4");
				} else {
					encData.setMp4_name(title + ".mp4");
				}
			}
		} else {
			String ts_name = recData.getTs_name();
			encData.setMp4_name(ts_name.replace(".m2ts", ".mp4"));
		}

		log.debug(encData.getRec_id() + ", " + encData.getProgram_id() + ", " + encData.getPreserved_date());

		// 登録
		int ret_insertEncData = dBUtil.updateDB(updateDAO, "B001.insertEncData", encData);
		if(ret_insertEncData == -1) return null;

		// シーケンスの取得
		Object data = dBUtil.getDBData(queryRowHandleDAO, "B001.getEncId", encData);
		if(data != null && data instanceof EncData) {
			encData.setEnc_id(((EncData)data).getEnc_id());
			return encData;
		} else {
			return null;
		}

	}


}
