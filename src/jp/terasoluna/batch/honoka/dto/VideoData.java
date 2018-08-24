package jp.terasoluna.batch.honoka.dto;

import jp.terasoluna.fw.file.annotation.InputFileColumn;
import jp.terasoluna.fw.file.annotation.OutputFileColumn;

public class VideoData {

	@InputFileColumn(columnIndex = 0)
	@OutputFileColumn(columnIndex = 0)
	private int video_id;

	@InputFileColumn(columnIndex = 1)
	@OutputFileColumn(columnIndex = 1)
	private int title_id;

	@InputFileColumn(columnIndex = 2)
	@OutputFileColumn(columnIndex = 2)
	private String video_type;

	@InputFileColumn(columnIndex = 3)
	@OutputFileColumn(columnIndex = 3)
	private String song_title;

	@InputFileColumn(columnIndex = 4)
	@OutputFileColumn(columnIndex = 4)
	private String artist;

	@InputFileColumn(columnIndex = 5)
	@OutputFileColumn(columnIndex = 5)
	private String used_story;

	@InputFileColumn(columnIndex = 6)
	@OutputFileColumn(columnIndex = 6)
	private int enc_id;

	@InputFileColumn(columnIndex = 7)
	@OutputFileColumn(columnIndex = 7)
	private String video_status;

	@InputFileColumn(columnIndex = 8)
	@OutputFileColumn(columnIndex = 8)
	private String delete_flag;

	public int getVideo_id() {
		return video_id;
	}

	public void setVideo_id(int video_id) {
		this.video_id = video_id;
	}

	public int getTitle_id() {
		return title_id;
	}

	public void setTitle_id(int title_id) {
		this.title_id = title_id;
	}

	public String getVideo_type() {
		return video_type;
	}

	public void setVideo_type(String video_type) {
		this.video_type = video_type;
	}

	public String getSong_title() {
		return song_title;
	}

	public void setSong_title(String song_title) {
		this.song_title = song_title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getUsed_story() {
		return used_story;
	}

	public void setUsed_story(String used_story) {
		this.used_story = used_story;
	}

	public int getEnc_id() {
		return enc_id;
	}

	public void setEnc_id(int enc_id) {
		this.enc_id = enc_id;
	}

	public String getVideo_status() {
		return video_status;
	}

	public void setVideo_status(String video_status) {
		this.video_status = video_status;
	}

	public String getDelete_flag() {
		return delete_flag;
	}

	public void setDelete_flag(String delete_flag) {
		this.delete_flag = delete_flag;
	}


}
