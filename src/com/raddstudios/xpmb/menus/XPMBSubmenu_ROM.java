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

package com.raddstudios.xpmb.menus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.XPMBSubmenu_ROM.XPMBSubmenuItem_ROM;
import com.raddstudios.xpmb.menus.utils.ROMInfo;
import com.raddstudios.xpmb.menus.utils.ROMInfo.ROMInfoNode;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_BroadCastReceiver;
import com.raddstudios.xpmb.utils.XPMB_DataBaseHelper;
import com.raddstudios.xpmb.utils.XPMB_Layout;
import com.raddstudios.xpmb.utils.XPMB_RetroArch;
import com.raddstudios.xpmb.utils.XPMB_Activity.IntentFinishedListener;
import com.raddstudios.xpmb.utils.XPMB_TheGamesDB;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

public class XPMBSubmenu_ROM extends XPMB_Layout {

	public class XPMBSubmenuItem_ROM {

		private Drawable bmGameCover = null, bmGameBackground = null;
		private File fROMPath = null;
		private String strGameName = null, strGameCode = null,
				strGameCRC = null, strGameDescription = null,
				strGameRegions = null, strGameLanguages;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private XPMB_TableRow trParentContainer = null;

		public XPMBSubmenuItem_ROM(File romPath, String gameCRC) {
			fROMPath = romPath;

			strGameName = fROMPath.getName();
			strGameCRC = gameCRC;
		}

		public File getROMPath() {
			return fROMPath;
		}

		public String getGameCode() {
			return strGameCode;
		}

		public void setGameRegions(String gameRegions) {
			strGameRegions = gameRegions;
		}

		public String getGameRegions() {
			return strGameRegions;
		}

		public void setGameLanguages(String gameLanguages) {
			strGameLanguages = gameLanguages;
		}

		public String getGameLanguages() {
			return strGameLanguages;
		}

		public void setGameBackground(Drawable gameBackground) {
			bmGameBackground = gameBackground;
		}

		public Drawable getGameBackground() {
			return bmGameBackground;
		}

		public void setGameCover(Drawable cover) {
			bmGameCover = cover;
		}

		public Drawable getGameCover() {
			return bmGameCover;
		}

		public void setGameName(String gameName) {
			strGameName = gameName;
		}

		public String getGameName() {
			return strGameName;
		}

		public String getGameCRC() {
			return strGameCRC;
		}

		public void setGameDescription(String gameDescription) {
			strGameDescription = gameDescription;
		}

		public String getGameDescription() {
			return strGameDescription;
		}

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		public XPMB_ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(XPMB_TextView label) {
			tvParentLabel = label;
		}

		public XPMB_TextView getParentLabel() {
			return tvParentLabel;
		}

		public void setParentContainer(XPMB_TableRow parent) {
			trParentContainer = parent;
		}

		public XPMB_TableRow getParentContainer() {
			return trParentContainer;
		}
	}

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0,
			ANIM_MENU_MOVE_DOWN = 1, ANIM_CENTER_ON_ITEM = 2;

	private class UIAnimatorWorker implements AnimatorUpdateListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
		private int pY = 0, destY = 0;
		private ValueAnimator mOwner = null;
		private float[] mArgs = null;

		public UIAnimatorWorker(ValueAnimator parentAnimator) {
			super();
			mOwner = parentAnimator;
		}

		public void setArguments(float[] arguments) {
			mArgs = arguments;
		}

		public void setAnimationType(int type) {
			if (mOwner.isStarted()) {
				mOwner.end();
			}
			intAnimType = type;
			intAnimItem = intSelItem;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				intNextItem = intAnimItem - 1;
				destY = pxFromDip(88) - (pxFromDip(50) * intNextItem);
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				intNextItem = intAnimItem + 1;
				destY = pxFromDip(88) - (pxFromDip(50) * intNextItem);
				break;
			case ANIM_CENTER_ON_ITEM:
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				intNextItem = (int) mArgs[0];
				destY = pxFromDip(88) - (pxFromDip(50) * intNextItem);
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			float dispY = 0, alphaO = 0, alphaI = 0, scaleO = 0, scaleI = 0;
			int marginO = 0, marginI = 0;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_CENTER_ON_ITEM:
				dispY = destY - pY;
				scaleO = 2.56f - (1.56f * completion);
				scaleI = 1.0f + (1.56f * completion);
				alphaO = 1.0f - completion;
				alphaI = completion;
				marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setTopMargin((int) (pY + (dispY * completion)));
				alItems.get(intAnimItem).getParentView().setViewScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setViewScaleY(scaleO);
				alItems.get(intAnimItem).getParentLabel().setAlphaLevel(alphaO);
				alItems.get(intAnimItem).getParentContainer()
						.setTopMargin(marginO);
				alItems.get(intAnimItem).getParentContainer()
						.setBottomMargin(marginO);
				alItems.get(intNextItem).getParentView().setViewScaleX(scaleI);
				alItems.get(intNextItem).getParentView().setViewScaleY(scaleI);
				alItems.get(intNextItem).getParentLabel().setAlphaLevel(alphaI);
				alItems.get(intNextItem).getParentContainer()
						.setTopMargin(marginI);
				alItems.get(intNextItem).getParentContainer()
						.setBottomMargin(marginI);
				break;
			case ANIM_NONE:
			default:
				break;
			}
		}
	};

	private final int SCROLL_DIR_UP = 0, SCROLL_DIR_DOWN = 1;

	private class RapidScroller extends TimerTask {

		private int mDirection = 0;
		private boolean bEnabled = false;

		public void setScrollDirection(int direction) {
			mDirection = direction;
		}

		public void setEnabled(boolean enabled) {
			bEnabled = enabled;
		}

		public boolean isEnabled() {
			return bEnabled;
		}

		@Override
		public void run() {
			if (bEnabled) {
				switch (mDirection) {
				case SCROLL_DIR_UP:
					getMessageBus().post(new Runnable() {
						@Override
						public void run() {
							moveUp();
						}
					});
					break;
				case SCROLL_DIR_DOWN:
					getMessageBus().post(new Runnable() {
						@Override
						public void run() {
							moveDown();
						}
					});
					break;
				}
			}
		}
	};

	private ArrayList<XPMBSubmenuItem_ROM> alItems = null;
	private int intSelItem = 0;
	private boolean isFocused = true;
	private File mROMRoot = null;
	private ROMInfo ridROMInfoDat = null;
	private XPMB_TextView tv_no_game = null;
	private XPMB_TableLayout tlRoot = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;
	private RapidScroller ttFastScroll = null;
	private int xmlId;
	private String platform;
	private String coverNotFound;
	private ArrayList<String> extensions;
	private ArrayList<Long> dlIds;
	protected DownloadManager mManager;
	private HashMap<String, XPMB_ImageView> gicons;
	private HashMap<String, XPMB_BroadCastReceiver> receivers;
	private XPMB_DataBaseHelper myDbHelper;
	/**
	 * Setting the ThreadPoolExecutor(s).
	 */
	private static final int CORE_POOL_SIZE = 5;
	private static final int MAXIMUM_POOL_SIZE = 5;
	private static final int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
	private final ThreadPoolExecutor mDownloadThreadPool;
	private final ThreadPoolExecutor mDecodeThreadPool;
	private final BlockingQueue<Runnable> mDownloadWorkQueue;
	private final BlockingQueue<Runnable> mDecodeWorkQueue;
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT;
	private static final int KEEP_ALIVE_TIME = 1;
	static{
		KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
	}
	

	public XPMBSubmenu_ROM(XPMB_Activity root, Handler messageBus,
			ViewGroup rootView, File fROMRoot, int xmlId,
			ArrayList<String> extensions, String coverNotFound, String platform) {
		super(root, messageBus, rootView, 0x1000);
		Log.i("Cores","Cores "+NUMBER_OF_CORES);
		mDownloadWorkQueue = new LinkedBlockingQueue<Runnable>();
		mDecodeWorkQueue = new LinkedBlockingQueue<Runnable>();
		mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadWorkQueue);
		mDecodeThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDecodeWorkQueue);
		mROMRoot = fROMRoot;

		alItems = new ArrayList<XPMBSubmenuItem_ROM>();

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);

		ttFastScroll = new RapidScroller();
		this.xmlId = xmlId;
		this.extensions = extensions;
		this.coverNotFound = coverNotFound;
		this.platform = platform;
		new Timer().scheduleAtFixedRate(ttFastScroll, 0, 50);
		this.mManager = (DownloadManager) getRootActivity().getSystemService(
				Activity.DOWNLOAD_SERVICE);
		this.gicons = new HashMap<String, XPMB_ImageView>();
		this.receivers = new HashMap<String,XPMB_BroadCastReceiver>();
		if (platform.equalsIgnoreCase("PSX")) {
			myDbHelper = new XPMB_DataBaseHelper(getRootActivity()
					.getApplicationContext());
			try {
				myDbHelper.createDataBase();
			} catch (IOException ioe) {
				throw new Error("Unable to create database");
			}
			try {
				myDbHelper.openDataBase();
			} catch (SQLException sqle) {
				throw sqle;
			}
		}
	}

	public void doInit() {
		initRoms(mROMRoot);
		// initRoms(new File(mROMRoot.getParent()+"/external_sdcard"));

	}

	private void initRoms(File mROMRoot) {
		long start = System.currentTimeMillis();
		mROMRoot.mkdirs();
		if (!mROMRoot.isDirectory()) {
			System.err
					.println("XPMBSubmenu_ROM::doInit() : can't create or access "
							+ mROMRoot.getAbsolutePath());
			return;
		}
		File mROMResDir = new File(mROMRoot, "Resources");
		if (!mROMResDir.exists()) {
			mROMResDir.mkdirs();
			if (!mROMResDir.isDirectory()) {
				System.err
						.println("XPMBSubmenu_ROM::doInit() : can't create or access "
								+ mROMResDir.getAbsolutePath());
				return;
			}
		}
		if (!platform.equalsIgnoreCase("PSX"))
			ridROMInfoDat = new ROMInfo(getRootActivity().getResources()
					.getXml(this.xmlId), ROMInfo.TYPE_CRC);

		try {
			File[] storPtCont = mROMRoot.listFiles();
			for (File f : storPtCont) {
				if (!f.getName().startsWith(".")
						&& f.getName().endsWith(".zip")
						&& !platform.equalsIgnoreCase("PSX")) {
					ZipFile zf = new ZipFile(f, ZipFile.OPEN_READ);
					Enumeration<? extends ZipEntry> ze = zf.entries();
					while (ze.hasMoreElements()) {
						ZipEntry zef = ze.nextElement();
						if (extensionCheck(zef.getName())) {
							String gameCRC = Long.toHexString(zef.getCrc())
									.toUpperCase(
											getRootActivity().getResources()
													.getConfiguration().locale);
							XPMBSubmenuItem_ROM cItem = new XPMBSubmenuItem_ROM(
									f, gameCRC);
							loadAssociatedMetadata(cItem);
							alItems.add(cItem);
							break;
						}
					}
					zf.close();
				} else if (extensionCheck(f.getName())
						&& !platform.equalsIgnoreCase("PSX")) {

					CRC32 cCRC = new CRC32();
					InputStream fi = new BufferedInputStream(
							new FileInputStream(f));

					int cByte = 0;
					byte[] buf = new byte[1024 * 512];
					while ((cByte = fi.read(buf)) > 0) {
						cCRC.update(buf, 0, cByte);
					}
					fi.close();
					String gameCRC = Long.toHexString(cCRC.getValue())
							.toUpperCase(
									getRootActivity().getResources()
											.getConfiguration().locale);
					XPMBSubmenuItem_ROM cItem = new XPMBSubmenuItem_ROM(f,
							gameCRC);
					loadAssociatedMetadata(cItem);
					alItems.add(cItem);
				} else if (!f.getName().startsWith(".")
						&& !f.getName().contains("SCPH")
						&& extensionCheck(f.getName())
						&& platform.equalsIgnoreCase("PSX")) {
					XPMBSubmenuItem_ROM cItem = new XPMBSubmenuItem_ROM(f,
							getPSXId(f));
					loadAssociatedMetadata(cItem);
					alItems.add(cItem);
				}

			}
		} catch (Exception e) {
			// TODO Handle errors when loading found ROMs
			e.printStackTrace();
		}
		Log.i("InitRoms", "Total time " + (System.currentTimeMillis() - start)
				+ " ms.");
	}

	private String getPSXId(File f) {
		Log.i("GetPSXID", f.getName());
		FileInputStream fin;
		try {
			fin = new FileInputStream(f);
			fin.skip(32768);
			byte[] buffer = new byte[512 * 1024];
			// long start = System.currentTimeMillis();
			while (fin.read(buffer) != -1) {
				String buffered = new String(buffer);

				if (buffered.contains("BOOT = cdrom:\\")) {
					String tmp = "";
					int lidx = buffered.lastIndexOf("BOOT = cdrom:\\") + 14;
					for (int i = 0; i < 11; i++) {
						tmp += buffered.charAt(lidx + i);
					}
					tmp = tmp.toUpperCase().replace(".", "").replace("_", "-");
					fin.close();
					return tmp;
				}

			}
			fin.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;

	}

	private boolean extensionCheck(String name) {
		for (String ext : extensions) {
			if (name.toLowerCase().endsWith(ext.toLowerCase()))
				return true;
		}
		return false;
	}

	private void loadAssociatedMetadata(XPMBSubmenuItem_ROM item) {
		if (platform.equalsIgnoreCase("PSX")) {
			String id = item.getGameCRC();
			// Log.i("GameID",id);
			String title = myDbHelper.getGameTitle(id);
			Log.i("[PSX] GAME INFO", id + " " + title);
			item.setGameName(title);
			File resStor = new File(mROMRoot, "Resources");
			if (resStor.exists()) {
				try {
					File fExtRes = new File(resStor, (item.getGameName()
							.contains("[") ? item.getGameName().substring(0,
							item.getGameName().indexOf("["))
							: item.getGameName())
							+ "-CV.jpg");
					if (fExtRes.exists()) {
						item.setGameCover(new BitmapDrawable(getRootActivity()
								.getResources(), BitmapFactory
								.decodeStream(new FileInputStream(fExtRes))));
					} else {
						item.setGameCover(getRootActivity()
								.getResources()
								.getDrawable(
										getRootActivity()
												.getResources()
												.getIdentifier(
														this.coverNotFound,
														null,
														getRootActivity()
																.getPackageName())));
						this.mDownloadThreadPool.execute(new XPMB_ROMTask(resStor, getRootActivity(),
								(title.contains("[") ? title.substring(0,
										title.indexOf("[")) : title), platform,
								item, this, mManager));
					}
				} catch (Exception e) {
				}
			}

		} else {
			try {

				ROMInfoNode rinCData = ridROMInfoDat.getNode(item.getGameCRC());
				if (platform.equals("GBC") && rinCData == null) {
					ridROMInfoDat = new ROMInfo(getRootActivity()
							.getResources().getXml(R.xml.rominfo_gb),
							ROMInfo.TYPE_CRC);
					rinCData = ridROMInfoDat.getNode(item.getGameCRC());
				}
				if (rinCData != null) {

					String romName = rinCData.getROMData().getROMName();

					if (romName.indexOf('(') != -1) {
						String romRegions = romName.substring(
								romName.indexOf('(') + 1, romName.indexOf(')'));
						item.setGameRegions(romRegions);
					}
					if (romName.indexOf('(', romName.indexOf(')')) != -1) {
						String romLanguages = romName.substring(romName
								.indexOf('(', romName.indexOf(')')));
						romLanguages = romLanguages.substring(
								romLanguages.indexOf('(') + 1,
								romLanguages.indexOf(')'));
						item.setGameLanguages(romLanguages);
					}
					if (rinCData.getNumReleases() == 0) {
						if (romName.indexOf('(') != -1) {

							item.setGameName(romName.substring(0,
									romName.indexOf('(') - 1));
						} else {
							item.setGameName(romName.substring(0,
									romName.lastIndexOf(".")));
						}
					} else {
						item.setGameName(rinCData.getReleaseData(0)
								.getReleaseName());
					}
				}

				File resStor = new File(mROMRoot, "Resources");
				if (resStor.exists()) {
					File fExtRes = new File(resStor, item.getGameName()
							+ "-CV.jpg");
					if (fExtRes.exists()) {
						item.setGameCover(new BitmapDrawable(getRootActivity()
								.getResources(), BitmapFactory
								.decodeStream(new FileInputStream(fExtRes))));
					} else {
						item.setGameCover(getRootActivity()
								.getResources()
								.getDrawable(
										getRootActivity()
												.getResources()
												.getIdentifier(
														this.coverNotFound,
														null,
														getRootActivity()
																.getPackageName())));
						this.mDownloadThreadPool.execute(new XPMB_ROMTask(resStor, getRootActivity(),
								item.getGameName(), platform, item, this,
								mManager));
					}
				}
			} catch (Exception e) {
				// TODO Handle errors when loading associated ROM metadata
				e.printStackTrace();
			}
		}
	}

	private OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (isFocused) {
				getRootActivity().setTouchedChildView(v);
			}
			return false;
		}
	};

	@Override
	public void sendClickEventToView(View v) {
		if (v.getTag() != null) {
			int tag = (Integer) v.getTag();

			if (tag != intSelItem) {
				doCenterOnItemPre();
				centerOnItem(tag);
			}
			execCustItem(tag);
		}
	}

	private void doCenterOnItemPre() {
		alItems.get(intSelItem).getParentContainer().setTopMargin(0);
		alItems.get(intSelItem).getParentContainer().setBottomMargin(0);
		alItems.get(intSelItem).getParentView().setViewScaleX(1.0f);
		alItems.get(intSelItem).getParentView().setViewScaleY(1.0f);
		alItems.get(intSelItem).getParentLabel().setAlphaLevel(0.0f);
	}

	private void centerOnNearestItem() {
		float cPosY = tlRoot.getTopMargin();
		int destItem = ((int) (pxFromDip(122) - cPosY) / pxFromDip(60)) + 1;
		if (destItem < 0) {
			destItem = 0;
		} else if (destItem > (alItems.size() - 1)) {
			destItem = (alItems.size() - 1);
		}
		centerOnItem(destItem);

	}

	private void centerOnItem(int index) {
		aUIAnimatorW.setArguments(new float[] { index });
		aUIAnimatorW.setAnimationType(ANIM_CENTER_ON_ITEM);
		aUIAnimator.start();
		intSelItem = index;
	}

	public void parseInitLayout() {

		if (alItems.size() == 0) {
			tv_no_game = new XPMB_TextView(getRootView().getContext());
			RelativeLayout.LayoutParams lp_ng = new RelativeLayout.LayoutParams(
					pxFromDip(320), pxFromDip(100));
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp_ng.leftMargin = pxFromDip(48);
			lp_ng.topMargin = pxFromDip(128);
			tv_no_game.setLayoutParams(lp_ng);
			tv_no_game.setText(getRootActivity().getText(R.string.strNoGames));
			tv_no_game.setTextColor(Color.WHITE);
			tv_no_game.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_game.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_game.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			getRootView().addView(tv_no_game);
			return;
		}

		tlRoot = new XPMB_TableLayout(getRootView().getContext());
		RelativeLayout.LayoutParams rootP = new RelativeLayout.LayoutParams(
				pxFromDip(464), pxFromDip(160 + (60 * alItems.size())));
		rootP.leftMargin = pxFromDip(48);
		rootP.topMargin = pxFromDip(88);
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_ROM xsi : alItems) {

			int idy = alItems.indexOf(xsi);

			XPMB_TableRow cItem = new XPMB_TableRow(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView()
					.getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cItem.setId(getNextID());
			
			if(!gicons.containsKey(xsi.getGameName())) {
				gicons.put(xsi.getGameName(), cIcon);
			}
			// Setup Container
			TableLayout.LayoutParams cItemP = new TableLayout.LayoutParams(
					pxFromDip(386), TableLayout.LayoutParams.WRAP_CONTENT);
			if (idy == 0) {
				cItemP.topMargin = pxFromDip(16);
				cItemP.bottomMargin = pxFromDip(16);
			}
			cItem.setLayoutParams(cItemP);

			// Setup Icon
			TableRow.LayoutParams cIconParams = new TableRow.LayoutParams(
					(int) pxFromDip(50), (int) pxFromDip(50));
			cIconParams.column = 0;
			cIcon.setLayoutParams(cIconParams);
			cIcon.resetScaleBase();
			if (idy == 0) {
				cIcon.setViewScaleX(2.56f);
				cIcon.setViewScaleY(2.56f);
			}

			cIcon.setImageDrawable(xsi.getGameCover());
			cIcon.setTag(idy);
			cIcon.setOnTouchListener(mTouchListener);

			// Setup Label
			TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams(
					(int) pxFromDip(320), (int) pxFromDip(50));
			cLabelParams.column = 1;
			cLabelParams.leftMargin = pxFromDip(16);
			cLabelParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			cLabel.setLayoutParams(cLabelParams);

			if (idy != 0) {
				cLabel.setAlphaLevel(0.0f);
			}
			cLabel.setText(xsi.getGameName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			cLabel.setTag(idy);
			cLabel.setOnTouchListener(mTouchListener);

			// Add everything to their parent containers and holders
			cItem.addView(cIcon);
			cItem.addView(cLabel);
			xsi.setParentView(cIcon);
			xsi.setParentLabel(cLabel);
			xsi.setParentContainer(cItem);
			tlRoot.addView(cItem);
		}
		// Prevent Image scale changes to distort layout during animations
		XPMB_TableRow tlFiller = new XPMB_TableRow(getRootView().getContext());
		XPMB_ImageView ivFiller = new XPMB_ImageView(getRootView().getContext());
		XPMB_TextView tvFiller = new XPMB_TextView(getRootView().getContext());
		TableRow.LayoutParams iv_f_lp = new TableRow.LayoutParams(
				pxFromDip(128), pxFromDip(128));
		TableRow.LayoutParams tv_f_lp = new TableRow.LayoutParams(
				pxFromDip(320), pxFromDip(128));
		iv_f_lp.column = 0;
		tv_f_lp.column = 1;
		ivFiller.setLayoutParams(iv_f_lp);
		tvFiller.setLayoutParams(tv_f_lp);
		tlFiller.addView(ivFiller);
		tlFiller.addView(tvFiller);
		tlRoot.addView(tlFiller);
		getRootView().addView(tlRoot);
		reloadGameBG(0);
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_DOWN:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveDown();
			}
			break;
		case XPMB_Main.KEYCODE_UP:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveUp();
			}
			break;
		case XPMB_Main.KEYCODE_START:
		case XPMB_Main.KEYCODE_CROSS:
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_LEFT:
		case XPMB_Main.KEYCODE_CIRCLE:
			getRootActivity().requestUnloadSubmenu();
			break;
		}
	}

	@Override
	public void sendKeyHold(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_UP:
			ttFastScroll.setScrollDirection(SCROLL_DIR_UP);
			ttFastScroll.setEnabled(true);
			break;
		case XPMB_Main.KEYCODE_DOWN:
			ttFastScroll.setScrollDirection(SCROLL_DIR_DOWN);
			ttFastScroll.setEnabled(true);
			break;
		}
	}

	public void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		++intSelItem;
	}

	public void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		--intSelItem;
	}

	private void reloadGameBG_Pre() {
		ValueAnimator va_bgr_pr = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_bgr_pr.setInterpolator(new DecelerateInterpolator());
		va_bgr_pr.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float alphaI = 1.0f - completion;
				getRootActivity().getCustomBGView().setAlphaLevel(alphaI);

				if (completion == 1.0f) {
					getRootActivity().getCustomBGView().setVisibility(
							View.INVISIBLE);
				}
			}
		});
		va_bgr_pr.setDuration(200);
		va_bgr_pr.start();
	}

	private void reloadGameBG_Pos() {
		getRootActivity().getCustomBGView().setVisibility(View.VISIBLE);
		ValueAnimator va_bgr_po = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_bgr_po.setInterpolator(new DecelerateInterpolator());
		va_bgr_po.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				float alphaI = completion;
				getRootActivity().getCustomBGView().setAlphaLevel(alphaI);
			}
		});
		va_bgr_po.setDuration(200);
		va_bgr_po.start();
	}

	private void reloadGameBG(final int index) {
		if (getRootActivity().getCustomBGView().getDrawable() != null) {
			reloadGameBG_Pre();
		}
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				getRootActivity().getCustomBGView().setImageDrawable(
						alItems.get(index).getGameBackground());
				reloadGameBG_Pos();
			}

		}, 201);
	}

	public void execCustItem(int index) {
		XPMB_RetroArch bridge = new XPMB_RetroArch(this.getRootActivity(),
				this.platform, alItems.get(intSelItem).getROMPath().toString());
		bridge.executeROM();
	}

	public void execSelectedItem() {
		execCustItem(intSelItem);
	}

	@Override
	public void doCleanup() {
		if (alItems.size() == 0 && tv_no_game != null) {
			getRootView().removeView(tv_no_game);
			return;
		}
		if (tlRoot != null) {
			tlRoot.removeAllViews();
			tlRoot.invalidate();
			getRootView().removeView(tlRoot);
		}
		if (getRootActivity().getCustomBGView().getDrawable() != null) {
			reloadGameBG_Pre();
		}
	}

	public void updateGameCover(final String gameName) {
		File resStor = new File(mROMRoot, "Resources");
		File fExtRes = new File(resStor,
				(gameName.contains("[") ? gameName.substring(0,
						gameName.indexOf("[")) : gameName)
						+ "-CV.jpg");
		getRootActivity().unregisterReceiver(receivers.get(gameName));
		receivers.remove(gameName);
		if (fExtRes.exists()) {
			
			try {
			final BitmapDrawable gameCover = new BitmapDrawable(
						getRootActivity().getResources(),
						BitmapFactory
								.decodeStream(new FileInputStream(fExtRes)));
				getRootActivity().runOnUiThread(new Runnable(){
					@Override
					public void run(){
						android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
						if (null != gicons.get(gameName))
							gicons.get(gameName).setImageDrawable(gameCover);
					}
				});
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private class XPMB_ROMTask implements Runnable{
		private File resStor;
		private XPMB_Activity activity;
		private String title;
		private String platform;
		private DownloadManager mManager;
		private XPMBSubmenuItem_ROM item;
		private XPMBSubmenu_ROM submenu;

		public XPMB_ROMTask(File resStor, XPMB_Activity activity, String title,
				String platform, XPMBSubmenuItem_ROM item,
				XPMBSubmenu_ROM submenu, DownloadManager mManager) {
			this.resStor = resStor;
			this.activity = activity;
			this.title = title;
			this.platform = platform;
			this.mManager = mManager;
			this.item = item;
			this.submenu = submenu;
		}

		@SuppressWarnings("rawtypes")
		public void run(){
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
			XPMB_TheGamesDB tgdb = new XPMB_TheGamesDB(resStor, activity,
					title, platform);
			Long dlId = tgdb.DownloadFromUrl();
			if (null != dlId) {
				XPMB_BroadCastReceiver receiver = new XPMB_BroadCastReceiver(dlId,
						mManager, activity, item, submenu);
				XPMBSubmenu_ROM.this.receivers.put(item.getGameName(), receiver);
				activity.registerReceiver(receiver, new IntentFilter(
						DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			}
			return;
		}

	}

	public void addDecodingJob(final Uri fileUri,final String gameName,
			final XPMBSubmenuItem_ROM item) {
		mDecodeThreadPool.execute(new Runnable(){

			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				try {
					BitmapDrawable bd = new BitmapDrawable(
							XPMBSubmenu_ROM.this.getRootActivity().getResources(),
							BitmapFactory.decodeStream(new FileInputStream(fileUri.getPath()))
							);
					item.setGameCover(bd);
					XPMBSubmenu_ROM.this.updateGameCover(item.getGameName());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
	}


}
