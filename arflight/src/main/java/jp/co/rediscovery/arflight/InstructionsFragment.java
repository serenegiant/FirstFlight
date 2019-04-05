package jp.co.rediscovery.arflight;
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

import android.animation.Animator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.serenegiant.utils.ViewAnimationHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.fragment.app.Fragment;
import jp.co.rediscovery.widget.DroneNoticeView;

/**
 * Created by saki on 2016/10/14.
 *
 */
public abstract class InstructionsFragment extends Fragment {
	private static final boolean DEBUG = true;
	private static final String TAG = InstructionsFragment.class.getSimpleName();

	private static final String PREF_NAME = "instructions";
	private static final String PREF_KEY_FIRST_TIME = "app_key_first_time";
	private static final int MIN_INDEX_BEFORE_SKIP = 5;
	private static final int MAX_INDEX = 34;

	@StringRes private int mInstructionResId = R.string.instruction1;
	private boolean mFirstTime;
	private View mInstructionsView;
	private TextView mInstructionTv;
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

		final SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, 0);
		mFirstTime = pref.getBoolean(PREF_KEY_FIRST_TIME, true);

		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		if (args != null) {
			mInstructionResId = args.getInt("INSTRUCTION_RES_ID", R.string.instruction1);
		}
		initView(rootView);
		return rootView;
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (!mFirstTime) {
			// 5秒後にフェードアウトさせる
			ViewAnimationHelper.fadeOut(mDroneNoticeView, 0, 5000, mViewAnimationListener);
		}
	}

	protected void setInstructions(@StringRes final int instructionResId) {
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
		}
		args.putInt(PREF_KEY_FIRST_TIME, instructionResId);
		setArguments(args);
	}

	private void initView(final View rootView) {
		mInstructionsView = rootView.findViewById(R.id.instructions_view);
		mInstructionsView.setVisibility(mFirstTime ? View.VISIBLE : View.INVISIBLE);
		mInstructionTv = (TextView)rootView.findViewById(R.id.instructions_textview);

		mDroneNoticeView = (DroneNoticeView)rootView.findViewById(R.id.drone_notice_view);
		mDroneNoticeView.setVisibility(mFirstTime ? View.INVISIBLE : View.VISIBLE);
		mDroneNoticeView.setOnClickListener(mOnClickListener);
		// 自動で切り替えないように切替時間を0にセット
		mDroneNoticeView.setAutoNextDuration(0);

		mSkipBtn = (Button)rootView.findViewById(R.id.skip_button);
		mSkipBtn.setOnClickListener(mOnClickListener);
		mSkipBtn.setVisibility(View.INVISIBLE);

		mNextBtn = (Button)rootView.findViewById(R.id.next_button);
		mNextBtn.setOnClickListener(mOnClickListener);
		mNextBtn.setVisibility(View.INVISIBLE);

		mPrevBtn = (Button)rootView.findViewById(R.id.prev_button);
		mPrevBtn.setOnClickListener(mOnClickListener);
		mPrevBtn.setVisibility(View.INVISIBLE);

		updateInstructions(0);
	}

	private void updateButtons() {
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
			mSkipBtn.setVisibility(View.INVISIBLE);
			mNextBtn.setVisibility(View.INVISIBLE);
			mPrevBtn.setVisibility(View.INVISIBLE);
		}
	}

	private String getInstructions(@StringRes final int id) {
		final String appName = getString(R.string.app_name);
		final String text = getString(id);
		if (!TextUtils.isEmpty(text)) {
			final Pattern p = Pattern.compile("%s");
			final Matcher m = p.matcher(text);
			return m.replaceAll(appName);
		} else {
			return appName;
		}
	}

	private void updateInstructions(final int index) {
		final boolean prev = mInstructionIndex > index;
		mInstructionIndex = index;
		if (mFirstTime) {
			switch (mInstructionIndex) {
			case -1:
				finishApp();
				break;
			case MAX_INDEX:
				finishInstructions();
				break;
			case 0:
				mInstructionsView.setVisibility(View.VISIBLE);
				mInstructionTv.setText(getInstructions(mInstructionResId));
				mDroneNoticeView.setVisibility(View.INVISIBLE);
				break;
			case 1:
				mInstructionsView.setVisibility(View.VISIBLE);
				mInstructionTv.setText(getInstructions(R.string.instruction3));
				mDroneNoticeView.setVisibility(View.INVISIBLE);
				break;
			case 2:
				mInstructionsView.setVisibility(View.INVISIBLE);
				mDroneNoticeView.setVisibility(View.VISIBLE);
				mDroneNoticeView.reset();
				break;
			default:
				if (prev) {
					mDroneNoticeView.prev();
				} else {
					mDroneNoticeView.next();
				}
				break;
			}
		} else {
			mDroneNoticeView.nextRandom();
		}
		updateButtons();
	}

	protected abstract Fragment getConnectionFragment();

	private void finishInstructions() {
		final Activity activity = getActivity();
		if ((activity == null) || activity.isFinishing()) return;
		final SharedPreferences pref = activity.getSharedPreferences(PREF_NAME, 0);
		pref.edit().putBoolean(PREF_KEY_FIRST_TIME, false).apply();
		final Fragment fragment = getConnectionFragment();
		getFragmentManager().beginTransaction()
			.add(R.id.container, fragment)
			.remove(this)
			.commit();
	}

	private void finishApp() {
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			final SharedPreferences pref = activity.getSharedPreferences(PREF_NAME, 0);
			pref.edit().remove(PREF_KEY_FIRST_TIME).apply();
			activity.finish();
		}
	}

	private final View.OnClickListener mOnClickListener
		= new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			int id = view.getId();
			if (id == R.id.skip_button) {
				finishInstructions();
			} else if (id == R.id.next_button) {
				updateInstructions(mInstructionIndex + 1);
			} else if (id == R.id.prev_button) {
				updateInstructions(mInstructionIndex - 1);
			} else if (id == R.id.drone_notice_view) {
				if (!mFirstTime || (mInstructionIndex > MIN_INDEX_BEFORE_SKIP)) {
					finishInstructions();
				} else {
					updateInstructions(mInstructionIndex + 1);
				}
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
			int id = target.getId();
			if (id == R.id.drone_notice_view) {
				finishInstructions();

			}
		}

		@Override
		public void onAnimationCancel(@NonNull final Animator animator, @NonNull final View target, final int animationType) {
		}
	};

}
