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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;

import com.serenegiant.utils.BuildCheck;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class ConfirmDialog extends BaseDialogFragment {
	private static final String TAG = "ConfirmDialog";

	public static ConfirmDialog showDialog(final FragmentActivity parent, final int id, final String title, final String message) {
		ConfirmDialog fragment = newInstance(id, title, message);
		try {
			fragment.show(parent.getSupportFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static ConfirmDialog showDialog(final Fragment parent, final int id, final String title, final String message) {
		ConfirmDialog fragment = newInstance(id, title, message);
		fragment.setTargetFragment(parent, parent.getId());
		try {
  			fragment.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			fragment = null;
		}
		return fragment;
	}

	public static ConfirmDialog newInstance(final int id, final String title, final String message) {
		final ConfirmDialog fragment = new ConfirmDialog();
		fragment.setArguments(saveArgument(id, title, message));
		return fragment;
	}

	private OnDialogResultIntListener mListener;

	public ConfirmDialog() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(@NonNull final Context context) {
		super.onAttach(context);
        // コールバックインターフェースを取得
    	try {
    		// 親がフラグメントの場合
			mListener = (OnDialogResultIntListener)getTargetFragment();
    	} catch (final NullPointerException e1) {
			// ignore
    	} catch (final ClassCastException e) {
			// ignore
    	}
		if ((mListener == null) && BuildCheck.isAndroid4_2())
    	try {
    		// 親がフラグメントの場合
			mListener = (OnDialogResultIntListener)getParentFragment();
    	} catch (final NullPointerException e1) {
			// ignore
    	} catch (final ClassCastException e) {
			// ignore
    	}
        if (mListener == null)
        try {
        	// 親がActivityの場合
			mListener = (OnDialogResultIntListener)context;
        } catch (final ClassCastException e) {
			// ignore
    	} catch (final NullPointerException e1) {
    		// ignore
        }
		if (mListener == null) {
        	throw new ClassCastException(context.toString() + " must implement OnDialogResultIntListener");
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 通常起動の場合はsavedInstanceState==null,
		// システムに破棄されたのが自動生成した時は
		// onSaveInstanceStateで保存した値が入ったBundleオブジェクトが入っている
		loadArgument(savedInstanceState);
	}

	@NonNull
	@Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    if (!TextUtils.isEmpty(mTitle))
	    	builder.setTitle(mTitle);
	    if (!TextUtils.isEmpty(mMessage))
	    	builder.setMessage(Html.fromHtml(mMessage));
	    builder.setPositiveButton(android.R.string.ok, mOnDialogClickListener);
	    builder.setNegativeButton(android.R.string.cancel , mOnDialogClickListener);
		final Dialog dialog = builder.create();
        return dialog;
	}

	/**
	 * ダイアログでボタンがクリックされた時の処理
	 * OKボタンが押された時はwhich=DialogInterface.BUTTON_POSITIVE
	 * Cancelボタンが押された時はwhich=DialogInterface.BUTTON_NEGATIVE
	 */
	private final DialogInterface.OnClickListener mOnDialogClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (mListener != null) {
				mListener.onDialogResult(dialog, mRequestID, which);
			}
		}
	};

}
