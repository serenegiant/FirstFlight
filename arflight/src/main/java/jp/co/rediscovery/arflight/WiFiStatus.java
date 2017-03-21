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

/** WiFiの接続状態保持用のクラス */
public class WiFiStatus {
	public final int txPower;
	public String ssid;
	public int rssi;
	public int band;	// 0: 2.4GHz, 1: 5GHz
	public int channel;

	public double factor = 2.0;

	public WiFiStatus(final int _txPower) {
		txPower = _txPower;
	}

// RSSI(受信信号強度[dbm])とTxPower(送信強度[dbm])とd(距離[m])の関係
//	RSSI = TxPower - 20 * log10(d)
//	d = 10 ^ ((TxPower - RSSI) / 20)
//	RSSI = TxPower - 10 * factor * log10(d)
//	(TxPower - RSSI) / (10 * factor) = log10(n)
//	factor = 2.0 : 障害物のない理想空間
//	factor < 2.0 : 電波が反射しながら伝搬する空間
//	factor > 2.0 : 障害物に吸収され減衰しながら伝搬する空間

	/**
	 * 概算で距離を計算してみる(電波状況・空間状況・送信強度全部わからへんからあまりあてにはならへんけど)
	 * @return
	 */
	public float distance() {
		try {
			return (float)Math.pow(10.0, (txPower - rssi) / (10 * factor));
		} catch (final Exception e) {
			return 0;
		}
	}
}
