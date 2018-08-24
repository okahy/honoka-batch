package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class SeqData {

	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int sequence;

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}


}
