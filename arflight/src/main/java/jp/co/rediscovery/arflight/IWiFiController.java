package jp.co.rediscovery.arflight;

/** WiFi経由で接続する機体用の追加メソッド */
public interface IWiFiController {
	/**
	 * 室外モードか室内モードかを設定
	 * @param is_outdoor
	 * @return
	 */
	public boolean sendSettingsOutdoor(final boolean is_outdoor);
	public boolean isOutdoor();
}
