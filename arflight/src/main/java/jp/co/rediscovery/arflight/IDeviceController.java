package jp.co.rediscovery.arflight;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.sql.Date;

public interface IDeviceController {
	public static final int STATE_STOPPED = 0x0000;
	public static final int STATE_STARTING = 0x0001;
	public static final int STATE_STARTED = 0x0002;
	public static final int STATE_STOPPING = 0x0003;

//	ARCONTROLLER_DEVICE_STATE_ENUM.
//		ARCONTROLLER_DEVICE_STATE_STOPPED (0, "device controller is stopped"),
//		ARCONTROLLER_DEVICE_STATE_STARTING (1, "device controller is starting"),
//		ARCONTROLLER_DEVICE_STATE_RUNNING (2, "device controller is running"),
//		ARCONTROLLER_DEVICE_STATE_PAUSED (3, "device controller is paused"),
//		ARCONTROLLER_DEVICE_STATE_STOPPING (4, "device controller is stopping"),

	/**
	 * コントローラーに関連付けられているARDiscoveryDeviceServiceを取得
	 * デバイス探索サービスから取得したARDiscoveryDeviceServiceを返す
	 * 変更しちゃダメ
	 * @return
	 */
	public ARDiscoveryDeviceService getDeviceService();

	/**
	 * 関係するリソースを破棄する
	 * 再利用は出来ない
	 */
	public void release();

	/**
	 * デバイス名を取得, 例えばローリングスパイダーだと通常はrs_xxxxxって奴
	 * @return
	 */
	public String getName();

	/**
	 * 製品名を取得
	 * @return
	 */
	public String getProductName();

	public int getProductId();

	/**
	 * 接続しているデバイスのソフトウエアバージョンを取得
	 * @return
	 */
	public String getSoftwareVersion();

	/**
	 * 接続しているデバイスのハードウエアバージョンを取得
	 * @return
	 */
	public String getHardwareVersion();

	/**
	 * 接続しているデバイスのシリアル番号を取得
	 * @return
	 */
	public String getSerial();

	/**
	 * デバイスの異常状態を取得
	 * @return
	 */
	public int getAlarm();

	/**
	 * バッテリー残量を取得
	 * バッテリー保護のために30%ぐらいになったら飛ばすのを止めるべき
	 * @return バッテリー残量[0,100][%]
	 */
	public int getBattery();

	/**
	 * WiFi信号強度を取得
	 * @return
	 */
	public int getWiFiSignal();

	/**
	 * コールバックリスナーを追加
	 * @param mListener
	 */
	public void addListener(final DeviceConnectionListener mListener);

	/**
	 * コールバックリスナーを除去
	 * @param mListener
	 */
	public void removeListener(final DeviceConnectionListener mListener);

	/**
	 * 下位2バイトは接続ステータス, その上2バイトは飛行ステータス
	 * @return
	 */
	public int getState();

	/**
	 * 接続開始
	 * @return
	 */
	public boolean start();

	/**
	 * 接続処理を中断
	 */
	public void cancelStart();

	/**
	 * 切断処理。子クラスで追加処理が必要であれば#internal_stopをOverrideすること
	 */
	public void stop();

	/**
	 * デバイスと接続しているかどうか
	 * スカイコントローラー経由の場合はスカイコントローラーとの接続状態
	 * @return
	 */
	public boolean isStarted();

	/***
	 * デバイスと接続しているかどうか
	 * 直接接続の時は#isStartedと同じ
 	 * スカイコントローラー経由の場合はスカイコントローラーを経由してデバイスと接続しているかどうか
	 * @return
	 */
	public boolean isConnected();

	/**
	 * ネットワーク切断要求
	 * @return
	 */
	public boolean sendNetworkDisconnect();

	/**
	 * 日付を送信
	 * 従来のAPIやと接続開始時に必ず送らなあかんかったけど、新しいAPIはどうなんやろ？
	 * 今のところは送らんでも問題なさそうやねんけど。中で勝手に送っとるのかもしれん
	 * @param currentDate
	 * @return
	 */
	public boolean sendDate(Date currentDate);

	/**
	 * 時刻を送信
	 * 従来のAPIやと接続開始時に必ず送らなあかんかったけど、新しいAPIはどうなんやろ？
	 * 今のところは送らんでも問題なさそうやねんけど。中で勝手に送っとるのかもしれん
	 * @param currentTime
	 * @return
	 */
	public boolean sendTime(Date currentTime);

	/**
	 * デバイスの設定を全て送るように要求する
	 * @return
	 */
	public boolean requestAllSettings();

	/**
	 * デバイスのステータスを全て送るように要求する
	 * @return
	 */
	public boolean requestAllStates();

	/**
	 * デバイス設定をリセットする
	 * @return
	 */
	public boolean sendSettingsReset();

	/**
	 * デバイス名をセットする
	 * コマンド名は製品名となっとるけど(ARSDKのメソッド名は'sendSettingsProductName')
	 * 実際にはデバイス名(WiFi接続やとアクセスポイント名として見えるやつ)
	 * @param name
	 * @return
	 */
	public boolean sendSettingsProductName(final String name);

	/**
	 * デバイスを再起動させる
	 * @return
	 */
	public boolean sendCommonReboot();

}
