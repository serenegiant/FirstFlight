package jp.co.rediscovery.arflight;

/** カメラを操作可能なデバイス用のControllerメソッド定義(実質Bebop/Bebop2用) */
public interface CameraControllerListener {
	/**
	 * カメラの方向が変化した
	 * @param controller
	 * @param pan
	 * @param tilt
	 */
	public void onCameraOrientationChanged(final IDeviceController controller, final int pan, final int tilt);
}
