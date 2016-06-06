package jp.co.rediscovery.arflight;

/** WiFi経由で接続するデバイス用の追加メソッド定義用のインターフェース */
public interface IWiFiController {
	/**
	 * 国コードを設定する　
	 * @param code 国コード(ISO 3166形式, 空文字列は国が不明であることを意味する)
	 * @return
	 */
	public boolean sendCountryCode(final String code);

	/**
	 * 自動国選択モードを使うかどうかを設定する
	 * @param auto
	 * @return
	 */
	public boolean sendAutomaticCountry(final boolean auto);
	/**
	 * 室内モードか屋外モードかをセット
	 * 日本だと外で飛ばす時は屋外モードにして2.4GHz帯を使わんと電波法違反になるんやけど
	 * 初期設定は5GHz帯を使うようになっとる場合があるから要注意や。
	 * つまり外で飛ばすならまず室内で屋外モードにしてから外に持っていかなあかんちゅうことやで
	 * @param outdoor
	 * @return
	 */
	public boolean sendSettingsOutdoor(final boolean outdoor);
	public boolean isOutdoor();
}
