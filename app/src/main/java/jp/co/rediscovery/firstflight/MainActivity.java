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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;
import com.serenegiant.gamepad.Joystick;

import jp.co.rediscovery.arflight.ManagerFragment;

import com.serenegiant.net.NetworkChangedReceiver;

public class MainActivity extends AppCompatActivity {
	private static final boolean DEBUG = false;    // FIXME 実働時はfalseにすること
	private static String TAG = MainActivity.class.getSimpleName();

	static {
		ARSDK.loadSDKLibs();
//		ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_DEBUG);
		ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_ERROR);
	}

	/*package*/Joystick mJoystick;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		NetworkChangedReceiver.enable(getApplicationContext());
		final ManagerFragment manager = ManagerFragment.getInstance(this);
		if (savedInstanceState == null) {
			final Fragment fragment = MyInstructionsFragment.newInstance();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment)
				.commit();
		}
		mJoystick = Joystick.getInstance(this);
	}

	@Override
	protected void onDestroy() {
		releaseJoystick();
		NetworkChangedReceiver.disable(getApplicationContext());
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		//　ActionBarActivity/AppCompatActivityはバックキーの処理がおかしくて
		// バックスタックの処理が正常にできない事に対するworkaround
		final FragmentManager fm = getFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			if (DEBUG) Log.i(TAG, "#onBackPressed:popBackStack");
			fm.popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mJoystick != null) {
			mJoystick.register();
		}
	}

	@Override
	public void onPause() {
		if (mJoystick != null) {
			mJoystick.unregister();
		}
		if (isFinishing()) {
			ManagerFragment.releaseAll(this);
		}
		super.onPause();
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (mJoystick != null) {
			if (mJoystick.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		if (mJoystick != null) {
			mJoystick.dispatchGenericMotionEvent(event);
		}
		return super.dispatchGenericMotionEvent(event);
	}

	private void releaseJoystick() {
		if (mJoystick != null) {
			mJoystick.release();
			mJoystick = null;
		}
	}

	public Joystick getJoystick() {
		return mJoystick;
	}

	private ProgressDialog mProgress;

	public synchronized void showProgress(final int title_resID, final boolean cancelable,
		final DialogInterface.OnCancelListener cancel_listener) {

		if (!isFinishing()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mProgress = ProgressDialog.show(MainActivity.this, getString(title_resID), null, true, cancelable, cancel_listener);
				}
			});
		}
	}

	public synchronized void hideProgress() {
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
