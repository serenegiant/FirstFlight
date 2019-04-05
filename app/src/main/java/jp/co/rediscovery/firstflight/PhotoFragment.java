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

import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.serenegiant.mediastore.MediaStoreImageAdapter;

/** 端末内の静止画を表示するためのFragment */
public class PhotoFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = PhotoFragment.class.getSimpleName();

	private static final String KEY_FILE_ID = "PhotoFragment_KEY_FILE_ID";

	private ViewPager mViewPager;
	private MediaStoreImageAdapter mAdapter;
	private long mId;

	public static PhotoFragment newInstance(final long id) {
		PhotoFragment fragment = new PhotoFragment();
		final Bundle args = new Bundle();
		args.putLong(KEY_FILE_ID, id);
		fragment.setArguments(args);
		return fragment;
	}

	public PhotoFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
		final ViewGroup container, final Bundle savedInstanceState) {

		loadArguments(savedInstanceState);

		final View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	protected void loadArguments(final Bundle savedInstanceState) {
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		mId = args.getLong(KEY_FILE_ID);
	}

	private void initView(final View rootView) {
		mViewPager = rootView.findViewById(R.id.viewpager);
		mViewPager.setKeepScreenOn(true);
		mAdapter = new MediaStoreImageAdapter(getActivity(), R.layout.grid_item_media, false);
		// MediaStoreImageAdapterのCursorクエリーは非同期で実行されるので
		// 生成直後はアイテム数が0になるのでクエリー完了時にViewPager#setAdapterを実行する
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				mViewPager.setAdapter(mAdapter);
				mViewPager.setCurrentItem(mAdapter.getItemPositionFromID(mId));
				mAdapter.unregisterDataSetObserver(this);	// 初回だけでOKなので登録解除する
			}
			@Override
			public void onInvalidated() {
				super.onInvalidated();
			}
		});
		mAdapter.startQuery();	// 非同期クエリー開始
	}
}
