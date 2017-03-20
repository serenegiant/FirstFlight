package jp.co.rediscovery.arflight.controllers;
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

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

public class FlightControllerMambo extends FlightControllerMiniDrone {
	public FlightControllerMambo(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}

	@Override
	public boolean hasClaw() {
		return super.hasClaw();
	}

	@Override
	public boolean isClawClosed() {
		return super.isClawClosed();
	}

	@Override
	public boolean isClawClosed(final int id) {
		return super.isClawClosed(id);
	}

	@Override
	public boolean requestClawClose() {
		return super.requestClawClose();
	}

	@Override
	public boolean requestClawClose(final int id) {
		return super.requestClawClose(id);
	}

	@Override
	public boolean requestClawOpen() {
		return super.requestClawOpen();
	}

	@Override
	public boolean requestClawOpen(final int id) {
		return super.requestClawOpen(id);
	}

	@Override
	public boolean requestClawOpenClose(final int id, final boolean open) {
		return super.requestClawOpenClose(id, open);
	}

	public void toggleClaw() {
		super.toggleClaw();
	}

	public void toggleClaw(final int id) {
		super.toggleClaw(id);
	}
//--------------------------------------------------------------------------------
	@Override
	public boolean hasGun() {
		return super.hasGun();
	}

	@Override
	public boolean requestFireGun() {
		return super.requestFireGun();
	}

	@Override
	public boolean requestFireGun(final int id) {
		return super.requestFireGun(id);
	}
}
