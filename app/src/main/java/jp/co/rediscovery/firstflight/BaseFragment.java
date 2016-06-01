package jp.co.rediscovery.firstflight;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ImageView;

import com.serenegiant.utils.HandlerThreadHandler;

import java.util.HashMap;
import java.util.Map;

public class BaseFragment extends Fragment {
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

}
