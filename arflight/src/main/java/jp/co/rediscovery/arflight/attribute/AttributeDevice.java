package jp.co.rediscovery.arflight.attribute;
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

/** デバイス情報の保持クラス */
public class AttributeDevice {
	/** デバイス製品名 */
	private String mProductName;

	/**
	 * デバイス製品名をセット
	 * @param name
	 */
	public void setProductName(final String name) {
		mProductName = name;
	}

	/** デバイス製品名を取得 */
	public String productName() {
		return mProductName;
	}

	/** デバイスのソフトウエアバージョン */
	private String mProductSoftware;
	/** デバイスのハードプエあバージョン */
	private String mProductHardware;

	/**
	 * ソフトウエア/ハードウエアバージョンをセット
	 * @param software	ソフトウエアバージョン
	 * @param hardware	ハードウエアバージョン
	 */
	public void setProduct(final String software, final String hardware) {
		mProductSoftware = software;
		mProductHardware = hardware;
	}

	/**
	 * ソフトウエアバージョンを取得
	 * @return
	 */
	public String productSoftware() {
		return mProductSoftware;
	}

	/**
	 * ハードウエアバージョンを取得
	 * @return
	 */
	public String productHardware() {
		return mProductHardware;
	}

	/** シリアル番号(上位・下位) */
	private String mSerialHigh, mSerialLow;

	/**
	 * シリアル番号の下位をセット
	 * @param low
	 */
	public void setSerialLow(final String low) {
		mSerialLow = low;
	}

	/**
	 * シリアル番号の上位をセット
	 * @param high
	 */
	public void setSerialHigh(final String high) {
		mSerialHigh = high;
	}

	/**
	 * シリアル番号を取得
	 * @return
	 */
	public String getSerial() {
		return mSerialHigh + mSerialLow;
	}
}
