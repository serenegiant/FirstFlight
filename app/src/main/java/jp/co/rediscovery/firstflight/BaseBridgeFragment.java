package jp.co.rediscovery.firstflight;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ListView;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import jp.co.rediscovery.arflight.ARDeviceInfoAdapter;
import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.ISkyController;
import jp.co.rediscovery.arflight.ManagerFragment;
import jp.co.rediscovery.arflight.SkyControllerListener;

import static jp.co.rediscovery.arflight.ARFlightConst.*;

/**
 * スカイコントローラーに接続してスカイコントローラーが
 * 検出している機体の一覧取得＆選択を行うためのFragment
 */
public abstract class BaseBridgeFragment extends BaseControllerFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = BaseBridgeFragment.class.getSimpleName();

	protected ListView mDeviceListView;
	protected ImageButton mDownloadBtn, mPilotBtn, mGalleyBrn;
	protected boolean mIsConnectToDevice;
	protected boolean mNeedRequestDeviceList;

	public BaseBridgeFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
		final IntentFilter filter = new IntentFilter(ARFLIGHT_ACTION_DEVICE_LIST_CHANGED);
		mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		onBeforeCreateView();
		final View rootView = inflater.inflate(R.layout.fragment_bridge, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume:");
		mIsConnectToDevice = false;
		if (mController instanceof ISkyController) {
			mController.addListener(mSkyControllerListener);
		}
		if (DEBUG) Log.v(TAG, "onResume:isAdded=" + isAdded() + ",isDetached=" + isDetached()
			+ ",isHidden=" + isHidden() + ",isInLayout=" + isInLayout()
			+ ",isRemoving=" + isRemoving() + ",isResumed=" + isResumed()
			+ ",isVisible=" + isVisible() + ",mIsConnectToDevice=" + mIsConnectToDevice);
		startDeviceController();
		updateButtons(false);
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause:");

		updateButtons(false);
		if (mController != null) {
			final ISkyController bridge = (ISkyController)mController;
			bridge.setCoPilotingSource(0);
			mController.removeListener(mSkyControllerListener);
			if (!mIsConnectToDevice) {
				releaseDeviceController(true);
			} else {
				mNeedRequestDeviceList = true;
			}
			mController = null;
		}
		if (DEBUG) Log.v(TAG, "onPause:isAdded=" + isAdded() + ",isDetached=" + isDetached()
			+ ",isHidden=" + isHidden() + ",isInLayout=" + isInLayout()
			+ ",isRemoving=" + isRemoving() + ",isResumed=" + isResumed()
			+ ",isVisible=" + isVisible() + ",mIsConnectToDevice=" + mIsConnectToDevice);
		super.onPause();
	}

	@Override
	protected void onBeforeCreateView() {
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {

		final ARDeviceInfoAdapter adapter = new ARDeviceInfoAdapter(getActivity(), R.layout.list_item_deviceservice);

		mDeviceListView = (ListView)rootView.findViewById(R.id.list);
		final View empty_view = rootView.findViewById(R.id.empty_view);
		mDeviceListView.setEmptyView(empty_view);
		mDeviceListView.setAdapter(adapter);
		mDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		mDownloadBtn = (ImageButton)rootView.findViewById(R.id.download_button);
		mDownloadBtn.setOnClickListener(mOnClickListener);
		mDownloadBtn.setOnLongClickListener(mOnLongClickListener);

		mPilotBtn = (ImageButton)rootView.findViewById(R.id.pilot_button);
		mPilotBtn.setOnClickListener(mOnClickListener);
		mPilotBtn.setOnLongClickListener(mOnLongClickListener);

		mGalleyBrn = (ImageButton)rootView.findViewById(R.id.gallery_button);
		mGalleyBrn.setOnClickListener(mOnClickListener);
		mGalleyBrn.setOnLongClickListener(mOnLongClickListener);

	}

	protected synchronized boolean startDeviceController() {
		final boolean already_connected = super.startDeviceController();
		if (already_connected) {
			onSkyControllerConnect(mController);
		}
		return already_connected;
	}

	protected void releaseDeviceController(final boolean disconnected) {
		mIsConnectToDevice = mNeedRequestDeviceList = false;
		super.releaseDeviceController(disconnected);
	}

	@Override
	protected void updateBattery(final IDeviceController controller, final int percent) {

	}

	@Override
	protected void updateWiFiSignal(final IDeviceController controller, final int rssi) {

	}

	@Override
	protected void updateAlarmState(final IDeviceController controller, final int alert_state) {

	}

	@Override
	protected void onConnect(final IDeviceController controller) {

	}

	@Override
	protected void onSkyControllerConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onSkyControllerConnect:controller=" + controller);
		if (mNeedRequestDeviceList) {
			mNeedRequestDeviceList = false;
			final ISkyController ctrl = (ISkyController)controller;
			ctrl.requestDeviceList();
		}
	}

	/**
	 * スカイコントローラーのキャリブレーションの状態が変化した時
	 * @param controller
	 * @param need_calibration
	 */
	protected void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
		if (DEBUG) Log.v(TAG, "onSkyControllerCalibrationRequiredChanged:controller=" + controller + ",need_calibration=" + need_calibration);
	}

	/**
	 * スカイコントローラーのキャリブレーションを開始した
	 */
	protected void onSkyControllerStartCalibration(final IDeviceController controller) {
	}

	/**
	 * スカイコントローラーのキャリブレーションが終了した
	 */
	protected void onSkyControllerStopCalibration(final IDeviceController controller) {
	}

	/**
	 * スカイコントローラーのキャリブレーション中の軸が変更された
	 * @param controller
	 * @param axis
	 */
	protected void updateSkyControllerCalibrationAxis(final IDeviceController controller, final int axis) {
		if (DEBUG) Log.v(TAG, "updateSkyControllerCalibrationAxis:controller=" + controller + ",axis=" + axis);
	}

	private final SkyControllerListener mSkyControllerListener = new SkyControllerListener() {
		@Override
		public void onSkyControllerConnect(final IDeviceController controller) {
			BaseBridgeFragment.this.onSkyControllerConnect(controller);
		}

		@Override
		public void onSkyControllerDisconnect(final IDeviceController controller) {
			BaseBridgeFragment.this.onSkyControllerDisconnect(controller);
		}

		@Override
		public void onSkyControllerUpdateBattery(final IDeviceController controller, final int percent) {
			BaseBridgeFragment.this.updateSkyControllerBattery(controller, percent);
		}

		@Override
		public void onSkyControllerAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			BaseBridgeFragment.this.updateSkyControllerAlarmState(controller, alarm_state);
		}

		@Override
		public void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
			BaseBridgeFragment.this.onSkyControllerCalibrationRequiredChanged(controller, need_calibration);
		}

		@Override
		public void onSkyControllerCalibrationStartStop(final IDeviceController controller, final boolean isStart) {
			if (isStart) {
				onSkyControllerStartCalibration(controller);
			} else {
				onSkyControllerStopCalibration(controller);
			}
		}

		@Override
		public void onSkyControllerCalibrationAxisChanged(final IDeviceController controller, final int axis) {
			updateSkyControllerCalibrationAxis(controller, axis);
		}

		@Override
		public void onConnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onConnect:controller=" + controller);
			final ISkyController bridge = (ISkyController)controller;
			queueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						final DeviceInfo info = bridge.getCurrentDevice();
						final int numDevices = bridge.getDeviceNum();
						if (bridge.isConnected() && (info != null) && (numDevices == 1)) {
							if (DEBUG) Log.v(TAG, "既に1機だけ検出&接続されていたら操縦画面へ");
							// XXX 検出している機体が1機でそれに接続している時は操縦画面へ
							// XXX ただし今はトレースモードにも移行できるようにしているので自動では遷移しない
//							replace(PilotFragment.newInstance(controller.getDeviceService(), info));
						}
					} catch (final Exception e) {
					}
				}
			} , 1000);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onDisconnect:controller=" + controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onUpdateBattery:controller=" + controller + ", percent=" + percent);
		}

		@Override
		public void onUpdateWiFiSignal(final IDeviceController controller, final int rssi) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onUpdateWiFiSignal:controller=" + controller + ", rssi=" + rssi);
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onAlarmStateChangedUpdate:controller=" + controller + ", alarm_state=" + alarm_state);
		}
	};

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
				final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
				adapter.clear();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
		mDownloadBtn.setVisibility(visibility);
		mPilotBtn.setVisibility(visibility);
	}

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
			if (ARFLIGHT_ACTION_DEVICE_LIST_CHANGED.equals(action)) {
				final DeviceInfo[] info_array
					= intent.hasExtra(ARFLIGHT_EXTRA_DEVICE_LIST)
					? (DeviceInfo[])intent.getParcelableArrayExtra(ARFLIGHT_EXTRA_DEVICE_LIST)
					: null;
				updateDeviceList(info_array);
			}
		}
	};

	/** 検出した機体をリストに登録する, Bridge接続はBebop/Bebop2のみ対応 */
	private void updateDeviceList(final DeviceInfo[] info_array) {
		if (DEBUG) Log.v(TAG, "updateDeviceList:" + info_array);
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter) mDeviceListView.getAdapter();
		adapter.clear();
		final int n = info_array != null ? info_array.length : 0;
		for (int i = 0; i < n; i++) {
			final DeviceInfo info = info_array[i];
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
			if (DEBUG) Log.v(TAG, "updateDeviceList:product=" + product);
			switch (product) {
			case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
			case ARDISCOVERY_PRODUCT_BEBOP_2:	// bebop2
				adapter.add(info);
				break;
			}
		}
		adapter.notifyDataSetChanged();
		mDeviceListView.setItemChecked(0, true);	// 先頭を選択
		updateButtons(n > 0);
	}

	private void clearCheck(final ViewGroup parent) {
		final int n = parent.getChildCount();
		for (int i = 0; i < n; i++) {
			final View v = parent.getChildAt(i);
			if (v instanceof Checkable) {
				((Checkable) v).setChecked(false);
			}
		}
	}

	/** アイコンにタッチした時の処理 */
	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			BaseBridgeFragment.this.onClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	/** アイコンを長押しした時の処理 */
	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			if (mPilotBtn.getVisibility() != View.VISIBLE) return false;
			if (DEBUG) Log.v(TAG, "onLongClick:");
			mVibrator.vibrate(50);
			return BaseBridgeFragment.this.onLongClick(view);
		}
	};

	protected abstract void onClick(final View view, final int position);
	protected abstract boolean onLongClick(final View view);

	/** アイコンにタッチした時の処理の下請け, 選択している機体に対応するFragmentを生成する */
	protected Fragment getFragment(final int position, final boolean isPiloting) {
		if (DEBUG) Log.v(TAG, "getFragment:");
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
		final DeviceInfo info = adapter.getItem(position);
		Fragment fragment = null;
		// 製品名を取得
		final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
		switch (product) {
		case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
		case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
			mIsConnectToDevice = mNeedRequestDeviceList = true;
			fragment = isPiloting ? PilotFragment.newInstance(mController.getDeviceService(), info)
				: MediaFragment.newInstance(mController.getDeviceService(), info);
			break;
		}
		return fragment;
	}

}
