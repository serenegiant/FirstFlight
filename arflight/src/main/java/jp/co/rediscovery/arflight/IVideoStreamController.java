package jp.co.rediscovery.arflight;

/**
 * ライブ映像に対応したコントローラー用のメソッド定義用のインターフェース
 */
public interface IVideoStreamController {
	public static final int DEFAULT_VIDEO_FRAGMENT_SIZE = 1000;
	public static final int DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER = 128;
	public static final int VIDEO_RECEIVE_TIMEOUT_MS = 500;

	/**
	 * ライブ映像処理用のVideoStreamインスタンスをセット
	 * @param video_stream
	 */
	public void setVideoStream(final VideoStream video_stream);

	/**
	 * ライブ映像取得が有効かどうかを取得
	 * @return
	 */
	public boolean isVideoStreamingEnabled();

	/**
	 * ライブ映像取得の有効/無効を切り替え
	 * @param enable
	 * @return
	 */
	public boolean enableVideoStreaming(final boolean enable);
}
