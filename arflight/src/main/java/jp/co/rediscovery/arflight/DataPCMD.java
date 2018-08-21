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

/** 飛行制御値保持用クラス */
public class DataPCMD {
	/** 移動するかどうか */
	public int flag;
	/** デバイスの左右方向の傾き[-100,+100], デバイスの最大傾斜設定[度]に対する割合[%] */
	public float roll;
	/** デバイスの機首を上げ下げ[-100,+100], デバイスの最大傾斜設定[度]に対する割合[%] */
	public float pitch;
	/** デバイスの水平回転[-100,+100], デバイスの最大回転速度[度/秒]に対する割合[%] */
	public float yaw;
	/** 高度制御[100,+100], デバイスの垂直移動速度設定[m/秒]に対する割合[%] */
	public float gaz;
	/** 方位角、デバイス側で未実装  */
	public float heading;
	/** 送信要求フラグ */
	public boolean requestSend;

	/** コンストラクタ */
	public DataPCMD() {
		flag = 0;
		roll = pitch = yaw = gaz = heading = 0;
	}

	/** 指定した他のDataPCMDから値を取得してセットする */
	public void set(final DataPCMD other) {
		flag = other.flag;
		roll = other.roll;
		pitch = other.pitch;
		yaw = other.yaw;
		gaz = other.gaz;
		heading = other.heading;
	}
}
