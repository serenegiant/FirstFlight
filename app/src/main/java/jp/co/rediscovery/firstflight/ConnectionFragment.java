package jp.co.rediscovery.firstflight;
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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import jp.co.rediscovery.arflight.ARDeviceServiceAdapter;
import jp.co.rediscovery.arflight.ManagerFragment;

/***
 * デバイス探索画面用Fragment
 **/
public class ConnectionFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = ConnectionFragment.class.getSimpleName();

	public static ConnectionFragment newInstance() {
		return new ConnectionFragment();
	}

	/*** 操縦画面等へ遷移するためのアイコン */
	protected ImageButton mDownloadBtn, mPilotBtn, mGalleyBrn, mAutoBtn;
	protected ListView mDeviceListView;

	public ConnectionFragment() {
		super();
		// Required empty public constructor
	}

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
		final ViewGroup container, final Bundle savedInstanceState) {

//		if (DEBUG) Log.v(TAG, "onCreateView:");
		loadArguments(savedInstanceState);
		final View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume:");
		runOnUiThread(mStartDiscoveryOnUITask);
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause:");

		updateButtons(false);
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		if (manager != null) {
			manager.stopDiscovery();
			manager.removeCallback(mManagerCallback);
		}
		super.onPause();
	}

	@Override
	protected void onUpdateLocationPermission(final String permission, final boolean hasPermission) {
		if (hasPermission) {
			runOnUiThread(mStartDiscoveryOnUITask, 100);
		}
	}

	private final Runnable mStartDiscoveryOnUITask = new Runnable() {
		@Override
		public void run() {
			if (checkPermissionLocation()) {
				final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
				manager.startDiscovery();
				manager.addCallback(mManagerCallback);
			}
			updateButtons(false);
		}
	};

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {

		final ARDeviceServiceAdapter adapter = new ARDeviceServiceAdapter(getActivity(), R.layout.list_item_deviceservice);

		mDeviceListView = rootView.findViewById(R.id.list);
		final View empty_view = rootView.findViewById(R.id.empty_view);
		mDeviceListView.setEmptyView(empty_view);
		mDeviceListView.setAdapter(adapter);
		mDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		mDownloadBtn = rootView.findViewById(R.id.download_button);
		mDownloadBtn.setOnClickListener(mOnClickListener);
		mDownloadBtn.setOnLongClickListener(mOnLongClickListener);

		mPilotBtn = rootView.findViewById(R.id.pilot_button);
		mPilotBtn.setOnClickListener(mOnClickListener);
		mPilotBtn.setOnLongClickListener(mOnLongClickListener);

		mGalleyBrn = rootView.findViewById(R.id.gallery_button);
		mGalleyBrn.setOnClickListener(mOnClickListener);
		mGalleyBrn.setOnLongClickListener(mOnLongClickListener);

		mAutoBtn = rootView.findViewById(R.id.auto_button);
		mAutoBtn.setOnClickListener(mOnClickListener);
		mAutoBtn.setOnLongClickListener(mOnLongClickListener);
	}

	private void updateButtons(final boolean visible) {
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateButtonsOnUiThread(visible);
				}
			});
		}
	}

	protected void updateButtonsOnUiThread(final boolean visible) {
		if (!visible) {
			try {
				final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
				adapter.clear();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
		mDownloadBtn.setVisibility(visibility);
		mPilotBtn.setVisibility(visibility);
		mAutoBtn.setVisibility(visibility);
	}

	/**
	 * 検出したデバイスのリストが更新された時のコールバック
	 */
	private ManagerFragment.ManagerCallback mManagerCallback = new ManagerFragment.ManagerCallback() {
		@Override
		public void onServicesDevicesListUpdated(final List<ARDiscoveryDeviceService> devices) {
			if (DEBUG) Log.v(TAG, "onServicesDevicesListUpdated:");
			final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter) mDeviceListView.getAdapter();
			adapter.clear();
			for (final ARDiscoveryDeviceService service : devices) {
				if (DEBUG) Log.d(TAG, "service :  " + service);
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
				switch (product) {
//				case ARDISCOVERY_PRODUCT_NSNETSERVICE:			// WiFi products category
				case ARDISCOVERY_PRODUCT_ARDRONE:				// Bebop Drone product
				case ARDISCOVERY_PRODUCT_BEBOP_2:				// Bebop drone 2.0 product
					adapter.add(service);
					break;
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER:			// Sky controller product
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:		// Sky controller 2 product
					adapter.add(service);
					break;
//				case ARDISCOVERY_PRODUCT_BLESERVICE:			// BlueTooth products category
				case ARDISCOVERY_PRODUCT_MINIDRONE:				// DELOS product
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:	// Delos EVO Light product
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:	// Delos EVO Brick product
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL:// Delos EVO Hydrofoil product
				case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:		// Delos3 product
				case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:		// WingX product
					adapter.add(service);
					break;
				case ARDISCOVERY_PRODUCT_JS:					// JUMPING SUMO product
				case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:			// Jumping Sumo EVO Light product
				case ARDISCOVERY_PRODUCT_JS_EVO_RACE:			// Jumping Sumo EVO Race product
					// FIXME JumpingSumoは未実装
					break;
//				case ARDISCOVERY_PRODUCT_POWER_UP:				// Power up product
//				case ARDISCOVERY_PRODUCT_EVINRUDE:				// Evinrude product
//				case ARDISCOVERY_PRODUCT_UNKNOWNPRODUCT_4:		// Unknownproduct_4 product
//				case ARDISCOVERY_PRODUCT_USBSERVICE:			// AOA/iAP usb product category
//				case ARDISCOVERY_PRODUCT_UNSUPPORTED_SERVICE:	// Service is unsupported:
//				case ARDISCOVERY_PRODUCT_TINOS:					// Tinos product
//				case ARDISCOVERY_PRODUCT_MAX:					// Max of products
				default:
					break;
				}
/*				// ブルートゥース接続の時だけ追加する
				if (service.getDevice() instanceof ARDiscoveryDeviceBLEService) {
					adapter.add(service.getName());
				} */
			}
			adapter.notifyDataSetChanged();
			mDeviceListView.setItemChecked(0, true);	// 先頭を選択
			updateButtons(adapter.getCount() > 0);
		}
	};

	private void clearCheck(final ViewGroup parent) {
		final int n = parent.getChildCount();
		for (int i = 0; i < n; i++) {
			final View v = parent.getChildAt(i);
			if (v instanceof Checkable) {
				((Checkable) v).setChecked(false);
			}
		}
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			ConnectionFragment.this.onClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			if (checkPermissionLocation()) {
				fragment = getFragment(position, true);
			}
			break;
		case R.id.download_button:
			if (checkPermissionWriteExternalStorage()) {
				fragment = getFragment(position, false);
			}
			break;
		case R.id.gallery_button:
			if (checkPermissionWriteExternalStorage()) {
				fragment = GalleyFragment.newInstance();
			}
			break;
		case R.id.auto_button:
		{
			if (checkPermissionLocation()) {
//				final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
				final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
//				final String itemValue = adapter.getItemName(position);
//				final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
				final ARDiscoveryDeviceService device = adapter.getItem(position);
				if (device != null) {
					// 製品名を取得
					final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());

					switch (product) {
					case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
						fragment = AutoPilotFragment.newInstance(device, null, "bebop", AutoPilotFragment.MODE_TRACE);
						break;
					case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
						fragment = AutoPilotFragment.newInstance(device, null, "bebop2", AutoPilotFragment.MODE_TRACE);
						break;
					case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyControllerNewAPI
					case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:
						fragment = newBridgetFragment(device);
						break;
					default:
						Toast.makeText(getActivity(), R.string.unsupported_product, Toast.LENGTH_SHORT).show();
						break;
					}
				}
			}
			break;
		}
		}
		replace(fragment);
	}

	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			if (mPilotBtn.getVisibility() != View.VISIBLE) return false;
			return ConnectionFragment.this.onLongClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	protected boolean onLongClick(final View view, final int position) {
		return false;
	}

	protected Fragment getFragment(final int position, final boolean isPiloting) {
//		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
//		final String itemValue = adapter.getItemName(position);
//		final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
		final ARDiscoveryDeviceService device = adapter.getItem(position);
		Fragment fragment = null;
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());

			switch (product) {
			case ARDISCOVERY_PRODUCT_ARDRONE:				// Bebop Drone product
			case ARDISCOVERY_PRODUCT_BEBOP_2:				// Bebop drone 2.0 product
				fragment = isPiloting ? PilotFragment.newInstance(device, null) : MediaFragment.newInstance(device, null);
				break;
			case ARDISCOVERY_PRODUCT_JS:					// JUMPING SUMO product
			case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:			// Jumping Sumo EVO Light product
			case ARDISCOVERY_PRODUCT_JS_EVO_RACE:			// Jumping Sumo EVO Race product
				//FIXME JumpingSumoは未実装
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE:				// DELOS product
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:	// Delos EVO Light product
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:	// Delos EVO Brick product
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL:// Delos EVO Hydrofoil product
			case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:		// Delos3 product
			case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:		// WingX product
				fragment = isPiloting ? PilotFragment.newInstance(device, null) : MediaFragment.newInstance(device, null);
				break;
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:			// SkyController
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:		// SkyController2
				fragment = newBridgetFragment(device);
				break;
			}
		}
		return fragment;
	}

	protected BridgeFragment newBridgetFragment(final ARDiscoveryDeviceService device) {
		return BridgeFragment.newInstance(device);
	}

}
