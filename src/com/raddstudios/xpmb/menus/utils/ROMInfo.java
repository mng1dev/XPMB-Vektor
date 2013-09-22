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

import java.util.ArrayList;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;

public class ROMInfo {

	public class ROMInfoNode_ROM {
		private String strROMName = null, strROMCRC = null, strROMMD5 = null,
				strROMSHA1 = null, strROMStatus = null;
		private int intROMSize = 0;

		public ROMInfoNode_ROM(String name, int size, String crc, String md5,
				String sha1, String status) {
			strROMName = name;
			intROMSize = size;
			strROMCRC = crc;
			strROMMD5 = md5;
			strROMSHA1 = sha1;
			strROMStatus = status;
		}

		public String getROMName() {
			return strROMName;
		}

		public int getROMSize() {
			return intROMSize;
		}

		public String getROMCRC() {
			return strROMCRC;
		}

		public String getROMMD5() {
			return strROMMD5;
		}

		public String getROMSHA1() {
			return strROMSHA1;
		}

		public String getROMStatus() {
			return strROMStatus;
		}
	}

	public class ROMInfoNode_Release {
		private String strReleaseName = null, strReleaseRegion = null;

		public ROMInfoNode_Release(String name, String region) {
			strReleaseName = name;
			strReleaseRegion = region;
		}

		public String getReleaseName() {
			return strReleaseName;
		}

		public String getReleaseRegion() {
			return strReleaseRegion;
		}
	}

	public class ROMInfoNode {
		String strGameName = null, strGameCloneOf = null,
				strGameDescription = null;
		ROMInfoNode_ROM rinROMData = null;
		Object[] aReleaseList = null;

		public ROMInfoNode(String name, String cloneOf, String description,
				ArrayList<ROMInfoNode_Release> releases, ROMInfoNode_ROM romData) {
			strGameName = name;
			strGameCloneOf = cloneOf;
			strGameDescription = description;
			aReleaseList = releases.toArray();
			rinROMData = romData;
		}

		public String getGameName() {
			return strGameName;
		}

		public String getGameDescription() {
			return strGameDescription;
		}

		public int getNumReleases() {
			return aReleaseList.length;
		}

		public ROMInfoNode_Release getReleaseData(int index) {
			return (ROMInfoNode_Release) aReleaseList[index];
		}

		public ROMInfoNode_ROM getROMData() {
			return rinROMData;
		}

	}

	public class ROMInfoHeader {
		private String strHeaderName = null, strHeaderDescription = null,
				strHeaderVersion = null, strHeaderDate = null,
				strHeaderAuthor = null, strHeaderUrl = null;

		public ROMInfoHeader(String name, String description, String version,
				String date, String author, String url) {
			strHeaderName = name;
			strHeaderDescription = description;
			strHeaderVersion = version;
			strHeaderDate = date;
			strHeaderAuthor = author;
			strHeaderUrl = url;
		}

		public String getHeaderName() {
			return strHeaderName;
		}

		public String getHeaderDescription() {
			return strHeaderDescription;
		}

		public String getHeaderVersion() {
			return strHeaderVersion;
		}

		public String getHeaderDate() {
			return strHeaderDate;
		}

		public String getHeaderAuthor() {
			return strHeaderAuthor;
		}

		public String getHeaderUrl() {
			return strHeaderUrl;
		}
	}

	public static final int TYPE_CRC = 0, TYPE_MD5 = 1, TYPE_SHA1 = 2;

	private Hashtable<String, ROMInfoNode> htItems = null;
	private int intCheckType = 0;
	private ROMInfoHeader mHeader = null;

	public ROMInfo(XmlResourceParser src, int checkType) {
		intCheckType = checkType;

		try {
			int eventType = src.getEventType();

			String cHeader = "";

			// Used in header
			String hn = null, hd = null, hv = null, hdt = null, ha = null, hu = null;
			// Used in game
			String gn = null, gc = null, gd = null;
			ArrayList<ROMInfoNode_Release> gr = null;
			ROMInfoNode_ROM grr = null;

			while (eventType != XmlPullParser.END_DOCUMENT) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					htItems = new Hashtable<String, ROMInfoNode>();
					break;
				case XmlResourceParser.START_TAG:
					cName = src.getName();
					if (cName.equals("header")) {
						cHeader = cName;
						break;
					}
					if (cName.equals("game")) {
						cHeader = cName;
						gr = new ArrayList<ROMInfoNode_Release>();
						gn = src.getAttributeValue(null, "name");
						gc = src.getAttributeValue(null, "cloneof");
						break;
					}
					if (cHeader.equals("header")) {
						if (cName.equals("name")) {
							hn = src.getText();
						}
						if (cName.equals("author")) {
							hd = src.getText();
						}
						if (cName.equals("version")) {
							hv = src.getText();
						}
						if (cName.equals("date")) {
							hdt = src.getText();
						}
						if (cName.equals("author")) {
							ha = src.getText();
						}
						if (cName.equals("url")) {
							hu = src.getText();
						}
					} else if (cHeader.equals("game")) {
						if (cName.equals("description")) {
							gd = src.getText();
						}
						if (cName.equals("release")) {
							gr.add(new ROMInfoNode_Release(src
									.getAttributeValue(null, "name"), src
									.getAttributeValue(null, "region")));
						}
						if (cName.equals("rom")) {
							grr = new ROMInfoNode_ROM(src.getAttributeValue(
									null, "name"), src.getAttributeIntValue(
									null, "size", 0), src.getAttributeValue(
									null, "crc"), src.getAttributeValue(null,
									"md5"),
									src.getAttributeValue(null, "sha1"),
									src.getAttributeValue(null, "status"));
						}
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = src.getName();
					if (cName.equals("game")) {
						switch (intCheckType) {
						case TYPE_MD5:
							htItems.put(
									grr.getROMMD5(),
									new ROMInfoNode(gn, gc, gd, gr, grr));
							break;
						case TYPE_SHA1:
							htItems.put(
									grr.getROMSHA1(),
									new ROMInfoNode(gn, gc, gd,	gr, grr));
							break;
						case TYPE_CRC:
						default:
							htItems.put(
									grr.getROMCRC(),
									new ROMInfoNode(gn, gc, gd, gr, grr));
							break;
						}
						gn = null;
						gc = null;
						gd = null;
						gr = null;
						grr = null;
						cHeader = "";
					}
					if (cName.equals("header")) {
						mHeader = new ROMInfoHeader(hn, hd, hv, hdt, ha, hu);
						hn = null;
						hd = null;
						hv = null;
						hdt = null;
						ha = null;
						hu = null;
						cHeader = "";
					}
					break;
				}
				eventType = src.next();
			}
			src.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getCheckType() {
		return intCheckType;
	}
	
	public ROMInfoHeader getHeader(){
		return mHeader;
	}

	public int getNumNodes(){
		return htItems.size();
	}
	
	public ROMInfoNode getNode(String key){
		return htItems.get(key);
	}
	
}
