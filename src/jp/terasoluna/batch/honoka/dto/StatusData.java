package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class StatusData {

	/**
	 * 録画ID
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int rec_id;

	/**
	 * rec_status
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private String ts_status;

	/**
	 * エンコードID
	 */
	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private int enc_id;

	/**
	 * enc_status
	 */
	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private String enc_status;

	public int getRec_id() {
		return rec_id;
	}

	public void setRec_id(int rec_id) {
		this.rec_id = rec_id;
	}

	public String getTs_status() {
		return ts_status;
	}

	public void setTs_status(String rec_status) {
		this.ts_status = rec_status;
	}

	public int getEnc_id() {
		return enc_id;
	}

	public void setEnc_id(int enc_id) {
		this.enc_id = enc_id;
	}

	public String getEnc_status() {
		return enc_status;
	}

	public void setEnc_status(String enc_status) {
		this.enc_status = enc_status;
	}


}
