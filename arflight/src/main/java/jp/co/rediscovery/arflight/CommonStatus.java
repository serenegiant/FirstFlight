package jp.co.rediscovery.arflight;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2016, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

import jp.co.rediscovery.arflight.attribute.AttributeCalibration;
import jp.co.rediscovery.arflight.attribute.AttributePosition;

/** 各デバイス共通のステータスクラス */
public class CommonStatus {
	public static final int ALARM_NON = 0;
	public static final int ALARM_USER_EMERGENCY = 1;
	public static final int ALARM_CUTOUT = 2;
	public static final int ALARM_BATTERY_CRITICAL = 3;
	public static final int ALARM_BATTERY = 4;
	public static final int ALARM_TOO_MUCH_ANGLE = 5;
	public static final int ALARM_DISCONNECTED = 100;		// 切断, これはアプリ内のみで有効

	protected final Object mStateSync = new Object();
	protected final Object mSync = new Object();
	protected int mAlarmState = ALARM_NON;
	protected int mBatteryState = -1;
	protected int mWiFiSignalRssi = 0;

	private AttributePosition mPosition = new AttributePosition();
	private AttributePosition mHomePosition = new AttributePosition();

	/**
	 * 異常状態をセット
	 * @param alarm_state
	 */
	public void setAlarm(final int alarm_state) {
		synchronized (mStateSync) {
			if (mAlarmState != alarm_state) {
				mAlarmState = alarm_state;
			}
		}
	}

	/**
	 * 異常状態を取得
	 * @return ALARM_XXX定数のどれか
	 */
	public int getAlarm() {
		synchronized (mStateSync) {
			return mAlarmState;
		}
	}

	/**
	 * バッテリー残量をセット
	 * @param battery_state [0,100]
	 */
	public void setBattery(final int battery_state) {
		synchronized (mStateSync) {
			mBatteryState = battery_state;
		}
	}

	/**
	 * バッテリー残量を取得
	 * @return [0,100]
	 */
	public int getBattery() {
		synchronized (mStateSync) {
			return mBatteryState;
		}
	}

	/**
	 * WiFi信号強度をセット
	 * @param rssi
	 */
	public void setWiFiSignal(final int rssi) {
		synchronized (mStateSync) {
			mWiFiSignalRssi = rssi;
		}
	}

	/**
	 * WiFi信号強度を取得
	 * @return
	 */
	public int getWiFiSignal() {
		synchronized (mStateSync) {
			return mWiFiSignalRssi;
		}
	}

	/** 機器と接続しているかどうかを取得 */
	public boolean isConnected() {
		synchronized (mStateSync) {
			return (mAlarmState != ALARM_DISCONNECTED);
		}
	}

	/**
	 * 座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setPosition(final double latitude, final double longitude, final double altitude) {
		synchronized (mSync) {
			mPosition.set(latitude, longitude, altitude, 0.0);
		}
	}

	/**
	 * 座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 * @param heading 方位角(北磁極に対する回転方向, 未使用)
	 */
	public void setPosition(final double latitude, final double longitude, final double altitude, final double heading) {
		synchronized (mSync) {
			mPosition.set(latitude, longitude, altitude, heading);
		}
	}

	/**
	 * 経度をセット
	 * @param latitude
	 */
	public void latitude(final double latitude) {
		synchronized (mSync) {
			mPosition.latitude(latitude);
		}
	}
	/** 緯度を取得[度] */
	public double latitude() {
		synchronized (mSync) {
			return mPosition.latitude();
		}
	}

	/**
	 * 経度をセット
	 * @param longitude
	 */
	public void longitude(final double longitude) {
		synchronized (mSync) {
			mPosition.longitude(longitude);
		}
	}

	/**
	 * 経度を取得[度]を取得
	 * @return
	 */
	public double longitude() {
		synchronized (mSync) {
			return mPosition.longitude();
		}
	}

	/**
	 * 高度[m]を設定
	 * @param altitude
	 */
	public void altitude(final double altitude) {
		synchronized (mSync) {
			mPosition.altitude(altitude);
		}
	}

	/**
	 * 高度[m]を取得
	 * @return
	 */
	public double altitude() {
		synchronized (mSync) {
			return mPosition.altitude();
		}
	}

	/**
	 * 方位角[度]を設定
	 * @param heading
	 */
	public void heading(final double heading) {
		synchronized (mSync) {
			mPosition.heading(heading);
		}
	}

	/**
	 * 方位角[度]を取得
	 * @return
	 */
	public double heading() {
		synchronized (mSync) {
			return mPosition.heading();
		}
	}

	/**
	 * ホーム座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setHome(final double latitude, final double longitude, final double altitude) {
		synchronized (mSync) {
			mHomePosition.set(latitude, longitude, altitude);
		}
	}

	/**
	 * ホーム座標の経度をセット
	 * @param latitude
	 */
	public void homeLatitude(final double latitude) {
		synchronized (mSync) {
			mHomePosition.latitude(latitude);
		}
	}
	/**
	 * ホーム座標の緯度を取得[度]
	 * @return
	 */
	public double homeLatitude() {
		synchronized (mSync) {
			return mHomePosition.latitude();
		}
	}

	/**
	 * ホーム座標の経度をセット
	 * @param longitude
	 */
	public void homeLongitude(final double longitude) {
		synchronized (mSync) {
			mHomePosition.longitude(longitude);
		}
	}

	/**
	 * ホーム座標の経度を取得[度]
	 * @return
	 */
	public double homeLongitude() {
		synchronized (mSync) {
			return mHomePosition.longitude();
		}
	}

	/**
	 * ホーム座標の高度[m]を設定
	 * @param altitude
	 */
	public void homeAltitude(final double altitude) {
		synchronized (mSync) {
			mHomePosition.altitude(altitude);
		}
	}

	/**
	 * ホーム座標の高度[m]を取得
	 * @return
	 */
	public double homeAltitude() {
		synchronized (mSync) {
			return mHomePosition.altitude();
		}
	}

	/** 磁気センサーのキャリブレーション状態 */
	private AttributeCalibration mAttributeCalibration = new AttributeCalibration();

	/**
	 * デバイスの磁気センサーのキャリブレーション状態を設定
	 * @param x
	 * @param y
	 * @param z
	 * @param failed
	 */
	public void updateCalibrationState(final boolean x, final boolean y, final boolean z, final boolean failed) {
		mAttributeCalibration.update(x, y, z, failed);
	}

	/**
	 * デバイスの磁気センサーのキャリブレーションが必要かどうかを設定
	 * @param need_calibration
	 */
	public void needCalibration(final boolean need_calibration) {
		mAttributeCalibration.needCalibration(need_calibration);
	}

	/**
	 * デバイスのキャリブレーションが必要かどうかを取得
	 * @return
	 */
	public boolean needCalibration() {
		return mAttributeCalibration.needCalibration();
	}

}
