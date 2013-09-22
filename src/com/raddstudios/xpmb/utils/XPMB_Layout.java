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

import com.raddstudios.xpmb.utils.backports.XPMB_View;

import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

public class XPMB_Layout {

	private XPMB_Activity mRoot = null;
	private ViewGroup mRootView = null;
	private Handler hMessageBus = null;
	private int cID = 0xC0DD;

	public XPMB_Layout(XPMB_Activity root, Handler messageBus, ViewGroup rootView, int idOffset) {
		mRoot = root;
		hMessageBus = messageBus;
		mRootView = rootView;
		cID += idOffset;
	}

	protected Handler getMessageBus() {
		return hMessageBus;
	}

	protected ViewGroup getRootView() {
		return mRootView;
	}
	
	protected XPMB_View getView(int id){
		return (XPMB_View) mRootView.findViewById(id);
	}

	protected XPMB_Activity getRootActivity() {
		return mRoot;
	}

	public void doInit() {
	}

	public void parseInitLayout() {
	}

	public void sendKeyDown(int keyCode) {
	}

	public void sendKeyUp(int keyCode) {
	}
	
	public void sendKeyHold(int keyCode){
	}
	
	public void sendClickEventToView(View v){
	}

	protected int pxFromDip(int dip) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, mRoot
				.getResources().getDisplayMetrics());
	}

	public void doCleanup() {
	}

	public void requestDestroy() {
	}

	protected int getNextID() {
		cID++;
		return cID;
	}
}
