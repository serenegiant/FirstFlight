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

import android.util.SparseArray;
import android.util.SparseIntArray;

public class AttributeUSBAccessory {
	public static class LightState {
		public int state;
		public int intensity;
	}

	private final SparseArray<LightState> mLightStates = new SparseArray<LightState>();
	private final SparseIntArray mClawStates = new SparseIntArray();
	private final SparseIntArray mGunStates = new SparseIntArray();

	public synchronized void lightState(final int id, final int state, final int intensity) {
		LightState sts = mLightStates.get(id);
		if (sts == null) {
			sts = new LightState();
		}
		sts.state = state;
		sts.intensity = intensity;
	}

	public synchronized boolean hasLight() {
		return mLightStates.size() > 0;
	}

	public synchronized int lightId() {
		return mLightStates.size() > 0 ? mLightStates.keyAt(0) : 0;
	}

	public synchronized LightState lightState(final int id) {
		return mLightStates.get(id);
	}

	public synchronized boolean hasClaw() {
		return mClawStates.size() > 0;
	}

	public synchronized int clawId() {
		return mClawStates.size() > 0 ? mClawStates.keyAt(0) : 0;
	}

	public synchronized void clawState(final int id, final int state) {
		mClawStates.put(id, state);
	}

	public synchronized int clawState() {
		return mClawStates.get(0, 0);
	}

	public synchronized int clawState(final int id) {
		return mClawStates.get(id, 0);
	}

	public synchronized boolean hasGun() {
		return mGunStates.size() > 0;
	}

	public synchronized int gunId() {
		return mGunStates.size() > 0 ? mGunStates.keyAt(0) : 0;
	}

	public synchronized  void gunState(final int id, final int state) {
		mGunStates.put(id, state);
	}

	public synchronized int gunState() {
		return mGunStates.get(0, 0);
	}

	public synchronized int gunState(final int id) {
		return mGunStates.get(id, 0);
	}
}
