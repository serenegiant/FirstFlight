package jp.co.rediscovery.firstflight;

import android.app.Fragment;

import jp.co.rediscovery.arflight.InstructionsFragment;

/**
 * Created by saki on 2016/10/15.
 *
 */
public class MyInstructionsFragment extends InstructionsFragment {

	public static InstructionsFragment newInstance() {
		final MyInstructionsFragment fragment = new MyInstructionsFragment();
		fragment.setInstructions(R.string.instruction1);
		return fragment;
	}

	@Override
	protected Fragment getConnectionFragment() {
		return new ConnectionFragment();
	}
}
