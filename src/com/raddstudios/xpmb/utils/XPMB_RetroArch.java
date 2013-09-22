package com.raddstudios.xpmb.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.NativeActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class XPMB_RetroArch {
	private static final int ACTIVITY_LOAD_ROM_EXTERNAL = 1;
	String platform;
	Activity callerActivity;
	String current_ime;
	ConfigFile config;
	Context retroArchCtx;
	String romPath;
	
	public XPMB_RetroArch(Activity callerActivity, String platform,
			String romPath) {
		if (callerActivity != null) {
			
			try {
				this.config = new ConfigFile(new File("/data/data/org.retroarch/"));
			} catch (IOException e) {
				this.config = new ConfigFile();
			}
			this.callerActivity = callerActivity;
			this.platform = platform;
			this.current_ime = Settings.Secure.getString(
					callerActivity.getContentResolver(),
					Settings.Secure.DEFAULT_INPUT_METHOD);
			this.romPath = romPath;
			try {
				this.retroArchCtx = this.callerActivity.createPackageContext(
						"org.retroarch", Context.MODE_WORLD_WRITEABLE);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


	public void executeROM() {
		Intent myIntent = new Intent().setClassName("org.retroarch", "org.retroarch.browser.MainMenuActivity");
		myIntent.putExtra("ROM", this.romPath);
		myIntent.putExtra("LIBRETRO", getCore());
		callerActivity.startActivity(myIntent);
	}

	private String getCore() {
		String libDir = "/data/data/org.retroarch/lib/";
		if (this.platform.equals("PCE")) {
			return libDir + "libretro_mednafen_pce_fast.so";
		} else if (this.platform.equals("GBA")) {
			return libDir + "libretro_vba_next.so";
		} else if (this.platform.equals("GB") || this.platform.equals("GBC")) {
			return libDir + "libretro_gambatte.so";
		} else if (this.platform.equals("MD") || this.platform.equals("SMS")) {
			return libDir + "libretro_genesis_plus_gx.so";
		} else if (this.platform.equals("N64")) {
			//There's no n64 core on retroarch.
			//Rely on n64oid or mupen64+-AE
			return "";
		} else if (this.platform.equals("NES")) {
			return libDir + "libretro_nestopia.so";
		} else if (this.platform.equals("SNES")) {
			return libDir + "libretro_snes9x_next.so";
		} else if (this.platform.equals("PSX")){
			if(cpuInfoIsNeon(readCPUInfo())){
				return libDir + "libretro_pcsx_rearmed-neon.so";
			}
			else return libDir + "libretro_pcsx_rearmed.so";
		} else if(this.platform.equals("NDS")){
			return libDir+"libretro_desmume.so";
		}
		return "";
	}

	private String readCPUInfo() {
		String result = "";

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream("/proc/cpuinfo")));

			String line;
			while ((line = br.readLine()) != null)
				result += line + "\n";
			br.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	private boolean cpuInfoIsNeon(String info) {
		return info.contains("neon");
	}
}
