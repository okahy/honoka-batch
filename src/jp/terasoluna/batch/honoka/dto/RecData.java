package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class RecData {

	/**
	 * tsファイル名
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int rec_id;

	/**
	 * tsファイル名
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private String ts_name;

	/**
	 * タイトル
	 */
	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private String title;

	/**
	 * サブタイトル
	 */
	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private String subtitle;

	/**
	 * 放送局
	 */
	@InputFileColumn(columnIndex = 4)
	@OutputFileColumn(columnIndex = 4)
	private String ch_name;

	/**
	 * 録画開始時間
	 */
	@InputFileColumn(columnIndex = 5)
	@OutputFileColumn(columnIndex = 5)
	private String start_time;

	/**
	 * 録画終了時間
	 */
	@InputFileColumn(columnIndex = 6)
	@OutputFileColumn(columnIndex = 6)
	private String end_time;


	/**
	 *  チャンネルID
	 */
	@InputFileColumn(columnIndex = 7)
	@OutputFileColumn(columnIndex = 7)
	private int ch_id;

	/**
	 * タイトルID
	 */
	@InputFileColumn(columnIndex = 8)
	@OutputFileColumn(columnIndex = 8)
	private int title_id;

	/**
	 * プログラムID
	 */
	@InputFileColumn(columnIndex = 9)
	@OutputFileColumn(columnIndex = 9)
	private int program_id;

	/**
	 * ステータス
	 */
	@InputFileColumn(columnIndex = 10)
	@OutputFileColumn(columnIndex = 10)
	private String ts_status;

	public int getRec_id() {
		return rec_id;
	}

	public void setRec_id(int rec_id) {
		this.rec_id = rec_id;
	}

	public String getTs_name() {
		return ts_name;
	}

	public void setTs_name(String ts_name) {
		this.ts_name = ts_name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getCh_name() {
		return ch_name;
	}

	public void setCh_name(String ch_name) {
		this.ch_name = ch_name;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

	public int getCh_id() {
		return ch_id;
	}

	public void setCh_id(int ch_id) {
		this.ch_id = ch_id;
	}

	public int getTitle_id() {
		return title_id;
	}

	public void setTitle_id(int title_id) {
		this.title_id = title_id;
	}

	public int getProgram_id() {
		return program_id;
	}

	public void setProgram_id(int program_id) {
		this.program_id = program_id;
	}

	public String getTs_status() {
		return ts_status;
	}

	public void setTs_status(String ts_status) {
		this.ts_status = ts_status;
	}



}