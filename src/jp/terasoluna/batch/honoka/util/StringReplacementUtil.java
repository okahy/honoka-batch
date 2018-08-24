package jp.terasoluna.batch.honoka.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

public class StringReplacementUtil {

	private Log log;
	public StringReplacementUtil (Log log) {
		this.log = log;
	}

	public String windowsFileName (String before) {

		if (before == null) {
			log.warn("StringReplacementUtil:null値のため置換できません。");
			return null;
		}

		String after = new String(before);

		Map<String, String> charMap = new HashMap<String, String>();
		charMap.put("\\", "￥");
		charMap.put("*", "＊");
		charMap.put("?", "？");
		charMap.put("(", "（");
		charMap.put(")", "）");
		charMap.put("|", "｜");
		charMap.put("<", "＜");
		charMap.put(">", "＞");
		charMap.put(":", "：");
		charMap.put("/", "／");
		charMap.put("\"", "”");
		charMap.put("〜", "～");
		charMap.put("!", "！");

		for(Map.Entry<String, String> e : charMap.entrySet()) {
		    after = after.replace(e.getKey(), e.getValue());
		   		}
		if(before.equals(after)) {
			log.info("StringReplacementUtil:" + before + "に置換対象文字はありませんでした。");
		} else {
			log.info("StringReplacementUtil:" + before + "を" + after + "に置換しました。");
		}

		return after;
	}

}
