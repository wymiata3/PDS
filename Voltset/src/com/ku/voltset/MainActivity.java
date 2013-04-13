package com.ku.voltset;

import java.util.Locale;

import com.ku.voltset.R;
import com.ku.voltset.services.HardwareController_service;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {
	Messenger mService = null;
	boolean mIsBound=false;
	String serial_number=null;
	private static final String TAG = "MainActivity";
	final Messenger mMessenger = new Messenger(new IncomingHandler());


	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String serial_number= this.getIntent().getStringExtra("serial_number");
		this.serial_number=serial_number;
		doBindService();
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		//TODO implement it
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		//TODO implement it
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment=null;
			if(position==0){
				fragment = new DIYFragment(	);
			}
			else if (position==1){
				fragment= new ProFragment();
			}
			else if(position==2){
				EduFragment edu= new EduFragment();
				edu.setSerial(serial_number);
				fragment=edu;
			}
			
//			Bundle args = new Bundle();
//			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	static class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HardwareController_service.MSG_SET_STRING_VALUE:
				String message = msg.getData().getString("serial");
				//TODO handle message = null	
				//TODO handle message = serial
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
	}
	//Connection between service and activity
		private ServiceConnection mConnection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				// This is called when the connection with the service has been
				// established, giving us the service object we can use to
				// interact with the service. We are communicating with our
				// service through an IDL interface, so get a client-side
				// representation of that from the raw service object.
				mService = new Messenger(service);
				Log.d(TAG, "Attached.");

				// We want to monitor the service for as long as we are
				// connected to it.
				try {
					Message msg = Message.obtain(null,
							HardwareController_service.MSG_REGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// In this case the service has crashed before we could even
					// do anything with it; we can count on soon being
					// disconnected (and then reconnected if it can be restarted)
					// so there is no need to do anything here.
				}

				// As part of the sample, tell the user what happened.
				Log.d(TAG, "Connected.");
			}

			public void onServiceDisconnected(ComponentName className) {
				// This is called when the connection with the service has been
				// unexpectedly disconnected -- that is, its process crashed.
				mService = null;
				Log.d(TAG, "Disconnected.");

			}
		};

		void doBindService() {
			// Establish a connection with the service. We use an explicit
			// class name because there is no reason to be able to let other
			// applications replace our component.
			bindService(new Intent(MainActivity.this, HardwareController_service.class),
					mConnection, Context.BIND_AUTO_CREATE);
			mIsBound = true;
			Log.d(TAG, "Binding.");
		}

		void doUnbindService() {
			if (mIsBound) {
				// If we have received the service, and hence registered with
				// it, then now is the time to unregister.
				if (mService != null) {
					try {
						Message msg = Message.obtain(null,
								HardwareController_service.MSG_UNREGISTER_CLIENT);
						msg.replyTo = mMessenger;
						mService.send(msg);
					} catch (RemoteException e) {
						// There is nothing special we need to do if the service
						// has crashed.
					}
				}

				// Detach our existing connection.
				unbindService(mConnection);
				mIsBound = false;
				Log.d(TAG, "MainActivity - unbinding.");
			}
		}
}
