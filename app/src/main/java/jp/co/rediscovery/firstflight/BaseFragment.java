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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.serenegiant.dialog.MessageDialogFragment;
import com.serenegiant.utils.BuildCheck;
import com.serenegiant.utils.HandlerThreadHandler;
import com.serenegiant.utils.PermissionCheck;

import java.util.HashMap;
import java.util.Map;

/**　Fragmentでの共通処理を実装したクラス　*/
public class BaseFragment extends Fragment implements MessageDialogFragment.MessageDialogListener {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = BaseFragment.class.getSimpleName();

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private Handler mAsyncHandler;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected Vibrator mVibrator;

	public BaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(activity);
		mVibrator = (Vibrator)getActivity().getSystemService(Activity.VIBRATOR_SERVICE);
		mIsReplacing = false;
	}

	@Override
	public synchronized void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
		mAsyncHandler = HandlerThreadHandler.createHandler(TAG);
		mIsReplacing = false;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		if (mAsyncHandler != null) {
			try {
				mAsyncHandler.getLooper().quit();
			} catch (final Exception e) {
				// ignore
			}
			mAsyncHandler = null;
		}
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		mLocalBroadcastManager = null;
		mVibrator = null;
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		final Bundle args = getArguments();
		if (args != null) {
			outState.putAll(args);
		}
		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		loadArguments(savedInstanceState);
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mIsReplacing = false;
	}

	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		removeRequestPopBackStack();
		super.onPause();
	}

	protected void loadArguments(final Bundle savedInstanceState) {
	}

	private boolean mIsReplacing;
	protected boolean isReplacing() {
		return mIsReplacing;
	}

	/**
	 * 指定したフラグメントに切り替える。元のフラグメントはbackstackに追加する。
	 * @param fragment nullなら何もしない
	 * @return
	 */
	protected Fragment replace(final Fragment fragment) {
		if (fragment != null) {
			mIsReplacing = true;
			getFragmentManager().beginTransaction()
				.addToBackStack(null)
				.replace(R.id.container, fragment)
				.commit();
		}
		return fragment;
	}

	protected void clearReplacing() {
		mIsReplacing = false;
	}

	/**
	 * １つ前のフラグメントに戻る
	 */
	protected void popBackStack() {
		mIsReplacing = false;
		try {
			getFragmentManager().popBackStack();
		} catch (final Exception e) {
			//
		}
	}

	/**
	 * UIスレッド上で実行依頼する
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
	 * UIスレッド上での実行要求を削除する
	 * @param task
	 */
	protected void removeFromUIThread(final Runnable task) {
		mUIHandler.removeCallbacks(task);
	}

	/**
	 * 指定時間後に指定したタスクをUIスレッド上で実行する。
	 * @param task UIスレッド上で行う処理
	 * @param delay_msec 0以下ならrunOnUiThreadと同じ
	 */
	protected void runOnUiThread(final Runnable task, final long delay_msec) {
		if (delay_msec <= 0) {
			runOnUiThread(task);
		} else if (task != null) {
			mUIHandler.postDelayed(task, delay_msec);
		}
	}

	/**
	 * プライベートスレッドでの実行待ちタスクを削除する
	 * @param task
	 */
	protected void removeEvent(final Runnable task) {
		if (mAsyncHandler != null) {
			mAsyncHandler.removeCallbacks(task);
		} else {
			removeFromUIThread(task);
		}
	}
	/**
	 * 指定時間後に指定したタスクをプライベートスレッド上で実行する
	 * @param task
	 * @param delay_msec
	 */
	protected void queueEvent(final Runnable task, final long delay_msec) {
		if (mAsyncHandler != null) {
			if (delay_msec <= 0) {
				mAsyncHandler.post(task);
			} else {
				mAsyncHandler.postDelayed(task, delay_msec);
			}
		} else {
			runOnUiThread(task, delay_msec);
		}
	}

	/**
	 * 指定時間後に前のフラグメントに戻る
	 * @param delay
	 */
	protected void requestPopBackStack(final long delay) {
		removeFromUIThread(mPopBackStackTask);
		runOnUiThread(mPopBackStackTask, delay);	// UIスレッド上で遅延実行
	}

	/**
	 * 指定時間後に前のフラグメントに戻るのをキャンセル
	 */
	protected void removeRequestPopBackStack() {
		removeFromUIThread(mPopBackStackTask);
	}

	/**
	 * 一定時間後にフラグメントを終了するためのRunnable
	 * 切断された時に使用
	 */
	private final Runnable mPopBackStackTask = new Runnable() {
		@Override
		public void run() {
			try {
				mIsReplacing = false;
				popBackStack();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	/**
	 * タッチレスポンス用にカラーフィルターを適用する時間
	 */
	protected static final long TOUCH_RESPONSE_TIME_MS = 100;	// 200ミリ秒
	/**
	 * タッチレスポンス時のカラーフィルター色
	 */
	protected static final int TOUCH_RESPONSE_COLOR = 0x7f331133;

	/**
	 * カラーフィルタクリア用のRunnableのキャッシュ
	 */
	@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
	private final Map<ImageView, ResetColorFilterTask> mResetColorFilterTasks = new HashMap<ImageView, ResetColorFilterTask>();

	/**
	 * タッチレスポンス用のカラーフィルターを規定時間適用する
	 * @param image
	 */
	protected void setColorFilter(final ImageView image) {
		setColorFilter(image, TOUCH_RESPONSE_COLOR, TOUCH_RESPONSE_TIME_MS);
	}

	/**
	 * 指定したImageViewに指定した色でカラーフィルターを適用する。
	 * reset_delayが0より大きければその時間経過後にカラーフィルターをクリアする
	 * @param image
	 * @param color
	 * @param reset_delay ミリ秒
	 */
	protected void setColorFilter(final ImageView image, final int color, final long reset_delay) {
		if (image != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					image.setColorFilter(color);
				}
			});
			if (reset_delay > 0) {
				ResetColorFilterTask task = mResetColorFilterTasks.get(image);
				if (task == null) {
					task = new ResetColorFilterTask(image);
				}
				removeFromUIThread(task);
				runOnUiThread(task, reset_delay);	// UIスレッド上で遅延実行
			}
		}
	}

	/**
	 * 一定時間後にImageView(とImageButton)のカラーフィルターをクリアするためのRunnable
	 */
	private static class ResetColorFilterTask implements Runnable {
		private final ImageView mImage;
		public ResetColorFilterTask(final ImageView image) {
			mImage = image;
		}
		@Override
		public void run() {
			mImage.setColorFilter(0);
		}
	}

//================================================================================
// Android6以降の動的パーミッション関係の処理
//================================================================================
	/**
	 * Callback listener from MessageDialogFragmentV4
	 * @param dialog
	 * @param requestCode
	 * @param permissions
	 * @param result
	 */
	@SuppressLint("NewApi")
	@Override
	public void onMessageDialogResult(final MessageDialogFragment dialog, final int requestCode, final String[] permissions, final boolean result) {
		if (result) {
			// request permission(s) when user touched/clicked OK
			if (BuildCheck.isMarshmallow()) {
				requestPermissions(permissions, requestCode);
				return;
			}
		}
		// check permission and call #checkPermissionResult when user canceled or not Android6
		final Context context = getActivity();
		for (final String permission: permissions) {
			checkPermissionResult(requestCode, permission, PermissionCheck.hasPermission(context, permission));
		}
	}

	/**
	 * callback method when app(Fragment) receive the result of permission result from ANdroid system
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);	// 何もしてないけど一応呼んどく
		final int n = Math.min(permissions.length, grantResults.length);
		for (int i = 0; i < n; i++) {
			checkPermissionResult(requestCode, permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
		}
	}

	/**
	 * check the result of permission request
	 * if app still has no permission, just show Toast
	 * @param requestCode
	 * @param permission
	 * @param result
	 */
	protected void checkPermissionResult(final int requestCode, final String permission, final boolean result) {
		// show Toast when there is no permission
		if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
			onUpdateAudioPermission(result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_audio, Toast.LENGTH_SHORT).show();
			}
		}
		if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
			onUpdateExternalStoragePermission(result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_ext_storage, Toast.LENGTH_SHORT).show();
			}
		}
		if (Manifest.permission.INTERNET.equals(permission)) {
			onUpdateNetworkPermission(result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_network, Toast.LENGTH_SHORT).show();
			}
		}
		if (Manifest.permission.ACCESS_COARSE_LOCATION.equals(permission)
			|| Manifest.permission.ACCESS_FINE_LOCATION.equals(permission)) {
			onUpdateLocationPermission(permission, result);
			if (!result) {
				Toast.makeText(getActivity().getApplicationContext(), R.string.permission_location, Toast.LENGTH_SHORT).show();
			}
		}
	}

	protected void onUpdateAudioPermission(final boolean hasPermission) {
	}

	protected void onUpdateExternalStoragePermission(final boolean hasPermission) {
	}

	protected void onUpdateNetworkPermission(final boolean hasPermission) {
	}

	protected void onUpdateLocationPermission(final String permission, final boolean hasPermission) {
	}

	protected static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 0x01;
	protected static final int REQUEST_PERMISSION_AUDIO_RECORDING = 0x02;
	protected static final int REQUEST_PERMISSION_NETWORK = 0x03;
	protected static final int REQUEST_PERMISSION_LOCATION = 0x04;
	protected static final int REQUEST_PERMISSION_LOCATION_COARSE = 0x05;
	protected static final int REQUEST_PERMISSION_LOCATION_FINE = 0x06;

	/**
	 * check whether this app has write external storage
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionWriteExternalStorage() {
		if (!PermissionCheck.hasWriteExternalStorage(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE,
				R.string.permission_title, R.string.permission_ext_storage_request,
				new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission of audio recording
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionAudio() {
		if (!PermissionCheck.hasAudio(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_AUDIO_RECORDING,
				R.string.permission_title, R.string.permission_audio_recording_request,
				new String[] {Manifest.permission.RECORD_AUDIO});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission of network access
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionNetwork() {
		if (!PermissionCheck.hasNetwork(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_NETWORK,
				R.string.permission_title, R.string.permission_network_request,
				new String[] {Manifest.permission.INTERNET});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission to access to coarse and fine location
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionLocation() {
		if (!PermissionCheck.hasAccessLocation(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_LOCATION,
				R.string.permission_title, R.string.permission_location_request,
				new String[] { Manifest.permission.ACCESS_COARSE_LOCATION,
							  Manifest.permission.ACCESS_FINE_LOCATION});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission to access to coarse location
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionLocationCoarse() {
		if (!PermissionCheck.hasAccessLocation(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_LOCATION,
				R.string.permission_title, R.string.permission_location_request,
				new String[] { Manifest.permission.ACCESS_COARSE_LOCATION});
			return false;
		}
		return true;
	}

	/**
	 * check whether this app has permission to access to fine location
	 * if this app has no permission, show dialog
	 * @return true this app has permission
	 */
	protected boolean checkPermissionLocationFine() {
		if (!PermissionCheck.hasAccessLocation(getActivity())) {
			MessageDialogFragment.showDialog(this, REQUEST_PERMISSION_LOCATION,
				R.string.permission_title, R.string.permission_location_request,
				new String[] { Manifest.permission.ACCESS_FINE_LOCATION});
			return false;
		}
		return true;
	}

}
