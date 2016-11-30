package jp.co.rediscovery.arflight;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2016, saki t_saki@serenegiant.com
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

/** スカイコントローラーからのコールバックリスナー */
public interface SkyControllerListener extends DeviceConnectionListener {
	/**
	 * 接続した時のコールバック
	 * @param controller
	 */
	public void onSkyControllerConnect(final IDeviceController controller);
	/**
	 * 切断された時のコールバック
	 */
	public void onSkyControllerDisconnect(final IDeviceController controller);
	/**
	 * 電池残量が変化した時のコールバック
	 * @param controller
	 * @param percent
	 */
	public void onSkyControllerUpdateBattery(final IDeviceController controller, final int percent);
	/**
	 * 機器からの異常通知時のコールバック
	 * @param controller
	 * @param alarm_state
	 * 0: No alert, 1:User emergency alert, 2:Cut out alert, 3:Critical battery alert, 4:Low battery alert
	 */
	public void onSkyControllerAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state);

	/**
	 * キャリブレーションが必要かどうかが変更された
	 * @param need_calibration
	 */
	public void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration);

	/**
	 * キャリブレーションを開始/終了した
	 * @param isStart
	 */
	public void onSkyControllerCalibrationStartStop(final IDeviceController controller, final boolean isStart);
	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	public void onSkyControllerCalibrationAxisChanged(final IDeviceController controller, final int axis);
}
