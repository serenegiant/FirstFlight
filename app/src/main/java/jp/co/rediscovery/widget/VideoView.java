package jp.co.rediscovery.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * 背景・デバイス3Dモデル表示用View
 * 今はライブ映像が取得できる場合にそれを表示するのに使うだけでデバイス3Dモデル・背景の描画は未実装
 * */
public class VideoView extends TextureView {
	public VideoView(final Context context) {
		this(context, null, 0);
	}

	public VideoView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setEnableVideo(final boolean enable) {
		// FIXME 未実装
	}

	public void hasGuard(final boolean has_guard) {
		// FIXME 未実装
	}

	/**
	 * デバイス姿勢をセット
	 * @param roll  左右の傾き[-100,100] => 今は[-30,+30][度]に対応
	 * @param pitch 前後の傾き(機種の上げ下げ)[-100,100] => 今は[-30,+30][度]に対応
	 * @param yaw 水平回転[-180,+180][度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz) {
		// FIXME 未実装
	}

	public void setAxis(final int axis) {
		// 表示中のデバイス3Dモデルの回転方向を変える
		// FIXME 未実装
	}

	public void startEngine() {
		// FIXME 未実装 デバイス3Dモデルの表示で羽根を回転させる
	}

	public void stopEngine() {
		// FIXME 未実装 デバイス3Dモデルの表示で羽根の回転を停止させる
	}

}
