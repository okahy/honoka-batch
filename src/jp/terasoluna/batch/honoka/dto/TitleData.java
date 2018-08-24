package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class TitleData {

	/**
	 * タイトルID
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int title_id;

	/**
	 * タイトル
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private String title;

	/**
	 * タイトルかな
	 */
	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private String title_kana;


	/**
	 * タイトルかな
	 */
	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private String short_title;


	/**
	 * 初回放送年月
	 */
	@InputFileColumn(columnIndex = 4)
	@OutputFileColumn(columnIndex = 4)
	private String first_broadcast;

	/**
	 * 格納パス
	 */
	@InputFileColumn(columnIndex = 5)
	@OutputFileColumn(columnIndex = 5)
	private String destination;

	/**
	 * 論理削除フラグ
	 */
	@InputFileColumn(columnIndex = 6)
	@OutputFileColumn(columnIndex = 6)
	private String delete_flag;

	public int getTitle_id() {
		return title_id;
	}

	public void setTitle_id(int title_id) {
		this.title_id = title_id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle_kana() {
		return title_kana;
	}

	public void setTitle_kana(String title_kana) {
		this.title_kana = title_kana;
	}


	public String getShort_title() {
		return short_title;
	}

	public void setShort_title(String short_title) {
		this.short_title = short_title;
	}

	public String getFirst_broadcast() {
		return first_broadcast;
	}

	public void setFirst_broadcast(String first_broadcast) {
		this.first_broadcast = first_broadcast;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDelete_flag() {
		return delete_flag;
	}

	public void setDelete_flag(String delete_flag) {
		this.delete_flag = delete_flag;
	}

}
