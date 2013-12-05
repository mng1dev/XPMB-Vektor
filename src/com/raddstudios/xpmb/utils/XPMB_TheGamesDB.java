package com.raddstudios.xpmb.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.raddstudios.xpmb.utils.Serialization.gameClass;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class XPMB_TheGamesDB {

	private static class XPMB_TheGamesDBClient {
		public static final AsyncHttpClient client = new AsyncHttpClient();
		private static final String BASEURL = "http://thegamesdb.net/api/";

		public static void get(String url, RequestParams params,
				AsyncHttpResponseHandler responseHandler) {
			client.get(getAbsoluteUrl(url), params, responseHandler);
		}

		private static String getAbsoluteUrl(String relativeUrl) {
			return BASEURL + relativeUrl;
		}
	}

	
	private static final int THRESHOLD = 50;
	private String platform;
	private String gameName;
	private Activity callerActivity;
	private File romRoot;

	public XPMB_TheGamesDB(File romRoot, Activity callerActivity,
			String gameName) {
		this(romRoot, callerActivity, gameName, null);
	}

	public XPMB_TheGamesDB(File romRoot, Activity callerActivity,
			String gameName, String platform) {
		this.romRoot = romRoot;
		this.gameName = gameName;
		this.platform = platform;
		this.callerActivity = callerActivity;
	}
	
	private static String xml = null;
	private void getXmlFromUrl(String url) {
		AsyncHttpResponseHandler resHandlerDir = new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Log.i("Success", "OK="+response);
				XPMB_TheGamesDB.xml = response;
			}
		};
		
		
		// return XML
		return;
		
	}
	


	private String getPlatform() {
		if (platform.equals("PCE"))
			return "TurboGrafx 16";
		else if (platform.equals("MD"))
			return "Sega Genesis";
		else if (platform.equals("SMS"))
			return "Sega Master System";
		else if (platform.equals("GBA"))
			return "Nintendo Game Boy Advance";
		else if (platform.equals("GBC"))
			return "Nintendo Game Boy Color";
		else if (platform.equals("GB"))
			return "Nintendo Game Boy";
		else if (platform.equals("NES"))
			return "Nintendo Entertainment System (NES)";
		else if (platform.equals("SNES"))
			return "Super Nintendo (SNES)";
		else if (platform.equals("NDS"))
			return "Nintendo DS";
		else if (platform.equals("PSX")) {
			return "Sony Playstation";
		} else
			return "Other";
	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) callerActivity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private int stringMatch(String romName, String dbName) {
		long start = System.currentTimeMillis();
		romName = Normalizer.normalize(romName, Form.NFD).replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");
		dbName = Normalizer.normalize(dbName, Form.NFD).replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");

		romName = romName.replaceAll("[^a-zA-Z0-9\\s]+", "");
		dbName = dbName.replaceAll("[^a-zA-Z0-9\\s]+", "");
		String[] rom = romName.split("\\s+");
		String[] db = dbName.split("\\s+");
		int match = 0;
		for (int i = 0; i < rom.length; i++) {
			for (int j = 0; j < db.length; j++) {

				if (rom[i].equalsIgnoreCase(db[j]))
					match++;
			}
		}

		int rate = (rom.length == db.length ? 1 : 0) + (match * 100)
				/ Math.max(rom.length, db.length);
		Log.i("Match", "ROMNAME=" + romName + " DBNAME=" + dbName + " ->"
				+ rate + "%. Time elapsed:"
				+ (System.currentTimeMillis() - start) + " ms.");
		return rate;

	}
	private Long dlid = null;
	public Long DownloadFromUrl() {
		if (this.isOnline()) {
			getCoverLink();
		}
		while(dlid==null){}
		return (dlid==-1?null:dlid);
	}
	private String coverURL = null;
	@SuppressWarnings("deprecation")
	private final void getCoverLink() {
		final long start = System.currentTimeMillis();
		
		final String cleanedGN = gameName.split("\\[")[0].split("\\(")[0];
		String URL = "GetGame.php?name="
				+ URLEncoder.encode(cleanedGN);
		if (null != this.platform && !this.platform.equalsIgnoreCase("other")) {
			URL += "&platform=" + URLEncoder.encode(getPlatform());
		}
		AsyncHttpResponseHandler resHandlerDir = new AsyncHttpResponseHandler() {
			@Override
			public void onFailure(Throwable error){
				dlid=(long) -1;
			}
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public void onSuccess(String response) {
				Log.i("Success", "OK");
				XPMB_TheGamesDB.xml = response;
				int matchRate = 0, bestMatchId = -1;
				ArrayList<gameClass> games = XPMB_GameSAXParser.parse(xml);
				for(int j=0;j<games.size();j++){
					int rate = stringMatch(games.get(j).getTitle(),cleanedGN);
					if(rate > matchRate && rate > XPMB_TheGamesDB.THRESHOLD){
						matchRate = rate; 
						bestMatchId = j;
						if(matchRate == 101) break;
					}
				}
				if(bestMatchId >-1) coverURL = XPMB_GameSAXParser.getBaseURL() + games.get(bestMatchId).getURL();
				int i = 0;
				i=bestMatchId;
				Log.i("Performance",
						"(" + gameName + ") Time="
								+ (System.currentTimeMillis() - start)
								+ "ms. Attempts=" + (i) + ". Result=" + coverURL);
				String DownloadUrl = (coverURL == null ? null : coverURL.replace(
						"http://", ""));
				if (DownloadUrl != null) {
					if (!romRoot.exists()) {
						romRoot.mkdirs();
					}
					DownloadUrl = ("http://" + Uri.encode(DownloadUrl)).replace(
							"%2F", "/");
					File file = new File(romRoot, gameName + "-CV.jpg");
					DownloadManager manager = (DownloadManager) callerActivity
							.getSystemService(Activity.DOWNLOAD_SERVICE);
					Request req = new Request(Uri.parse(DownloadUrl))
							.setNotificationVisibility(Request.VISIBILITY_VISIBLE)
							.setTitle(gameName)
							.setDescription(getPlatform())
							.setDestinationUri(Uri.fromFile(file))
							.setAllowedNetworkTypes(
									Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
					long dlId = manager.enqueue(req);
					dlid = dlId;
					//return dlId;
				}
				//return null;
				
			}
		};
		XPMB_TheGamesDBClient.get(URL, null,resHandlerDir);
	}
}
