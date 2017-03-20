package jp.co.rediscovery.firstflight;
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

public class AppConst {
	public static final String APP_EXTRA_DEVICE_SERVICE = "APP_EXTRA_DEVICE_SERVICE";
	public static final String APP_EXTRA_DEVICE_INFO = "APP_EXTRA_DEVICE_INFO";

	public static final String APP_CONFIG_KEY_OPERATION_TYPE = "APP_CONFIG_OPERATION_TYPE";
	// ゲームパッド
	public static final String APP_CONFIG_KEY_GAMEPAD_SENSITIVITY = "APP_CONFIG_GAMEPAD_SENSITIVITY";
	public static final String APP_CONFIG_KEY_GAMEPAD_SCALE_X = "APP_CONFIG_GAMEPAD_SCALE_X";
	public static final String APP_CONFIG_KEY_GAMEPAD_SCALE_Y = "APP_CONFIG_GAMEPAD_SCALE_Y";
	public static final String APP_CONFIG_KEY_GAMEPAD_SCALE_Z = "APP_CONFIG_GAMEPAD_SCALE_Z";
	public static final String APP_CONFIG_KEY_GAMEPAD_SCALE_R = "APP_CONFIG_GAMEPAD_SCALE_R";
	// デバイス制御
	public static final String APP_CONFIG_KEY_MAX_CONTROL_VALUE = "APP_CONFIG_MAX_CONTROL_VALUE";
	public static final float APP_CONFIG_DEFAULT_MAX_CONTROL_VALUE = 100.0f;
	public static final String APP_CONFIG_KEY_SCALE_X = "APP_CONFIG_SCALE_X";
	public static final float APP_CONFIG_DEFAULT_SCALE_X = 1.0f;
	public static final String APP_CONFIG_KEY_SCALE_Y = "APP_CONFIG_SCALE_Y";
	public static final float APP_CONFIG_DEFAULT_SCALE_Y = 1.0f;
	public static final String APP_CONFIG_KEY_SCALE_Z = "APP_CONFIG_SCALE_Z";
	public static final float APP_CONFIG_DEFAULT_SCALE_Z = 1.0f;
	public static final String APP_CONFIG_KEY_SCALE_R = "APP_CONFIG_SCALE_R";
	public static final float APP_CONFIG_DEFAULT_SCALE_R = 1.0f;
	// スケール設定値を表示用に変換する際の係数
	public static final float APP_SCALE_FACTOR = 250f;
	public static final int APP_SCALE_OFFSET = 500;

}
