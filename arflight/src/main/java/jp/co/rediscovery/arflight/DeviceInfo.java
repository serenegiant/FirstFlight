package jp.co.rediscovery.arflight;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2018, saki t_saki@serenegiant.com
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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import java.util.Locale;

import static com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM.*;

/** デバイス状態の保持用 */
public class DeviceInfo implements Parcelable {
	public static final int CONNECT_STATE_DISCONNECT = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_NOTCONNECTED.getValue();	// 0
	public static final int CONNECT_STATE_CONNECTING = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTING.getValue();		// 1
	public static final int CONNECT_STATE_CONNECTED = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTED.getValue();		// 2
	public static final int CONNECT_STATE_DISCONNECTing = ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_DISCONNECTING.getValue();// 3

	public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>() {
		@Override
		public DeviceInfo createFromParcel(final Parcel in) {
			return new DeviceInfo(in);
		}

		@Override
		public DeviceInfo[] newArray(final int size) {
			return new DeviceInfo[size];
		}
	};

	private final Object mSync = new Object();
	private final String mName;
	private final int mProductId;
	private int connectionState;

	/** コンストラクタ */
	public DeviceInfo(final String name, final int product_id) {
		mName = name;
		mProductId = product_id;
		connectionState = CONNECT_STATE_DISCONNECT;
	}

	/** コピーコンストラクタ */
	public DeviceInfo(final DeviceInfo other) {
		mName = other.mName;
		mProductId = other.mProductId;
		synchronized (other.mSync) {
			connectionState = other.connectionState;
		}
	}

	/** Parcelからの生成用コンストラクタ */
	protected DeviceInfo(final Parcel in) {
		mName = in.readString();
		mProductId = in.readInt();
		connectionState = in.readInt();
	}

	/**
	 * デバイス名を取得
	 * @return
	 */
	public String name() {
		return mName;
	}

	/**
	 * 製品IDを取得
	 * @return
	 */
	public int productId() {
		return mProductId;
	}
	
	/**
	 * スカイコントローラーかどうかを取得
	 * @return
	 */
	public boolean isSkyController() {
		switch (ARDiscoveryService.getProductFromProductID(mProductId)) {
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER_NG:
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2P:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * ジャンピングスーモ達かどうかを取得
	 * @param device
	 * @return
	 */
	public static boolean isSkyController(@Nullable final ARDiscoveryDeviceService device) {
		if (device != null) {
			switch (ARDiscoveryService.getProductFromProductID(device.getProductID())) {
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER_NG:
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2P:
				return true;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * ミニドローン達かどうかを取得
	 * @return
	 */
	public boolean isMiniDrone() {
		switch (ARDiscoveryService.getProductFromProductID(mProductId)) {
		case ARDISCOVERY_PRODUCT_MINIDRONE:
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
		case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:
		case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * ジャンピングスーモ達かどうかを取得
	 * @param device
	 * @return
	 */
	public static boolean isMiniDrone(@Nullable final ARDiscoveryDeviceService device) {
		if (device != null) {
			switch (ARDiscoveryService.getProductFromProductID(device.getProductID())) {
			case ARDISCOVERY_PRODUCT_MINIDRONE:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
			case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:
			case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:
				return true;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * ジャンピングスーモ達かどうかを取得
	 * @return
	 */
	public boolean isJS() {
		switch (ARDiscoveryService.getProductFromProductID(mProductId)) {
		case ARDISCOVERY_PRODUCT_JS:
		case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
		case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * ジャンピングスーモ達かどうかを取得
	 * @param device
	 * @return
	 */
	public static boolean isJS(@Nullable final ARDiscoveryDeviceService device) {
		if (device != null) {
			switch (ARDiscoveryService.getProductFromProductID(device.getProductID())) {
			case ARDISCOVERY_PRODUCT_JS:
			case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
				return true;
			default:
				break;
			}
		}
		return false;
	}

	/**
	 * 接続状態をセット
	 * @param connection_state
	 */
	public void connectionState(final int connection_state) {
		synchronized (mSync) {
			if (connectionState != connection_state) {
				connectionState = connection_state;
			}
		}
	}

	/**
	 * 接続状態を取得
	 * @return
	 */
	public int connectionState() {
		synchronized (mSync) {
			return connectionState;
		}
	}

	/**
	 * デバイスと接続しているかどうか
	 * @return
	 */
	public boolean isConnected() {
		synchronized (mSync) {
			return (connectionState == CONNECT_STATE_CONNECTING) || (connectionState == CONNECT_STATE_CONNECTED);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeString(mName);
		dest.writeInt(mProductId);
		synchronized (mSync) {
			dest.writeInt(connectionState);
		}
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "DeviceInfo(%s,id=%d,state=%d)", mName, mProductId, connectionState);
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof DeviceInfo) {
			return (((mName == null) && (((DeviceInfo)other).mName == null))
					|| ((mName != null) && mName.equals(((DeviceInfo)other).mName)))
				&& (mProductId == ((DeviceInfo)other).mProductId)
				&& (connectionState == ((DeviceInfo)other).connectionState);
		} else {
			return super.equals(other);
		}
	}
}
