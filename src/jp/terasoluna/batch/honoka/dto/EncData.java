package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class EncData {

	/**
	 * エンコードID
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int enc_id;

	/**
	 * ソースフルパス
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private String source;

	/**
	 * mp4名
	 */
	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private String mp4_name;

	/**
	 * エンコード予約日時（yyyy/M/dd HH:mm:ss）
	 */
	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private String preserved_date;

	/**
	 * 録画ID
	 */
	@InputFileColumn(columnIndex = 4)
	@OutputFileColumn(columnIndex = 4)
	private int rec_id;

	/**
	 * 番組ID
	 */
	@InputFileColumn(columnIndex = 5)
	@OutputFileColumn(columnIndex = 5)
	private int program_id;

	/**
	 * エンコード区分
	 */
	@InputFileColumn(columnIndex = 6)
	@OutputFileColumn(columnIndex = 6)
	private String enc_format;

	/**
	 * 切り取り開始
	 */
	@InputFileColumn(columnIndex = 7)
	@OutputFileColumn(columnIndex = 7)
	private double start_sec;

	/**
	 * 切り取り終了
	 */
	@InputFileColumn(columnIndex = 8)
	@OutputFileColumn(columnIndex = 8)
	private double end_sec;

	/**
	 * エンコードステータス
	 */
	@InputFileColumn(columnIndex = 9)
	@OutputFileColumn(columnIndex = 9)
	private String enc_status;

	/**
	 * 論理削除フラグ
	 */
	@InputFileColumn(columnIndex = 10)
	@OutputFileColumn(columnIndex = 10)
	private String delete_flag;

	public int getEnc_id() {
		return enc_id;
	}

	public void setEnc_id(int enc_id) {
		this.enc_id = enc_id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getMp4_name() {
		return mp4_name;
	}

	public void setMp4_name(String mp4_name) {
		this.mp4_name = mp4_name;
	}

	public String getPreserved_date() {
		return preserved_date;
	}

	public void setPreserved_date(String preserved_date) {
		this.preserved_date = preserved_date;
	}

	public int getRec_id() {
		return rec_id;
	}

	public void setRec_id(int rec_id) {
		this.rec_id = rec_id;
	}

	public int getProgram_id() {
		return program_id;
	}

	public void setProgram_id(int program_id) {
		this.program_id = program_id;
	}

	public String getEnc_format() {
		return enc_format;
	}

	public void setEnc_format(String enc_format) {
		this.enc_format = enc_format;
	}

	public double getStart_sec() {
		return start_sec;
	}

	public void setStart_sec(double start_sec) {
		this.start_sec = start_sec;
	}

	public double getEnd_sec() {
		return end_sec;
	}

	public void setEnd_sec(double end_sec) {
		this.end_sec = end_sec;
	}

	public String getEnc_status() {
		return enc_status;
	}

	public void setEnc_status(String enc_status) {
		this.enc_status = enc_status;
	}

	public String getDelete_flag() {
		return delete_flag;
	}

	public void setDelete_flag(String delete_flag) {
		this.delete_flag = delete_flag;
	}


}
