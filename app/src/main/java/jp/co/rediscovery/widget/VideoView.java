package jp.co.rediscovery.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import com.serenegiant.glutils.GLTextureView;

public class VideoView extends GLTextureView {
	public VideoView(final Context context) {
		this(context, null, 0);
	}

	public VideoView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VideoView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public SurfaceTexture getTexture() {
		// FIXME 未実装
		return null;
	}

	public void setEnableVideo(final boolean enable) {
		// FIXME 未実装
	}

	public void hasGuard(final boolean has_guard) {
		// FIXME 未実装
	}

	/**
	 * 機体姿勢をセット
	 * @param roll  左右の傾き[-100,100] => 今は[-30,+30][度]に対応
	 * @param pitch 前後の傾き(機種の上げ下げ)[-100,100] => 今は[-30,+30][度]に対応
	 * @param yaw 水平回転[-180,+180][度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz) {
		// FIXME 未実装
	}

	public void setAxis(final int axis) {
		// FIXME 未実装
	}

	public void startEngine() {
		// FIXME 未実装
	}

	public void stopEngine() {
		// FIXME 未実装
	}

}
