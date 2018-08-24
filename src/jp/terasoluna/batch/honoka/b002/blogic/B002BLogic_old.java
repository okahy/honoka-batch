package jp.terasoluna.batch.honoka.b002.blogic;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import jp.terasoluna.batch.honoka.cons.EncStatus;
import jp.terasoluna.batch.honoka.cons.Param;
import jp.terasoluna.batch.honoka.cons.Path;
import jp.terasoluna.batch.honoka.cons.TsStatus;
import jp.terasoluna.batch.honoka.cons.VideoStatus;
import jp.terasoluna.batch.honoka.dto.EncData;
import jp.terasoluna.batch.honoka.dto.RecData;
import jp.terasoluna.batch.honoka.dto.VideoData;
import jp.terasoluna.batch.honoka.util.DBUtil;
import jp.terasoluna.fw.batch.blogic.BLogic;
import jp.terasoluna.fw.batch.blogic.vo.BLogicParam;
import jp.terasoluna.fw.dao.QueryRowHandleDAO;
import jp.terasoluna.fw.dao.UpdateDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * B002 エンコード処理
 * @author hyite
 *
 */
@Component
public class B002BLogic_old implements BLogic {

	private static Log log = LogFactory.getLog(B002BLogic_old.class);

	@Autowired
	protected UpdateDAO updateDAO;

	@Autowired
	protected QueryRowHandleDAO queryRowHandleDAO;

	DBUtil dBUtil = new DBUtil(log);

	@Override
	public int execute(BLogicParam param) {

		/*
		 * ■キックするコマンド
		 * java jp.terasoluna.fw.batch.executor.SyncBatchExecutor B002
		 */

		/*
		 * 起動判定・エンコード中シグナルの配置
		 */
		File encodingSignal = new File(Path.ENCODEING_SIGNAL);
		File stopEncodeSignal = new File(Path.STOP_ENCODE_SIGNAL);
		if (encodingSignal.exists()) {
			log.info("エンコード中シグナルを検出しました。"
					+ "他プロセスがエンコード中のため、B002 エンコード処理 を終了します。");
			return 0;
		} else if (stopEncodeSignal.exists()) {
			log.info("エンコード停止シグナルを検出したため、B002 エンコード処理 を終了します。");
			return 0;
		}
		log.info("B002 エンコード処理 を開始します。");


		/*
		 * １．エンコード対象のリストを取得する
		 */
		ArrayList<Object> encList = dBUtil.getDBDataList(queryRowHandleDAO,"B002.selectEncList", null);
		if (encList == null) {
			log.error("DBとの接続に失敗したためエンコード対象を取得できませんでした。処理を終了します。");
			return 255;
		}
		if (encList.size() == 0) {
			log.info("エンコード対象ファイルが存在しませんでした。処理を終了します。");
			return 0;
		} else {
			log.info(encList.size() + "件のエンコード対象を取得しました。処理を継続します。");
		}


		/*
		 * ２．エンコード処理
		 */
		try {
			encodingSignal.createNewFile();
		} catch (IOException e) {
			log.error("起動時、エンコード中シグナルの作成に失敗しました。処理を終了します。");
			return 255;
		}

		// エンコード時間・連続エンコード回数の定義
		long processingTime = 0;
		int numEncoded = 0;

		for (Object data : encList) {

			// シグナルファイルの確認
			if (!encodingSignal.exists()) {
				log.info("エンコード中シグナルが削除されたため、B002 エンコード処理 を終了します。");
				return 0;
			} else if (stopEncodeSignal.exists()) {
				log.info("エンコード停止シグナルが検出されました。");
				boolean signalDeleted = encodingSignal.delete();
				if (signalDeleted) {
					log.info("エンコード中シグナルを削除しました。B002 エンコード処理 を終了します。");
				return 0;
				} else {
					log.error("エンコード中シグナルの削除に失敗しました。B002 エンコード後処理 を終了します。");
					return 255;
				}
			}

			// 前回エンコード時間・連続エンコード回数の確認
			if (processingTime >= Param.SLEEP_TIME_THRESHOLD
					|| numEncoded >= Param.SLEEP_FILE_THRESHOLD) {
				try {
					log.warn("processingTime:" + processingTime
							+ ", nunEncodeFile:" + numEncoded + " "
							+ Param.SLEEP_TIME + "ミリ秒スリープします。");
					Thread.sleep(Param.SLEEP_TIME);
				} catch (InterruptedException e) {
					log.error("スリープが中断されました。B002 エンコード後処理 を終了します。", e);
					return 255;
				}
			}

			// 変数の初期化
			EncData encData = null;
			RecData recData = new RecData();
			if (data instanceof EncData) {
				encData = (EncData) data;
				log.info("enc_id:" + encData.getEnc_id() + ", mp4_name:"
						+ encData.getMp4_name() + "のエンコードを行います。");
				recData.setRec_id(encData.getRec_id());
			} else {
				log.error("エンコード対象取得時、想定外のエラー（型の不一致）が発生しました。"
						+ "処理をスキップします。");
				continue;
			}

			// ワークフォルダの初期化
			boolean ret_delete = true;
			File[] delFileList = new File(Path.COPIED_FOLDER).listFiles();
			for (File del_file : delFileList) {
				ret_delete = del_file.delete() && ret_delete;
			}
			if (ret_delete) {
				log.info("ワークフォルダの初期化が完了しました。");
			} else {
				/*
				 * ＊．sourceファイルの移動・ステータスの変更
				 */
				log.error("ワークフォルダの初期化に失敗しました。");
				this.saveSource(false, recData, encData);
				continue;
			}

			/*
			 * ２．１．sourceチェック
			 */
			boolean ret_copy = this.getSource(recData, encData);
			if (!ret_copy){
				continue;
			}

			/*
			 * ２．２．ファイルエンコード
			 */
			processingTime = this.encodeFile(encData);

			/*
			 * ＊．sourceファイルの移動・ステータスの変更
			 */
			if (processingTime < 0){
				this.saveSource(false, recData, encData);
			} else {
				this.saveSource(true, recData, encData);
			}

			numEncoded ++;
		}

		/*
		 * エンコード中シグナルの削除・ジョブの終了
		 */
		boolean signalDeleted = encodingSignal.delete();
		if (signalDeleted) {
			log.info("B002 エンコード後処理 を終了します。");
			return 0;
		} else {
			log.error("エンコード中シグナルの削除に失敗しました。B002 エンコード後処理 を終了します。");
			return 255;
		}

	}


	/**
	 * ２．１．sourceチェック
	 * @param recData
	 * @param encData
	 * @return
	 */
	private boolean getSource(RecData recData, EncData encData) {

		// 変数の初期化
		File source = new File(encData.getSource());

		// チェックロジック
		if (!source.exists()) {
			log.error(recData.getTs_name() + "が存在しません。");
		} else if (source.length() == 0) {
			log.error(recData.getTs_name() + "のファイルサイズが0です。ファイルを削除します。"
					+ "録画ファイルの場合、同時間の予約が重複していないか、確認して下さい。");
			boolean ret_delete = source.delete();
			if (!ret_delete) {
				log.error("sourceの削除に失敗しました。");
			}
		} else if (source.length() >= Param.SOURCE_MAX_LENGTH) {
			log.warn(recData.getTs_name() + "のファイルサイズが大きすぎます。（ファイルサイズ："
					+ source.length() + "）処理をスキップします。");
		} else {
			// チェック終了
			log.info(source.getName() + "のsourceチェックが完了しました。");
			return true;
		}

		// source不正のため、DB更新
		recData.setTs_status(TsStatus.ERROR);
		encData.setEnc_status(EncStatus.ERROR);
		dBUtil.updateDB(updateDAO, "B002.updateEncStatus", encData);
		dBUtil.updateDB(updateDAO, "B002.updateRecStatus", recData);
		return false;
	}


	/**
	 * ２．２．ファイルエンコード
	 * @param encData
	 * @return
	 */
	private long encodeFile(EncData encData) {

		// sourceファイルのコピー
		File source = new File(encData.getSource());
		File targetFile = new File(Path.COPIED_FOLDER + "\\" + source.getName());
		try {
			Files.copy(Paths.get(source.getAbsolutePath()),
			        Paths.get(targetFile.getAbsolutePath()),
			        StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("sourceコピー中にエラーが発生しました。エンコードをスキップします。", e);
			return -1;
		}

		String encodeCmd = null;

		if(encData.getEnc_format().equals(Param.NORMAL_ENCODE)) {
			/*
			 * handbrake用コマンド
			 */

			encodeCmd = "\"C:\\Program Files\\Handbrake\\HandBrakeCLI.exe\""
					+ " -i \""+  targetFile + "\""
					+ " -t 1 --angle 1"
					+ " -o \"" + Path.ENC_MP4_FOLDER + "\\" + encData.getMp4_name() + "\""
					+ " -f av_mp4 -w 1280 -l 720 --crop 0:0:0:0 --non-anamorphic --modulus 2 -e x264 -q 20 -r 23.976 --cfr --detelecine"
					+ " -a 1 -E av_aac -6 dpl2 -R Auto -B 160 -D 0 --gain 0 --audio-fallback ac3"
					+ " --encoder-preset=veryfast --encoder-level=\"4.0\" --encoder-profile=main --verbose=1";

			/*
			 * 旧コマンド
			 */

			/**
			encodeCmd = "\"C:\\Program Files\\Handbrake\\HandBrakeCLI.exe\""
					+ " -i \""+  targetFile + "\""
					+ " -t 1 --angle 1 -c 1"
					+ " -o \"" + Path.ENC_MP4_FOLDER + "\\" + encData.getMp4_name() + "\""
					+ " -f mp4  --detelecine --cfr --decomb -w 1280 --crop 0:0:0:0 --loose-anamorphic  --modulus 2 -e qsv_h264"
					+ " -b 2300 -2 -a 1 -E av_aac -6 dpl2 -R Auto -B 160 -D 0 --gain 0 --audio-fallback ac3"
					+ " --encoder-preset=quality  --encoder-level=\"4.0\" --encoder-profile=main  --verbose=1";
			*/


			/*
			 * ffmpeg用コマンド
			 */

			/**
			encodeCmd = "C:\\rec\\ffmpeg\\bin\\ffmpeg.exe -i " + "\""
					+ targetFile + "\" -f mp4 -vcodec libx264 -vsync 1 "
					+ "-b:v 1.8M -s 1280x720 -acodec aac -strict experimental -ab 160k \""
					+ Path.ENC_MP4_FOLDER + "\\" + encData.getMp4_name() + "\"";
			*/


		} else if(encData.getEnc_format().equals(Param.CUT_OUT)) {

			double start_sec = encData.getStart_sec();
			double video_sec = encData.getEnd_sec() - start_sec;
			DecimalFormat df = new DecimalFormat("#.#");

			encodeCmd = "\"C:\\Program Files\\Handbrake\\HandBrakeCLI.exe\""
					+ " -i \""+  targetFile + "\""
					+ " -t 1 --angle 1 -c 1"
					+ " --start-at duration:" + df.format(start_sec) + " --stop-at duration:" + df.format(video_sec)
					+ " -o \"" + Path.ENC_MP4_FOLDER + "\\" + encData.getMp4_name() + "\""
					+ " -f av_mp4 -w 1280 -l 720 --crop 0:0:0:0 --non-anamorphic --modulus 2 -e x264 -q 18 -r 23.976 --cfr --detelecine"
					+ " -a 1 -E av_aac -6 dpl2 -R Auto -B 160 -D 0 --gain 0 --audio-fallback ac3"
					+ " --encoder-preset=veryfast --encoder-level=\"4.0\" --encoder-profile=main --verbose=1";

		}

		ProcessBuilder pb_encode = new ProcessBuilder("cmd", "/c", encodeCmd);
		pb_encode.redirectErrorStream(true);

		log.info("エンコードを開始します。:" + encodeCmd);
		Process process_encode = null;
		InputStream is = null;
		long encode_start = System.currentTimeMillis();
		try {
			process_encode = pb_encode.start();
			is = process_encode.getInputStream();
			while(is.read() >= 0);
		} catch (IOException e) {
			log.error("エンコード処理に失敗しました。ステータスを更新して処理をスキップします。",e);
			return -1;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				log.warn("ProcessのInputStreamのクローズに失敗しました。", e);
			}
		}
		File mp4_file = new File(Path.ENC_MP4_FOLDER + "\\" + encData.getMp4_name());
		if (mp4_file.exists()) {
			long encode_end = System.currentTimeMillis();
			long processing_time = TimeUnit.MILLISECONDS.toMinutes(encode_end - encode_start);
			log.info("エンコードが完了しました。{"
					+ "\"enc_id\":"+ encData.getEnc_id()
					+ ", \"mp4_name\":\"" + encData.getMp4_name()
					+ "\", \"file_size\":" + mp4_file.length()
					+ ", \"processing_time\":" + processing_time +"}");
			return processing_time;
		} else {
			log.error("エンコード後ファイル:" + encData.getMp4_name() + "が見つかりませんでした。"
					+ "mp4ファイル名の不正の可能性があります。処理を終了します。");
			return -1;
		}

	}


	/**
	 * ＊．sourceファイルの移動・ステータスの変更
	 * @param saved
	 * @param recData
	 * @param encData
	 */
	private void saveSource(Boolean saved, RecData recData, EncData encData) {

		String enc_format = encData.getEnc_format();

		// 変数の初期化
		String saveDestination = null;
		File source = new File(encData.getSource());

		// 変数の設定
		if (saved) {
			saveDestination = Path.ENC_TS_FOLDER + "\\" + source.getName();
			recData.setTs_status(TsStatus.ENCODED);
			encData.setEnc_status(EncStatus.ENCODED);
		} else {
			saveDestination = Path.UNENC_TS_FOLDER + "\\" + source.getName();
			recData.setTs_status(TsStatus.ERROR);
			encData.setEnc_status(EncStatus.ERROR);
		}
		// DB更新
		dBUtil.updateDB(updateDAO, "B002.updateEncStatus", encData);

		// 通常のエンコードの場合
		if(enc_format.equals(Param.NORMAL_ENCODE)) {

			// tsファイルの移動
			boolean ret_move = false;
			if(!source.getAbsolutePath().equals(saveDestination)) {
				ret_move = source.renameTo(new File(saveDestination));
				if (!ret_move) {
					log.error("sourceの移動に失敗しました。");
				}
			}
			// DB更新
			dBUtil.updateDB(updateDAO, "B002.updateRecStatus", recData);

		// 切り抜きの場合
		} else if (enc_format.equals(Param.CUT_OUT)) {

			// DB更新
			VideoData videoData = new VideoData();
			videoData.setEnc_id(encData.getEnc_id());
			if(saved) {
				videoData.setVideo_status(VideoStatus.CREATED);
			} else {
				videoData.setVideo_status(VideoStatus.ERROR);
			}
			dBUtil.updateDB(updateDAO, "B002.updateVideoStatus", videoData);

		}


	}
}

