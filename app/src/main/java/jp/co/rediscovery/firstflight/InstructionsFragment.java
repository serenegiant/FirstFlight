package jp.co.rediscovery.firstflight;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import jp.co.rediscovery.widget.DroneNoticeView;

/**
 * Created by saki on 2016/10/14.
 *
 */
public class InstructionsFragment extends BaseFragment {
	private static final boolean DEBUG = true;
	private static final String TAG = InstructionsFragment.class.getSimpleName();

	public static InstructionsFragment newInstance() {
		return new InstructionsFragment();
	}

	private static final int MIN_INDEX_BEFORE_SKIP = 5;
	private static final int MAX_INDEX = 10;

	private boolean mFirstTime;
	private View mInstructionsView;
	private DroneNoticeView mDroneNoticeView;
	private Button mSkipBtn, mNextBtn, mPrevBtn;
	private int mInstructionIndex;
	public InstructionsFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_instructions, container, false);
		final SharedPreferences pref = getActivity().getSharedPreferences(AppConst.PREF_NAME, 0);
		mFirstTime = pref.getBoolean(AppConst.APP_KEY_FIRST_TIME, true);
		initView(rootView);
		return rootView;
	}

	private void initView(final View rootView) {
		mInstructionsView = rootView.findViewById(R.id.instructions_view);
		mInstructionsView.setVisibility(mFirstTime ? View.VISIBLE : View.INVISIBLE);

		mDroneNoticeView = (DroneNoticeView)rootView.findViewById(R.id.drone_notice_view);
		mDroneNoticeView.setVisibility(mFirstTime ? View.INVISIBLE : View.VISIBLE);
		mDroneNoticeView.setOnClickListener(mOnClickListener);

		mSkipBtn = (Button)rootView.findViewById(R.id.skip_button);
		mSkipBtn.setOnClickListener(mOnClickListener);
		mSkipBtn.setVisibility(View.INVISIBLE);

		mNextBtn = (Button)rootView.findViewById(R.id.next_button);
		mNextBtn.setOnClickListener(mOnClickListener);
		mNextBtn.setVisibility(View.INVISIBLE);

		mPrevBtn = (Button)rootView.findViewById(R.id.prev_button);
		mPrevBtn.setOnClickListener(mOnClickListener);
		mPrevBtn.setVisibility(View.INVISIBLE);

		updateButton();
	}

	private void updateButton() {
		if (mFirstTime) {
			switch (mInstructionIndex) {
			case 0:
				mSkipBtn.setVisibility(View.INVISIBLE);
				mNextBtn.setVisibility(View.VISIBLE);
				mNextBtn.setText(R.string.agree);
				mPrevBtn.setVisibility(View.VISIBLE);
				mPrevBtn.setText(R.string.disagree);
				break;
			case MAX_INDEX:
				mSkipBtn.setVisibility(View.VISIBLE);
				mSkipBtn.setText(R.string.skip);
				mNextBtn.setVisibility(View.INVISIBLE);
				mPrevBtn.setVisibility(View.VISIBLE);
				break;
			default:
				if (mInstructionIndex > MIN_INDEX_BEFORE_SKIP) {
					mSkipBtn.setVisibility(View.VISIBLE);
					mSkipBtn.setText(R.string.skip);
					mNextBtn.setVisibility(View.VISIBLE);
					mNextBtn.setText(R.string.next);
					mPrevBtn.setVisibility(View.VISIBLE);
					mPrevBtn.setText(R.string.prev);
				} else {
					mSkipBtn.setVisibility(View.INVISIBLE);
					mNextBtn.setVisibility(View.VISIBLE);
					mNextBtn.setText(R.string.next);
					mPrevBtn.setVisibility(View.VISIBLE);
					mPrevBtn.setText(R.string.prev);
				}
			}
		} else {
			mSkipBtn.setVisibility(View.VISIBLE);
			mNextBtn.setVisibility(View.VISIBLE);
			mNextBtn.setText(R.string.next);
			mPrevBtn.setVisibility(View.INVISIBLE);
		}
	}

	private void nextInstructions() {
		if (mFirstTime) {
			if (++mInstructionIndex > MAX_INDEX) {
				finishInstructions();
	//		} else {
	//			// FIXME ここで表示内容を更新する
			}
		} else {
			mDroneNoticeView.next();
		}
		updateButton();
	}

	private void prevInstructions() {
		if (mFirstTime) {
			if (--mInstructionIndex < 0) {
				finishApp();
	//		} else {
	//			// FIXME ここで表示内容を更新する
			}
		} else {
			mDroneNoticeView.prev();
		}
		updateButton();
	}

	private void finishInstructions() {
		if (!DEBUG) {
			final SharedPreferences pref = getActivity().getSharedPreferences(AppConst.PREF_NAME, 0);
			pref.edit().putBoolean(AppConst.APP_KEY_FIRST_TIME, false).apply();
		}
		final Fragment fragment = ConnectionFragment.newInstance();
		getFragmentManager().beginTransaction()
			.add(R.id.container, fragment)
			.remove(this)
			.commit();
	}

	private void finishApp() {
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			activity.finish();
		}
	}

	private final View.OnClickListener mOnClickListener
		= new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.skip_button:
				finishInstructions();
				break;
			case R.id.next_button:
				nextInstructions();
				break;
			case R.id.prev_button:
				prevInstructions();
				break;
			case R.id.drone_notice_view:
				mDroneNoticeView.nextRandom();
				break;
			}
		}
	};
}
