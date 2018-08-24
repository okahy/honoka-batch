package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class ProgramCountData {

	/**
	 * 番組ID
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int program_id;

	/**
	 * 登録数
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private int program_count;

	public int getProgram_id() {
		return program_id;
	}

	public void setProgram_id(int program_id) {
		this.program_id = program_id;
	}

	public int getProgram_count() {
		return program_count;
	}

	public void setProgram_count(int program_count) {
		this.program_count = program_count;
	}



}
