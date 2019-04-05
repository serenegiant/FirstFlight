package jp.co.rediscovery.dialog;
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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import jp.co.rediscovery.firstflight.R;
import com.serenegiant.utils.BuildCheck;

import java.util.Locale;

public class TransferProgressDialogFragment extends BaseDialogFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "TransferProgressDialogFragment";

	public interface TransferProgressDialogListener {
		public void onCancel(final int requestID);
	}

	public static TransferProgressDialogFragment showDialog(final FragmentActivity parent, final String title, final String message) {
		TransferProgressDialogFragment fragment = newInstance(-1, title, message);
		try {
			fragment.show(parent.getSupportFragmentManager(), TAG);
			final Dialog dialog = fragment.getDialog();
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static TransferProgressDialogFragment showDialog(final Fragment parent, final String title, final String message) {
		TransferProgressDialogFragment fragment = newInstance(parent.getId(), title, message);
		fragment.setTargetFragment(parent, parent.getId());
		try {
			fragment.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static TransferProgressDialogFragment newInstance(final int requestID, final String title, final String message) {
		final TransferProgressDialogFragment fragment = new TransferProgressDialogFragment();
		final Bundle args = saveArgument(requestID, title, message);
		fragment.setArguments(args);
		fragment.setCancelable(false);
		return fragment;
	}

	private TransferProgressDialogListener mListener;

	public TransferProgressDialogFragment() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArgument(savedInstanceState);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(mTitle);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(false);
		return dialog;
	}

	@Override
	public void onAttach(@NonNull final Context context) {
		super.onAttach(context);
		if (DEBUG) Log.v(TAG, "onAttach:");
        // コールバックインターフェースを取得
    	try {
    		// 親がフラグメントの場合
			mListener = (TransferProgressDialogListener)getTargetFragment();
    	} catch (final NullPointerException e1) {
			// ignore
    	} catch (final ClassCastException e) {
			// ignore
    	}
        if ((mListener == null) && BuildCheck.isAndroid4_2())
    	try {
    		// 親がフラグメントの場合
			mListener = (TransferProgressDialogListener)getParentFragment();
    	} catch (final NullPointerException e1) {
			// ignore
    	} catch (final ClassCastException e) {
			// ignore
    	}
        if (mListener == null)
        try {
        	// 親がActivityの場合
			mListener = (TransferProgressDialogListener)context;
        } catch (final ClassCastException e) {
			// ignore
    	} catch (final NullPointerException e1) {
			// ignore
        }
		if (mListener == null) {
        	throw new ClassCastException(context.toString() + " must implement TransferProgressDialogListener");
		}
	}

	private ProgressBar progressbar1;
	private TextView progressTv1;
	private ProgressBar progressbar2;
	private TextView progressTv2;

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
		final ViewGroup container, final Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_transfer_progress, container, false);
		progressbar1 = rootView.findViewById(R.id.progressBar1);
		progressTv1 = rootView.findViewById(R.id.progress_textview1);
		progressbar2 = rootView.findViewById(R.id.progressBar2);
		progressTv2 = rootView.findViewById(R.id.progress_textview2);
		final Button button = rootView.findViewById(R.id.cancel_btn);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				doCancel();
			}
		});
		return rootView;
	}

	public void setProgress(final int current, final int total, final float progress) {
		if (progressbar1 != null) {
			progressbar1.setProgress((current * 100) / total);
		}
		if (progressbar2 != null) {
			progressbar2.setProgress((int) progress);
		}
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			activity.runOnUiThread(mProgressUpdateTask);
		}
	}

	private final Runnable mProgressUpdateTask = new Runnable() {
		@Override
		public void run() {
			if ((progressbar1 != null) && (progressTv1 != null)) {
				progressTv1.setText(String.format(Locale.US, "%d%%", progressbar1.getProgress()));
			}
			if ((progressbar2 != null) && (progressTv2 != null)) {
				progressTv2.setText(String.format(Locale.US,"%d%%", progressbar2.getProgress()));
			}
		}
	};

	@Override
	public void onCancel(@NonNull final DialogInterface dialog) {
		super.onCancel(dialog);
		doCancel();
	}

	private final void doCancel() {
		mListener.onCancel(mRequestID);
		dismiss();
	}
}
