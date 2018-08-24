package jp.terasoluna.batch.honoka.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jp.terasoluna.batch.honoka.cons.Param;
import jp.terasoluna.batch.honoka.dto.RecData;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

public class AccessShoboCal {

	private Log log;
	public AccessShoboCal (Log log) {
		this.log = log;
	}

	/**
	 * ＊．番組情報の取得
	 * @param recData
	 * @return
	 */
	public JSONObject getShoboCalRss2(RecData recData) {

		/*
		 * 検索条件の取得
		 */
		String shobocal1 = "http://cal.syoboi.jp/rss2.php?";
		String shobocal2 = "alt=json";

		String title = recData.getTitle();
		int ch_id = recData.getCh_id();

		String start_time = null;
		String end_time = null;

		// YYYY/M/DD h:mm 1970-01-01 09:00:00.000
		if(!recData.getStart_time().equals("197001010900")) {
			start_time = recData.getStart_time();
			end_time = recData.getEnd_time();
		} else {
			return null;
		}

		/*
		 * クエリSQLの作成
		 */
		String urlString = shobocal1 + "&" + "start=" + start_time + "&" + "end=" + end_time + "&" + shobocal2;
		log.info(urlString);

		/*
		 * しょぼいカレンダーから結果を取得
		 */
		JSONObject rssResponse = null;

		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = null;

			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(Param.HTTP_CONNECTION_TIMEOUT);
				connection.setRequestMethod("GET");

				/*
				 * 結果をJSON形式で格納
				 */
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					try (
						InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
						BufferedReader reader = new BufferedReader(isr)) {
						String line;
						while ((line = reader.readLine()) != null) {
							rssResponse = new JSONObject(line);
						}
					}
				} else {
					log.warn("HTTP接続に失敗しました。インターネット接続を確認してください。");
				}

			} catch (Exception e) {
				log.warn("HTTP接続に失敗しました。インターネット接続を確認してください。");

			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}

		} catch (Exception e) {
			log.warn("しょぼいカレンダーでの検索に失敗しました。");
		}

		if(rssResponse == null){
			log.warn("検索対象時間の放送が見つかりませんでした。");
			return null;
		}


		/*
		 * 取得した結果の処理
		 */
		JSONArray items = null;
		try {
			items = rssResponse.getJSONArray("items");
		} catch(JSONException e) {
			log.error("items情報がありません。しょぼいカレンダーの仕様変更を確認してください。",e);
		}
		if(items == null) {
			return null;
		}

		/*
		 * 検索条件のチャンネルのデータを返却する
		 */
		for(Object data : items){

			// 型の変換
			JSONObject item = null;
			if (data instanceof JSONObject) {
				item = (JSONObject) data;
			} else {
				break;
			}
			log.debug(item);

			// JSONObjectから検索結果を返却
			try {
				if(item.getInt("ChID") == ch_id && item.get("Title").equals(title)){
					log.info("しょぼいカレンダーでの検索結果が見つかりました。");
					return item;
				} else if(item.getInt("ChID") == ch_id){
					log.info("しょぼいカレンダーでの検索結果が見つかりました。(タイトル不一致)");
					return item;
				}
			} catch (JSONException e) {
				log.warn("しょぼいカレンダーの検索結果を正しい形式で取得できませんでした。");
			}

		}

		// 返却できなかった場合nullを返す
		log.warn("該当するタイトル・チャンネルでの放送が見つかりませんでした。");
		return null;

	}


	/**
	 * ＊．作品情報の取得
	 * @param tid
	 * @return
	 */
	public JSONObject getShoboCalDB(String tid) {

		String xml = "";

		try {
			URL url = new URL("http://cal.syoboi.jp/db.php?Command=TitleLookup&TID=" + tid);
			HttpURLConnection connection = null;

			try {
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				/*
				 * 結果をJSON形式で格納
				 */
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					try (InputStreamReader isr = new InputStreamReader(connection.getInputStream(),
							StandardCharsets.UTF_8);
							BufferedReader reader = new BufferedReader(isr)) {
						String line;
						while ((line = reader.readLine()) != null) {
							xml = xml + line + " ";
						}
					}
				}

			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}

		} catch (IOException e) {
			log.error("しょぼいカレンダーでのTID検索に失敗しました。", e);
			return null;
		}

		JSONObject titleItem = XML.toJSONObject(xml).getJSONObject("TitleLookupResponse").getJSONObject("TitleItems").getJSONObject("TitleItem");

		return titleItem;
	}

}
