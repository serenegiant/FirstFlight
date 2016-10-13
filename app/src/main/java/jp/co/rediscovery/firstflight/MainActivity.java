package jp.co.rediscovery.firstflight;

import android.animation.Animator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;
import com.serenegiant.gamepad.Joystick;

import jp.co.rediscovery.arflight.ManagerFragment;
import jp.co.rediscovery.widget.DroneNoticeView;

import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.utils.ViewAnimationHelper;

public class MainActivity extends AppCompatActivity {
	private static final boolean DEBUG = false;    // FIXME 実働時はfalseにすること
	private static String TAG = MainActivity.class.getSimpleName();

	static {
		ARSDK.loadSDKLibs();
//		ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_DEBUG);
		ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_ERROR);
	}

	/*package*/Joystick mJoystick;
	private DroneNoticeView mDroneNoticeView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		NetworkChangedReceiver.enable(getApplicationContext());
		final ManagerFragment manager = ManagerFragment.getInstance(this);
		if (savedInstanceState == null) {
			final Fragment fragment = ConnectionFragment.newInstance();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment).commit();
		}
		mJoystick = Joystick.getInstance(this);
		mDroneNoticeView = (DroneNoticeView)findViewById(R.id.drone_notice_view);
		mDroneNoticeView.setOnClickListener(mOnClickListener);
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
		showNoticeView();
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

	private void showNoticeView() {
		mDroneNoticeView.setVisibility(View.VISIBLE);
		// 自動で切り替えないように切替時間を0にセット
		mDroneNoticeView.setAutoNextDuration(0);
		mDroneNoticeView.next();
		// 10秒後にフェードアウトさせる
		ViewAnimationHelper.fadeOut(mDroneNoticeView, 0, 10000, mViewAnimationListener);
	}

	private final View.OnClickListener mOnClickListener
		= new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.drone_notice_view:
				// 直ぐにフェードアウトさせる
				ViewAnimationHelper.fadeOut(view, 0, 0, mViewAnimationListener);
				break;
			}
		}
	};

	private final ViewAnimationHelper.ViewAnimationListener
		mViewAnimationListener = new ViewAnimationHelper.ViewAnimationListener() {
		@Override
		public void onAnimationStart(@NonNull final Animator animator, @NonNull final View target, final int animationType) {
		}

		@Override
		public void onAnimationEnd(@NonNull final Animator animator, @NonNull final View target, final int animationType) {
			switch(target.getId()) {
			case R.id.drone_notice_view:
				target.setVisibility(View.GONE);
				break;
			}
		}

		@Override
		public void onAnimationCancel(@NonNull final Animator animator, @NonNull final View target, final int animationType) {
		}
	};
}
