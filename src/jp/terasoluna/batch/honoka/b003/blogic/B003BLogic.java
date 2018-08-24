package jp.terasoluna.batch.honoka.b003.blogic;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import jp.terasoluna.batch.honoka.cons.EncStatus;
import jp.terasoluna.batch.honoka.cons.Param;
import jp.terasoluna.batch.honoka.cons.Path;
import jp.terasoluna.batch.honoka.cons.TsStatus;
import jp.terasoluna.batch.honoka.cons.VideoStatus;
import jp.terasoluna.batch.honoka.dto.EncData;
import jp.terasoluna.batch.honoka.dto.RecData;
import jp.terasoluna.batch.honoka.dto.TitleData;
import jp.terasoluna.batch.honoka.dto.VideoData;
import jp.terasoluna.batch.honoka.util.AccessShoboCal;
import jp.terasoluna.batch.honoka.util.DBUtil;
import jp.terasoluna.fw.batch.blogic.BLogic;
import jp.terasoluna.fw.batch.blogic.vo.BLogicParam;
import jp.terasoluna.fw.dao.QueryRowHandleDAO;
import jp.terasoluna.fw.dao.UpdateDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * B003 格納処理
 * @author hyite
 *
 */
@Component
public class B003BLogic implements BLogic {

	private static Log log = LogFactory.getLog(B003BLogic.class);

	@Autowired
	protected UpdateDAO updateDAO;

	@Autowired
	protected QueryRowHandleDAO queryRowHandleDAO;

	// DBUtilのインスタンス作成
	DBUtil dBUtil = new DBUtil(log);

	@Override
	public int execute(BLogicParam param) {

		/*
		 * ■キックするコマンド
		 * java jp.terasoluna.fw.batch.executor.SyncBatchExecutor B003
		 */

		log.info("B003 格納処理 を開始します。");

		/*
		 * １．エンコード対象のリストを取得する
		 */
		ArrayList<Object> encList = dBUtil.getDBDataList(queryRowHandleDAO,"B003.selectEncList", null);
		if (encList == null) {
			log.error("DBとの接続に失敗したためエンコード対象を取得できませんでした。処理を終了します。");
			return 255;
		}
		if (encList.size() == 0) {
			log.info("格納対象ファイルが存在しませんでした。処理を終了します。");
			return 0;
		} else {
			log.info(encList.size() + "件の配信対象を取得しました。処理を継続します。");
		}

		// 各EncDataに対して
		for (Object data : encList) {

			EncData encData = null;
			if (data instanceof EncData) {
				encData = (EncData) data;
				log.info("enc_id:" + encData.getEnc_id() + ", mp4_name:"
				+ encData.getMp4_name() + "の格納を行います。");
			} else {
				continue;
			}
			String enc_format = encData.getEnc_format();

			// 通常のエンコードの場合
			if(enc_format.equals(Param.NORMAL_ENCODE)) {

				/*
				 * ２．TitleDataの取得
				 */
				TitleData titleData = null;
				Object tData = dBUtil.getDBData(queryRowHandleDAO, "B003.selectTitle", encData);
				if (tData instanceof TitleData) {
					titleData = (TitleData) tData;
				}


				/*
				 * ３．配信処理（通常のエンコード）
				 */
				int ret_broadcastMp4 = this.broadcastMp4(encData, titleData);
				if (ret_broadcastMp4 == 0) {
					encData.setEnc_status(EncStatus.BROADCASTED);
				} else {
					encData.setEnc_status(EncStatus.ERROR);
				}
				dBUtil.updateDB(updateDAO, "B003.updateEncStatus", encData);

				/*
				 * ４．tsデータの退避
				 */
				RecData recData = this.saveTs(encData);
				if (recData == null) {
					recData = new RecData();
					recData.setRec_id(encData.getRec_id());
					recData.setTs_status(TsStatus.ERROR);
				}
				dBUtil.updateDB(updateDAO, "B003.updateTsStatus", recData);


				/*
				 * ５．動画データの更新
				 */
				if (titleData != null) {
					this.updateVideoData(titleData);
				}


			// 切り抜きの場合
			} else if (enc_format.equals(Param.CUT_OUT)) {

				/*
				 * ６．配信処理（切り抜き）
				 */
				int ret_broadcastCutout = this.broadcastCutout(encData);
				VideoData videoData = new VideoData();
				videoData.setEnc_id(encData.getEnc_id());
				if (ret_broadcastCutout == 0) {
					encData.setEnc_status(EncStatus.BROADCASTED);
					videoData.setVideo_status(VideoStatus.BROADCASTED);
				} else {
					encData.setEnc_status(EncStatus.ERROR);
					videoData.setVideo_status(VideoStatus.ERROR);
				}
				dBUtil.updateDB(updateDAO, "B003.updateEncStatus", encData);
				dBUtil.updateDB(updateDAO, "B003.updateVideoStatus", videoData);

			}
		}

		return 0;

	}



	/**
	 * ３．配信処理（通常のエンコード）
	 * @param titleData
	 */
	private int broadcastMp4(EncData encData, TitleData titleData) {

		// 配信するmp4ファイルを定義
		String mp4_name = encData.getMp4_name();
		File mp4_file = new File(Path.ENC_MP4_FOLDER + "\\" + mp4_name);

		// DLNAサーバの配信先を定義
		String destination = "タイトルなし";
		if (titleData != null) {
			destination = titleData.getDestination();
		}
		File dlna_folder = new File(Path.DLNA_FOLDER + "\\" + destination);
		if (!dlna_folder.exists()) {
			dlna_folder.mkdir();
			log.info("配信先フォルダ：" + dlna_folder.getAbsolutePath() + "を作成しました。");
		}

		// ファイルの配信先①：DLNAサーバ
		try {
			log.info("配信先フォルダ：" + dlna_folder.getAbsolutePath() + "へのコピーを開始します。");
			Files.copy(Paths.get(mp4_file.getAbsolutePath()),
					Paths.get(dlna_folder.getAbsolutePath() + "\\" + mp4_name),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			log.error("DLNAサーバ配信対象フォルダへのコピーに失敗しました。", e);
			return 255;
		}
		log.info("配信先フォルダ：" + dlna_folder.getAbsolutePath() + "へのコピーが完了しました。");

//		// ファイルの配信先②：itunes
//		try {
//			log.info("itunes連携フォルダへのコピーを開始します。");
//			Files.copy(Paths.get(mp4_file.getAbsolutePath()),
//					Paths.get(Path.ITUNES_FOLDER + "\\" + mp4_name),
//					StandardCopyOption.REPLACE_EXISTING);
//		} catch (Exception e) {
//			log.error("itunes連携フォルダへのコピーに失敗しました。", e);
//			return 255;
//		}
//		log.info("itunes連携フォルダへのコピーが完了しました。");

		// 配信前ファイルの削除
		boolean ret_delete = mp4_file.delete();
		if (ret_delete) {
			log.info("格納処理を終えたため、エンコード後ファイル：" + mp4_name + "を削除しました。");
		}

		return 0;
	}


	/**
	 * ４．tsデータの退避
	 * @param encData
	 * @return
	 */
	private RecData saveTs(EncData encData) {

		// tsファイルの情報を取得
		Object data = dBUtil.getDBData(queryRowHandleDAO, "B003.selectRecData", encData);
		RecData recData = null;
		if (data == null) {
			log.error("退避する対象をDBから取得できませんでした。");
			return null;
		} else if (data instanceof RecData) {
			recData = (RecData) data;
		} else {
			log.error("退避する対象を正しく取得できませんでした。");
			return null;
		}

		// tsファイルの定義
		File ts_file = new File(Path.ENC_TS_FOLDER + "\\" + recData.getTs_name());
		if (!ts_file.exists()) {
			log.error("退避対象ファイルが見つかりませんでした。");
			return null;
		}

		// 移動先フォルダの定義
		File moved_folder = null;
		if (encData.getEnc_status().equals(EncStatus.BROADCASTED)) {
			moved_folder = new File(Path.MOVED_ENC_TS_FOLDER);
		} else {
			moved_folder = new File(Path.MOVED_UNENC_TS_FOLDER);
		}
		if (!moved_folder.exists()) {
			moved_folder.mkdir();
			log.info("移動先フォルダ：" + moved_folder.getAbsolutePath() + "を作成しました。");
		}

		// 移動先ファイルの定義
		File moved_file = new File(moved_folder.getAbsolutePath() + "\\" + recData.getTs_name());
		log.info("移動先ファイル：" + moved_file.getAbsolutePath() + "への移動を開始します。");
		boolean ret_move = false;
		try {
			ret_move = ts_file.renameTo(moved_file);
		} catch (Exception e) {
			log.error("ファイル退避時に例外が発生しました。", e);
			return null;
		}
		if (!ret_move) {
			log.error("ファイル退避に失敗しました。");
			return null;
		}


//		try {
//			log.info("移動先ファイル：" + moved_file.getAbsolutePath() + "への移動を開始します。");
//			Files.copy(Paths.get(ts_file.getAbsolutePath()),
//			        Paths.get(moved_file.getAbsolutePath()),
//			        StandardCopyOption.REPLACE_EXISTING);
//		} catch (IOException e) {
//			log.error("ファイル退避時にエラーが発生しました。", e);
//			return null;
//		}
//		if (!moved_file.exists()) {
//			log.error("退避時、コピーに失敗しました。");
//			return null;
//		}
//
//
//		// 移動元ファイルの削除
//		boolean ret_delete = ts_file.delete();
//		if (!ret_delete) {
//			log.error(recData.getTs_name() + "の削除に失敗しました。。");
//			return null;
//		}

		// 結果の返却
		log.info("退避が完了しました。");
		recData.setTs_status(TsStatus.SAVED);
		return recData;
	}


	/**
	 * ５．動画情報の登録
	 * @param title_id
	 * @return
	 */
	private int updateVideoData(TitleData titleData) {

		// 作品情報の取得
		AccessShoboCal asc = new AccessShoboCal(log);
		int title_id = titleData.getTitle_id();
		JSONObject dbResult = asc.getShoboCalDB(String.valueOf(title_id));
		log.info("取得作品情報：" + dbResult);

		ArrayList<VideoData> videoDataList = new ArrayList<VideoData>();

		String comment = dbResult.getString("Comment");

		comment = comment.replace("*", "§");
		System.out.println(comment);
		String[] comments = comment.split("§", 0);

		for (String line : comments) {

			line = line.replaceFirst("「", ":");
			line = line.replaceFirst("」", "");
			String[] items = null;
			if (line.startsWith("オープニングテーマ")) {
				items = line.replace("オープニングテーマ", "OP").split("\\s:", 0);
			} else if (line.startsWith("オープニング")){
				items = line.replace("オープニング", "OP").split("\\s:", 0);
			} else if (line.startsWith("エンディングテーマ")){
				items = line.replace("エンディングテーマ", "ED").split("\\s:", 0);
			} else if (line.startsWith("挿入歌")){
				items = line.split("\\s:", 0);
			} else if (line.startsWith("主題歌")){
				items = line.split("\\s:", 0);
			} else {
				continue;
			}

			String video_type = items[0].substring(0,items[0].indexOf(":")).trim();
			String song_title = items[0].substring(items[0].indexOf(":") + 1).trim();
			String artist = null;
			for (String item : items) {
				if (item.startsWith("歌")){
					int commentStart = -1;
					commentStart = item.indexOf(" -");
					if (commentStart == -1) {
						artist = item.substring(item.indexOf(":") + 1).trim();
					} else {
						artist = item.substring(item.indexOf(":") + 1, commentStart).trim();
					}
				}
			}
			String used_story = null;
			for (String item : items) {
				if (item.contains("使用話数:")){
					used_story = item.substring(item.indexOf(":") + 1).trim();
				}
			}

			VideoData videoData = new VideoData();
			videoData.setTitle_id(title_id);
			videoData.setVideo_status(VideoStatus.REGISTERED);
			videoData.setDelete_flag("0");
			videoData.setVideo_type(video_type);
			videoData.setSong_title(song_title);
			videoData.setArtist(artist);
			videoData.setUsed_story(used_story);

			videoDataList.add(videoData);
		}

		for (VideoData videoData : videoDataList) {

			log.info("{\"title_id\":\"" + videoData.getTitle_id()
					+ "\", \"video_status\":\"" + videoData.getVideo_status()
					+ "\", \"delete_flag\":\"" + videoData.getDelete_flag()
					+ "\", \"video_type\":\"" + videoData.getVideo_type()
					+ "\", \"song_title\":\"" + videoData.getSong_title()
					+ "\", \"artist\":\"" + videoData.getArtist()
					+ "\", \"used_story\":\"" + videoData.getUsed_story()
					+ "\"}");

			// 登録済データを取得
			Object data = dBUtil.getDBData(queryRowHandleDAO, "B003.selectVideoData", videoData);

			// 登録済みデータが存在しない場合、DBへ登録する
			if(data == null) {
				log.info("登録済み動画が見つかりませんでした。動画データを新規登録します。");
				int ret_insertVideoData = dBUtil.updateDB(updateDAO, "B003.insertVideoData", videoData);
				if(ret_insertVideoData == -1) return -1;
				continue;

			// 登録済みデータが存在した場合
			} else if(data instanceof VideoData) {
				VideoData registeredVideoData = (VideoData) data;

				// 更新対象チェックを行うデータの定義
				String registeredUsed_story = registeredVideoData.getUsed_story();
				String latestUsed_story = videoData.getUsed_story();

				// 更新対象のデータがある場合
				if(latestUsed_story != null && !latestUsed_story.equals(registeredUsed_story)) {
					log.info("登録済みデータが見つかりました。使用話数を更新します。");
					int ret_updateVideoData = dBUtil.updateDB(updateDAO, "B003.updateVideoData", videoData);
					if(ret_updateVideoData == -1) return -1;
					continue;

				} else {
					log.info("登録済みデータが見つかりました。更新対象はありませんでした。");
					continue;

				}
			}

		}

		return 0;

	}


	/**
	 * ６．配信処理（切り抜き）
	 */
	private int broadcastCutout(EncData encData) {

	// 配信先フォルダの定義
	File broadcast_folder = new File(Path.VJ_FOLDER);
	if (!broadcast_folder.exists()) {
		broadcast_folder.mkdir();
		log.info("配信先フォルダ：" + broadcast_folder.getAbsolutePath() + "を作成しました。");
	}

	// 配信元動画の定義
	String mp4_name = encData.getMp4_name();
	File mp4_file = new File(Path.ENC_MP4_FOLDER + "\\" + mp4_name);

	// 配信先フォルダへコピーする
	try {
		log.info("配信先フォルダ：" + broadcast_folder.getAbsolutePath() + "へのコピーを開始します。");
		Files.copy(Paths.get(mp4_file.getAbsolutePath()),
		        Paths.get(broadcast_folder.getAbsolutePath() + "\\" + mp4_name),
		        StandardCopyOption.REPLACE_EXISTING);
	} catch (Exception e) {
		log.error("DLNAサーバ配信対象フォルダへのコピーに失敗しました。", e);
		return 255;
	}
	log.info("配信先フォルダ：" + broadcast_folder.getAbsolutePath() + "へのコピーが完了しました。");

	// 配信前ファイルの削除
	boolean ret_delete = mp4_file.delete();
	if (ret_delete) {
		log.info("格納処理を終えたため、エンコード後ファイル：" + mp4_name + "を削除しました。");
	}

	return 0;

	}
}

