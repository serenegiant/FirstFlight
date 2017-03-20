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

public class AttributeFlightDuration {
	private int mFlightCounts;			// 飛行回数
	private int mFlightDuration;		// 飛行時間[秒]
	private int mTotalFlightDuration;	// 合計飛行時間[秒]


	public AttributeFlightDuration set(final AttributeFlightDuration other) {
		mFlightCounts = other != null ? other.mFlightCounts : 0;
		mFlightDuration = other != null ? other.mFlightDuration : 0;
		mTotalFlightDuration = other != null ? other.mTotalFlightDuration : 0;
		return this;
	}

	public AttributeFlightDuration set(final int counts, final int duration, final int total) {
		mFlightCounts = counts;
		mFlightDuration = duration;
		mTotalFlightDuration = total;
		return this;
	}

	public void counts(final int counts) {
		mFlightCounts = counts;
	}

	public int counts() {
		return mFlightCounts;
	}

	public void duration(final int duration) {
		mFlightDuration = duration;
	}

	public int duration() {
		return mFlightDuration;
	}

	public void total(final int total) {
		mTotalFlightDuration = total;
	}

	public int total() {
		return mTotalFlightDuration;
	}
 }
