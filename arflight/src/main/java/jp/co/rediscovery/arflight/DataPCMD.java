package jp.co.rediscovery.arflight;

/** 飛行制御値保持用クラス */
public class DataPCMD {
	/** 移動するかどうか */
	public int flag;
	/** デバイスの左右方向の傾き[-100,+100], デバイスの最大傾斜設定[度]に対する割合[%] */
	public float roll;
	/** デバイスの機首を上げ下げ[-100,+100], デバイスの最大傾斜設定[度]に対する割合[%] */
	public float pitch;
	/** デバイスの水平回転[-100,+100], デバイスの最大回転速度[度/秒]に対する割合[%] */
	public float yaw;
	/** 高度制御[100,+100], デバイスの垂直移動速度設定[m/秒]に対する割合[%] */
	public float gaz;
	/** 方位角、デバイス側で未実装  */
	public float heading;
	/** 送信要求フラグ */
	public boolean requestSend;

	/** コンストラクタ */
	public DataPCMD() {
		flag = 0;
		roll = pitch = yaw = gaz = heading = 0;
	}

	/** 指定した他のDataPCMDから値を取得してセットする */
	public void set(final DataPCMD other) {
		flag = other.flag;
		roll = other.roll;
		pitch = other.pitch;
		yaw = other.yaw;
		gaz = other.gaz;
		heading = other.heading;
	}
}
