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

package com.raddstudios.xpmb;

import java.io.File;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import com.raddstudios.xpmb.menus.XPMBMenu;
import com.raddstudios.xpmb.menus.XPMBSubmenu_APP;
import com.raddstudios.xpmb.menus.XPMBSubmenu_MUSIC;
import com.raddstudios.xpmb.menus.XPMBSubmenu_ROM;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Layout;
import com.raddstudios.xpmb.utils.XPMB_MainMenu;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;

public class XPMB_Main extends XPMB_Activity {

	// XPERIA Play's physical button Key Codes
	public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP,
			KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN,
			KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT,
			KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT,
			KEYCODE_CROSS = KeyEvent.KEYCODE_BUTTON_B,
			KEYCODE_CIRCLE = KeyEvent.KEYCODE_BUTTON_A,
			KEYCODE_SQUARE = KeyEvent.KEYCODE_BUTTON_X,
			KEYCODE_TRIANGLE = KeyEvent.KEYCODE_BUTTON_Y,
			KEYCODE_SELECT = KeyEvent.KEYCODE_SPACE,
			KEYCODE_START = KeyEvent.KEYCODE_ENTER,
			KEYCODE_MENU = KeyEvent.KEYCODE_BUTTON_R2,
			KEYCODE_SHOULDER_LEFT = KeyEvent.KEYCODE_BUTTON_L1,
			KEYCODE_SHOULDER_RIGHT = KeyEvent.KEYCODE_BUTTON_R1,
			KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN,
			KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;

	private XPMB_MainMenu mMenu = null;
	private XPMB_Layout mSub = null;
	private Handler hMessageBus = null;
	private boolean showingSubmenu = false, bLockedKeys = false,
			firstInitDone = false;
	private AnimationDrawable bmAnim = null;
	AudioManager amVolControl = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		hMessageBus = new Handler();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.xpmb_main);
		//findViewById(R.id.main_l).setOnTouchListener(mTouchListener);

		amVolControl = (AudioManager) getBaseContext().getSystemService(
				AUDIO_SERVICE);

		setupAnimations();

		if (firstInitDone && mMenu != null) {
			return;
		}
		mMenu = new XPMBMenu(getResources().getXml(R.xml.xmb_layout),
				hMessageBus, (ViewGroup) findViewById(R.id.main_l), this);

		super.onCreate(savedInstanceState);
	}

	private static final int MOVING_DIR_VERT = 0, MOVING_DIR_HORZ = 1;
	private float motStX = 0, motStY = 0, dispX = 0, dispY = 0;
	private int polarity = 1;
	private boolean isMoving = false;
	private boolean isTouchEnabled = true;
	private View mTouchedView = null;

	@Override
	public void setTouchedChildView(View v) {
		mTouchedView = v;
	}

	private OnTouchListener mTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			if (!isTouchEnabled) {
				return true;
			}
			int action = arg1.getActionMasked();
			int pointerIndex = arg1.getActionIndex();
			int pointerId = arg1.getPointerId(pointerIndex);
			if (pointerId != 0) {
				return true;
			}

			switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (!isMoving) {
					motStX = arg1.getX(pointerId);
					motStY = arg1.getY(pointerId);
				}
				break;
			case MotionEvent.ACTION_MOVE:
				dispX = arg1.getX(pointerId) - motStX;
				dispY = arg1.getY(pointerId) - motStY;
				float absX = Math.abs(dispX),
				absY = Math.abs(dispY);
				if (!isMoving && (absX > 50 || absY > 50)) {
					if (absX > 50) {
						polarity = MOVING_DIR_HORZ;
					}
					if (absY > 50) {
						polarity = MOVING_DIR_VERT;
					}
					isMoving = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (arg1.getEventTime() - arg1.getDownTime() > 150) {
					switch (polarity) {
					case MOVING_DIR_HORZ:
						if (dispX < 0) {
							onKeyDown(KEYCODE_RIGHT, new KeyEvent(
									KeyEvent.ACTION_DOWN, KEYCODE_RIGHT));
							onKeyUp(KEYCODE_RIGHT, new KeyEvent(
									KeyEvent.ACTION_UP, KEYCODE_RIGHT));
						}
						if (dispX > 0) {
							onKeyDown(KEYCODE_LEFT, new KeyEvent(
									KeyEvent.ACTION_DOWN, KEYCODE_LEFT));
							onKeyUp(KEYCODE_LEFT, new KeyEvent(
									KeyEvent.ACTION_UP, KEYCODE_LEFT));
						}
						break;
					case MOVING_DIR_VERT:
						if (dispY < 0) {
							onKeyDown(KEYCODE_DOWN, new KeyEvent(
									KeyEvent.ACTION_DOWN, KEYCODE_DOWN));
							onKeyUp(KEYCODE_DOWN, new KeyEvent(
									KeyEvent.ACTION_UP, KEYCODE_DOWN));
						}
						if (dispY > 0) {
							onKeyDown(KEYCODE_UP, new KeyEvent(
									KeyEvent.ACTION_DOWN, KEYCODE_UP));
							onKeyUp(KEYCODE_UP, new KeyEvent(
									KeyEvent.ACTION_UP, KEYCODE_UP));
						}
						break;
					}
				} else {
					if (mTouchedView != null) {
						if (showingSubmenu) {
							mSub.sendClickEventToView(mTouchedView);
						} else {
							mMenu.sendClickEventToView(mTouchedView);
						}
						mTouchedView = null;
					}
				}
				motStX = 0;
				motStY = 0;
				isMoving = false;
				break;
			}
			return true;
		}
	};

	@Override
	public void enableTouchEvents(boolean enabled) {
		isTouchEnabled = enabled;
	}

	

	private void setupAnimations() {
		bmAnim = new AnimationDrawable();
		Bitmap drwAnimSrc = BitmapFactory.decodeResource(getResources(),
				R.drawable.ui_loading);
		int bmSizeX = drwAnimSrc.getWidth(), bmSizeY = drwAnimSrc.getHeight(), bmFrameSzx = bmSizeX / 18;
		for (int dp = 0; (dp * bmFrameSzx) < bmSizeX; dp++) {
			bmAnim.addFrame(
					new BitmapDrawable(getResources(), Bitmap
							.createBitmap(drwAnimSrc, dp * bmFrameSzx, 0,
									bmFrameSzx, bmSizeY)), 50);
		}
		bmAnim.setOneShot(false);
		drwAnimSrc = null;
		((ImageView) findViewById(R.id.ivLoadAnim)).setImageDrawable(bmAnim);
	}

	@Override
	public void onResume() {
		if (!firstInitDone && mMenu != null) {
			showLoadingAnim(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					mMenu.doInit();
					hMessageBus.post(new Runnable() {

						@Override
						public void run() {
							mMenu.parseInitLayout();
							showLoadingAnim(false);
							firstInitDone = true;
						}

					});
				}

			}).start();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		if (showingSubmenu) {
			mSub.sendKeyDown(keyCode);
		} else {
			mMenu.sendKeyDown(keyCode);
		}
		event.startTracking();

		switch (keyCode) {
		case KEYCODE_VOLUME_UP:
			amVolControl.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
			break;
		case KEYCODE_VOLUME_DOWN:
			amVolControl.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (bLockedKeys) {
			return true;
		}
		if (showingSubmenu) {
			mSub.sendKeyUp(keyCode);
		} else {
			mMenu.sendKeyUp(keyCode);
		}
		return true;
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent vent) {
		if (bLockedKeys) {
			return true;
		}
		if (showingSubmenu) {
			mSub.sendKeyHold(keyCode);
		} else {
			mMenu.sendKeyHold(keyCode);
		}
		return true;
	}

	// Normally, as this is a launcher, we should not call this procedure.
	// As we aren't finished yet, we can't use this launcher as a day-to-day
	// replacement one,
	// that's the reason to be for this procedure.
	@Override
	public void requestActivityEnd() {
		if (mSub != null) {
			mSub.doCleanup();
			mSub.requestDestroy();
		}
		if (mMenu != null) {
			mMenu.doCleanup();
			mMenu.requestDestroy();
		}
		finish();
	}

	@Override
	public void onDestroy() {
		requestUnloadSubmenu();
		if (mMenu != null) {
			mMenu.doCleanup();
		}
		super.onDestroy();
	}

	@Override
	public void requestUnloadSubmenu() {
		if (showingSubmenu) {
			unloadSubmenu();
			mMenu.postExecuteFinished();
			showingSubmenu = false;
		}
	}

	@Override
	public XPMB_ImageView getCustomBGView() {
		return (XPMB_ImageView) findViewById(R.id.ivCustomBG);
	}

	@Override
	public void lockKeys(boolean locked) {
		bLockedKeys = locked;
	}

	@Override
	public void showLoadingAnim(boolean show) {
		ImageView iv_la = (ImageView) findViewById(R.id.ivLoadAnim);
		if (iv_la != null) {
			if (show) {
				iv_la.setVisibility(View.VISIBLE);
				bmAnim.start();
			} else {
				bmAnim.stop();
				iv_la.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	public void preloadSubmenu(String submenu) {
		if (!showingSubmenu) {
			if (submenu.equals("XPMB_Submenu_APP")) {
				mSub = new XPMBSubmenu_APP(this, hMessageBus,
						(ViewGroup) findViewById(R.id.main_l));
			} else if (submenu.equals("XPMB_Submenu_GBA")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".gba");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"GBA"),R.xml.rominfo_gba,
							exts,"drawable/ui_cover_not_found_gba", "GBA");
				}
			} else if (submenu.equals("XPMB_Submenu_NES")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".nes");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"NES"),R.xml.rominfo_nes,
							exts,"drawable/ui_cover_not_found_nes", "NES");
				}

			} else if (submenu.equals("XPMB_Submenu_PCE")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".pce");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"PCE"),R.xml.rominfo_pce,
							exts,"drawable/ui_cover_not_found_pce", "PCE");
				}

			} else if (submenu.equals("XPMB_Submenu_MD")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();

					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".bin");
					exts.add(".gen");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"MD"),R.xml.rominfo_genesis,
							exts,"drawable/ui_cover_not_found_md", "MD");
				}

			} else if (submenu.equals("XPMB_Submenu_SNES")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".smc");
					exts.add(".sfc");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"SNES"),R.xml.rominfo_snes,
							exts,"drawable/ui_cover_not_found_snes", "SNES");
				}

			} else if (submenu.equals("XPMB_Submenu_SMS")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".sms");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"SMS"),R.xml.rominfo_sms,
							exts,"drawable/ui_cover_not_found_sms", "SMS");
				}

			} /*else if (submenu.equals("XPMB_Submenu_GB")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					mSub = new XPMBSubmenu_GB(this, hMessageBus,
							(ViewGroup) findViewById(R.id.main_l), new File(
									rP,
									"GB"));
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".bin");
					exts.add(".gen");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"MD"),R.xml.rominfo_genesis,
							exts,"drawable/ui_cover_not_found_md", "MD");
				}

			}*/ else if (submenu.equals("XPMB_Submenu_GBC")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".gbc");
					exts.add(".gb");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"GBC"),R.xml.rominfo_gbc,
							exts,"drawable/ui_cover_not_found_gb", "GBC");
				}

			} else if (submenu.equals("XPMB_Submenu_N64")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".v64");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"N64"),R.xml.rominfo_n64,
							exts,"drawable/ui_cover_not_found_n64", "N64");
					
				}

			} else if (submenu.equals("XPMB_Submenu_DS")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".nds");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"NDS"),R.xml.rominfo_nds,
							exts,"drawable/ui_cover_not_found_nds", "NDS");
					
				}

			} else if (submenu.equals("XPMB_Submenu_PSX")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					File rP = new File (Environment.getExternalStorageDirectory()+File.separator+"roms"+File.separator);
					rP.mkdirs();
					ArrayList<String> exts = new ArrayList<String>();
					exts.add(".iso");
					exts.add(".bin");
					exts.add(".img");
					mSub = new XPMBSubmenu_ROM(this, hMessageBus, (ViewGroup) findViewById(R.id.main_l), new File(rP,"PSX"),-1,
							exts,"drawable/ui_cover_not_found_psx", "PSX");
					
				}

			} else if (submenu.equals("XPMB_Submenu_MUSIC")) {
				boolean mExternalStorageAvailable = false;
				boolean mExternalStorageWriteable = false;
				String state = Environment.getExternalStorageState();

				if (Environment.MEDIA_MOUNTED.equals(state)) {
					mExternalStorageAvailable = mExternalStorageWriteable = true;
				} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					mExternalStorageAvailable = true;
					mExternalStorageWriteable = false;
				} else {
					mExternalStorageAvailable = mExternalStorageWriteable = false;
				}

				if (mExternalStorageAvailable && mExternalStorageWriteable) {
					mSub = new XPMBSubmenu_MUSIC(this, hMessageBus,
							(ViewGroup) findViewById(R.id.main_l));
				}

			}
			if (mSub == null) {
				System.err
						.println("XPMB_Main::preloadSubmenu() : can't load submenu '"
								+ submenu + "'");
				return;
			}
			showLoadingAnim(true);
			lockKeys(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					mSub.doInit();

					hMessageBus.post(new Runnable() {

						@Override
						public void run() {
							mSub.parseInitLayout();
							showLoadingAnim(false);
							showingSubmenu = true;
							lockKeys(false);
						}

					});
				}

			}).start();
		}
	}

	private void unloadSubmenu() {
		mSub.doCleanup();
	}


	
}
