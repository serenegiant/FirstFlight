package jp.co.rediscovery.arflight.attribute;

/** 機体情報の保持クラス */
public class AttributeDevice {
	/** 機体製品名 */
	private String mProductName;
	public void setProductName(final String name) {
		mProductName = name;
	}

	/** 機体製品名を取得 */
	public String productName() {
		return mProductName;
	}

	/** 機体のソフトウエアバージョン */
	private String mProductSoftware;
	/** 機体のハードプエあバージョン */
	private String mProductHardware;

	/**
	 * ソフトウエア/ハードウエアバージョンをセット
	 * @param software	ソフトウエアバージョン
	 * @param hardware	ハードウエアバージョン
	 */
	public void setProduct(final String software, final String hardware) {
		mProductSoftware = software;
		mProductHardware = hardware;
	}

	/**
	 * ソフトウエアバージョンを取得
	 * @return
	 */
	public String productSoftware() {
		return mProductSoftware;
	}

	/**
	 * ハードウエアバージョンを取得
	 * @return
	 */
	public String productHardware() {
		return mProductHardware;
	}

	/** シリアル番号(上位・下位) */
	private String mSerialHigh, mSerialLow;

	/**
	 * シリアル番号の下位をセット
	 * @param low
	 */
	public void setSerialLow(final String low) {
		mSerialLow = low;
	}

	/**
	 * シリアル番号の上位をセット
	 * @param high
	 */
	public void setSerialHigh(final String high) {
		mSerialHigh = high;
	}

	/**
	 * シリアル番号を取得
	 * @return
	 */
	public String getSerial() {
		return mSerialHigh + mSerialLow;
	}
}
