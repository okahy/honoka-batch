package jp.terasoluna.batch.honoka.util;

import java.util.ArrayList;

import jp.terasoluna.fw.collector.Collector;
import jp.terasoluna.fw.collector.db.DBCollector;
import jp.terasoluna.fw.collector.util.CollectorUtility;
import jp.terasoluna.fw.dao.QueryRowHandleDAO;
import jp.terasoluna.fw.dao.UpdateDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

public class CopyOfDBUtil {



	/*
	 * インスタンス生成時ログクラスを受け取る
	 */
	private static final Log log = LogFactory.getLog(CopyOfDBUtil.class);
	// private static final Log log = LogFactory.getLog(Util.class);

	/**
	 * ＊．データベースへの登録
	 * @param updateDAO
	 * @param sql_id
	 * @param data
	 * @return
	 */
	public int updateDB(UpdateDAO updateDAO, String sql_id, Object data) {
		int returnCode = 0;
		try {
			if (updateDAO != null && data != null) {
				updateDAO.execute(sql_id, data);
			}
		} catch (DataAccessException e) {
			if (log.isErrorEnabled()) {
				log.error("データアクセスエラーが発生しました", e);
			}
			returnCode = -1;
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("エラーが発生しました", e);
			}
			returnCode = -1;
		} finally {
			// 正常終了した場合、ログを出力
			if (returnCode == 0 && log.isInfoEnabled()) {
				log.info(sql_id + "：DBの更新が正常に終了しました。");
			}
		}
		if(returnCode == -1) {
			log.error(sql_id + "実行時、エラーが発生しました。");
			return returnCode;
		}
		return 0;
	}

	/**
	 * ＊．登録データの取得（１件）
	 * @param queryRowHandleDAO
	 * @param inputData
	 * @return
	 */
	public  Object getDBData(QueryRowHandleDAO queryRowHandleDAO, String sql_id, Object inputData) {

		// 返却するオブジェクトの定義
		Object data = null;

		// return code の定義
		int returnCode = 0;

			// コレクタ
			Collector<Object> collector = new DBCollector<Object>(
					queryRowHandleDAO, sql_id, inputData);

			try {

				// DBからデータを取得
				while (collector.hasNext()) {

					// ファイルへデータを出力（1行）
					data = collector.next();

					// 必ずSQLはtop1とすること
					if (data != null) {
						break;
					}
				}

			} catch (DataAccessException e) {
				if (log.isErrorEnabled()) {
					log.error(sql_id + "：データアクセスエラーが発生しました", e);
				}
				returnCode = -1;

			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error(sql_id + "：エラーが発生しました", e);
				}
				returnCode = -1;

			} finally {
				// コレクタのクローズ
				CollectorUtility.closeQuietly(collector);
			}

			// 正常終了時のログ
			if (returnCode == 0 && log.isInfoEnabled()) {
				log.info(sql_id + "：データの取得が正常に終了しました。");
				return data;

			// エラー時はnullを返却する。
			} else {
				return null;
			}
	}

	/**
	 * ＊．登録データの取得（複数件数）
	 * @param queryRowHandleDAO
	 * @param inputData
	 * @return
	 */
	public ArrayList<Object> getDBDataList(QueryRowHandleDAO queryRowHandleDAO, String sql_id, Object inputData) {

		// 返却するオブジェクトの定義
		ArrayList<Object> dataList = new ArrayList<Object>();

		// return code の定義
		int returnCode = 0;

			// コレクタ
			Collector<Object> collector = new DBCollector<Object>(
					queryRowHandleDAO, sql_id, inputData);

			try {

				// DBからデータを取得
				while (collector.hasNext()) {

					// ファイルへデータを出力（1行）
					Object data = collector.next();
					if (data != null) {
						dataList.add(data);
					}
				}

			} catch (DataAccessException e) {
				if (log.isErrorEnabled()) {
					log.error(sql_id + "：データアクセスエラーが発生しました", e);
				}

				returnCode = -1;
			} catch (Exception e) {
				if (log.isErrorEnabled()) {
					log.error(sql_id + "：エラーが発生しました", e);
				}

				returnCode = -1;
			} finally {
				// コレクタのクローズ
				CollectorUtility.closeQuietly(collector);
			}

			// 正常終了時のログ
			if (returnCode == 0 && log.isInfoEnabled()) {
				log.info(sql_id + "：データの取得が正常に終了しました。");
				return dataList;

			// エラー時はnullを返却する
			} else {
				return null;
			}
	}
}
