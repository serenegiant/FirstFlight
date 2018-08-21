package jp.co.rediscovery.arflight.controllers;
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

import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_USBACCESSORY_CLAWCONTROL_ACTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_USBACCESSORY_GUNCONTROL_ACTION_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFeatureMiniDrone;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import jp.co.rediscovery.arflight.DroneSettings;
import jp.co.rediscovery.arflight.DroneStatus;
import jp.co.rediscovery.arflight.attribute.AttributeDevice;
import jp.co.rediscovery.arflight.attribute.AttributeIMU;
import jp.co.rediscovery.arflight.attribute.AttributeMotor;
import jp.co.rediscovery.arflight.attribute.AttributeUSBAccessory;

public class FlightControllerMiniDrone extends FlightController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private final String TAG = "FlightControllerMiniDrone:" + getClass().getSimpleName();

	public final AttributeIMU mIMU = new AttributeIMU();
	public final AttributeUSBAccessory mUSBAcc = new AttributeUSBAccessory();

	public FlightControllerMiniDrone(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
		if (DEBUG) Log.v (TAG, "コンストラクタ:");
		init();
	}

	/** 共通の初期化処理 */
	private void init() {
		if (DEBUG) Log.v (TAG, "init:");
		mInfo = new AttributeDevice();
		mSettings = new DroneSettings();
		mStatus = new DroneStatus(4);
	}

	@Override
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args,
		final ARControllerDictionary elementDictionary) {

		super.onCommandReceived(deviceController, commandKey, args, elementDictionary);

		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE:	// (100, "Key used to define the feature <code>MiniDrone</code>"),
			break;
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLATTRIMCHANGED:	// (101, "Key used to define the command <code>FlatTrimChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		{	// フラットトリム変更を受信した時
			callOnFlatTrimChanged();
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED:	// (102, "Key used to define the command <code>FlyingStateChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		{	// 飛行状態が変化した時の処理
			final ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state
				= ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(
					(Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));
			((DroneStatus)mStatus).setFlyingState(state.getValue() * 0x100);
			callOnFlyingStateChangedUpdate(getState());
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED:	// (103, "Key used to define the command <code>AlertStateChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		{	// デバイスからの異常通知時
			final int state = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED_STATE);
			mStatus.setAlarm(state);
			callOnAlarmStateChangedUpdate(getAlarm());
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_AUTOTAKEOFFMODECHANGED:	// (104, "Key used to define the command <code>AutoTakeOffModeChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		{	// 自動離陸モード設定を受信した時
			final boolean state = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_AUTOTAKEOFFMODECHANGED_STATE) != 0;
			mSettings.setAutoTakeOffMode(state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGED:	// (105, "Key used to define the command <code>PictureStateChanged</code> of class <code>MediaRecordState</code> in project <code>MiniDrone</code>"),
		{	// 写真撮影状態を受信した時
			final int state = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGED_STATE);
			final int mass_storage_id = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGED_MASS_STORAGE_ID);
			if (DEBUG) Log.v(TAG, "onMinidroneMediaRecordStatePictureStateChangedUpdate:state=" + state + ",mass_storage_id=" + mass_storage_id);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2:	// (106, "Key used to define the command <code>PictureStateChangedV2</code> of class <code>MediaRecordState</code> in project <code>MiniDrone</code>"),
		{	// 写真撮影状態を受信した時
			final ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM state
				= ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM.getFromValue(
					(Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE)
			);
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM error
				= ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM.getFromValue(
					(Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR)
			);
			int _state;
			switch (state) {
			case ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_READY:		// 撮影可能
				_state = DroneStatus.MEDIA_READY;
				break;
			case ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_BUSY:			// 撮影中
				_state = DroneStatus.MEDIA_BUSY;
				break;
			case ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_NOTAVAILABLE:	// 撮影不可
			default:
				_state = DroneStatus.MEDIA_UNAVAILABLE;
				break;
			}
			callOnStillCaptureStateChanged(_state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED:	// (107, "Key used to define the command <code>PictureEventChanged</code> of class <code>MediaRecordEvent</code> in project <code>MiniDrone</code>"),
		{	// 写真撮影イベントを受信した時
			final ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM event
				= ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM.getFromValue(
					(Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT)
			);
			final ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error
				= ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.getFromValue(
					(Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR)
			);
			int _state;
			switch (event) {
			case ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_TAKEN:			// 撮影成功
				_state = DroneStatus.MEDIA_SUCCESS;
				break;
			case ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_FAILED:			// 撮影失敗
			default:
				_state = DroneStatus.MEDIA_ERROR;
				break;
			}
			callOnStillCaptureStateChanged(_state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED:	// (108, "Key used to define the command <code>MaxAltitudeChanged</code> of class <code>PilotingSettingsState</code> in project <code>MiniDrone</code>"),
		{	// 最大高度設定を受信した時
			final float current = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED_MAX)).floatValue();
			mSettings.setMaxAltitude(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXTILTCHANGED:	// (109, "Key used to define the command <code>MaxTiltChanged</code> of class <code>PilotingSettingsState</code> in project <code>MiniDrone</code>"),
		{	// 最大傾斜設定を受信した時
			final float current = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXTILTCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXTILTCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXTILTCHANGED_MAX)).floatValue();
			mSettings.setMaxTilt(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED:	// (110, "Key used to define the command <code>MaxVerticalSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		{	// 上昇/降下速度設定を受信した時
			final float current = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED_MAX)).floatValue();
			mSettings.setMaxVerticalSpeed(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED:	// (111, "Key used to define the command <code>MaxRotationSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		{	// 最大回転速度設定を受信した時
			final float current = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED_MAX)).floatValue();
			mSettings.setMaxRotationSpeed(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_WHEELSCHANGED:	// (112, "Key used to define the command <code>WheelsChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		{	// ホイールの有無設定を受信した時
			final boolean present = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_WHEELSCHANGED_PRESENT) != 0;
			mSettings.setHasGuard(present);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXHORIZONTALSPEEDCHANGED:	// (113, "Key used to define the command <code>MaxHorizontalSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		{	// 最大飛行速度設定を受信した時
			final float current = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXHORIZONTALSPEEDCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXHORIZONTALSPEEDCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXHORIZONTALSPEEDCHANGED_MAX)).floatValue();
			mSettings.setMaxHorizontalSpeed(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTMOTORSVERSIONCHANGED:	// (114, "Key used to define the command <code>ProductMotorsVersionChanged</code> of class <code>SettingsState</code> in project <code>MiniDrone</code>"),
		{	// モーターバージョンを受信した時のコールバックリスナー
			final int motor = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTMOTORSVERSIONCHANGED_MOTOR);
			final String type = (String)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTMOTORSVERSIONCHANGED_TYPE);
			final String software = (String)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTMOTORSVERSIONCHANGED_SOFTWARE);
			final String hardware = (String)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTMOTORSVERSIONCHANGED_HARDWARE);
			if (DEBUG) Log.v(TAG, "onMiniDroneSettingsStateProductMotorsVersionChangedUpdate:");
			try {
				final int ix = (motor - 1) % getMotorNums();
				final AttributeMotor _motor = ((DroneStatus)mStatus).getMotor(ix);
				if (_motor != null) {
					_motor.set(type, software, hardware);
				} else {
					Log.w(TAG, "モーターNo.が予期したのと違う:" + motor);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTINERTIALVERSIONCHANGED:	// (115, "Key used to define the command <code>ProductInertialVersionChanged</code> of class <code>SettingsState</code> in project <code>MiniDrone</code>"),
		{	// フライトコントローラのバージョンを受信した時
			final String software = (String)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTINERTIALVERSIONCHANGED_SOFTWARE);
			final String hardware = (String)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTINERTIALVERSIONCHANGED_HARDWARE);

			if (DEBUG) Log.v(TAG, "onMiniDroneSettingsStateProductInertialVersionChangedUpdate:");
			mIMU.set(software, hardware);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_CUTOUTMODECHANGED:	// (116, "Key used to define the command <code>CutOutModeChanged</code> of class <code>SettingsState</code> in project <code>MiniDrone</code>"),
		{	// カットオフモード設定を受信した時
			if (DEBUG) Log.v(TAG, "onMiniDroneSettingsStateCutOutModeChangedUpdate:");
			final boolean enable = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_CUTOUTMODECHANGED_ENABLE) != 0;
			mSettings.setCutOffMode(enable);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_FLOODCONTROLSTATE_FLOODCONTROLCHANGED:	// (117, "Key used to define the command <code>FloodControlChanged</code> of class <code>FloodControlState</code> in project <code>MiniDrone</code>"),
		{	// FloodControl設定を受信した時のコールバックリスナー
			// 操縦コマンド(PCMD)を連続して送る時の最短間隔・・・これ以下の間隔で送った時に無視するってことかな?
			/** Delay (in ms) between two PCMD */
			final int delay = (Integer)args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_FLOODCONTROLSTATE_FLOODCONTROLCHANGED_DELAY);
			if (DEBUG) Log.v(TAG, "onMiniDroneFloodControlStateFloodControlChangedUpdate:delay=" + delay);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE:
		{	// ライトの状態を受信した時
			// ライトがない機種だとnullで来るみたい?
			Object id = null, state = null, intensity = null;
			if (args != null) {
				id = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE_ID);
				state = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE_STATE);
				intensity = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE_INTENSITY);
			}
			if (((id == null) || (state == null) || (intensity == null)) && (elementDictionary != null) && (elementDictionary.values() != null)) {
				for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
					id = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE_ID);
					state = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE_STATE);
					intensity = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_LIGHTSTATE_INTENSITY);
				}
			}
			if ((id != null) && (state != null) && (intensity != null)) {
				try {
					mUSBAcc.lightState((Integer)id, (Integer)state, (Integer)intensity);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
			if (DEBUG) Log.v(TAG, "onMiniDroneUSBAccessoryStateLightStateUpdate:id=" + id + ",state=" + state + ",intensity=" + intensity);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_CLAWSTATE:
		{	// Mamboのアームの状態を受信した時
			// アームが付いてないとnullで来るみたい?
			Object id = null, state = null;
			if (args != null) {
				id = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_CLAWSTATE_ID);
				state = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_CLAWSTATE_STATE);
			}
			if (((id == null) || (state == null)) && (elementDictionary != null) && (elementDictionary.values() != null)) {
				for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
					id = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_CLAWSTATE_ID);
					state = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_CLAWSTATE_STATE);
				}
			}
			if ((id != null) && (state != null)) {
				try {
					mUSBAcc.clawState((Integer)id, (Integer)state);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
			if (DEBUG) Log.v(TAG, "onMiniDroneUSBAccessoryStateClawStateUpdate:id=" + id + ",state=" + state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_GUNSTATE:
		{	// Mamboのキャノンの状態を受信した時
			// キャノンが付いてないとnullで来るみたい?
			Object id = null, state = null;
			if (args != null) {
				id = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_GUNSTATE_ID);
				state = args.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_GUNSTATE_STATE);
			}
			if (((id == null) || (state == null)) && (elementDictionary != null) && (elementDictionary.values() != null)) {
				for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
					id = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_GUNSTATE_ID);
					state = element.get(ARFeatureMiniDrone.ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_USBACCESSORYSTATE_GUNSTATE_STATE);
				}
			}
			if ((id != null) && (state != null)) {
				try {
					mUSBAcc.gunState((Integer)id, (Integer)state);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
			if (DEBUG) Log.v(TAG, "onMiniDroneUSBAccessoryStateGunStateUpdate:id=" + id + ",state=" + state);
			break;
		}
		default:
			break;
		}
	}

	private int seqNum;
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			try {
				/** setPilotingPCMD/sendPilotingPCMD
				 * @param _flag Boolean flag to activate roll/pitch movement
				 * @param _roll Roll consign for the drone [-100;100]
				 * @param _pitch Pitch consign for the drone [-100;100]
				 * @param _yaw Yaw consign for the drone [-100;100]
				 * @param _gaz Gaz consign for the drone [-100;100]
				 * @param _timestampAndSeqNum Command timestamp in milliseconds (low 24 bits) + command sequence number [0;255] (high 8 bits).
				 */
				final int timestampAndSeqNum = (int)(System.currentTimeMillis() & 0xffffff) + (seqNum ++) << 24;
				result = mARDeviceController.getFeatureMiniDrone().setPilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, timestampAndSeqNum);
				if (result == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
					result = mARDeviceController.getFeatureMiniDrone().sendPilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, timestampAndSeqNum);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendPCMD failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestTakeoff() {
		if (DEBUG) Log.v (TAG, "requestTakeoff:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingTakeOff();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestTakeoff failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestLanding() {
		if (DEBUG) Log.v (TAG, "requestLanding:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingLanding();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestLanding failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestEmergencyStop() {
		if (DEBUG) Log.v (TAG, "requestEmergencyStop:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingEmergency();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestEmergencyStop failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestFlatTrim() {
		if (DEBUG) Log.v (TAG, "requestFlatTrim:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingFlatTrim();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestFlatTrim failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean startCalibration(final boolean start) {
		return false;
	}

	@Override
	public boolean setMaxAltitude(final float altitude) {
		if (DEBUG) Log.v (TAG, "setMaxAltitude:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingSettingsMaxAltitude(altitude);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxAltitude failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxTilt(final float tilt) {
		if (DEBUG) Log.v (TAG, "setMaxTilt:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingSettingsMaxTilt(tilt);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxTilt failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxVerticalSpeed(final float speed) {
		if (DEBUG) Log.v (TAG, "setMaxVerticalSpeed:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendSpeedSettingsMaxVerticalSpeed(speed);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxVerticalSpeed failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	public boolean setMaxHorizontalSpeed(final float speed) {
		if (DEBUG) Log.v (TAG, "setMaxHorizontalSpeed:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendSpeedSettingsMaxHorizontalSpeed(speed);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxHorizontalSpeed failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxRotationSpeed(final float speed) {
		if (DEBUG) Log.v (TAG, "setMaxRotationSpeed:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendSpeedSettingsMaxRotationSpeed(speed);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxRotationSpeed failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean canGetAttitude() {
		return false;
	}

	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		if (DEBUG) Log.v (TAG, "sendCutOutMode:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendSettingsCutOutMode(enabled ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendCutOutMode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		if (DEBUG) Log.v (TAG, "sendAutoTakeOffMode:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendPilotingAutoTakeOffMode(enable ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendAutoTakeOffMode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setHasGuard(final boolean has_guard) {
		if (DEBUG) Log.v (TAG, "setHasGuard:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendSpeedSettingsWheels(has_guard ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setHasGuard failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAnimationsFlip(final int direction) {
		if (DEBUG) Log.v (TAG, "requestAnimationsFlip:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			final ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM dir
				= ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.getFromValue(direction);
			result = mARDeviceController.getFeatureMiniDrone().sendAnimationsFlip(dir);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAnimationsFlip failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAnimationsCap(final int degree) {
		if (DEBUG) Log.v (TAG, "requestAnimationsCap:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendAnimationsCap((short)degree);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAnimationsCap failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAnimationsCap(final int degree, final Object sync) {
		return requestAnimationsCap(degree);
	}

	@Override
	public boolean requestTakePicture(final int mass_storage_id) {
		if (DEBUG) Log.v (TAG, "requestTakePicture:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendMediaRecordPicture((byte)mass_storage_id);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestTakePicture failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestTakePicture() {
		if (DEBUG) Log.v (TAG, "requestTakePicture:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureMiniDrone().sendMediaRecordPictureV2();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestTakePicture failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setHeadlightsIntensity(final int left, final int right) {
		if (DEBUG) Log.v (TAG, "setHeadlightsIntensity:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureCommon().sendHeadlightsIntensity((byte)left, (byte)right);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setHeadlightsIntensity failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	public boolean startAnimation(final int animation) {
		if (DEBUG) Log.v (TAG, "startAnimation:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			final ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM anim
				= ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM.getFromValue(animation);
			result = mARDeviceController.getFeatureCommon().sendAnimationsStartAnimation(anim);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#startAnimation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	public boolean stopAnimation(final int animation) {
		if (DEBUG) Log.v (TAG, "stopAnimation:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			final ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM anim
				= ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM.getFromValue(animation);
			result = mARDeviceController.getFeatureCommon().sendAnimationsStopAnimation(anim);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#stopAnimation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	public boolean stopAllAnimation() {
		if (DEBUG) Log.v (TAG, "stopAllAnimation:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureCommon().sendAnimationsStopAllAnimations();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#stopAllAnimation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

//public ARCONTROLLER_ERROR_ENUM sendGPSControllerLatitudeForRun (double _latitude)
//public ARCONTROLLER_ERROR_ENUM sendGPSControllerLongitudeForRun (double _longitude)
//public ARCONTROLLER_ERROR_ENUM sendConfigurationControllerType (String _type)
//public ARCONTROLLER_ERROR_ENUM sendConfigurationControllerName (String _name)

//================================================================================
	/**
	 * アームがついているかどうか
	 * 接続されていなければ常にfalse
	 * @return
	 */
	protected boolean hasClaw() {
		return isConnected() && mUSBAcc.hasClaw();
	}

	protected boolean isClawClosed() {
		return isClawClosed(mUSBAcc.clawId());
	}

	protected boolean isClawClosed(final int id) {
		return hasClaw() && (mUSBAcc.clawState(id) != 0);
	}

	/**
	 * Mamboの指定したidのアームを開閉する
	 * 状態が同じ時は何もしない
	 * @param id 多分0だけ
	 * @param open trueなら開く, falseなら閉じる
	 * @return
	 */
	protected boolean requestClawOpenClose(final int id, final boolean open) {
		if (DEBUG) Log.v(TAG, "requestClawOpenClose:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (hasClaw()) {
			if (mUSBAcc.clawState(id) != (open ? 0 : 1)) {
				final ARCOMMANDS_MINIDRONE_USBACCESSORY_CLAWCONTROL_ACTION_ENUM
					action = ARCOMMANDS_MINIDRONE_USBACCESSORY_CLAWCONTROL_ACTION_ENUM.getFromValue(open ? 0 : 1);
				result = mARDeviceController.getFeatureMiniDrone().sendUsbAccessoryClawControl((byte)id, action);
			} else {
				result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
			}
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * Mamboのidが一番最初のアームを開く
	 * 既に開いている時は何もしない
	 * @return
	 */
	protected boolean requestClawOpen() {
		return requestClawOpenClose(mUSBAcc.clawId(), true);
	}

	/**
	 * Mamboの指定したidのアームを開く
	 * 既に開いている時は何もしない
	 * @param id 多分0だけ
	 * @return
	 */
	protected boolean requestClawOpen(final int id) {
		return requestClawOpenClose(id, true);
	}

	/**
	 * Mamboのidが一番最初のアームを閉じる
	 * 既に閉じている時は何もしない
	 * @return
	 */
	protected boolean requestClawClose() {
		return requestClawOpenClose(mUSBAcc.clawId(), false);
	}

	/**
	 * Mamboの指定したidのアームを閉じる
	 * 既に閉じている時は何もしない
	 * @param id 多分0だけ
	 * @return
	 */
	protected boolean requestClawClose(final int id) {
		return requestClawOpenClose(id, false);
	}

	protected void toggleClaw() {
		if (hasClaw()) {
			final int id = mUSBAcc.clawId();
			requestClawOpenClose(id, isClawClosed(id));
		}
	}

	protected void toggleClaw(final int id) {
		if (hasClaw()) {
			requestClawOpenClose(id, isClawClosed(id));
		}
	}

//================================================================================
	/**
	 * キャノンがついているかどうか
	 * 接続されていなければ常にfalse
	 * @return
	 */
	protected boolean hasGun() {
		return isConnected() && mUSBAcc.hasGun();
	}

	/**
	 * Mamboの指定したidのキャノンを発射する
	 * 状態が同じ時は何もしない
	 * @param id 多分0だけ
	 * @return
	 */
	protected boolean requestFireGun(final int id) {
		if (DEBUG) Log.v(TAG, "requestClawOpenClose:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (hasGun()) {
			result = mARDeviceController.getFeatureMiniDrone().sendUsbAccessoryGunControl((byte)id,
				ARCOMMANDS_MINIDRONE_USBACCESSORY_GUNCONTROL_ACTION_ENUM.ARCOMMANDS_MINIDRONE_USBACCESSORY_GUNCONTROL_ACTION_FIRE);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * Mamboのidが一番最初のキャノンを発射する
	 * 既に開いている時は何もしない
	 * @return
	 */
	protected boolean requestFireGun() {
		return requestFireGun(mUSBAcc.gunId());
	}
}
