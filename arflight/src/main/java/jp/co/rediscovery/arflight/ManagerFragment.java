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
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiver;
import com.parrot.arsdk.ardiscovery.receivers.ARDiscoveryServicesDevicesListUpdatedReceiverDelegate;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.rediscovery.arflight.controllers.FlightControllerBebop2;
import jp.co.rediscovery.arflight.controllers.FlightControllerBebop;
import jp.co.rediscovery.arflight.controllers.FlightControllerCargoDrone;
import jp.co.rediscovery.arflight.controllers.FlightControllerMambo;
import jp.co.rediscovery.arflight.controllers.FlightControllerMiniDrone;
import jp.co.rediscovery.arflight.controllers.FlightControllerWingX;
import jp.co.rediscovery.arflight.controllers.SkyController;
import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.utils.HandlerThreadHandler;

/*** 検出・接続したデバイスを管理するための非UI Fragment */
public class ManagerFragment extends Fragment {
	private static final String TAG = ManagerFragment.class.getSimpleName();

	public interface ManagerCallback {
		public void onServicesDevicesListUpdated(final List<ARDiscoveryDeviceService> devices);
	}

	public interface StartControllerListener {
		public void onResult(final IDeviceController controller, final boolean success);
	}

	/**
	 * ManagerFragmentを取得する
	 * @param activity
	 * @return
	 */
	public static synchronized ManagerFragment getInstance(final Activity activity) {
		ManagerFragment result = null;
		if ((activity != null) && !activity.isFinishing()) {
			final FragmentManager fm = activity.getFragmentManager();
			result = (ManagerFragment)fm.findFragmentByTag(TAG);
			if (result == null) {
				result = new ManagerFragment();
				fm.beginTransaction().add(result, TAG).commit();
			}
		}
		return result;
	}

	/**
	 * 指定したインデックスのARDiscoveryDeviceServiceインスタンスを取得する
	 * @param activity
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static ARDiscoveryDeviceService getDevice(final Activity activity, final int index) {
		ARDiscoveryDeviceService result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getDevice(index);
		return result;
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスを取得する
	 * @param activity
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static ARDiscoveryDeviceService getDevice(final Activity activity, final String name) {
		ARDiscoveryDeviceService result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getDevice(name);
		return result;
	}

	/**
	 * 指定したインデックスのARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static IDeviceController getController(final Activity activity, final int index) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(index);
		return result;
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public static IDeviceController getController(final Activity activity, final String name) {
		IDeviceController result = null;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			result = fragment.getController(name);
		return result;
	}

	/**
	 * 指定したARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param activity
	 * @param device
	 * @return
	 */
	public static IDeviceController getController(final Activity activity, final ARDiscoveryDeviceService device) {
		IDeviceController result = null;
		final ManagerFragment fragment = getInstance(activity);
		if (fragment != null)
			result = fragment.getController(device);
		return result;
	}

	public static IDeviceController startController(final Activity activity, final IDeviceController controller, final StartControllerListener listener) {
		if (controller != null) {
			final ManagerFragment fragment = getInstance(activity);
			if (fragment != null) {
				fragment.startController(controller, listener);
			} else {
				throw new RuntimeException("not attached to Activity");
			}
		}
		return controller;
	}

	/**
	 * 指定したIDeviceControllerを取り除く, IFlightController#releaseを呼んで破棄する
	 * @param activity
	 * @param controller
	 */
	public static void releaseController(final Activity activity, final IDeviceController controller) {
		if (controller == null) return;
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null) {
			fragment.releaseController(controller);
		} else {
			Log.d(TAG, "no activity, try to release on private thread.");
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						controller.release();
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			}, TAG).start();
		}
	}

	/**
	 * 全てのARDiscoveryDeviceServiceとIDeviceControllerを取り除く
	 * @param activity
	 */
	public static void releaseAll(final Activity activity) {
		final ManagerFragment fragment =  getInstance(activity);
		if (fragment != null)
			fragment.releaseAll();
	}

	private final Object mSync = new Object();
	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();
	private Handler mAsyncHandler;

	private ARDiscoveryService ardiscoveryService;
	private boolean ardiscoveryServiceBound = false;
	private boolean mRegistered = false;
	private IBinder discoveryServiceBinder;
	private final List<ARDiscoveryDeviceService> mDevices = new ArrayList<ARDiscoveryDeviceService>();
	private final Map<String, WeakReference<IDeviceController>> mControllers = new HashMap<String, WeakReference<IDeviceController>>();

	private final List<ManagerCallback> mCallbacks = new ArrayList<ManagerCallback>();

	public ManagerFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		synchronized (mSync) {
			mAsyncHandler = HandlerThreadHandler.createHandler(TAG);
		}
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//	}

	@Override
	public void onPause() {
		stopDiscovery();
		super.onPause();
	}

	@Override
	public void onDetach() {
		releaseAll();
		synchronized (mSync) {
			if (mAsyncHandler != null) {
				try {
					mAsyncHandler.getLooper().quit();
				} catch (final Exception e) {
					//
				}
				mAsyncHandler = null;
			}
		}
		super.onDetach();
	}

	/** デバイス探索開始 */
	public void startDiscovery() {
		mDeviceListUpdatedReceiverDelegate.onServicesDevicesListUpdated();
		bindServices();
		registerReceivers();
	}

	/** デバイス探索終了 */
	public void stopDiscovery() {
		unregisterReceivers();
		unbindServices();
	}

	/**
	 * 検出したデバイス一覧が変更された時のコールバックを追加する
	 * @param callback
	 */
	public void addCallback(final ManagerCallback callback) {
		synchronized (mDevices) {
			boolean found = false;
			for (final ManagerCallback cb: mCallbacks) {
				if (cb.equals(callback)) {
					found = true;
					break;
				}
			}
			if (!found) {
				mCallbacks.add(callback);
			}
		}
		callOnServicesDevicesListUpdated();
	}

	/**
	 * 検出したデバイス一覧が変更された時のコールバックを除去する
	 * @param callback
	 */
	public void removeCallback(final ManagerCallback callback) {
		synchronized (mDevices) {
			for (; mCallbacks.remove(callback) ;) {}
		}
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param name
	 * @return nameに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public IDeviceController getController(final String name) {
		IDeviceController result = null;
		synchronized (mControllers) {
			final ARDiscoveryDeviceService device = getDevice(name);
			if (device != null) {
				result = internalGetController(name);
				if (result == null) {
					result = createController(device);
				}
			}
		}
		return result;
	}

	/**
	 * 指定したindexのARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param index
	 * @return indexに対応するARDiscoveryDeviceServiceが見つからなければnull
	 */
	public IDeviceController getController(final int index) {
		IDeviceController result = null;
		synchronized (mControllers) {
			final ARDiscoveryDeviceService device = getDevice(index);
			if (device != null) {
				result = internalGetController(device.getName());
				if (result == null) {
					result = createController(device);
				}
			}
		}
		return result;
	}

	/**
	 * 指定したARDiscoveryDeviceServiceインスタンスに対応するIDeviceControllerを取得する
	 * @param device
	 * @return 一致するものがなければ生成する
	 */
	public IDeviceController getController(final ARDiscoveryDeviceService device) {
		IDeviceController result = null;
		synchronized (mControllers) {
			if (device != null) {
				result = internalGetController(device.getName());
				if (result == null) {
					result = createController(device);
				}
			}
		}
		return result;
	}

	/**
	 * #getControllerの下請け
	 * @param name
	 * @return nameに一致するものがなければnull
	 */
	private IDeviceController internalGetController(final String name) {
		IDeviceController result = null;
		if (mControllers.containsKey(name)) {
			final WeakReference<IDeviceController> weak_controller = mControllers.get(name);
			result = weak_controller != null ? weak_controller.get() : null;
			if (result == null) {
				mControllers.remove(name);
			}
		}
		return result;
	}

	/**
	 * 指定した名前のARDiscoveryDeviceServiceインスタンスを取得する
	 * @param name
	 * @return nameに一致するものがなければnull
	 */
	public ARDiscoveryDeviceService getDevice(final String name) {
		ARDiscoveryDeviceService result = null;
		if (!TextUtils.isEmpty(name)) {
			synchronized (mDevices) {
				for (final ARDiscoveryDeviceService device : mDevices) {
					if (name.equals(device.getName())) {
						result = device;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 指定したindexのARDiscoveryDeviceServiceインスタンスを取得する
	 * @param index
	 * @return indexが範囲外ならnull
	 */
	public ARDiscoveryDeviceService getDevice(final int index) {
		ARDiscoveryDeviceService device = null;
		synchronized (mDevices) {
			if ((index >= 0) && (index < mDevices.size())) {
				device = mDevices.get(index);
			}
		}
		return device;
	}

	/**
	 * IDeviceControllerを生成する (FIXME JumpingSumoとハイドロフォイルは未対応)
	 * @param device
	 * @return
	 */
	public IDeviceController createController(final ARDiscoveryDeviceService device) {
		IDeviceController result = null;
		if (device != null) {
			switch (ARDiscoveryService.getProductFromProductID(device.getProductID())) {
			case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				result = new FlightControllerBebop(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
				result = new FlightControllerBebop2(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
				result = new SkyController(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_NSNETSERVICE:
			case ARDISCOVERY_PRODUCT_JS:		// FIXME JumpingSumoは未対応
			case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
				result = new FlightControllerMiniDrone(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
				result = new FlightControllerCargoDrone(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:
				result = new FlightControllerMambo(getActivity(), device);
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:
				result = new FlightControllerWingX(getActivity(), device);
				break;
			}
			if (result != null) {
				synchronized (mControllers) {
					mControllers.put(device.getName(), new WeakReference<IDeviceController>(result));
				}
			}
		} else {
			Log.w(TAG, "deviceがnullやんか");
		}
		return result;
	}

	/**
	 * デバイスとの接続を開始する
	 * @param controller
	 * @param listener
	 */
	public void startController(final IDeviceController controller, final StartControllerListener listener) {
		if (controller != null) {
			final Activity activity = getActivity();
			if (activity != null) {
				showProgress(R.string.connecting, true, new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(final DialogInterface dialog) {
						Log.w(TAG, "startController:ユーザーキャンセル");
						controller.cancelStart();
					}
				});
			}

			queueEvent(new Runnable() {
				@Override
				public void run() {
					boolean failed = true;
					synchronized (mControllers) {
						if (mControllers.containsKey(controller.getName())) {
							try {
								failed = controller.start();
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						} else {
							Log.w(TAG, "controller is already removed:" + controller);
						}
					}
					hideProgress();

					if (listener != null) {
						try {
							listener.onResult(controller, !failed);
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					}
				}
			});
		}
	}

	/**
	 * 指定したIDeviceControllerをHashMapから取り除く, IDeviceController#releaseを呼んで開放する
	 * @param controller
	 */
	public void releaseController(final IDeviceController controller) {
		if (controller != null) {
			synchronized (mControllers) {
				mControllers.remove(controller.getName());
			}
			final Activity activity = getActivity();
			if ((activity != null) && !activity.isFinishing()) {
				showProgress(R.string.disconnecting, false, null);
			}
			if (controller instanceof IVideoStreamController) {
				queueEvent(new Runnable() {
					@Override
					public void run() {
						((IVideoStreamController)controller).enableVideoStreaming(false);
					}
				});
			}
			queueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						controller.stop();
						controller.release();
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
					hideProgress();
				}
			});
		}
	}

	/**
	 * 指定したARDiscoveryDeviceServiceをListから取り除く
	 * @param device
	 */
	public void releaseDevice(final ARDiscoveryDeviceService device) {
		synchronized (mDevices) {
			mDevices.remove(device);
		}
	}

	/**
	 * 全てのARDiscoveryDeviceServiceをListから取り除き
	 * 全てのIDeviceControllerをHashMapから取り除く
	 */
	public void releaseAll() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mControllers) {
					for (final WeakReference<IDeviceController> weak_controller: mControllers.values()) {
						final IDeviceController controller = weak_controller != null ? weak_controller.get() : null;
						if (controller != null) {
							controller.release();
						}
					}
					mControllers.clear();
				}
			}
		}).start();
		synchronized (mDevices) {
			mDevices.clear();
		}
	}

	/***
	 * ARSDKのデバイス探索サービスに接続する
	 * #startDiscoveryの実際の処理
	 */
	private void bindServices() {
		if (discoveryServiceBinder == null) {
			final Context app = getActivity().getApplicationContext();
			final Intent intent = new Intent(app, ARDiscoveryService.class);
			app.bindService(intent, ardiscoveryServiceConnection, Context.BIND_AUTO_CREATE);
		} else if (!ardiscoveryServiceBound) {
			ardiscoveryService = ((ARDiscoveryService.LocalBinder) discoveryServiceBinder).getService();
			ardiscoveryServiceBound = true;

			ardiscoveryService.start();
		}
	}

	/**
	 * ARSDKのデバイス探索サービスから切断する
	 * #stopDiscoveryの実際の処理
	 */
	private void unbindServices() {
		if (ardiscoveryServiceBound) {
			ardiscoveryServiceBound = false;
			final Activity activity = getActivity();
			if (activity == null) return;
			final Context app_context = activity.getApplicationContext();
			queueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						ardiscoveryService.stop();
						app_context.unbindService(ardiscoveryServiceConnection);
						discoveryServiceBinder = null;
						ardiscoveryService = null;
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			});
		}
	}

	/** デバイス探索サービスとの接続/切断イベント処理用コールバック */
	private final ServiceConnection ardiscoveryServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(final ComponentName name, final IBinder service) {
			discoveryServiceBinder = service;
			ardiscoveryService = ((ARDiscoveryService.LocalBinder) service).getService();
			ardiscoveryServiceBound = true;
			ardiscoveryService.start();
		}
		@Override
		public void onServiceDisconnected(final ComponentName name) {
			discoveryServiceBinder = null;
			ardiscoveryService = null;
			ardiscoveryServiceBound = false;
		}
	};

	/** ネットワークへの接続状態監視用BroadcastReceiverのインスタンス */
	private NetworkChangedReceiver mNetworkChangedReceiver;
	/** ネットワークへの接続状態監視用BroadcastReceiverを登録する  */
	private void registerReceivers() {
		if (!mRegistered) {
			mRegistered = true;
			final LocalBroadcastManager localBroadcastMgr
				= LocalBroadcastManager.getInstance(getActivity().getApplicationContext());
			localBroadcastMgr.registerReceiver(mDevicesListUpdatedReceiver,
				new IntentFilter(ARDiscoveryService.kARDiscoveryServiceNotificationServicesDevicesListUpdated));
			if (mNetworkChangedReceiver == null) {
				mNetworkChangedReceiver = NetworkChangedReceiver.registerGlobal(getActivity(), mOnNetworkChangedListener);
			}
		}
	}

	/** ネットワークへの接続状態監視用BroadcastReceiverを登録解除する */
	private void unregisterReceivers() {
		mRegistered = false;
		final LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(
			getActivity().getApplicationContext());
		localBroadcastMgr.unregisterReceiver(mDevicesListUpdatedReceiver);
		if (mNetworkChangedReceiver != null) {
			NetworkChangedReceiver.unregisterGlobal(getActivity(), mNetworkChangedReceiver);
			mNetworkChangedReceiver = null;
		}
	}

	/** ネットワークへの接続状態が変化した時のコールバックリスナー */
	private final NetworkChangedReceiver.OnNetworkChangedListener mOnNetworkChangedListener
		= new NetworkChangedReceiver.OnNetworkChangedListener() {
		@Override
		public void onNetworkChanged(final int isConnectedOrConnecting, final int isConnected, final int activeNetworkFlag) {
			if (mRegistered && (ardiscoveryService != null)) {
				if (NetworkChangedReceiver.isWifiNetworkReachable()) {
					ardiscoveryService.startWifiDiscovering();
				} else {
					ardiscoveryService.stopWifiDiscovering();
				}
			}
		}
	};

	/** 検出したデバイス一覧が変更された時のデバイス検出サービスからのコールバックリスナー */
	private final ARDiscoveryServicesDevicesListUpdatedReceiverDelegate
		mDeviceListUpdatedReceiverDelegate
			= new ARDiscoveryServicesDevicesListUpdatedReceiverDelegate() {
		@Override
		public void onServicesDevicesListUpdated() {
			if (ardiscoveryService != null) {
				final List<ARDiscoveryDeviceService> list = ardiscoveryService.getDeviceServicesArray();

				if (list != null) {
					synchronized (mDevices) {
						mDevices.clear();
						mDevices.addAll(list);
					}
					callOnServicesDevicesListUpdated();
				}
			}
		}
	};

	private final BroadcastReceiver mDevicesListUpdatedReceiver
		= new ARDiscoveryServicesDevicesListUpdatedReceiver(mDeviceListUpdatedReceiverDelegate);

	/** ManagerFragmentに登録されている検出デバイス一覧変更コールバックを呼び出すためのヘルパーメソッド */
	private void callOnServicesDevicesListUpdated() {
		synchronized (mDevices) {
			for (final ManagerCallback cb: mCallbacks) {
				try {
					cb.onServicesDevicesListUpdated(mDevices);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	/**
	 * UIスレッド上で指定したRunnableを実行するためのヘルパーメソッド
	 * @param task
	 */
	protected void runOnUiThread(final Runnable task) {
		if (task != null) {
			try {
				if (mUIThreadId != Thread.currentThread().getId()) {
					mUIHandler.post(task);
				} else {
					task.run();
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * ワーカースレッド上で指定したRunnableを実行するためのヘルパーメソッド
	 * @param task
	 */
	protected void queueEvent(final Runnable task) {
		synchronized (mSync) {
			if (mAsyncHandler != null) {
				mAsyncHandler.post(task);
			} else {
				throw new RuntimeException("mAsyncHandler already released");
			}
		}
	}

	/**
	 * ワーカースレッド上で指定したRunnableを実行するためのヘルパーメソッド
	 * @param task
	 * @param delay 遅延時間[ミリ秒]
	 */
	protected void queueEvent(final Runnable task, final long delay) {
		synchronized (mSync) {
			if (mAsyncHandler != null) {
				if (delay > 0) {
					mAsyncHandler.postDelayed(task, delay);
				} else {
					mAsyncHandler.post(task);
				}
			} else {
				throw new RuntimeException("mAsyncHandler already released");
			}
		}
	}

	/**
	 * 指定したIDeviceControllerを接続解除する
	 * @param controller
	 */
	protected void stopController(final IDeviceController controller) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final boolean show_progress = controller.isStarted();
				final ProgressDialog dialog;
				if (show_progress) {
					dialog = new ProgressDialog(getActivity());
					dialog.setTitle(R.string.disconnecting);
					dialog.setIndeterminate(true);
					dialog.show();
				} else {
					dialog = null;
				}

				queueEvent(new Runnable() {
					@Override
					public void run() {
						try {
							controller.stop();
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
						if (dialog != null) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									dialog.dismiss();
								}
							});
						}
					}
				});
			}
		});
	}

	/**　IDeviceControllerからのコールバックリスナー　*/
	private final DeviceConnectionListener mConnectionListener
		= new DeviceConnectionListener() {
		@Override
		public void onConnect(final IDeviceController controller) {
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			releaseController(controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
		}

		@Override
		public void onUpdateWiFiSignal(final IDeviceController controller, final int rssi) {
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
		}
	};

	/** 接続・切断時のプログレスダイアログ */
	private ProgressDialog mProgress;

	/**
	 * プログレスダイアログを表示する
	 * @param title_resID
	 * @param cancelable
	 * @param cancel_listener
	 */
	private synchronized void showProgress(final int title_resID, final boolean cancelable,
		final DialogInterface.OnCancelListener cancel_listener) {

		final Activity activity = getActivity();

		if ((activity != null) && !activity.isFinishing()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mProgress = ProgressDialog.show(activity, getString(title_resID), null, true, cancelable, cancel_listener);
				}
			});
		}
	}

	/**
	 * プログレスダイアログを非表示にして破棄する
	 */
	private synchronized void hideProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgress != null) {
					mProgress.dismiss();
					mProgress = null;
				}
			}
		});
	}

}
