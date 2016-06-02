package jp.co.rediscovery.firstflight;

import android.app.Fragment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.serenegiant.media.MediaStoreAdapter;
import com.serenegiant.media.MediaStoreHelper;

/** 端末内の静止画・動画一覧を表示するためのFragment */
public class GalleyFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = GalleyFragment.class.getSimpleName();

	public static GalleyFragment newInstance() {
		GalleyFragment fragment = new GalleyFragment();
		return fragment;
	}

	private GridView mGalleyGridView;
	private MediaStoreAdapter mMediaStoreAdapter;

	public GalleyFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_galley, container, false);
		initView(rootView);
		return rootView;
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {
		mGalleyGridView = (GridView)rootView.findViewById(R.id.media_gridview);
		mMediaStoreAdapter = new MediaStoreAdapter(getActivity(), R.layout.grid_item_media);
		mGalleyGridView.setAdapter(mMediaStoreAdapter);
		mGalleyGridView.setOnItemClickListener(mOnItemClickListener);
	}

	private final AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
			switch (parent.getId()) {
			case R.id.media_gridview:
				doPlay(position, id);
				break;
			}
		}
	};

	private void doPlay(final int position, final long id) {
		final MediaStoreHelper.MediaInfo info = mMediaStoreAdapter.getMediaInfo(position);
		if (DEBUG) Log.v(TAG, "" + info);
		final Fragment fragment;
		switch (info.mediaType) {
		case MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE:
			// 静止画を選択した時
			fragment = PhotoFragment.newInstance(id);
			break;
		case MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO:
			// 動画を選択した時
			fragment = PlayerFragment.newInstance(info.data);
			break;
		default:
			fragment = null;
			break;
		}
		replace(fragment);
	}
}
