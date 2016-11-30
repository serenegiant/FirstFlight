package jp.co.rediscovery.firstflight;
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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.IWiFiController;
import jp.co.rediscovery.arflight.attribute.AttributeFloat;
import com.serenegiant.widget.RelativeRadioGroup;

import static jp.co.rediscovery.firstflight.AppConst.*;

/** 設定画面 */
public class ConfigFragment extends BaseFlightControllerFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigFragment.class.getSimpleName();

	public static ConfigFragment newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info) {
		final ConfigFragment fragment = new ConfigFragment();
		fragment.setDevice(device, info);
		return fragment;
	}

	private ARDISCOVERY_PRODUCT_ENUM mProduct;
	private SharedPreferences mPref;

	private TextView mMaxAltitudeLabel;
	private TextView mMaxTiltLabel;
	private TextView mMaxVerticalSpeedLabel;
	private TextView mMaxRotationSpeedLabel;

	private TextView mAutopilotScaleXLabel;
	private TextView mAutopilotScaleYLabel;
	private TextView mAutopilotScaleZLabel;
	private TextView mAutopilotScaleRLabel;
	private TextView mAutopilotMaxControlValueLabel;

	private TextView mGamepadScaleXLabel;
	private TextView mGamepadScaleYLabel;
	private TextView mGamepadScaleZLabel;
	private TextView mGamepadScaleRLabel;
	private TextView mGamepadMaxControlValueLabel;

	private String mMaxAltitudeFormat;
	private String mMaxTiltFormat;
	private String mMaxVerticalSpeedFormat;
	private String mMaxRotationSpeedFormat;

	private String mGamepadScaleXFormat;
	private String mGamepadScaleYFormat;
	private String mGamepadScaleZFormat;
	private String mGamepadScaleRFormat;
	private String mGamepadSensitivityFormat;

	private String mAutopilotScaleXFormat;
	private String mAutopilotScaleYFormat;
	private String mAutopilotScaleZFormat;
	private String mAutopilotScaleRFormat;
	private String mAutopilotMaxControlValueFormat;

	public ConfigFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
		mMaxAltitudeFormat = getString(R.string.config_max_altitude);
		mMaxTiltFormat = getString(R.string.config_max_tilt);
		mMaxVerticalSpeedFormat = getString(R.string.config_max_vertical_speed);
		mMaxRotationSpeedFormat = getString(R.string.config_max_rotating_speed);

		mGamepadScaleXFormat = getString(R.string.config_scale_x);
		mGamepadScaleYFormat = getString(R.string.config_scale_y);
		mGamepadScaleZFormat = getString(R.string.config_scale_z);
		mGamepadScaleRFormat = getString(R.string.config_scale_r);
		mGamepadSensitivityFormat = getString(R.string.config_control_max_gamepad);

		mAutopilotScaleXFormat = getString(R.string.config_scale_x);
		mAutopilotScaleYFormat = getString(R.string.config_scale_y);
		mAutopilotScaleZFormat = getString(R.string.config_scale_z);
		mAutopilotScaleRFormat = getString(R.string.config_scale_r);
		mAutopilotMaxControlValueFormat = getString(R.string.config_control_max);

		mPref = activity.getPreferences(0);
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		mPref = null;
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		onBeforeCreateView();
		mProduct = getProduct();
		final View rootView = inflater.inflate(R.layout.fragment_config, container, false);
		final ConfigPagerAdapter adapter = new ConfigPagerAdapter(this, inflater, getConfigs(mProduct));
		final ViewPager pager = (ViewPager)rootView.findViewById(R.id.pager);
		pager.setAdapter(adapter);
		return rootView;
	}

	@Override
	protected boolean canReleaseController() {
		return false;
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
	protected void updateFlyingState(final IDeviceController controller, final int state) {

	}

	private AttributeFloat mMaxAltitude;
	private AttributeFloat mMaxTilt;
	private AttributeFloat mMaxVerticalSpeed;
	private AttributeFloat mMaxRotationSpeed;

	/**
	 * 飛行設定画面の準備
	 * @param root
	 */
	private void initConfigFlight(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigFlight:");
		// 最大高度設定
		mMaxAltitudeLabel = (TextView)root.findViewById(R.id.max_altitude_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.max_altitude_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxAltitude = mFlightController.getMaxAltitude();
		try {
			seekbar.setProgress((int) ((mMaxAltitude.current() - mMaxAltitude.min()) / (mMaxAltitude.max() - mMaxAltitude.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxAltitude(mMaxAltitude.current());
		// 最大傾斜設定
		// bebopは5-30度。最大時速約50km/hrからすると13.9m/s/30度≒0.46[m/s/度]
		mMaxTiltLabel = (TextView)root.findViewById(R.id.max_tilt_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_tilt_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxTilt = mFlightController.getMaxTilt();
		try {
			seekbar.setProgress((int) ((mMaxTilt.current() - mMaxTilt.min()) / (mMaxTilt.max() - mMaxTilt.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxTilt(mMaxTilt.current());
		// 最大上昇/降下速度設定
		mMaxVerticalSpeedLabel = (TextView)root.findViewById(R.id.max_vertical_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_vertical_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxVerticalSpeed = mFlightController.getMaxVerticalSpeed();
		try {
			seekbar.setProgress((int) ((mMaxVerticalSpeed.current() - mMaxVerticalSpeed.min()) / (mMaxVerticalSpeed.max() - mMaxVerticalSpeed.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxVerticalSpeed(mMaxVerticalSpeed.current());
		// 最大回転速度
		mMaxRotationSpeedLabel = (TextView)root.findViewById(R.id.max_rotation_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_rotation_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxRotationSpeed = mFlightController.getMaxRotationSpeed();
		try {
			seekbar.setProgress((int) ((mMaxRotationSpeed.current() - mMaxRotationSpeed.min()) / (mMaxRotationSpeed.max() - mMaxRotationSpeed.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxRotationSpeed(mMaxRotationSpeed.current());
	}

	/**
	 * ミニドローン設定画面の準備
	 * @param root
	 */
	private void initConfigMinidrone1(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigMinidrone1:");
		// 自動カットアウトモード
		CheckBox checkbox = (CheckBox)root.findViewById(R.id.cutout_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mFlightController.isCutoffMode());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		// 車輪
		checkbox = (CheckBox)root.findViewById(R.id.wheel_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mFlightController.hasGuard());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		// 自動離陸モード
		checkbox = (CheckBox)root.findViewById(R.id.auto_takeoff_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mFlightController.isAutoTakeOffModeEnabled());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * Bebop設定画面の準備
	 * @param root
	 */
	private void initConfigBebop(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigMinidrone1:");
		// 自動カットアウトモード
		CheckBox checkbox = (CheckBox)root.findViewById(R.id.cutout_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mFlightController.isCutoffMode());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		// 車輪
		checkbox = (CheckBox)root.findViewById(R.id.wheel_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mFlightController.hasGuard());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		// 自動離陸モード
		checkbox = (CheckBox)root.findViewById(R.id.auto_takeoff_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mFlightController.isAutoTakeOffModeEnabled());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * 操作設定画面の準備
	 * @param root
	 */
	private void initConfigOperation(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigOperation:");
		final RadioGroup group = (RadioGroup)root.findViewById(R.id.operation_radiogroup);
		switch (mPref.getInt(APP_CONFIG_KEY_OPERATION_TYPE, 0)) {
		case 1:		// モード2
			group.check(R.id.operation_mode2_radiobutton);
			break;
		case 0:	// モード1
			group.check(R.id.operation_mode1_radiobutton);
		default:
			break;
		}
		group.setOnCheckedChangeListener(mOnRadioButtonCheckedChangeListener);

	}

	private float mGamepadMaxControlValue;
	private float mGamepadScaleX;
	private float mGamepadScaleY;
	private float mGamepadScaleZ;
	private float mGamepadScaleR;
	/**
	 * ゲームパッド設定画面の準備
	 * @param root
	 */
	private void initConfigGamepad(final View root) {
		// 最大制御値設定
//		final CheckBox checkbox = (CheckBox)root.findViewById(R.id.usb_driver_checkbox);
//		checkbox.setChecked(mPref.getBoolean(KEY_GAMEPAD_USE_DRIVER, false));
//		checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		mGamepadMaxControlValueLabel = (TextView)root.findViewById(R.id.gamepad_sensitivity_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.gamepad_sensitivity_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadMaxControlValue = mPref.getFloat(APP_CONFIG_KEY_GAMEPAD_SENSITIVITY, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadMaxControlValue + APP_SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(APP_SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadMaxControlValue(mGamepadMaxControlValue);
		// スケールX設定
		mGamepadScaleXLabel = (TextView)root.findViewById(R.id.gamepad_scale_x_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_x);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleX = mPref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_X, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleX * APP_SCALE_FACTOR + APP_SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(APP_SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleX(mGamepadScaleX);
		// スケールY設定
		mGamepadScaleYLabel = (TextView)root.findViewById(R.id.gamepad_scale_y_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_y);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleY = mPref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_Y, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleY * APP_SCALE_FACTOR + APP_SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(APP_SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleY(mGamepadScaleY);
		// スケールZ設定
		mGamepadScaleZLabel = (TextView)root.findViewById(R.id.gamepad_scale_z_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_z);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleZ = mPref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_Z, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleZ * APP_SCALE_FACTOR + APP_SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(APP_SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleZ(mGamepadScaleZ);
		// スケールR設定
		mGamepadScaleRLabel = (TextView)root.findViewById(R.id.gamepad_scale_r_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_r);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleR = mPref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_R, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleR * APP_SCALE_FACTOR + APP_SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(APP_SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleR(mGamepadScaleR);
	}

	/**
	 * ネットワーク設定画面の準備 FIXME 未実装
	 * @param root
	 */
	private void initConfigNetwork(final View root) {
		final IWiFiController wifi = (mController instanceof IWiFiController) ? (IWiFiController)mController : null;
		final RadioGroup group = (RadioGroup)root.findViewById(R.id.network_wifi_mode_radiogroup);
		if (wifi != null) {
			final boolean outdoor = wifi.isOutdoor();
			group.check(outdoor ? R.id.network_outdoor_radiobutton : R.id.network_indoor_radiobutton);
			group.setOnCheckedChangeListener(mOnRadioButtonCheckedChangeListener);
		} else {
			group.check(R.id.network_indoor_radiobutton);
			group.setEnabled(false);
		}
	}

	/**
	 * ドローン情報画面の準備
	 * @param root
	 */
	private void initConfigInfo(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigInfo:");
		TextView tv = (TextView)root.findViewById(R.id.app_version_textview);
		tv.setText(BuildConfig.VERSION_NAME);
		tv = (TextView)root.findViewById(R.id.product_name_textview);
		tv.setText(mController.getName());
		tv = (TextView)root.findViewById(R.id.software_version_textview);
		tv.setText(mController.getSoftwareVersion());
		tv = (TextView)root.findViewById(R.id.hardware_version_textview);
		tv.setText(mController.getHardwareVersion());
	}

	/**
	 * 最大高度設定値表示を更新
	 * @param max_altitude
	 */
	private void updateMaxAltitude(final float max_altitude) {
		if (mMaxAltitudeLabel != null) {
			mMaxAltitudeLabel.setText(String.format(mMaxAltitudeFormat, max_altitude));
		}
	}

	/**
	 * 最大傾斜設定表示を更新
	 * @param max_tilt
	 */
	private void updateMaxTilt(final float max_tilt) {
		if (mMaxTiltLabel != null) {
			mMaxTiltLabel.setText(String.format(mMaxTiltFormat, max_tilt));
		}
	}

	/**
	 * 最大上昇/降下速度設定表示を更新
	 * @param max_vertical_speed
	 */
	private void updateMaxVerticalSpeed(final float max_vertical_speed) {
		if (mMaxVerticalSpeedLabel != null) {
			mMaxVerticalSpeedLabel.setText(String.format(mMaxVerticalSpeedFormat, max_vertical_speed));
		}
	}

	/**
	 * 最大回転速度設定表示を更新
	 * @param max_rotation_speed
	 */
	private void updateMaxRotationSpeed(final float max_rotation_speed) {
		if (mMaxRotationSpeedLabel != null) {
			mMaxRotationSpeedLabel.setText(String.format(mMaxRotationSpeedFormat, max_rotation_speed));
		}
	}

	/**
	 * ゲームパッド:最大制御設定値表示を更新
	 * @param sensitivity
	 */
	private void updateGamepadMaxControlValue(final float sensitivity) {
		if (mGamepadMaxControlValueLabel != null) {
			mGamepadMaxControlValueLabel.setText(String.format(mGamepadSensitivityFormat, sensitivity));
		}
	}

	/**
	 * 自動操縦:スケールZ設定表示を更新
	 * @param scale_x
	 */
	private void updateGamepadScaleX(final float scale_x) {
		if (mGamepadScaleXLabel != null) {
			mGamepadScaleXLabel.setText(String.format(mGamepadScaleXFormat, scale_x));
		}
	}

	/**
	 * ゲームパッド:スケールY設定表示を更新
	 * @param scale_y
	 */
	private void updateGamepadScaleY(final float scale_y) {
		if (mGamepadScaleYLabel != null) {
			mGamepadScaleYLabel.setText(String.format(mGamepadScaleYFormat, scale_y));
		}
	}

	/**
	 * ゲームパッド:スケールZ設定表示を更新
	 * @param scale_z
	 */
	private void updateGamepadScaleZ(final float scale_z) {
		if (mGamepadScaleZLabel != null) {
			mGamepadScaleZLabel.setText(String.format(mGamepadScaleZFormat, scale_z));
		}
	}

	/**
	 * ゲームパッド:スケールR設定表示を更新
	 * @param scale_r
	 */
	private void updateGamepadScaleR(final float scale_r) {
		if (mGamepadScaleRLabel != null) {
			mGamepadScaleRLabel.setText(String.format(mGamepadScaleRFormat, scale_r));
		}
	}

	/**
	 * 自動操縦:最大制御設定値表示を更新
	 * @param max_control_value
	 */
	private void updateAutopilotMaxControlValue(final float max_control_value) {
		if (mAutopilotMaxControlValueLabel != null) {
			mAutopilotMaxControlValueLabel.setText(String.format(mAutopilotMaxControlValueFormat, max_control_value));
		}
	}

	/**
	 * 自動操縦:スケールZ設定表示を更新
	 * @param scale_x
	 */
	private void updateAutopilotScaleX(final float scale_x) {
		if (mAutopilotScaleXLabel != null) {
			mAutopilotScaleXLabel.setText(String.format(mAutopilotScaleXFormat, scale_x));
		}
	}

	/**
	 * 自動操縦:スケールY設定表示を更新
	 * @param scale_y
	 */
	private void updateAutopilotScaleY(final float scale_y) {
		if (mAutopilotScaleYLabel != null) {
			mAutopilotScaleYLabel.setText(String.format(mAutopilotScaleYFormat, scale_y));
		}
	}

	/**
	 * 自動操縦:スケールZ設定表示を更新
	 * @param scale_z
	 */
	private void updateAutopilotScaleZ(final float scale_z) {
		if (mAutopilotScaleZLabel != null) {
			mAutopilotScaleZLabel.setText(String.format(mAutopilotScaleZFormat, scale_z));
		}
	}

	/**
	 * 自動操縦:スケールR設定表示を更新
	 * @param scale_r
	 */
	private void updateAutopilotScaleR(final float scale_r) {
		if (mAutopilotScaleRLabel != null) {
			mAutopilotScaleRLabel.setText(String.format(mAutopilotScaleRFormat, scale_r));
		}
	}

	/**
	 * シークバーのイベント
	 */
	private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		/**
		 * タッチ処理の開始
		 * @param seekBar
		 */
		@Override
		public void onStartTrackingTouch(final SeekBar seekBar) {
		}

		/**
		 * シークバーの値が変更された時の処理
		 * @param seekBar
		 * @param progress
		 * @param fromUser
		 */
		@Override
		public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
			if (fromUser) {
				// ユーザーのタッチ処理でシークバーの値が変更された時
				switch (seekBar.getId()) {
				case R.id.max_altitude_seekbar:
					final float altitude = (int) (progress / 100f * (mMaxAltitude.max() - mMaxAltitude.min())) / 10f + mMaxAltitude.min();
					updateMaxAltitude(altitude);
					break;
				case R.id.max_tilt_seekbar:
					final float tilt = (int) (progress / 100f * (mMaxTilt.max() - mMaxTilt.min())) / 10f + mMaxTilt.min();
					updateMaxTilt(tilt);
					break;
				case R.id.max_vertical_speed_seekbar:
					final float vertical = (int) (progress / 100f * (mMaxVerticalSpeed.max() - mMaxVerticalSpeed.min())) / 10f + mMaxVerticalSpeed.min();
					updateMaxVerticalSpeed(vertical);
					break;
				case R.id.max_rotation_speed_seekbar:
					final float rotation = (int) (progress / 1000f * (mMaxRotationSpeed.max() - mMaxRotationSpeed.min())) + mMaxRotationSpeed.min();
					updateMaxRotationSpeed(rotation);
					break;
				case R.id.gamepad_sensitivity_seekbar:	// -5.00〜+5.00
					final float sensitivity = (progress - APP_SCALE_OFFSET) / 100f;
					updateGamepadMaxControlValue(sensitivity);
					break;
				case R.id.gamepad_scale_seekbar_x:
					final float gamepad_scale_x = (progress - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
					updateGamepadScaleX(gamepad_scale_x);
					break;
				case R.id.gamepad_scale_seekbar_y:
					final float gamepad_scale_y = (progress - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
					updateGamepadScaleY(gamepad_scale_y);
					break;
				case R.id.gamepad_scale_seekbar_z:
					final float gamepad_scale_z = (progress - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
					updateGamepadScaleZ(gamepad_scale_z);
					break;
				case R.id.gamepad_scale_seekbar_r:
					final float gamepad_scale_r = (progress - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
					updateGamepadScaleR(gamepad_scale_r);
					break;
				}
			}
		}

		/**
		 * シークバーのタッチ処理が終了した時の処理
		 * ここで設定を適用する
		 * @param seekBar
		 */
		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {
			if (mController == null) {
				Log.w(TAG, "deviceControllerがnull");
				return;
			}
			switch (seekBar.getId()) {
			case R.id.max_altitude_seekbar:
				final float altitude = (int)(seekBar.getProgress() / 100f * (mMaxAltitude.max() - mMaxAltitude.min())) / 10f + mMaxAltitude.min();
				if (altitude != mMaxAltitude.current()) {
					mFlightController.setMaxAltitude(altitude);
				}
				break;
			case R.id.max_tilt_seekbar:
				final float tilt = (int)(seekBar.getProgress() / 100f * (mMaxTilt.max() - mMaxTilt.min())) / 10f + mMaxTilt.min();
				if (tilt != mMaxTilt.current()) {
					mFlightController.setMaxTilt(tilt);
				}
				break;
			case R.id.max_vertical_speed_seekbar:
				final float vertical = (int)(seekBar.getProgress() / 100f * (mMaxVerticalSpeed.max() - mMaxVerticalSpeed.min())) / 10f + mMaxVerticalSpeed.min();
				if (vertical != mMaxVerticalSpeed.current()) {
					mFlightController.setMaxVerticalSpeed(vertical);
				}
				break;
			case R.id.max_rotation_speed_seekbar:
				final float rotation = (int)(seekBar.getProgress() / 1000f * (mMaxRotationSpeed.max() - mMaxRotationSpeed.min())) + mMaxRotationSpeed.min();
				if (rotation != mMaxRotationSpeed.current()) {
					mFlightController.setMaxRotationSpeed(rotation);
				}
				break;
			// ゲームパッド
			case R.id.gamepad_sensitivity_seekbar:
				final float sensitivity = (seekBar.getProgress() - APP_SCALE_OFFSET) / 100f;
				if (sensitivity != mGamepadMaxControlValue) {
					mGamepadMaxControlValue = sensitivity;
					mPref.edit().putFloat(APP_CONFIG_KEY_GAMEPAD_SENSITIVITY, sensitivity).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_x:
				final float gamepad_scale_x = (seekBar.getProgress() - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
				if (gamepad_scale_x != mGamepadScaleX) {
					mGamepadScaleX = gamepad_scale_x;
					mPref.edit().putFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_X, gamepad_scale_x).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_y:
				final float gamepad_scale_y = (seekBar.getProgress() - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
				if (gamepad_scale_y != mGamepadScaleY) {
					mGamepadScaleY = gamepad_scale_y;
					mPref.edit().putFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_Y, gamepad_scale_y).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_z:
				final float gamepad_scale_z = (seekBar.getProgress() - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
				if (gamepad_scale_z != mGamepadScaleZ) {
					mGamepadScaleZ = gamepad_scale_z;
					mPref.edit().putFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_Z, gamepad_scale_z).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_r:
				final float gamepad_scale_r = (seekBar.getProgress() - APP_SCALE_OFFSET) / APP_SCALE_FACTOR;
				if (gamepad_scale_r != mGamepadScaleR) {
					mGamepadScaleR = gamepad_scale_r;
					mPref.edit().putFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_R, gamepad_scale_r).apply();
				}
				break;
			}
		}
	};

	/**
	 * チェックボックスの選択状態が変更された時の処理
	 */
	private final CompoundButton.OnCheckedChangeListener
		mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.cutout_checkbox:
				if (mFlightController.isCutoffMode() != isChecked) {
					mFlightController.sendCutOutMode(isChecked);
				}
				break;
			case R.id.wheel_checkbox:
				if (mFlightController.hasGuard() != isChecked) {
					mFlightController.setHasGuard(isChecked);
				}
				break;
			case R.id.auto_takeoff_checkbox:
				if (mFlightController.isAutoTakeOffModeEnabled() != isChecked) {
					mFlightController.sendAutoTakeOffMode(isChecked);
				}
				break;
			}
		}
	};

	/**ラジオグループで選択が変更された時の処理 */
	private final RadioGroup.OnCheckedChangeListener mOnRadioButtonCheckedChangeListener
		= new RadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(final RadioGroup group, final int checkedId) {
			ConfigFragment.this.onCheckedChanged(checkedId);
		}
	};

	/**ラジオグループで選択が変更された時の処理 */
	private final RelativeRadioGroup.OnCheckedChangeListener mOnRelativeRadioButtonCheckedChangeListener
		= new RelativeRadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(final RelativeRadioGroup group, final int checkedId) {
			ConfigFragment.this.onCheckedChanged(checkedId);
		}
	};

	/**ラジオグループで選択が変更された時の処理 */
	private void onCheckedChanged(final int checkedId) {
		switch (checkedId) {
		case R.id.operation_mode1_radiobutton:
			mPref.edit().putInt(APP_CONFIG_KEY_OPERATION_TYPE, 0).apply();
			break;
		case R.id.operation_mode2_radiobutton:
			mPref.edit().putInt(APP_CONFIG_KEY_OPERATION_TYPE, 1).apply();
			break;
		case R.id.network_outdoor_radiobutton:
			if ((mController instanceof IWiFiController)
				&& !((IWiFiController)mController).isOutdoor()) {
				((IWiFiController)mController).sendSettingsOutdoor(true);
			}
			break;
		case R.id.network_indoor_radiobutton:
			if ((mController instanceof IWiFiController)
				&& ((IWiFiController)mController).isOutdoor()) {
				((IWiFiController)mController).sendSettingsOutdoor(false);
			}
			break;
		}
	}

	private static PagerAdapterConfig[] PAGER_CONFIG_MINIDRONE;
	private static PagerAdapterConfig[] PAGER_CONFIG_BEBOP;
	private static PagerAdapterConfig[] PAGER_CONFIG_BEBOP2;
	static {
		// Minidrone(RollingSpider用)
		PAGER_CONFIG_MINIDRONE = new PagerAdapterConfig[8];
		PAGER_CONFIG_MINIDRONE[0] = new PagerAdapterConfig(R.string.config_title_flight, R.layout.config_flight, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigFlight(view);
			}
		});
		PAGER_CONFIG_MINIDRONE[1] = new PagerAdapterConfig(R.string.config_title_drone, R.layout.config_minidrone, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigMinidrone1(view);
			}
		});
		PAGER_CONFIG_MINIDRONE[2] = new PagerAdapterConfig(R.string.config_title_operation, R.layout.config_operation, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigOperation(view);
			}
		});
		PAGER_CONFIG_MINIDRONE[3] = new PagerAdapterConfig(R.string.config_title_gamepad, R.layout.config_gamepad, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigGamepad(view);
			}
		});
		PAGER_CONFIG_MINIDRONE[4] = new PagerAdapterConfig(R.string.config_title_info, R.layout.config_info, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigInfo(view);
			}
		});
// ここからbebop用
		PAGER_CONFIG_BEBOP = new PagerAdapterConfig[6];
		PAGER_CONFIG_BEBOP[0] = PAGER_CONFIG_MINIDRONE[0];
		PAGER_CONFIG_BEBOP[1] = new PagerAdapterConfig(R.string.config_title_drone, R.layout.config_bebop, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigBebop(view);
			}
		});
		PAGER_CONFIG_BEBOP[2] = PAGER_CONFIG_MINIDRONE[2];
		PAGER_CONFIG_BEBOP[3] = PAGER_CONFIG_MINIDRONE[3];
		PAGER_CONFIG_BEBOP[4] = new PagerAdapterConfig(R.string.config_title_network, R.layout.config_network, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigFragment)parent).initConfigNetwork(view);
			}
		});
		PAGER_CONFIG_BEBOP[5] = PAGER_CONFIG_MINIDRONE[4];
// ここからbebop2用
		PAGER_CONFIG_BEBOP2 = new PagerAdapterConfig[6];
		PAGER_CONFIG_BEBOP2[0] = PAGER_CONFIG_BEBOP[0];
		PAGER_CONFIG_BEBOP2[1] = PAGER_CONFIG_BEBOP[1];
		PAGER_CONFIG_BEBOP2[2] = PAGER_CONFIG_BEBOP[2];
		PAGER_CONFIG_BEBOP2[3] = PAGER_CONFIG_BEBOP[3];
		PAGER_CONFIG_BEBOP2[4] = PAGER_CONFIG_BEBOP[4];
		PAGER_CONFIG_BEBOP2[5] = PAGER_CONFIG_BEBOP[5];
	}

	private static PagerAdapterConfig[] getConfigs(final ARDISCOVERY_PRODUCT_ENUM product) {
		PagerAdapterConfig[] result;
		switch(product) {
		case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
			result = PAGER_CONFIG_BEBOP;
			break;
		case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
			result = PAGER_CONFIG_BEBOP2;
			break;
		case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
			result = PAGER_CONFIG_MINIDRONE;
			break;
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
			// FIXME SkyController用の設定画面を追加する?
			result = null;
			break;
		case ARDISCOVERY_PRODUCT_NSNETSERVICE:
		case ARDISCOVERY_PRODUCT_JS:		// FIXME JumpingSumoは未対応
		case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
		case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
		default:
			result = null;
		}
		return result;
	}

}
