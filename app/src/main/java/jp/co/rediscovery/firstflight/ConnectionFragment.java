package jp.co.rediscovery.firstflight;

import android.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import jp.co.rediscovery.arflight.ARDeviceServiceAdapter;
import jp.co.rediscovery.arflight.ManagerFragment;

public class ConnectionFragment extends BaseConnectionFragment {

	public static ConnectionFragment newInstance() {
		return new ConnectionFragment();
	}

	public ConnectionFragment() {
		super();
	}

	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			fragment = getFragment(position, true);
			break;
		case R.id.download_button:
			fragment = getFragment(position, false);
			break;
		case R.id.gallery_button:
			fragment = GalleyFragment.newInstance();
			break;
		case R.id.auto_button:
		{
			final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
			final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
			final String itemValue = adapter.getItemName(position);
			final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
			if (device != null) {
				// 製品名を取得
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());

				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
					fragment = AutoPilotFragment.newInstance(device, null, "bebop", AutoPilotFragment.MODE_TRACE);
					break;
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
					fragment = AutoPilotFragment.newInstance(device, null, "bebop2", AutoPilotFragment.MODE_TRACE);
					break;
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyControllerNewAPI
					fragment = newBridgetFragment(device);
					break;
				default:
					Toast.makeText(getActivity(), R.string.unsupported_product, Toast.LENGTH_SHORT).show();
					break;
				}
			}
			break;
		}
		}
		replace(fragment);
	}

	protected boolean onLongClick(final View view, final int position) {
		return false;
	}

	@Override
	protected BaseBridgeFragment newBridgetFragment(final ARDiscoveryDeviceService device) {
		return BridgeFragment.newInstance(device);
	}

}
