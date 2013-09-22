//-----------------------------------------------------------------------------
//    
//    This file is part of XPMB.
//
//    XPMB is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    XPMB is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with XPMB.  If not, see <http://www.gnu.org/licenses/>.
//
//-----------------------------------------------------------------------------

package com.raddstudios.xpmb.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.raddstudios.xpmb.XPMBServices;
import com.raddstudios.xpmb.XPMBServices.MediaPlayerControl;
import com.raddstudios.xpmb.XPMBServices.ObjectCollections;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;

@SuppressLint("Registered")
public class XPMB_Activity extends Activity {

	public interface IntentFinishedListener {
		public void onFinished(Intent intent);
	}

	private XPMBServices bgHolder = null;
	private IntentFinishedListener cIntentWaitListener = null;
	private boolean mIsBound = false;

	public XPMB_Activity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		doBindService();
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			bgHolder = ((XPMBServices.LocalBinder) service).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			bgHolder = null;
		}
	};

	private void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		mIsBound = bindService(new Intent(this, XPMBServices.class), mConnection,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	public XPMB_ImageView getCustomBGView() {
		return null;
	}

	public void lockKeys(boolean locked) {
	}

	public void showLoadingAnim(boolean showAnim) {
	}

	public void preloadSubmenu(String submenu) {
	}

	public void requestUnloadSubmenu() {
	}

	public void requestActivityEnd() {
	}

	public void enableTouchEvents(boolean enabled) {
	}

	public void setTouchedChildView(View v) {
	}

	public boolean isActivityAvailable(Intent intent) {
		final PackageManager packageManager = getBaseContext().getPackageManager();
		ResolveInfo resolveInfo = packageManager.resolveActivity(intent,
				PackageManager.GET_ACTIVITIES);
		if (resolveInfo != null) {
			return true;
		}
		return false;
	}

	public void postIntentStartWait(IntentFinishedListener listener, Intent intent) {
		cIntentWaitListener = listener;
		startActivityForResult(intent, 0);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (cIntentWaitListener != null) {
			cIntentWaitListener.onFinished(data);
		}
	}

	public ObjectCollections getStorage() {
		return bgHolder.getObjectCollectionsService();
	}

	public MediaPlayerControl getPlayerControl() {
		return bgHolder.getMediaPlayerService();
	}
}
