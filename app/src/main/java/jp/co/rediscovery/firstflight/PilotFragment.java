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

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import java.util.ArrayList;
import java.util.List;

import jp.co.rediscovery.arflight.CameraControllerListener;
import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.DroneStatus;
import jp.co.rediscovery.arflight.ICameraController;
import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.IFlightController;
import jp.co.rediscovery.arflight.ISkyController;
import jp.co.rediscovery.arflight.IVideoStreamController;
import jp.co.rediscovery.widget.OrientationView;
import jp.co.rediscovery.widget.StickView;
import jp.co.rediscovery.widget.TouchableLinearLayout;

public class PilotFragment extends BasePilotFragment {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = PilotFragment.class.getSimpleName();

	public static PilotFragment newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info) {
		final PilotFragment fragment = new PilotFragment();
		fragment.setDevice(device, info);
		return fragment;
	}

	private ViewGroup mControllerFrame;			// 操作パネル全体
	private TouchableLinearLayout mPilotFrame;	// 操縦パネル
	private OrientationView mCameraView;		// カメラのPan/Tiltの十字線描画用

	// 上パネル
	private View mTopPanel;
	private TextView mBatteryLabel;			// バッテリー残量表示
	private ImageButton mFlatTrimBtn;		// フラットトリム
	private TextView mAlertMessage;			// 非常メッセージ
	private String mBatteryFmt;
	// 下パネル
	private View mBottomPanel;
	private ImageButton mEmergencyBtn;		// 非常停止ボタン
	private ImageButton mTakeOnOffBtn;		// 離陸/着陸ボタン
	private ImageButton mConfigShowBtn;		// 設定パネル表示ボタン
	// 右サイドパネル
	private View mRightSidePanel;
	private ImageButton mCopilotBtn;		// コパイロットボタン
	private ImageButton mStillCaptureBtn;
	private ImageButton mVideoRecordingBtn;
	// 左サイドパネル
	private View mLeftSidePanel;
	// 操縦用
	private int mOperationType;				// 操縦スティックのモード
	private boolean mOperationTouch;		// タッチ描画で操縦モードかどうか

	private StickView mRightStickPanel;		// 右スティックパネル
	private StickView mLeftStickPanel;		// 左スティックパネル

	/** 操縦に使用するボタン等の一括変更用。操作可・不可に応じてenable/disableを切り替える */
	private final List<View> mActionViews = new ArrayList<View>();

	public PilotFragment() {
		super();
	}

	@Override
	public void onResume() {
		super.onResume();
		mControllerFrame.setKeepScreenOn(true);
	}

	@Override
	public void onPause() {
		mControllerFrame.setKeepScreenOn(false);
		super.onPause();
	}

	@Override
	protected View internalCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState, final int layout_id) {
		mActionViews.clear();

		final ViewGroup rootView = (ViewGroup) inflater.inflate(layout_id, container, false);

		mControllerFrame = rootView.findViewById(R.id.controller_frame);
		mControllerFrame.setOnClickListener(mOnClickListener);

		mPilotFrame = rootView.findViewById(R.id.pilot_frame);
		mPilotFrame.setOnTouchableListener(mOnTouchableListener);

		mCameraView = rootView.findViewById(R.id.camera_view);
		if (mCameraView != null) {
			mCameraView.setPanTilt(0, 0);
			mCameraView.setOnClickListener(mOnClickListener);
		}

// 上パネル
		mTopPanel = rootView.findViewById(R.id.top_panel);
		mTopPanel.setOnClickListener(mOnClickListener);
		mTopPanel.setOnLongClickListener(mOnLongClickListener);
		mActionViews.add(mTopPanel);
		// フラットトリムボタン
		mFlatTrimBtn = rootView.findViewById(R.id.flat_trim_btn);
		mFlatTrimBtn.setOnClickListener(mOnClickListener);
		mFlatTrimBtn.setOnLongClickListener(mOnLongClickListener);
		mActionViews.add(mFlatTrimBtn);
		// 設定表示ボタン
		mConfigShowBtn = rootView.findViewById(R.id.config_show_btn);
		mConfigShowBtn.setOnClickListener(mOnClickListener);
		//
		mBatteryLabel = rootView.findViewById(R.id.batteryLabel);
		mAlertMessage = rootView.findViewById(R.id.alert_message);
		mAlertMessage.setVisibility(View.INVISIBLE);

// 下パネル
		// 非常停止ボタン
		mBottomPanel = rootView.findViewById(R.id.bottom_panel);
		mEmergencyBtn = rootView.findViewById(R.id.emergency_btn);
		mEmergencyBtn.setOnClickListener(mOnClickListener);
		// 離着陸指示ボタン
		mTakeOnOffBtn = rootView.findViewById(R.id.take_onoff_btn);
		mTakeOnOffBtn.setOnClickListener(mOnClickListener);
		mTakeOnOffBtn.setOnLongClickListener(mOnLongClickListener);
		mActionViews.add(mTakeOnOffBtn);

// 操縦パネル
		ImageButton button;
// 右サイドパネル
		mRightSidePanel = rootView.findViewById(R.id.right_side_panel);
		mActionViews.add(mRightSidePanel);

		// コパイロットボタン
		mCopilotBtn = rootView.findViewById(R.id.copilot_btn);
		mCopilotBtn.setOnClickListener(mOnClickListener);
		mCopilotBtn.setVisibility(mController instanceof ISkyController ? View.VISIBLE : View.GONE);

		// 静止画撮影
		mStillCaptureBtn = rootView.findViewById(R.id.still_capture_btn);
		mStillCaptureBtn.setOnClickListener(mOnClickListener);

		// 動画撮影
		mVideoRecordingBtn = rootView.findViewById(R.id.video_capture_btn);
		mVideoRecordingBtn.setOnClickListener(mOnClickListener);

		button = rootView.findViewById(R.id.cap_p45_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = rootView.findViewById(R.id.cap_m45_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

// 左サイドパネル
		mLeftSidePanel = rootView.findViewById(R.id.left_side_panel);
		mActionViews.add(mLeftSidePanel);

		button = rootView.findViewById(R.id.flip_right_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = rootView.findViewById(R.id.flip_left_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = rootView.findViewById(R.id.flip_front_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		button = rootView.findViewById(R.id.flip_back_btn);
		button.setOnClickListener(mOnClickListener);
		mActionViews.add(button);

		// 右スティックパネル
		mRightStickPanel = rootView.findViewById(R.id.stick_view_right);
		if (mRightStickPanel != null) {
			mRightStickPanel.setOnStickMoveListener(mOnStickMoveListener);
			mActionViews.add(mRightStickPanel);
		}

		// 左スティックパネル
		mLeftStickPanel = rootView.findViewById(R.id.stick_view_left);
		if (mLeftStickPanel != null) {
			mLeftStickPanel.setOnStickMoveListener(mOnStickMoveListener);
			mActionViews.add(mRightStickPanel);
		}

		final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(getProductId());
		if (mController instanceof ICameraController) {
			((ICameraController)mController).setCameraControllerListener(mCameraControllerListener);
			((ICameraController)mController).sendCameraOrientation(0, 0);
		}
		mVideoView = rootView.findViewById(R.id.drone_view);
		return rootView;
	}

	private final TouchableLinearLayout.OnTouchableListener mOnTouchableListener
		= new TouchableLinearLayout.OnTouchableListener() {

		/** minimum distance between touch positions*/
		private static final float MIN_DISTANCE = 15.f;
		private static final float MIN_DISTANCE_SQUARE = MIN_DISTANCE * MIN_DISTANCE;
		/** コマンドを送る最小間隔[ミリ秒]  */
		private static final long MIN_CMD_INTERVALS_MS = 50;	// 50ミリ秒
		/** pan/tiltをリセットするための長押し時間 */
		private static final long RESET_DURATION_MS = 2000;	// 2秒

		private boolean inited;
		/** マルチタッチ開始時のタッチポインタのインデックス */
		private int mPrimaryId, mSecondaryId;
		/** マルチタッチ開始時のタッチ位置 */
		private float mPrimaryX, mPrimaryY, mSecondX, mSecondY;
		/** マルチタッチ開始時のタッチ中点 */
		private float mPivotX, mPivotY;
		/** マルチタッチ開始時のタッチ距離 */
		private float mTouchDistance;

		private float mPanLen, mTiltLen;
		private int mPan, mTilt;
		private long prevTime;

		@Override
		public boolean onInterceptTouchEvent(final MotionEvent event) {
			final boolean intercept = (mController instanceof ICameraController) && (event.getPointerCount() > 1);	// マルチタッチした時は横取りする
			if (intercept) {
				// マルチタッチ開始時のタッチ位置等を保存
				initTouch(event);
			}
			return intercept;
		}

		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			final int action = event.getActionMasked();
			final int n = event.getPointerCount();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
//				if (DEBUG) Log.v(TAG, "ACTION_DOWN:");
				// シングルタッチ
				return n > 1;	// 多分ここにはこない
			case MotionEvent.ACTION_POINTER_DOWN:
//				if (DEBUG) Log.v(TAG, "ACTION_POINTER_DOWN:");
				return true;
			case MotionEvent.ACTION_MOVE:
//				if (DEBUG) Log.v(TAG, "ACTION_MOVE:");
				if ((n > 1) && (System.currentTimeMillis() - prevTime > MIN_CMD_INTERVALS_MS) && checkTouchMoved(event)) {
					prevTime = System.currentTimeMillis();
					removeEvent(mResetRunnable);
					if (!inited) {
						initTouch(event);
					}
					// 現在のタッチ座標
					final float x0 = event.getX(0);
					final float y0 = event.getY(0);
					final float x1 = event.getX(1);
					final float y1 = event.getY(1);
					// 現在の中点座標
					final float cx = (x0 + x1) / 2.0f;
					final float cy = (y0 + y1) / 2.0f;
					// 最初のタッチ中点との距離を計算
					final float dx = (mPivotX - cx) * mPanLen + mPan;
					final float dy = (cy - mPivotY) * mTiltLen + mTilt;
					final int pan = dx < -100 ? -100 : (dx > 100 ? 100 : (int)dx);
					final int tilt = dy < -100 ? -100 : (dy > 100 ? 100 : (int)dy);
//					if (DEBUG) Log.v(TAG, String.format("ACTION_MOVE:dx=%5.2f,dy=%5.2f,pan=%d,tilt=%d", dx, dy, pan, tilt));
					if (mController instanceof ICameraController) {
						((ICameraController)mController).sendCameraOrientation(tilt, pan);
					}
				}
				return true;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			}
			if (n == 0) {
				inited = false;
				removeEvent(mResetRunnable);
			}
			return false;
		}

		/** Pan/TiltをリセットするためのRunnable */
		private Runnable mResetRunnable = new Runnable() {
			@Override
			public void run() {
				inited = false;
				if (mController instanceof ICameraController) {
					((ICameraController)mController).sendCameraOrientation(0, 0);
					if (mCameraView != null) {
						mCameraView.setPanTilt(0, 0);
					}
				}
			}
		};

		private void initTouch(final MotionEvent event) {
			// primary touch
			mPrimaryId = event.getPointerId(0);
			mPrimaryX = event.getX(0);
			mPrimaryY = event.getY(0);
			// secondary touch
			mSecondaryId = event.getPointerId(1);
			mSecondX = event.getX(1);
			mSecondY = event.getY(1);
			// calculate the distance between first and second touch
			final float dx = mSecondX - mPrimaryX;
			final float dy = mSecondY - mPrimaryY;
			mTouchDistance = (float)Math.hypot(dx, dy);
			// set pivot position to the middle coordinate
			mPivotX = (mPrimaryX + mSecondX) / 2.0f;
			mPivotY = (mPrimaryY + mSecondY) / 2.0f;
			prevTime = System.currentTimeMillis() - MIN_CMD_INTERVALS_MS;
			mPanLen = 80.0f / mPilotFrame.getWidth();
			mTiltLen = 80.0f / mPilotFrame.getHeight();
			if (mController instanceof ICameraController) {
				mPan = ((ICameraController)mController).getPan();
				mTilt = ((ICameraController)mController).getTilt();
			}
			inited = true;
			removeEvent(mResetRunnable);
			queueEvent(mResetRunnable, RESET_DURATION_MS);
		}

		/** タッチ位置を動かしたかどうかを取得 */
		private final boolean checkTouchMoved(final MotionEvent event) {
			final int ix0 = event.findPointerIndex(mPrimaryId);
			final int ix1 = event.findPointerIndex(mSecondaryId);
			if (ix0 >= 0) {
				// check primary touch
				float x = event.getX(ix0) - mPrimaryX;
				float y = event.getY(ix0) - mPrimaryY;
				if (x * x + y * y < MIN_DISTANCE_SQUARE) {
					// primary touch is at the almost same position
					if (ix1 >= 0) {
						// check secondary touch
						x = event.getX(ix1) - mSecondX;
						y = event.getY(ix1) - mSecondY;
						return !(x * x + y * y < MIN_DISTANCE_SQUARE);
					} else {
						return false;
					}
				}
			}
			return true;
		}

	};

	private final CameraControllerListener mCameraControllerListener = new CameraControllerListener() {
		@Override
		public void onCameraOrientationChanged(final IDeviceController controller, final int pan, final int tilt) {
			if (mCameraView != null) {
				mCameraView.setPanTilt(-pan, tilt);
			}
		}
	};

	protected boolean onClick(final View view) {
		return false;
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			if (PilotFragment.this.onClick(view)) return;
			switch (view.getId()) {
			case R.id.flat_trim_btn:
				// フラットトリム
				if ((mFlightController != null) && (getState() == IFlightController.STATE_STARTED)) {
					mFlightController.requestFlatTrim();
				}
				break;
			case R.id.config_show_btn:
				// 設定パネル表示処理
				if (isStarted()) {
					if ((getState() & IFlightController.STATE_MASK_FLYING) == DroneStatus.STATE_FLYING_LANDED) {
						replace(ConfigFragment.newInstance(getDevice(), getDeviceInfo()));
					} else {
						landing();
					}
				}
				break;
			case R.id.emergency_btn:
				// 非常停止指示ボタンの処理
				emergencyStop();
				break;
			case R.id.copilot_btn:
				if ((mController instanceof ISkyController) && mController.isConnected()) {
					((ISkyController)mController).setCoPilotingSource(
						((ISkyController)mController).getCoPilotingSource() == 0 ? 1 : 0
					);
					runOnUiThread(mUpdateButtonsTask, 300);
				}
				break;
			case R.id.take_onoff_btn:
				// 離陸指示/着陸指示ボタンの処理
				if (!isFlying()) {
//					takeOff();
					Toast.makeText(getActivity(), R.string.notify_takeoff, Toast.LENGTH_SHORT).show();
				} else {
					landing();
				}
				updateButtons();
				break;
			case R.id.flip_front_btn:
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_FRONT);
				}
				break;
			case R.id.flip_back_btn:
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_BACK);
				}
				break;
			case R.id.flip_right_btn:
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_RIGHT);
				}
				break;
			case R.id.flip_left_btn:
				if (mFlightController != null) {
					mFlightController.requestAnimationsFlip(IFlightController.FLIP_LEFT);
				}
				break;
			case R.id.still_capture_btn:
				// 静止画撮影ボタンの処理
				if (getStillCaptureState() == DroneStatus.MEDIA_READY) {
					if (mFlightController != null) {
						mFlightController.requestTakePicture();
					}
				}
				break;
			case R.id.video_capture_btn:
				// 動画撮影ボタンの処理
				if (mController instanceof ICameraController) {
					mVideoRecording = !mVideoRecording;
					((ICameraController)mController).sendVideoRecording(mVideoRecording);
				}
				break;
			case R.id.cap_p45_btn:
				if (mFlightController != null) {
					mFlightController.requestAnimationsCap(45);
				}
				break;
			case R.id.cap_m45_btn:
				if (mFlightController != null) {
					mFlightController.requestAnimationsCap(-45);
				}
				break;
			}
		}
	};

	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View view) {
			mVibrator.vibrate(50);
			switch (view.getId()) {
			case R.id.flat_trim_btn:
				if ((mFlightController != null) && (getState() == IFlightController.STATE_STARTED)) {
					replace(CalibrationFragment.newInstance(getDevice()));
					return true;
				}
				break;
			case R.id.take_onoff_btn:
				// 離陸/着陸ボタンを長押しした時の処理
				if (!isFlying()) {
					takeOff();
					return true;
				}
			}
			return false;
		}
	};

	private static final int CTRL_STEP = 5;
	private float mFirstPtRightX, mFirstPtRightY;
	private int mPrevRightMX, mPrevRightMY;
	private float mFirstPtLeftX, mFirstPtLeftY;
	private int mPrevLeftMX, mPrevLeftMY;
	private final StickView.OnStickMoveListener mOnStickMoveListener = new StickView.OnStickMoveListener() {
		@Override
		public void onStickMove(final View view, final float dx, final float dy) {
			int mx = (int) (dx * 100);
			if (mx < -100) mx = -100;
			else if (mx > 100) mx = 100;
			mx = (mx / CTRL_STEP) * CTRL_STEP;
			int my = (int) (dy * 100);
			if (my < -100) my = -100;
			else if (my > 100) my = 100;
			my = (my / CTRL_STEP) * CTRL_STEP;
			switch (mOperationType) {
			case 1:	// mode2
				stick_mode2(view.getId(), mx, my);
				break;
			case 0:	// mode1
			default:
				stick_mode1(view.getId(), mx, my);
			}
		}
	};

	private void stick_mode1(final int id, final int _mx, final int _my) {
		// モード1
		// 右スティック: 左右=左右移動, 上下=上昇下降
		// 左スティック: 左右=左右回転, 上下=前後移動
		switch (id) {
		case R.id.stick_view_right: {
			if (_my != mPrevRightMY) {	// 上昇下降
				mPrevRightMY = _my;
				if (mFlightController != null) {
					mFlightController.setGaz(-_my);
				}
			}
			int mx = _mx;
			if ((Math.abs(_mx) < 20)) mx = 0;
			if (mx != mPrevRightMX) {	// 左右移動
				mPrevRightMX = mx;
				if (mFlightController != null) {
					mFlightController.setRoll(mx, true);
				}
			}
			break;
		}
		case R.id.stick_view_left: {
			if (_mx != mPrevLeftMX) {	// 左右回転
				mPrevLeftMX = _mx;
				if (mFlightController != null) {
					mFlightController.setYaw(_mx);
				}
			}
			if (_my != mPrevLeftMY) {	// 前後移動
				mPrevLeftMY = _my;
				if (mFlightController != null) {
					mFlightController.setPitch(-_my, true);
				}
			}
			break;
		}
		}
	}

	private void stick_mode2(final int id, final int _mx, final int _my) {
		// モード2
		// 右スティック: 左右=左右移動, 上下=前後移動
		// 左スティック: 左右=左右回転, 上下=上昇下降
		switch (id) {
		case R.id.stick_view_right: {
			if (_my != mPrevRightMY) {	// 前後移動
				mPrevRightMY = _my;
				if (mFlightController != null) {
					mFlightController.setPitch(-_my, true);
				}
			}
			if (_mx != mPrevRightMX) {	// 左右移動
				mPrevRightMX = _mx;
				if (mFlightController != null) {
					mFlightController.setRoll(_mx, true);
				}
			}
			break;
		}
		case R.id.stick_view_left: {
			int mx = _mx;
			if ((Math.abs(_mx) < 20)) mx = 0;
			if (mx != mPrevLeftMX) {	// 左右回転
				mPrevLeftMX = mx;
				if (mFlightController != null) {
					mFlightController.setYaw(mx);
				}
			}
			if (_my != mPrevLeftMY) {	// 上昇下降
				mPrevLeftMY = _my;
				if (mFlightController != null) {
					mFlightController.setGaz(-_my);
				}
			}
			break;
		}
		}
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
//		if (DEBUG) Log.v(TAG, "onConnect:");
		super.onConnect(controller);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setChildVisibility(mVideoRecordingBtn, controller instanceof IVideoStreamController ? View.VISIBLE : View.INVISIBLE);
			}
		});
		if (mController instanceof ICameraController) {
			((ICameraController)mController).sendExposure(0);
			((ICameraController)mController).sendCameraOrientation(0, 0);
			((ICameraController)mController).sendAutoWhiteBalance(0);	// 自動ホワイトバランス
		}
	}

	@Override
	protected void updateAlarmMessageOnUIThread(final int alarm) {
		switch (alarm) {
		case DroneStatus.ALARM_NON:					// No alert
			break;
		case DroneStatus.ALARM_USER_EMERGENCY:		// User emergency alert
			mAlertMessage.setText(R.string.alarm_user_emergency);
			break;
		case DroneStatus.ALARM_CUTOUT:				// Cut out alert
			mAlertMessage.setText(R.string.alarm_motor_cut_out);
			break;
		case DroneStatus.ALARM_BATTERY_CRITICAL:	// Critical battery alert
			mAlertMessage.setText(R.string.alarm_low_battery_critical);
			break;
		case DroneStatus.ALARM_BATTERY:				// Low battery alert
			mAlertMessage.setText(R.string.alarm_low_battery);
			break;
		case DroneStatus.ALARM_DISCONNECTED:		// 切断された
			mAlertMessage.setText(R.string.alarm_disconnected);
			break;
		default:
			Log.w(TAG, "unexpected alarm state:" + alarm);
			break;
		}
		mAlertMessage.setVisibility(alarm != 0 ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	protected void updateBatteryOnUIThread(final int battery) {
		final boolean isSkyController = mController instanceof ISkyController;
		if (mBatteryFmt == null) {
			mBatteryFmt = getString(isSkyController ? R.string.battery_skycontroller : R.string.battery);
		}
		if (battery >= 0) {
			mBatteryLabel.setText(isSkyController
				? String.format(mBatteryFmt, battery, ((ISkyController)mController).getBatterySkyController())
				: String.format(mBatteryFmt, battery));
		} else {
			mBatteryLabel.setText("---");
		}
	}


	/**
	 * ボタン表示の更新(UIスレッドで処理)
	 */
	protected void updateButtons() {
		runOnUiThread(mUpdateButtonsTask);
	}

	/**
	 *　ボタンの表示更新をUIスレッドで行うためのRunnable
	 */
	private final Runnable mUpdateButtonsTask = new Runnable() {
		@Override
		public void run() {
			final int state = getState();
			final int alarm_state = getAlarm();
			final int still_capture_state = getStillCaptureState();
			final int video_recording_state = getVideoRecordingState();
			final boolean is_connected = isStarted();
			final boolean can_fly = alarm_state == DroneStatus.ALARM_NON;
			final boolean can_flattrim = can_fly && (state == IFlightController.STATE_STARTED);
			final boolean can_config = can_flattrim;
			final boolean is_battery_alarm
				= (alarm_state == DroneStatus.ALARM_BATTERY) || (alarm_state == DroneStatus.ALARM_BATTERY_CRITICAL);

			// 上パネル
			mTopPanel.setEnabled(is_connected);
			mFlatTrimBtn.setEnabled(can_flattrim);	// フラットトリム
			mBatteryLabel.setTextColor(is_battery_alarm ? 0xffff0000 : 0xff9400d3);
			mConfigShowBtn.setEnabled(can_config);
			mConfigShowBtn.setColorFilter(can_config ? 0 : DISABLE_COLOR);

			// 下パネル
			mBottomPanel.setEnabled(is_connected);
			mEmergencyBtn.setEnabled(is_connected);	// 非常停止
			mCopilotBtn.setEnabled(is_connected);	// コパイロット
			mCopilotBtn.setColorFilter(
				(mController instanceof ISkyController)
				&& ((ISkyController)mController).getCoPilotingSource() == 0
					? 0 : 0xffff0000);

			// 離陸/着陸
			switch (state & IFlightController.STATE_MASK_FLYING) {
			case DroneStatus.STATE_FLYING_LANDED:		// 0x0000;		// FlyingState=0
				mVideoView.stopEngine();
			case DroneStatus.STATE_FLYING_LANDING:		// 0x0400;		// FlyingState=4
				mTakeOnOffBtn.setImageResource(R.mipmap.ic_takeoff);
				break;
			case DroneStatus.STATE_FLYING_TAKEOFF:		// 0x0100;		// FlyingState=1
			case DroneStatus.STATE_FLYING_HOVERING:		// 0x0200;		// FlyingState=2
			case DroneStatus.STATE_FLYING_FLYING:		// 0x0300;		// FlyingState=3
			case DroneStatus.STATE_FLYING_ROLLING:		// 0x0600;		// FlyingState=6
				mTakeOnOffBtn.setImageResource(R.mipmap.ic_landing);
				mVideoView.startEngine();
				break;
			case DroneStatus.STATE_FLYING_EMERGENCY:	// 0x0500;		// FlyingState=5
				mVideoView.stopEngine();
				break;
			}

			// 右サイドパネル(とmCapXXXBtn等)
			mRightSidePanel.setEnabled(can_fly);

			mStillCaptureBtn.setEnabled(still_capture_state == DroneStatus.MEDIA_READY);
			setChildVisibility(mStillCaptureBtn, still_capture_state != DroneStatus.MEDIA_UNAVAILABLE ? View.VISIBLE : View.INVISIBLE);

			mVideoRecordingBtn.setEnabled((video_recording_state == DroneStatus.MEDIA_READY) || (video_recording_state == DroneStatus.MEDIA_BUSY));
			setChildVisibility(mStillCaptureBtn, video_recording_state != DroneStatus.MEDIA_UNAVAILABLE ? View.VISIBLE : View.INVISIBLE);
			mVideoRecordingBtn.setColorFilter(mVideoRecording ? 0x7fff0000 : 0);

			// 左サイドパネル(とmFlipXXXBtn等)
			mLeftSidePanel.setEnabled(can_fly);
			// 右スティックパネル(東/西ボタン)
			if (mRightStickPanel != null) {
				mRightStickPanel.setEnabled(can_fly);
			}
			// 左スティックパネル(北/南ボタン)
			if (mLeftStickPanel != null) {
				mLeftStickPanel.setEnabled(can_fly);
			}

			for (final View view: mActionViews) {
				view.setEnabled(can_fly);
				if (view instanceof ImageView) {
					((ImageView)view).setColorFilter(can_fly ? 0 : DISABLE_COLOR);
				}
			}
		}
	};

	private int mSurfaceId = 0;
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			if ((mVideoStream != null) && (mSurfaceId == 0)) {
				final Surface _surface = new Surface(surface);
				mSurfaceId = _surface.hashCode();
				mVideoStream.addSurface(mSurfaceId, _surface);
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
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
