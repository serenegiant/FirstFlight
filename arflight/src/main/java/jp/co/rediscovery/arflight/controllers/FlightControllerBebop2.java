package jp.co.rediscovery.arflight.controllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

/**
 * Bebop2用のFlightController
 * FlightControllerBebopのシノニム
 */
public class FlightControllerBebop2 extends FlightControllerBebop {
	public FlightControllerBebop2(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}
}
