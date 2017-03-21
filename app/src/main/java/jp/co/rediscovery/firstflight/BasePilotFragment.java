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
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.gamepad.Joystick;

import jp.co.rediscovery.arflight.DroneStatus;
import jp.co.rediscovery.arflight.ICameraController;
import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.IFlightController;
import jp.co.rediscovery.arflight.IVideoStreamController;
import jp.co.rediscovery.arflight.VideoStream;
import jp.co.rediscovery.widget.VideoView;

import com.serenegiant.math.Vector;

import static jp.co.rediscovery.firstflight.AppConst.*;

public abstract class BasePilotFragment extends BaseFlightControllerFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private final String TAG = "BasePilotFragment:" + getClass().getSimpleName();

	protected VideoView mVideoView;
	protected VideoStream mVideoStream;
	protected boolean mVideoRecording;

	public BasePilotFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onDetach() {
//		if (DEBUG) Log.v(TAG, "onDetach:");
		mJoystick = null;
		super.onDetach();
	}

	// 操縦用
	protected int mOperationType;			// 操縦スティックのモード
	protected double mMaxControlValue = APP_CONFIG_DEFAULT_MAX_CONTROL_VALUE;
	protected double mScaleX, mScaleY, mScaleZ, mScaleR;
	protected float mGamepadSensitivity = 1.0f;
	protected float mGamepadScaleX, mGamepadScaleY, mGamepadScaleZ, mGamepadScaleR;

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		onBeforeCreateView();
		final SharedPreferences pref = getActivity().getPreferences(0);
		mOperationType = pref.getInt(APP_CONFIG_KEY_OPERATION_TYPE, 0);
		mMaxControlValue = pref.getFloat(APP_CONFIG_KEY_MAX_CONTROL_VALUE, APP_CONFIG_DEFAULT_MAX_CONTROL_VALUE);
		mScaleX = pref.getFloat(APP_CONFIG_KEY_SCALE_X, APP_CONFIG_DEFAULT_SCALE_X);
		mScaleY = pref.getFloat(APP_CONFIG_KEY_SCALE_Y, APP_CONFIG_DEFAULT_SCALE_Y);
		mScaleZ = pref.getFloat(APP_CONFIG_KEY_SCALE_Z, APP_CONFIG_DEFAULT_SCALE_Z);
		mScaleR = pref.getFloat(APP_CONFIG_KEY_SCALE_R, APP_CONFIG_DEFAULT_SCALE_R);
		mGamepadSensitivity = pref.getFloat(APP_CONFIG_KEY_GAMEPAD_SENSITIVITY, 1.0f);
		mGamepadScaleX = pref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_X, 1.0f);
		mGamepadScaleY = pref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_Y, 1.0f);
		mGamepadScaleZ = pref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_Z, 1.0f);
		mGamepadScaleR = pref.getFloat(APP_CONFIG_KEY_GAMEPAD_SCALE_R, 1.0f);
		int layout_id;
		switch (mOperationType) {
		case 1:
			layout_id = R.layout.fragment_pilot_mode2;
			break;
//		case 0:
		default:
			layout_id = R.layout.fragment_pilot_mode1;
			break;
		}
		return internalCreateView(inflater, container, savedInstanceState, layout_id);

	}

	@Override
	public void onResume() {
		super.onResume();
//		if (DEBUG) Log.v(TAG, "onResume:");
		final Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			mJoystick = ((MainActivity)activity).getJoystick();
		}
		startDeviceController();
		if (mVideoView != null) {
			if ((mFlightController != null) && isStarted()) {
				mVideoView.hasGuard(mFlightController.hasGuard());
			}
		}
	}

	@Override
	public void onPause() {
//		if (DEBUG) Log.v(TAG, "onPause:");
		mJoystick = null;
		if (mController instanceof ICameraController) {
			((ICameraController)mController).sendVideoRecording(false);
		}
		stopVideoStreaming();
		removeEvent(mGamePadTask);
		removeEvent(mUpdateStatusTask);
		super.onPause();
	}

	protected abstract View internalCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState, final int layout_id);

	@Override
	protected void startVideoStreaming() {
//		if (DEBUG) Log.v(TAG, "startVideoStreaming:");
		if (mController instanceof IVideoStreamController) {
			if (mVideoStream == null) {
				mVideoStream = new VideoStream();
			}
			((IVideoStreamController)mController).setVideoStream(mVideoStream);
			queueEvent(new Runnable() {
				@Override
				public void run() {
					if (mVideoStream == null) {
						Log.w(TAG, "mVideoStreamが破棄されてる");
						return;
					}
					final SurfaceTexture surface = mVideoView.getSurfaceTexture();
					if (surface != null) {
						try {
							if (mSurfaceId == 0) {
								final Surface _surface = new Surface(surface);
								mSurfaceId = _surface.hashCode();
								mVideoStream.addSurface(mSurfaceId, _surface);
								mVideoView.setEnableVideo(true);
							}
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					} else {
						queueEvent(this, 500);
					}
				}
			}, 100);
		} else if (DEBUG) Log.d(TAG, "IVideoStreamControllerじゃない");
		super.startVideoStreaming();
	}

	@Override
	protected void stopVideoStreaming() {
//		if (DEBUG) Log.v(TAG, "stopVideoStreaming:");
		super.stopVideoStreaming();
		mVideoView.setEnableVideo(false);
		mSurfaceId = 0;
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).setVideoStream(null);
		}
		if (mVideoStream != null) {
			mVideoStream.release();
			mVideoStream = null;
		}
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onConnect:");
		super.onConnect(controller);

		mVideoRecording = false;
		if (mVideoView != null) {
			mVideoView.hasGuard((controller instanceof IFlightController) && ((IFlightController)controller).hasGuard());
		}
		startGamePadTask();
		startUpdateStatusTask();
		updateButtons();
		// キャリブレーションが必要ならCalibrationFragmentへ遷移させる
		if ((controller instanceof IFlightController) && ((IFlightController)controller).needCalibration()) {
			replace(CalibrationFragment.newInstance(getDevice()));
		}
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "#onDisconnect");
		mVideoRecording = false;
		stopGamePadTask();
		removeEvent(mUpdateStatusTask);
		requestPopBackStack(POP_BACK_STACK_DELAY);
		super.onDisconnect(controller);
	}

	/**
	 * 飛行ステータスが変化した時のコールバック
	 * @param state
	 */
	@Override
	protected void updateFlyingState(final IDeviceController controller, final int state) {
		updateButtons();
	}

	@Override
	protected void updateWiFiSignal(final IDeviceController controller, final int rssi) {

	}

	/**
	 * 異常ステータスが変化した時のコールバック
	 * @param alert_state
	 */
	@Override
	protected void updateAlarmState(final IDeviceController controller, final int alert_state) {
		runOnUiThread(mUpdateAlarmMessageTask);
		updateButtons();
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 */
	@Override
	protected void updateBattery(final IDeviceController controller, final int percent) {
		runOnUiThread(mUpdateBatteryTask);
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバック
	 * @param picture_state DroneStatus#MEDIA_XXX
	 */
	@Override
	protected void updatePictureCaptureState(final IDeviceController controller, final int picture_state) {
		switch (picture_state) {
		case DroneStatus.MEDIA_UNAVAILABLE:
		case DroneStatus.MEDIA_READY:
		case DroneStatus.MEDIA_BUSY:
			break;
		case DroneStatus.MEDIA_SUCCESS:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), R.string.capture_success, Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case DroneStatus.MEDIA_ERROR:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), R.string.capture_error, Toast.LENGTH_SHORT).show();
				}
			});
			break;
		}
		updateButtons();
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバック
	 * @param video_state DroneStatus#MEDIA_XXX
	 */
	@Override
	protected void updateVideoRecordingState(final IDeviceController controller, final int video_state) {
		switch (video_state) {
		case DroneStatus.MEDIA_UNAVAILABLE:
		case DroneStatus.MEDIA_READY:
		case DroneStatus.MEDIA_BUSY:
			break;
		case DroneStatus.MEDIA_SUCCESS:
			if (mVideoRecording) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), R.string.video_success, Toast.LENGTH_SHORT).show();
					}
				});
			}
			mVideoRecording = false;
			break;
		case DroneStatus.MEDIA_ERROR:
			if (mVideoRecording) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), R.string.video_error, Toast.LENGTH_SHORT).show();
					}
				});
			}
			mVideoRecording = false;
			break;
		}
		updateButtons();
	}

	/**
	 * 離陸指示
	 */
	protected void takeOff() {
		// 離陸指示
		if (mFlightController != null) {
			mFlightController.requestTakeoff();
		}
	}

	/**
	 * 着陸指示
	 */
	protected void landing() {
		// 着陸指示
		stopMove();
		if (mFlightController != null) {
			mFlightController.requestLanding();
		}
	}

	protected static final String asString(final int[] values) {
		final int n = values != null ? values.length : 0;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(values[i]).append(",");
		}
		return sb.toString();
	}

	protected void startUpdateStatusTask() {
		queueEvent(mUpdateStatusTask, 100);
	}

	// デバイス姿勢と高度
	private float mCurrentRoll = 0;
	private float mCurrentPitch = 0;
	private float mCurrentYaw = 0;
	private float mCurrentAltitude = 0;
	/** 定期的にステータスをポーリングして処理するスレッドの実行部 */
	private final Runnable mUpdateStatusTask = new Runnable() {
		private final Vector mAttitude = new Vector();
		private int prevState;
		@Override
		public void run() {
			if (mFlightController != null) {
				if (mFlightController.canGetAttitude()) {
					// デバイス姿勢を取得できる時
					mAttitude.set(mFlightController.getAttitude());
					mAttitude.toDegree();	// ラジアンを度に変換
					final float altitude = mFlightController.getAltitude();
					float yaw = mAttitude.z();
					if ((mCurrentRoll != mAttitude.x())
						|| (mCurrentPitch != mAttitude.y())
						|| (mCurrentYaw != yaw)
						|| (mCurrentAltitude != altitude)) {

							mCurrentRoll = mAttitude.x();
							mCurrentPitch = mAttitude.y();
							mCurrentYaw = yaw;
							mCurrentAltitude = altitude;
							if (mVideoView != null) {
								mVideoView.setAttitude(mCurrentRoll, mCurrentPitch, yaw, altitude);
							}
					}
				}
			}
			if (mController instanceof ICameraController) {
				// FIXME カメラの方向を更新する
			}
			if (prevState != getState()) {
				prevState = getState();
				updateButtons();
			}
			queueEvent(this, 50);	// 50ミリ秒=1秒間に最大で約20回更新
		}
	};

	/** アラート表示の更新処理をUIスレッドで実行するためのRunnable */
	private final Runnable mUpdateAlarmMessageTask = new Runnable() {
		@Override
		public void run() {
			updateAlarmMessageOnUIThread(getAlarm());
		}
	};

	protected abstract void updateAlarmMessageOnUIThread(final int alarm);
	protected abstract void updateBatteryOnUIThread(final int battery);
	/**
	 * バッテリー残量表示の更新処理をUIスレッドでするためのRunnable
	 */
	private final Runnable mUpdateBatteryTask = new Runnable() {
		@Override
		public void run() {
			updateBatteryOnUIThread(mFlightController != null ? mFlightController.getBattery() : -1);
		}
	};

	protected void setChildVisibility(final View view, final int visibility) {
		if (view != null) {
			view.setVisibility(visibility);
		}
	}

	/**
	 * ボタン表示の更新(UIスレッドで処理)
	 */
	protected abstract void updateButtons();

	protected static final int DISABLE_COLOR = 0xcf777777;

	/** ゲームパッド読み取りスレッド操作用Handler */
	private Handler mGamePadHandler;
	/** ゲームパッド読み取りスレッド開始 */
	protected void startGamePadTask() {
		if (mGamePadHandler == null) {
			final HandlerThread thread = new HandlerThread("GamePadThread");
			thread.start();
			mGamePadHandler = new Handler(thread.getLooper());
		}
		mGamePadHandler.removeCallbacks(mGamePadTask);
		mGamePadHandler.postDelayed(mGamePadTask, 100);
	}

	/** ゲームパッド読み取りスレッド終了 */
	protected void stopGamePadTask() {
		if (mGamePadHandler != null) {
			final Handler handler = mGamePadHandler;
			mGamePadHandler = null;
			handler.removeCallbacks(mGamePadTask);
			handler.getLooper().quit();
		}
	}

	private static final long YAW_LIMIT = 200;
	private Joystick mJoystick;
	private final boolean[] downs = new boolean[GamePadConst.KEY_NUMS];
	private final long[] down_times = new long[GamePadConst.KEY_NUMS];
	private final int[] analogSticks = new int[4];
	private volatile boolean moved;
	/** ゲームパッド読み取りスレッドの実行部 */
	private final Runnable mGamePadTask = new Runnable() {
		private int mCurrentPan = Integer.MAX_VALUE, mCurrentTilt = Integer.MAX_VALUE;

		@Override
		public void run() {
			final Handler handler = mGamePadHandler;
			if (handler == null) return;	// 既に終了指示が出てる

			long interval = 50;
			handler.removeCallbacks(this);
			try {
				if (mJoystick != null) {
					mJoystick.updateState(downs, down_times, analogSticks, false);
				}

				// 左右の上端ボタン(手前側)を同時押しすると非常停止
				if (((downs[GamePadConst.KEY_RIGHT_RIGHT] || downs[GamePadConst.KEY_RIGHT_1]))
					&& (downs[GamePadConst.KEY_RIGHT_LEFT] || downs[GamePadConst.KEY_LEFT_1]) ) {
					emergencyStop();
					return;
				}

				// 飛行していない時にL2/R2同時押しするとフラットトリム実行
				if ((getState() == IFlightController.STATE_STARTED)
					&& (getAlarm() == DroneStatus.ALARM_NON)
					&& downs[GamePadConst.KEY_LEFT_2] && downs[GamePadConst.KEY_RIGHT_2]) {

					mFlightController.requestFlatTrim();
					return;
				}

				// L2押しながら左アナログスティックでカメラのpan/tilt
				if (downs[GamePadConst.KEY_LEFT_2] && (mController instanceof ICameraController)) {
					if (mCurrentPan > 100) {
						mCurrentPan = ((ICameraController)mController).getPan();
						mCurrentTilt = ((ICameraController)mController).getTilt();
					}
					final int pan = mCurrentPan;
					final int tilt = mCurrentTilt;
					// 左アナログスティックの左右=左右移動
					int p = pan + (int)(analogSticks[0] / 51.2f);
					if (p < -100) {
						p = -100;
					} else if (p > 100) {
						p = 100;
					}
					// 左アナログスティックの上下=前後移動
					int t = tilt + (int)(analogSticks[1] / 51.2f);
					if (t < -100) {
						t = -100;
					} else if (t > 100) {
						t = 100;
					}
//					if (DEBUG) Log.d(TAG, String.format("(%d,%d),pan=%d/%d,tilt=%d/%d", analogSticks[0], analogSticks[1], pan, p, tilt, t));
					if ((p != pan) || (t != tilt)) {
						((ICameraController)mController).sendCameraOrientation(t, p);
						mCurrentPan = p;
						mCurrentTilt = t;
						return;
					}
					interval = 20;
				} else {
					mCurrentPan = mCurrentTilt = Integer.MAX_VALUE;
				}

				// R2押しながら左スティックでフリップ
				if (downs[GamePadConst.KEY_RIGHT_2]) {
					if (downs[GamePadConst.KEY_LEFT_LEFT]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_LEFT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_RIGHT]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_RIGHT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_UP]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_FRONT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_DOWN]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_BACK);
						return;
					}
				}

				// 中央の右側ボタン[12]=着陸
				if (downs[GamePadConst.KEY_CENTER_RIGHT]) {
					landing();
					return;
				}
				// 中央の左側ボタン[11]=離陸
				if (downs[GamePadConst.KEY_CENTER_LEFT]) {
					takeOff();
					return;
				}
				// ここまでは共通操作の処理

				// 操作モード毎の処理
				switch (mOperationType) {
				case 1:	// mode2
					gamepad_mode2();
					break;
				case 0:	// mode1
				default:
					gamepad_mode1();
					break;
				}
//				KeyGamePad.KEY_LEFT_CENTER:		// = 0;
//				KeyGamePad.KEY_LEFT_UP:			// = 1;
//				KeyGamePad.KEY_LEFT_RIGHT:		// = 2;
//				KeyGamePad.KEY_LEFT_DOWN:		// = 3;
//				KeyGamePad.KEY_LEFT_LEFT:		// = 4;
//				KeyGamePad.KEY_RIGHT_CENTER:	// = 5;
//				KeyGamePad.KEY_RIGHT_UP:		// = 6;
//				KeyGamePad.KEY_RIGHT_RIGHT:		// = 7;
//				KeyGamePad.KEY_RIGHT_DOWN:		// = 8;
//				KeyGamePad.KEY_RIGHT_LEFT:		// = 9;
//				KeyGamePad.KEY_LEFT_1:			// = 10;	// 左上前
//				KeyGamePad.KEY_LEFT_2:			// = 11;	// 左上後
//				KeyGamePad.KEY_CENTER_LEFT:		// = 12;	// 中央左
//				KeyGamePad.KEY_RIGHT_1:			// = 13;	// 右上前
//				KeyGamePad.KEY_RIGHT_2:			// = 14;	// 右上後
//				KeyGamePad.KEY_CENTER_RIGHT:	// = 15;	// 中央右
			} finally {
				handler.postDelayed(this, interval);
			}
		}
	};

	private volatile boolean mMoveByGamepad;
	protected boolean isMoveByGamepad() {
		return mMoveByGamepad;
	}

	/**
	 * ゲームパッド操作時の実際の移動コマンド発行処理
	 * @param roll
	 * @param pitch
	 * @param gaz
	 * @param yaw
	 */
	private void gamepad_move(final float roll, final float pitch, final float gaz, final float yaw) {
		sendMove(roll, pitch, gaz, yaw, true);
	}

	protected void sendMove(final float roll, final float pitch, final float gaz, final float yaw) {
		sendMove(roll, pitch, gaz, yaw, false);
	}

	private static final float DEAD_ZONE = 2.0f;
	private int prev_r, prev_p, prev_g, prev_y;

	private void sendMove(final float roll, final float pitch, final float gaz, final float yaw, final boolean moveByGamepad) {
		if (isMoveByGamepad() && !moveByGamepad) return;
		final int r = (int)(Math.abs(roll) > DEAD_ZONE ? roll : 0.0f);
		final int p = (int)(Math.abs(pitch) > DEAD_ZONE ? pitch : 0.0f);
		final int g = (int)(Math.abs(gaz) > DEAD_ZONE ? gaz : 0.0f);
		final int y = (int)(Math.abs(yaw) > DEAD_ZONE ? yaw : 0.0f);
		if ((r != prev_r) || (p != prev_p) || (g != prev_g) || (y != prev_y)) {
			prev_r = r;
			prev_p = p;
			prev_g = g;
			prev_y = y;
			if ((r != 0) || (p != 0) || (g != 0) || (y != 0)) {
				if (mFlightController != null) {
					moved = true;
					mMoveByGamepad = moveByGamepad;
					mFlightController.setMove(r, p, g, y);
				}
			} else if (moved) {
				if (mFlightController != null) {
					mFlightController.setMove(0, 0, 0, 0);
				}
				moved = mMoveByGamepad = false;
			}
		}
	}

	/** モード1でのゲームパッド入力処理 */
	private void gamepad_mode1() {
		// モード1
		// 右スティック: 左右=左右移動, 上下=上昇下降
		// 左スティック: 左右=左右回転, 上下=前後移動
		// 右アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * analogSticks[2];
		// 左アナログスティックの上下=前後移動
		final float pitch = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleY;
		// 右アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleZ;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** モード2でのゲームパッド入力処理 */
	private void gamepad_mode2() {
		// モード2
		// 右スティック: 左右=左右移動, 上下=前後移動
		// 左スティック: 左右=左右回転, 上下=上昇下降
		// 右アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * analogSticks[2];
		// 左アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleZ;
		// 右アナログスティックの上下=前後移動
		final float pitch = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleY;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	private int mSurfaceId = 0;
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:");
			if ((mVideoStream != null) && (mSurfaceId == 0)) {
				final Surface _surface = new Surface(surface);
				mSurfaceId = _surface.hashCode();
				mVideoStream.addSurface(mSurfaceId, _surface);
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:");
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:");
			if (mVideoStream != null) {
				mVideoStream.removeSurface(mSurfaceId);
			}
			mSurfaceId = 0;
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};

}
