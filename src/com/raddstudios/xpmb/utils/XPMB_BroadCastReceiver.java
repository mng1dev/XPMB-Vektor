package com.raddstudios.xpmb.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.raddstudios.xpmb.menus.XPMBSubmenu_ROM;
import com.raddstudios.xpmb.menus.XPMBSubmenu_ROM.XPMBSubmenuItem_ROM;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;

public class XPMB_BroadCastReceiver<item> extends BroadcastReceiver {
	private long enqueued;
	private DownloadManager mManager;
	private XPMB_Activity rootActivity;
	private XPMBSubmenuItem_ROM item;
	private XPMBSubmenu_ROM submenu;

	public XPMB_BroadCastReceiver(long enqueued, DownloadManager mManager,
			XPMB_Activity rootActivity, XPMBSubmenuItem_ROM item,
			XPMBSubmenu_ROM submenu) {
		this.enqueued = enqueued;
		Log.i("BroadcastReceiver", "DownID=" + enqueued);
		this.mManager = mManager;
		this.rootActivity = rootActivity;
		this.item = item;
		this.submenu = submenu;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			long downloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			if (downloadId == enqueued) {
				Query query = new Query();
				query.setFilterById(enqueued); // dlid
				Cursor c = mManager.query(query);
				if (c.moveToFirst()) {
					int columnIndex = c
							.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c
							.getInt(columnIndex)) {
						String uriString = c
								.getString(c
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						final Uri fileUri = Uri.parse(uriString);
						submenu.addDecodingJob(fileUri, item.getGameName(),
								item);
						c.close();
						return;
					}
				}
				c.close();
			}
		}
	}

}
