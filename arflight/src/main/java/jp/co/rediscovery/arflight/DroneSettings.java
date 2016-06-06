package jp.co.rediscovery.arflight;

import jp.co.rediscovery.arflight.attribute.AttributeCamera;
import jp.co.rediscovery.arflight.attribute.AttributeFloat;
import jp.co.rediscovery.arflight.attribute.AttributeTimeLapse;

/** デバイスの設定値保持用のクラス */
public class DroneSettings {
	/** 国コード */
	protected String mCountryCode;

	/**
	 * 国コードをセット
	 * @param code 国コード(ISO 3166形式, 空文字列は国が不明であることを意味する)
	 */
	public void setCountryCode(final String code) {
		mCountryCode = code;
	}
	public String countryCode() {
		return mCountryCode;
	}

	/** 自動国選択が有効かどうか */
	protected boolean mAutomaticCountry;

	/**
	 * 自動国選択設定をセット
	 * @param auto
	 */
	public void setAutomaticCountry(final boolean auto) {
		mAutomaticCountry = auto;
	}

	/**
	 * 自動国選択設定が有効かどうかを取得
	 * @return
	 */
	public boolean automaticCountry() {
		return mAutomaticCountry;
	}

	/**
	 * モーターの自動カット機能が有効かどうか
	 */
	protected boolean mCutOffMode;

	/**
	 * モーターの自動カット機能が有効かどうかをセット
	 * @param cutoff_mode
	 */
	public void setCutOffMode(final boolean cutoff_mode) {
		mCutOffMode = cutoff_mode;
	}

	/**
	 * モーターの自動カット機能が有効かどうかを取得
	 * @return
	 */
	public boolean cutOffMode() {
		return mCutOffMode;
	}

	/**
	 * 自動離陸モードが有効かどうか
	 */
	protected boolean mAutoTakeOffMode;

	/**
	 * 自動離陸モードが有効かどうかをセット
	 * @param auto_takeoff
	 */
	public void setAutoTakeOffMode(final boolean auto_takeoff) {
		mAutoTakeOffMode = auto_takeoff;
	}

	/**
	 * 自動離陸モードが有効かどうかを取得
	 * @return
	 */
	public boolean autoTakeOffMode() {
		return mAutoTakeOffMode;
	}

	/**
	 * ガード(ハル)を装着しているかどうか
	 */
	protected boolean mHasGuard;

	/**
	 * ガード(ハル)を装着しているかどうかをセット
	 * @param has_guard
	 */
	public void setHasGuard(final boolean has_guard) {
		mHasGuard = has_guard;
	}

	/**
	 * ガード(ハル)を装着しているかどうかを取得
	 * @return
	 */
	public boolean hasGuard() {
		return mHasGuard;
	}

	/** 室外モードかどうか */
	private boolean mOutdoorMode;

	/**
	 * 室外モードかどうかをセット
	 * @param outdoor_mode
	 */
	public void outdoorMode(final boolean outdoor_mode) {
		mOutdoorMode = outdoor_mode;
	}

	/**
	 * 室外モードかどうかを取得
	 * @return
	 */
	public boolean outdoorMode() {
		return mOutdoorMode;
	}

	/** カメラ設定 */
	protected final AttributeCamera mCamera = new AttributeCamera();

	/**
	 * カメラ設定を取得
	 * @return
	 */
	public AttributeCamera getCamera() {
		return mCamera;
	}

	/**
	 * カメラ設定をセット
	 * @param fov 視野角
	 * @param panMax pan最大値
	 * @param panMin pan最小値
	 * @param tiltMax tilt最大値
	 * @param tiltMin tilt最小値
	 */
	public void setCameraSettings(final float fov, final float panMax, final float panMin, final float tiltMax, final float tiltMin) {
		mCamera.setSettings(fov, panMax, panMin, tiltMax, tiltMin);
	}

	/**
	 * カメラのtilt設定を取得
	 * @return
	 */
	public AttributeFloat cameraTilt() {
		return mCamera.tilt();
	}

	/**
	 * 現在のtilt値をセット
	 * @param tilt
	 */
	public void currentCameraTilt(final float tilt) {
		mCamera.tilt().current(tilt);
	}

	/**
	 * 現在のtilt値を取得
	 * @return
	 */
	public float currentCameraTilt() {
		return mCamera.tilt().current();
	}

	/**
	 * カメラのpan設定を取得
	 * @return
	 */
	public AttributeFloat cameraPan() {
		return mCamera.pan();
	}

	/**
	 * 現在のpan値をセット
	 * @param pan
	 */
	public void currentCameraPan(final float pan) {
		mCamera.pan().current(pan);
	}

	/**
	 * 現在のpan値を取得
	 * @return
	 */
	public float currentCameraPan() {
		return mCamera.pan().current();
	}

	/**
	 * オートホワイトバランス設定をセット
	 * @param auto_white_balance   <br>
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 */
	public void autoWhiteBalance(final int auto_white_balance) {
		mCamera.autoWhiteBalance(auto_white_balance);
	}

	/**
	 * オートホワイトバランス設定を取得
	 * @return <br>
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 */
	public int autoWhiteBalance() {
		return mCamera.autoWhiteBalance();
	}

	/**
	 * 露出設定をセット
	 * @param current
	 * @param min
	 * @param max
	 */
	public void setExposure(final float current, final float min, final float max) {
		mCamera.setExposure(current, min, max);
	}

	/**
	 * 露出設定を取得
	 * @return
	 */
	public AttributeFloat exposure() {
		return mCamera.exposure();
	}

	/**
	 * 彩度設定をセット
	 * @param current
	 * @param min
	 * @param max
	 */
	public void setSaturation(final float current, final float min, final float max) {
		mCamera.setSaturation(current, min, max);
	}

	/**
	 * 彩度設定を取得
	 * @return
	 */
	public AttributeFloat saturation() {
		return mCamera.saturation();
	}

	/**
	 * タイムラプス撮影設定をセット
	 * @param enabled
	 * @param current
	 * @param min
	 * @param max
	 */
	public void setTimeLapse(final boolean enabled, final float current, final float min, final float max) {
		mCamera.setTimeLapse(enabled, current, min, max);
	}

	/**
	 * タイムラプス撮影設定を取得
	 * @return
	 */
	public AttributeTimeLapse timeLapse() {
		return mCamera.timeLapse();
	}

	// 操縦設定
	protected final AttributeFloat mMaxAltitude = new AttributeFloat();
	protected final AttributeFloat mMaxTilt = new AttributeFloat();
	protected final AttributeFloat mMaxVerticalSpeed = new AttributeFloat();
	protected final AttributeFloat mMaxHorizontalSpeed = new AttributeFloat();
	protected final AttributeFloat mMaxRotationSpeed = new AttributeFloat();
	protected final AttributeFloat mMaxDistance = new AttributeFloat();

	/**
	 * 最大高度設定をセット
	 * @param current 現在値[m]
	 * @param min 設定可能最小値[m]
	 * @param max 設定可能最大値
	 */
	public void setMaxAltitude(final float current, final float min, final float max) {
		mMaxAltitude.set(current, min, max);
	}

	/**
	 * 最大高度設定を取得
	 * @return m
	 */
	public AttributeFloat maxAltitude() {
		return mMaxAltitude;
	}

	/**
	 * 最大傾斜角設定(機体の前後左右への移動速度に対応)をセット
	 * @param current 現在値[度]
	 * @param min 設定可能最小値[度]
	 * @param max 設定可能最大値[度]
	 */
	public void setMaxTilt(final float current, final float min, final float max) {
		mMaxTilt.set(current, min, max);
	}

	/**
	 * 最大傾斜角設定を取得
	 * @return
	 */
	public AttributeFloat maxTilt() {
		return mMaxTilt;
	}

	/**
	 * 最大上昇・降下速度設定をセット
	 * @param current 現在値[m/s]
	 * @param min 設定可能最小値[m/s]
	 * @param max 設定可能最大値[m/s]
	 */
	public void setMaxVerticalSpeed(final float current, final float min, final float max) {
		mMaxVerticalSpeed.set(current, min, max);
	}

	/**
	 * 最大上昇・降下速度設定を取得
	 * @return m/s
	 */
	public AttributeFloat maxVerticalSpeed() {
		return mMaxVerticalSpeed;
	}

	/**
	 * 最大飛行速度設定をセット
	 * @param current
	 * @param min
	 * @param max
	 */
	public void setMaxHorizontalSpeed(final float current, final float min, final float max) {
		mMaxHorizontalSpeed.set(current, min, max);
	}

	/**
	 * 最大飛行速度設定を取得
	 * @return
	 */
	public AttributeFloat maxHorizontalSpeed() {
		return mMaxHorizontalSpeed;
	}

	/**
	 * 最大回転速度設定をセット
	 * @param current 現在値[度/秒]
	 * @param min 設定可能最小値[度/秒]
	 * @param max 設定可能最大値[度/秒]
	 */
	public void setMaxRotationSpeed(final float current, final float min, final float max) {
		mMaxRotationSpeed.set(current, min, max);
	}

	/**
	 * 最大回転速度設定を取得
	 * @return
	 */
	public AttributeFloat maxRotationSpeed() {
		return mMaxRotationSpeed;
	}

	/**
	 * 最大飛行可能距離をセット
	 * @param current
	 * @param min
	 * @param max
	 */
	public void setMaxDistance(final float current, final float min, final float max) {
		mMaxDistance.set(current, min, max);
	}

	/**
	 * 最大飛行可能距離設定を取得
	 * @return
	 */
	public AttributeFloat maxDistance() {
		return mMaxDistance;
	}
}
