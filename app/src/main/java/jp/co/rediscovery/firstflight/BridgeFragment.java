package jp.co.rediscovery.firstflight;

import android.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;

import jp.co.rediscovery.arflight.ARDeviceInfoAdapter;
import jp.co.rediscovery.arflight.ManagerFragment;

/**
 * スカイコントローラーに接続してスカイコントローラーが
 * 検出している機体の一覧取得＆選択を行うためのFragment
 */
public class BridgeFragment extends BaseBridgeFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = BridgeFragment.class.getSimpleName();

	public static BridgeFragment newInstance(final ARDiscoveryDeviceService device) {
		final BridgeFragment fragment = new BridgeFragment();
		fragment.setDevice(device);
		return fragment;
	}

	public BridgeFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			fragment = getFragment(mDeviceListView.getCheckedItemPosition(), true);
			break;
		case R.id.download_button:
			fragment = getFragment(mDeviceListView.getCheckedItemPosition(), false);
			break;
		case R.id.gallery_button:
			fragment = GalleyFragment.newInstance();
			break;
		case R.id.auto_button:
		{
			final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
			final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
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
				default:
					Toast.makeText(getActivity(), R.string.unsupported_product, Toast.LENGTH_SHORT).show();
					break;
				}
			}
			break;
		}
		}
		if (fragment != null) {
			replace(fragment);
		}
	}

	@Override
	protected boolean onLongClick(final View view) {
		return false;
	}

}
