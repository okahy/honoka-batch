package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.FileFormat;
import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

@FileFormat(overWriteFlg = true, fileEncoding = "MS932")
public class TitleCountData {

	/**
	 * タイトルID
	 */
	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int title_id;

	/**
	 * 登録数
	 */
	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private int title_count;

	public int getTitle_id() {
		return title_id;
	}

	public void setTitle_id(int title_id) {
		this.title_id = title_id;
	}

	public void setTitle_id(String title_id) {
		this.title_id = Integer.parseInt(title_id);
	}

	public int getTitle_count() {
		return title_count;
	}

	public void setTitle_count(int title_count) {
		this.title_count = title_count;
	}

}
