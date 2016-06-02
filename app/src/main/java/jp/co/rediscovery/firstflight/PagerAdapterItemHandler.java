package jp.co.rediscovery.firstflight;

import android.view.View;

/** 設定画面の各ページを初期化するためのハンドラメソッド */
public interface PagerAdapterItemHandler {
	public void initialize(final BaseFragment parent, final View view);
}
