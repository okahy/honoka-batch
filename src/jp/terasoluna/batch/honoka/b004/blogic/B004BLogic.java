package jp.terasoluna.batch.honoka.b004.blogic;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import jp.terasoluna.batch.honoka.cons.Param;
import jp.terasoluna.batch.honoka.cons.Path;
import jp.terasoluna.batch.honoka.cons.TsStatus;
import jp.terasoluna.batch.honoka.dto.RecData;
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
 * B004 ローテート処理
 * @author hyite
 *
 */
@Component
public class B004BLogic implements BLogic {

	private static Log log = LogFactory.getLog(B004BLogic.class);

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
		 * java jp.terasoluna.fw.batch.executor.SyncBatchExecutor B004
		 */

		log.info("B004 ローテート処理 を開始します。");

		/*
		 * １．削除対象のリストを取得
		 */
		ArrayList<Object> delList = this.getDelTsList();
		if (delList == null) {
			log.error("DBとの接続に失敗したためエンコード対象を取得できませんでした。処理を終了します。");
			return 255;
		}
		if (delList.size() == 0) {
			log.info("削除対象のtsファイルが存在しませんでした。処理を終了します。");
			return 0;
		}

		/*
		 * ２．tsファイルの削除
		 */
		for (Object data : delList) {

			if (data instanceof RecData) {
				RecData recData = (RecData) data;

				/*
				 * ２．１．tsファイルの削除
				 */
				int ret_deleteTs = this.deleteTs(recData);

				/*
				 * ２．２．ステータスの変更
				 */
				if (ret_deleteTs == 0) {
					recData.setTs_status(TsStatus.DELETED);
				} else {
					recData.setTs_status(TsStatus.ERROR);
				}
				dBUtil.updateDB(updateDAO, "B004.updateTsStatus", recData);

			}
		}

		log.info("B004 ローテート処理 を終了します。");
		return 0;

	}


	/**
	 * １．削除対象のリストを取得
	 * @return
	 */
	private ArrayList<Object> getDelTsList() {

		// カレンダーを取得
		Calendar cal = Calendar.getInstance();

		// 保存基準日数を減算
        cal.add(Calendar.DATE, Param.SAVE_DAY);

        // 形式を変換
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd h:mm:ss");
        String referenceRecDay = sdf.format(cal.getTime());

        // リクエストDTOを作成
        RecData referenceRecData = new RecData();
        referenceRecData.setStart_time(referenceRecDay);
        referenceRecData.setTs_status(TsStatus.SAVED);

        // データの取得
		ArrayList<Object> delList = dBUtil.getDBDataList(queryRowHandleDAO,"B004.selectDelList", referenceRecData);

		// 結果の返却
		return delList;

	}


	/**
	 * ２．１．tsファイルの削除
	 * @param recData
	 * @return
	 */
	private int deleteTs(RecData recData) {

		// tsファイルの定義
		File ts_file = new File(Path.MOVED_ENC_TS_FOLDER + "\\" + recData.getTs_name());

		// tsファイルが存在したら
		if (ts_file.exists()) {

			// 削除
			boolean ret_delete = ts_file.delete();
			if (ret_delete) {
				log.info("移動後ファイル：" + recData.getTs_name() + "を削除しました。");
			}

		// 存在しなかったら何もしない
		} else {
			log.warn("移動後ファイル：" + recData.getTs_name() + "が見つかりませんでした。");
		}

		// 結果を返却
		return 0;
	}




}

