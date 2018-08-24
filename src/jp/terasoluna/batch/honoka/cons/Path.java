package jp.terasoluna.batch.honoka.cons;

public class Path {

	// インスタンス化させない。
	private Path() {}

	public static final String REC_FOLDER = "\\\\HYITE-REC\\public\\recorded\\ts";
	public static final String PGDATA_FOLDER = "\\\\HYITE-REC\\public\\recorded\\json";

	public static final String COPIED_FOLDER = "C:\\Users\\kazuya\\Videos\\copied";
	public static final String SPLITTED_FOLDER = "C:\\Users\\kazuya\\Videos\\splitted";
	public static final String ENC_MP4_FOLDER = "C:\\Users\\kazuya\\Videos\\mp4";

	public static final String ENC_TS_FOLDER = "\\\\HYITE-REC\\public\\encoded\\ts";
	public static final String UNENC_TS_FOLDER = "\\\\HYITE-REC\\public\\encoded\\ts\\error";

	public static final String DLNA_FOLDER = "E:\\video\\animation";
	public static final String ITUNES_FOLDER = "E:\\kazuya\\Music\\iTunes\\iTunes Media\\iTunesに自動的に追加";
	public static final String VJ_FOLDER = "E:\\video\\VJ";

	public static final String MOVED_PGDATA_FOLDER = "\\\\HYITE-REC\\public\\moved\\json";
	public static final String MOVED_UNENC_PGDATA_FOLDER = "\\\\HYITE-REC\\public\\moved\\json\\error";
	public static final String MOVED_ENC_TS_FOLDER = "\\\\HYITE-REC\\public\\moved\\ts";
	public static final String MOVED_UNENC_TS_FOLDER = "\\\\HYITE-REC\\public\\moved\\ts\\error";

	public static final String ENCODEING_SIGNAL = "C:\\Users\\kazuya\\Videos\\now_encoding.signal";
	public static final String STOP_ENCODE_SIGNAL = "C:\\Users\\kazuya\\Videos\\stop_encode.signal";


}
