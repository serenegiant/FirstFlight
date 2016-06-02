package jp.co.rediscovery.arflight;

/**
 * カメラコントロール可能な機体の場合の追加メソッド定義
 */
public interface ICameraController extends IVideoStreamController {

	/**
	 * カメラコントロールのコールバックをセット
	 * 現在はpan/tiltが変更された時のコールバックのみ対応
	 * @param listener
	 */
	public void setCameraControllerListener(final CameraControllerListener listener);

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera, 3:take jpeg fisheye image only
	 * @return
	 */
	public boolean sendPictureFormat(final int pictureFormat);

	/**
	 * 動画録画開始/終了指示, こっちは旧タイプのAPI
	 * ちなみにこのコマンドで鹿害開始/終了しなくても、何も設定していなければ自動録画が有効なので、
	 * 離陸と同時に撮影開始＆着陸と同時に撮影終了する
	 * @param start true: 録画開始, false: 録画終了
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id);

	/**
	 * 動画録画開始/終了指示, こっちは新しいタイプのAPI
	 * ちなみにこのコマンドで鹿害開始/終了しなくても、何も設定していなければ自動録画が有効なので、
	 * 離陸と同時に撮影開始＆着陸と同時に撮影終了する
	 * @param start true: 録画開始, false: 録画終了
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start);

	/**
	 * カメラの方向変更指示
	 * と言っても実際にカメラの物理的な向きが変わるわけではなく、
	 * 広角の魚眼レンズのどの領域を切り出すかを指定するだけ
	 * コールバックの返り値から推測すると設定可能なのは[-100;100]<br>
	 * ただし機体の種類によって設定可能な値が異なり可動範囲外だと飽和する
	 * (特に上方向は可動範囲が狭い)
	 * Tilt and pan value is saturated by the drone.<br>
	 * Saturation value is sent by the drone through CameraSettingsChanged command.
	 * @param tilt Tilt camera consign for the drone (in degree).
	 * @param pan Pan camera consign for the drone (in degree)
	 * @return
	 */
	public boolean sendCameraOrientation(final int tilt, final int pan);

	/**
	 * カメラの方向を取得
	 * @return
	 */
	public int getPan();

	/**
	 * カメラの方向を取得
	 * @return
	 */
	public int getTilt();

	/**
	 * オートホワイトバランス設定
	 * @param auto_white_balance<br>
	 * -1: 手動(これは従来のAPIを使う時だけ有効, 新しいAPIではオートホワイトバランスを無効にできなくなったのでとりあえずフラッシュにしている)
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 * @return
	 */
	public boolean sendAutoWhiteBalance(final int auto_white_balance);

	/**
	 * オートホワイトバランス設定を取得
	 * @return
	 */
	public int autoWhiteBalance();

	/**
	 * 露出設定
	 * @param exposure Exposure value (bounds given by ExpositionChanged arg min and max, by default [-3:3])
	 * @return
	 */
	public boolean sendExposure(final float exposure);

	/**
	 * 露出設定を取得
	 * @return
	 */
	public float exposure();
	/**
	 * 彩度設定
	 * @param saturation Saturation value (bounds given by SaturationChanged arg min and max, by default [-100:100])
	 * @return
	 */
	public boolean sendSaturation(final float saturation);

	/**
	 * 彩度設定値を取得
	 * @return
	 */
	public float saturation();

	/**
	 * タイムラプス設定(一定時間毎に自動撮影)
	 * @param enabled
	 * @param interval 撮影間隔[秒]
	 * @return
	 */
	public boolean sendTimelapseSelection(final boolean enabled, final float interval);

	/**
	 * 自動録画設定
	 * @param enabled
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendVideoAutoRecord(final boolean enabled, final int mass_storage_id);

	/**
	 * 映像のブレ補正設定
	 * @param enabled
	 * @return
	 */
	public boolean sendVideoStabilization(final boolean enabled);

	/**
	 * 映像のブレ補正用のジャイロ設定
	 * @param anglesDelay_s Shift by x seconds angles (video stabilization)
	 * @param gyrosDelay_s Shift by x seconds t gyros (wobble cancellation
	 * @return
	 */
	public boolean sendVideoSyncAnglesGyros(final float anglesDelay_s, final float gyrosDelay_s);

}
