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

/** WiFi経由で接続するデバイス用の追加メソッド定義用のインターフェース */
public interface IWiFiController {
	/**
	 * 国コードを設定する　
	 * @param code 国コード(ISO 3166形式, 空文字列は国が不明であることを意味する)
	 * @return
	 */
	public boolean sendCountryCode(final String code);

	/**
	 * 自動国選択モードを使うかどうかを設定する
	 * @param auto
	 * @return
	 */
	public boolean sendAutomaticCountry(final boolean auto);
	/**
	 * 室内モードか屋外モードかをセット
	 * 日本だと外で飛ばす時は屋外モードにして2.4GHz帯を使わんと電波法違反になるんやけど
	 * 初期設定は5GHz帯を使うようになっとる場合があるから要注意や。
	 * つまり外で飛ばすならまず室内で屋外モードにしてから外に持っていかなあかんちゅうことやで
	 * @param outdoor
	 * @return
	 */
	public boolean sendSettingsOutdoor(final boolean outdoor);
	public boolean isOutdoor();
}
