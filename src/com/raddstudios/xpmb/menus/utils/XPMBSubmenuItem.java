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

package com.raddstudios.xpmb.menus.utils;

import android.graphics.Bitmap;

public class XPMBSubmenuItem {

	private String strLabel = null;
	private Bitmap bmImage = null;
	private int intParentLabel = -1, intParentView = -1;

	public XPMBSubmenuItem(String label, Bitmap image) {
		strLabel = label;
		bmImage = image;
	}

	public String getLabel() {
		return strLabel;
	}

	public Bitmap getImage() {
		return bmImage;
	}

	public void setParentView(int id) {
		intParentView = id;
	}

	public int getParentView() {
		return intParentView;
	}

	public void setParentLabel(int id) {
		intParentLabel = id;
	}

	public int getParentLabel() {
		return intParentLabel;
	}

}
