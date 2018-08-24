package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class ProgramData {


	/**
	 * 番組ID
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int program_id;

	/**
	 * タイトルID
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private int title_id;

	/**
	 * サブタイトル
	 */
	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private String subtitle;

	/**
	 * 放送回
	 */
	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private String count;

	/**
	 * 放送チャンネル
	 */
	@InputFileColumn(columnIndex = 4)
	@OutputFileColumn(columnIndex = 4)
	private int ch_id;

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
	 * 論理削除フラグ
	 */
	@InputFileColumn(columnIndex = 7)
	@OutputFileColumn(columnIndex = 7)
	private String delete_flag;

	public int getProgram_id() {
		return program_id;
	}

	public void setProgram_id(int program_id) {
		this.program_id = program_id;
	}

	public int getTitle_id() {
		return title_id;
	}

	public void setTitle_id(int title_id) {
		this.title_id = title_id;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public int getCh_id() {
		return ch_id;
	}

	public void setCh_id(int ch_id) {
		this.ch_id = ch_id;
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

	public String getDelete_flag() {
		return delete_flag;
	}

	public void setDelete_flag(String delete_flag) {
		this.delete_flag = delete_flag;
	}

}
