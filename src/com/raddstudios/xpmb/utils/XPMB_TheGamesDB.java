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
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
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

	private String getXmlFromUrl(String url) {
		String xml = null;
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			xml = EntityUtils.toString(httpEntity);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// return XML
		return xml;
	}

	@SuppressWarnings("deprecation")
	private final String getCoverLink() {
		long start = System.currentTimeMillis();
		int i = 0;
		String coverURL = null;

		String cleanedGN = this.gameName.split("\\[")[0].split("\\(")[0];
		String URL = "http://thegamesdb.net/api/GetGamesList.php?name="
				+ URLEncoder.encode(cleanedGN);
		if (null != this.platform && !this.platform.equalsIgnoreCase("other")) {
			try {
				int matchRate = 0;
				String gameid = null;
				URL += "&platform=" + URLEncoder.encode(getPlatform());
				String xml = getXmlFromUrl(URL);
				Serializer serializer = new Persister();
				Data data = serializer.read(Data.class, xml);
				Log.i("Data", "Data " + data.toString());
				for (Game g : data.getGames()) {
					if (i < 4) {
						int rate = stringMatch(g.getTitle(), cleanedGN);
						if (rate > matchRate) {
							matchRate = rate;
							gameid = Integer.toString(g.getId());
							if (matchRate == 101)
								break;
						}
						i++;
					} else
						break;
				}
				if (null != gameid && matchRate > XPMB_TheGamesDB.THRESHOLD) {
					String URL2 = "http://thegamesdb.net/api/GetGame.php?id="
							+ gameid;
					String xml2 = this.getXmlFromUrl(URL2);
					GameData gd = serializer.read(GameData.class, xml2);
					String baseURL = gd.getBaseImgUrl();
					for (BoxArt ba : gd.getGame().getBoxArts()) {
						if (null != ba.getSide()
								&& ba.getSide().equalsIgnoreCase("front")) {
							coverURL = baseURL + ba.getThumb();
						}
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.i("Performance",
				"(" + gameName + ") Time="
						+ (System.currentTimeMillis() - start)
						+ "ms. Attempts=" + (1 + i) + ". Result=" + coverURL);
		return coverURL;
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Long DownloadFromUrl() {
		if (this.isOnline()) {
			String clink = getCoverLink();
			String DownloadUrl = (clink==null? null : clink.replace("http://", ""));
			if (DownloadUrl != null) {
				if (!romRoot.exists()) {
					romRoot.mkdirs();
				}
				DownloadUrl = ("http://" + Uri.encode(DownloadUrl)).replace(
						"%2F", "/");
				File file = new File(romRoot, this.gameName + "-CV.jpg");
				DownloadManager manager = (DownloadManager) callerActivity
						.getSystemService(Activity.DOWNLOAD_SERVICE);
				Request req = new Request(Uri.parse(DownloadUrl))
						.setNotificationVisibility(Request.VISIBILITY_VISIBLE)
						.setTitle(this.gameName)
						.setDescription(getPlatform())
						.setDestinationUri(Uri.fromFile(file))
						.setAllowedNetworkTypes(
								Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
				long dlId = manager.enqueue(req);

				return dlId;
			}
			return null;

		}
		return null;
	}

	@Root(name = "Data")
	public static class Data {
		@ElementList(inline = true, required = false)
		private List<Game> list = new ArrayList<Game>();

		public List<Game> getGames() {
			return list;
		}
	}

	
	
	@Root(name = "Game", strict = false)
	public static class Game {
		@Element(name = "id")
		private int id;
		@Element(name = "GameTitle")
		private String GameTitle;

		public String getTitle() {
			return GameTitle;
		}

		public int getId() {
			return id;
		}
	}

	@Root(name = "Data", strict = false)
	public static class GameData {
		@Element(name = "baseImgUrl")
		private String baseImgUrl;
		@Element(name = "Game")
		private GameInfo Game;

		public String getBaseImgUrl() {
			return baseImgUrl;
		}

		public GameInfo getGame() {
			return Game;
		}
	}

	@Root(name = "Game", strict = false)
	public static class GameInfo {
		@ElementList(name = "Images")
		private List<BoxArt> boxarts = new ArrayList<BoxArt>();

		public List<BoxArt> getBoxArts() {
			return boxarts;
		}
	}

	@Root(name = "boxart", strict = false)
	public static class BoxArt {
		@Attribute(name = "side", required = false)
		private String side;
		@Attribute(name = "thumb", required = false)
		private String thumb;

		public String getSide() {
			return side;
		}

		public String getThumb() {
			return thumb;
		}
	}

}
