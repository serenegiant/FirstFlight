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
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
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

/**
 * IDeviceController, IFlightControllerからのからのコールバックリスナー
 * 飛行状態が変化した時
 */
public interface FlightControllerListener extends DeviceConnectionListener {
	/**
	 * 飛行状態が変化した時のコールバック
	 * @param controller
	 * @param state
	 * 0: Landed state, 1:Taking off state, 2:Hovering state, 3:Flying state
	 * 4:Landing state, 5:Emergency state, 6:Rolling state
	 */
	public void onFlyingStateChangedUpdate(final IDeviceController controller, final int state);

	/**
	 * フラットトリム状態が変更された
	 * @param controller
	 */
	public void onFlatTrimChanged(final IDeviceController controller);

	/**
	 * キャリブレーションが必要かどうかが変更された
	 * @param controller
	 * @param need_calibration
	 */
	public void onCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration);

	/**
	 * キャリブレーションを開始/終了した
	 * @param controller
	 * @param isStart
	 */
	public void onCalibrationStartStop(final IDeviceController controller, final boolean isStart);
	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	public void onCalibrationAxisChanged(final IDeviceController controller, final int axis);

	/**
	 * 静止画撮影ステータスが変更された
	 * ホントはこれは飛行状態と直接関係ないから別に分けたほうがええけど
	 * @param controller
	 * @param state
	 */
	public void onStillCaptureStateChanged(final IDeviceController controller, final int state);

	/**
	 * 動画撮影ステータスが変更された
	 * ホントはこれは飛行状態と直接関係ないから別に分けたほうがええけど
	 * @param controller
	 * @param state
	 */
	public void onVideoRecordingStateChanged(final IDeviceController controller, final int state);

	/**
	 * デバイスのストレージ状態が変化した時にコールバック
	 * ホントはこれは飛行状態と直接関係ないから別に分けたほうがええけど
	 * @param controller
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	public void onUpdateStorageState(final IDeviceController controller, final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal);
}
