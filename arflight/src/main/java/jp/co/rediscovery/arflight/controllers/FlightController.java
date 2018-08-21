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
import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.util.ArrayList;
import java.util.List;

import jp.co.rediscovery.arflight.DataPCMD;
import jp.co.rediscovery.arflight.DeviceConnectionListener;
import jp.co.rediscovery.arflight.DroneSettings;
import jp.co.rediscovery.arflight.DroneStatus;
import jp.co.rediscovery.arflight.FlightControllerListener;
import jp.co.rediscovery.arflight.IFlightController;
import jp.co.rediscovery.arflight.IVideoStreamController;
import jp.co.rediscovery.arflight.attribute.AttributeFloat;
import jp.co.rediscovery.arflight.attribute.AttributeMotor;
import com.serenegiant.math.Vector;

public abstract class FlightController extends DeviceController implements IFlightController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = FlightController.class.getSimpleName();

	private FlightCMDThread mFlightCMDThread;

	private final DataPCMD mDataPCMD = new DataPCMD();

	private final List<FlightControllerListener> mListeners = new ArrayList<FlightControllerListener>();

	protected DroneSettings mSettings;

	public FlightController(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
		if (DEBUG) Log.v (TAG, "コンストラクタ:");
	}

	@Override
	protected void onStarted() {
		if (DEBUG) Log.v (TAG, "onStarted:");
		super.onStarted();
		if (this instanceof IVideoStreamController) {
			// ビデオストリーミング用スレッドを生成&開始
			startVideoThread();
		}
		// 操縦コマンド送信スレッドを生成&開始
		startFlightCMDThread();
	}

	@Override
	protected void onBeforeStop() {
		if (DEBUG) Log.v(TAG, "onBeforeStop:");
		requestLanding();
		// 操縦コマンド送信スレッドを終了(終了するまで戻らない)
		stopFlightCMDThread();
		// ビデオストリーミングスレッドを終了(終了するまで戻らない)
		stopVideoThread();
		super.onBeforeStop();
	}

	@Override
	protected void setCountryCode(final String code) {
		if (DEBUG) Log.v (TAG, "setCountryCode:");
		mSettings.setCountryCode(code);
	}

	@Override
	protected void setAutomaticCountry(final boolean auto) {
		if (DEBUG) Log.v (TAG, "setAutomaticCountry:");
		mSettings.setAutomaticCountry(auto);
	}

	protected void onOutdoorSettingChanged(final boolean outdoor) {
		mSettings.outdoorMode(outdoor);
	}

	@Override
	public int getState() {
		synchronized (mStateSync) {
			return super.getState() + (((DroneStatus)mStatus).getFlyingState() << 8);
		}
	}

	public boolean isFlying() {
		return ((DroneStatus)mStatus).isFlying();
	}

	@Override
	public int getStillCaptureState() {
		return ((DroneStatus)mStatus).getStillCaptureState();
	}

	@Override
	public int getVideoRecordingState() {
		return ((DroneStatus)mStatus).getVideoRecordingState();
	}

	@Override
	public int getMassStorageId() {
		return ((DroneStatus)mStatus).massStorageId();
	}

	@Override
	public String getMassStorageName() {
		return ((DroneStatus)mStatus).massStorageName();
	}

	@Override
	public boolean needCalibration() {
		return mStatus.needCalibration();
	}

	/** 操縦コマンド送信スレッドを生成&開始 */
	private void startFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "startFlightCMDThread");
		if (mFlightCMDThread != null) {
			mFlightCMDThread.stopThread();
		}
		mFlightCMDThread = new FlightCMDThread(25); // new FlightCMDThread(mNetConfig.getPCMDLoopIntervalsMs());
		mFlightCMDThread.start();
	}

	/** 操縦コマンド送信を終了(終了するまで戻らない) */
	private void stopFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:");
		/* Cancel the looper thread and block until it is stopped. */
		if (null != mFlightCMDThread) {
			mFlightCMDThread.stopThread();
			try {
				mFlightCMDThread.join();
				mFlightCMDThread = null;
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}
		}
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:終了");
	}

	/** 映像ストリーミングデータ受信スレッドを開始(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void startVideoThread() {
		if (DEBUG) Log.v (TAG, "startVideoThread:");
	}

	/** 映像ストリーミングデータ受信スレッドを終了(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void stopVideoThread() {
		if (DEBUG) Log.v (TAG, "stopVideoThread:");
	}

	@Override
	public AttributeFloat getMaxAltitude() {
		return mSettings.maxAltitude();
	}

	@Override
	public AttributeFloat getMaxTilt() {
		return mSettings.maxTilt();
	}

	@Override
	public AttributeFloat getMaxVerticalSpeed() {
		return mSettings.maxVerticalSpeed();
	}

	@Override
	public AttributeFloat getMaxRotationSpeed() {
		return mSettings.maxRotationSpeed();
	}

	@Override
	public Vector getAttitude(){
		return ((DroneStatus)mStatus).attitude();
	}

	public float getAltitude() {
		return (float)mStatus.altitude();
	}

	@Override
	public boolean isCutoffMode() {
		return mSettings.cutOffMode();
	}

	@Override
	public boolean isAutoTakeOffModeEnabled() {
		return mSettings.autoTakeOffMode();
	}

	@Override
	public boolean hasGuard() {
		return mSettings.hasGuard();
	}

	@Override
	public int getMotorNums() {
		return 4;
	}

	@Override
	public AttributeMotor getMotor(final int index) {
		return ((DroneStatus)mStatus).getMotor(index);
	}

//================================================================================
// コールバック関係
//================================================================================
	@Override
	public void addListener(final DeviceConnectionListener listener) {
		if (DEBUG) Log.v (TAG, "addListener:");
		super.addListener(listener);
		if (listener instanceof FlightControllerListener) {
			synchronized (mListeners) {
				mListeners.add((FlightControllerListener) listener);
				callOnFlyingStateChangedUpdate(((DroneStatus)mStatus).getFlyingState());
			}
		}
	}

	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		if (DEBUG) Log.v (TAG, "removeListener:");
		synchronized (mListeners) {
			if (listener instanceof FlightControllerListener) {
				mListeners.remove((FlightControllerListener)listener);
			}
		}
		super.removeListener(listener);
	}

	/**
	 * 飛行ステータス変更コールバック呼び出し用のヘルパーメソッド
	 * @param state
	 */
	protected void callOnFlyingStateChangedUpdate(final int state) {
		if (DEBUG) Log.v (TAG, "callOnFlyingStateChangedUpdate:");
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlyingStateChangedUpdate(this, state);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * フラットトリム実行完了時のコールバック呼び出し用のヘルパーメソッド
	 */
	protected void callOnFlatTrimChanged() {
		if (DEBUG) Log.v (TAG, "callOnFlatTrimChanged:");
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlatTrimChanged(this);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	@Override
	protected void callOnCalibrationRequiredChanged(final boolean need_calibration) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationRequiredChanged(this, need_calibration);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	@Override
	protected void callOnCalibrationStartStop(final boolean is_start) {
		if (DEBUG) Log.v (TAG, "callOnCalibrationStartStop:");
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationStartStop(this, is_start);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	@Override
	protected void callOnCalibrationAxisChanged(final int axis) {
		if (DEBUG) Log.v (TAG, "callOnCalibrationAxisChanged:");
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationAxisChanged(this, axis);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	@Override
	protected void callOnStillCaptureStateChanged(final int state) {
		if (DEBUG) Log.v (TAG, "callOnStillCaptureStateChanged:");
		final boolean changed = ((DroneStatus)mStatus).setStillCaptureState(state);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onStillCaptureStateChanged(this, state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	@Override
	protected void callOnVideoRecordingStateChanged(final int state) {
		if (DEBUG) Log.v (TAG, "callOnVideoRecordingStateChanged:");
		final boolean changed = ((DroneStatus)mStatus).setVideoRecordingState(state);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onVideoRecordingStateChanged(this, state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	@Override
	protected void callOnUpdateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
		if (DEBUG) Log.v (TAG, "callOnUpdateStorageState:");
		final boolean changed = ((DroneStatus)mStatus).setMassStorageInfo(mass_storage_id, size, used_size, plugged, full, internal);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onUpdateStorageState(this, mass_storage_id, size, used_size, plugged, full, internal);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

//********************************************************************************
// 操縦関係
//********************************************************************************
	@Override
	public void setFlag(final int flag) {
		synchronized (mDataPCMD) {
			mDataPCMD.flag = flag == 0 ? 0 : (flag != 0 ? 1 : 0);
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setGaz(final float gaz) {
		synchronized (mDataPCMD) {
			mDataPCMD.gaz = gaz > 100 ? 100 : (gaz < -100 ? -100 : gaz);
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setRoll(final float roll) {
		synchronized (mDataPCMD) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setRoll(final float roll, final boolean move) {
		synchronized (mDataPCMD) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
			mDataPCMD.flag = move ? 1 : 0;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setPitch(final float pitch) {
		synchronized (mDataPCMD) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setPitch(final float pitch, final boolean move) {
		synchronized (mDataPCMD) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
			mDataPCMD.flag = move ? 1 : 0;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setYaw(final float yaw) {
		synchronized (mDataPCMD) {
			mDataPCMD.yaw = yaw > 100 ? 100 : (yaw < -100 ? -100 : yaw);
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setHeading(final float heading) {
		synchronized (mDataPCMD) {
			mDataPCMD.heading = heading;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setMove(final float roll, final float pitch) {
		synchronized (mDataPCMD) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.flag = 1;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setMove(final float roll, final float pitch, final float gaz) {
		synchronized (mDataPCMD) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.flag = 1;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw) {
		synchronized (mDataPCMD) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = 1;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw, int flag) {
		synchronized (mDataPCMD) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = flag;
			mDataPCMD.requestSend = true;
			mDataPCMD.notify();
		}
	}

	protected void getPCMD(final DataPCMD dest) {
		if (dest != null) {
			synchronized (mDataPCMD) {
				dest.set(mDataPCMD);
			}
		}
	}

	/**
	 * 操縦コマンドを送信
	 * @param flag flag to activate roll/pitch movement
	 * @param roll [-100,100]
	 * @param pitch [-100,100]
	 * @param yaw [-100,100]
	 * @param gaz [-100,100]
	 * @param heading [-180,180] (無効みたい)
	 * @return
	 */
	protected abstract boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading);

	/** 操縦コマンドを定期的に送信するためのスレッド */
	protected class FlightCMDThread extends Thread {
		private final long intervals_ms;
		private volatile boolean mIsRunning;

		public FlightCMDThread(final long _intervals_ms) {
			intervals_ms = _intervals_ms;
		}

		public void stopThread() {
			mIsRunning = false;
			synchronized (mDataPCMD) {
				mDataPCMD.notifyAll();
			}
		}

		@Override
		public void run() {
			mIsRunning = true;
			int flag, roll, pitch, yaw, gaz, heading;
			for ( ; mIsRunning ; ) {
				final long lastTime = SystemClock.elapsedRealtime();
				synchronized (mDataPCMD) {
					if (mIsRunning && isFlying()) {
						if (mDataPCMD.requestSend) {
							flag = mDataPCMD.flag;
							roll = (int)mDataPCMD.roll;
							pitch = (int)mDataPCMD.pitch;
							yaw = (int)mDataPCMD.yaw;
							gaz = (int)mDataPCMD.gaz;
							heading = (int)mDataPCMD.heading;
							mDataPCMD.requestSend = false;
							// 操縦コマンド送信
							sendPCMD(flag, roll, pitch, yaw, gaz, heading);
						}
					}
					// 次の送信予定時間までの休止時間を計算[ミリ秒]
					final long sleepTime = (SystemClock.elapsedRealtime() + intervals_ms) - lastTime;
					try {
						mDataPCMD.wait(sleepTime);
					} catch (final InterruptedException e) {
						break;
					}
				}
			}
		}
	}

}
