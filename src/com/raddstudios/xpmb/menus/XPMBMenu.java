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

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.IntentFinishedListener;
import com.raddstudios.xpmb.utils.XPMB_MainMenu;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_LinearLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_RelativeLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

public class XPMBMenu extends XPMB_MainMenu {

	private class XPMBMenuItem {
		private String strID = null, strIcon = null;
		private ArrayList<XPMBMenuSubitem> alSubitems = null;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		// private XPMB_RelativeLayout rlParentContainer = null;
		private XPMB_TableLayout tlChildContainer = null;
		private int intCurSubitem = 0;

		public XPMBMenuItem(String id) {
			strID = id;
			alSubitems = new ArrayList<XPMBMenuSubitem>();
		}

		public String getID() {
			return strID;
		}

		public void setIcon(String icon) {
			strIcon = icon;
		}

		public String getIcon() {
			return strIcon;
		}

		public void addSubItem(XPMBMenuSubitem subitem) {
			alSubitems.add(subitem);
		}

		public XPMBMenuSubitem getSubItem(int index) {
			return alSubitems.get(index);
		}

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		public XPMB_ImageView getParentView() {
			return ivParentView;
		}

		public void setParentLabel(XPMB_TextView parent) {
			tvParentLabel = parent;
		}

		public XPMB_TextView getParentLabel() {
			return tvParentLabel;
		}

		// public void setParentContainer(XPMB_RelativeLayout container) {
		// rlParentContainer = container;
		// }

		// public XPMB_RelativeLayout getParentContainer() {
		// return rlParentContainer;
		// }

		public void setChildContainer(XPMB_TableLayout container) {
			tlChildContainer = container;
		}

		public XPMB_TableLayout getChildContainer() {
			return tlChildContainer;
		}

		public int getNumSubItems() {
			return alSubitems.size();
		}

		public void setSelectedSubItem(int subitem) {
			intCurSubitem = subitem;
		}

		public int getSelectedSubitem() {
			return intCurSubitem;
		}

		public XPMBMenuSubitem[] getSubitems() {
			return alSubitems.toArray(new XPMBMenuSubitem[0]);
		}

		public int getIndexOf(XPMBMenuSubitem value) {
			return alSubitems.indexOf(value);
		}
	}

	private class XPMBMenuSubitem {
		static final int TYPE_DUMMY = 0, TYPE_EXEC = 1, TYPE_SUBMENU = 2;
		static final int FLAG_MENU_HIDE_HALF = 0, FLAG_MENU_HIDE_FULL = 1,
				FLAG_MENU_HIGHLIGHT_ITEM = 2;

		private int intType = TYPE_DUMMY, intFlags = 0;
		private String strID = null, strIcon = null, strSubmenu;
		private Intent inttIntent = null;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private XPMB_TableRow trParentContainer = null;

		public XPMBMenuSubitem(String id, String type) {
			strID = id;
			setTypeFromString(type);
		}

		private void setTypeFromString(String data) {
			if (data == null) {
				intType = XPMBMenuSubitem.TYPE_DUMMY;
				return;
			}
			if (data.equalsIgnoreCase("exec")) {
				intType = XPMBMenuSubitem.TYPE_EXEC;
				return;
			}
			if (data.startsWith("submenu")) {
				intType = XPMBMenuSubitem.TYPE_SUBMENU;
				String flag = data.substring(data.indexOf('.') + 1);
				if (flag.equals("half")) {
					intFlags = FLAG_MENU_HIDE_HALF;
				}
				if (flag.equals("full")) {
					intFlags = FLAG_MENU_HIDE_FULL;
				}
				if (flag.equals("highlight")) {
					intFlags = FLAG_MENU_HIGHLIGHT_ITEM;
				}
				return;
			}
			intType = XPMBMenuSubitem.TYPE_DUMMY;
		}

		public int getType() {
			return intType;
		}

		public int getFlags() {
			return intFlags;
		}

		public String getID() {
			return strID;
		}

		public void setExecIntent(Intent intent) {
			inttIntent = intent;
		}

		public Intent getExecIntent() {
			return inttIntent;
		}

		public void setSubmenu(String submenu) {
			strSubmenu = submenu;
		}

		public String getSubmenu() {
			return strSubmenu;
		}

		public void setIcon(String icon) {
			strIcon = icon;
		}

		public String getIcon() {
			return strIcon;
		}

		public void setParentView(XPMB_ImageView parent) {
			ivParentView = parent;
		}

		@SuppressWarnings("unused")
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

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_MENUITEM = 2, ANIM_CENTER_ON_SUBMENUITEM = 3, ANIM_HIDE_MENU_HALF = 4,
			ANIM_HIDE_MENU_FULL = 5, ANIM_MENU_MOVE_LEFT = 6, ANIM_MENU_MOVE_RIGHT = 7,
			ANIM_SHOW_MENU_HALF = 8, ANIM_SHOW_MENU_FULL = 9, ANIM_HIGHLIGHT_MENU_PRE = 10,
			ANIM_HIGHLIGHT_MENU_POS = 11;

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
		private float pY = 0, dispY = 0, pX = 0, dispX = 0;
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

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(250);
				pY = alItems.get(cMenuItem).getChildContainer().getTopMargin();
				intAnimItem = alItems.get(cMenuItem).getSelectedSubitem();
				intNextItem = intAnimItem - 1;
				dispY = (pxFromDip(48) - (pxFromDip(80) * intNextItem)) - pY;
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(250);
				pY = alItems.get(cMenuItem).getChildContainer().getTopMargin();
				intAnimItem = alItems.get(cMenuItem).getSelectedSubitem();
				intNextItem = intAnimItem + 1;
				dispY = (pxFromDip(48) - (pxFromDip(80) * intNextItem)) - pY;
				break;
			case ANIM_MENU_MOVE_LEFT:
				mOwner.setDuration(250);
				pX = tlRoot.getLeftMargin();
				intAnimItem = cMenuItem;
				intNextItem = intAnimItem - 1;
				dispX = (pxFromDip(56) - (pxFromDip(80) * intNextItem)) - pX;
				alItems.get(intNextItem).getChildContainer().setVisibility(View.VISIBLE);
				break;
			case ANIM_MENU_MOVE_RIGHT:
				mOwner.setDuration(250);
				pX = tlRoot.getLeftMargin();
				intAnimItem = cMenuItem;
				intNextItem = intAnimItem + 1;
				dispX = (pxFromDip(56) - (pxFromDip(80) * intNextItem)) - pX;
				alItems.get(intNextItem).getChildContainer().setVisibility(View.VISIBLE);
				break;
			case ANIM_CENTER_ON_MENUITEM:
				mOwner.setDuration(250);
				pX = tlRoot.getLeftMargin();
				intAnimItem = cMenuItem;
				intNextItem = (int) mArgs[0];
				dispX = (pxFromDip(56) - (pxFromDip(80) * intNextItem)) - pX;
				alItems.get(intNextItem).getChildContainer().setVisibility(View.VISIBLE);
				break;
			case ANIM_CENTER_ON_SUBMENUITEM:
				mOwner.setDuration(250);
				pY = alItems.get(cMenuItem).getChildContainer().getTopMargin();
				intAnimItem = alItems.get(cMenuItem).getSelectedSubitem();
				intNextItem = (int) mArgs[0];
				dispY = (pxFromDip(48) - (pxFromDip(80) * intNextItem)) - pY;
				break;
			case ANIM_HIDE_MENU_HALF:
				((XPMB_ImageView) getRootActivity().findViewById(R.id.ivSubmenuShown))
						.setLeftMargin(0);
				mOwner.setDuration(250);
				pX = tlRoot.getLeftMargin();
				pY = alItems.get(cMenuItem).getChildContainer().getLeftMargin();
				dispX = pxFromDip(-96);
				dispY = pxFromDip(-96);
				break;
			case ANIM_SHOW_MENU_HALF:
				mOwner.setDuration(250);
				pX = tlRoot.getLeftMargin();
				pY = alItems.get(cMenuItem).getChildContainer().getLeftMargin();
				dispX = pxFromDip(96);
				dispY = pxFromDip(96);
				break;
			case ANIM_HIGHLIGHT_MENU_PRE:
				mOwner.setDuration(250);
				((XPMB_ImageView) getRootActivity().findViewById(R.id.ivSubmenuShown))
						.setLeftMargin(pxFromDip(88));
				pX = tlRoot.getLeftMargin();
				pY = alItems.get(cMenuItem).getChildContainer().getLeftMargin();
				dispX = pxFromDip(-56);
				dispY = pxFromDip(-56);
				break;
			case ANIM_HIGHLIGHT_MENU_POS:
				mOwner.setDuration(250);
				pX = tlRoot.getLeftMargin();
				pY = alItems.get(cMenuItem).getChildContainer().getLeftMargin();
				dispX = pxFromDip(56);
				dispY = pxFromDip(56);
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			float alphaO = 0, alphaI = 0, alphaA = 0, scaleO = 0, scaleI = 0;
			int marginO = 0, marginI = 0;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_CENTER_ON_SUBMENUITEM:
				marginO = (int) (pxFromDip(80) - (pxFromDip(80) * completion));
				marginI = (int) (pxFromDip(80) * completion);
				alphaO = 1.0f - completion;
				alphaI = completion;

				XPMBMenuItem xmi = alItems.get(cMenuItem);

				xmi.getChildContainer().setTopMargin((int) (pY + (dispY * completion)));
				xmi.getSubItem(intAnimItem).getParentLabel().setAlphaLevel(alphaO);
				xmi.getSubItem(intNextItem).getParentLabel().setAlphaLevel(alphaI);
				xmi.getSubItem(intAnimItem).getParentContainer().setTopMargin(marginO);
				xmi.getSubItem(intNextItem).getParentContainer().setTopMargin(marginI);
				break;
			case ANIM_MENU_MOVE_LEFT:
			case ANIM_MENU_MOVE_RIGHT:
			case ANIM_CENTER_ON_MENUITEM:
				scaleO = 1.0f - (0.3f * completion);
				alphaO = 1.0f - completion;
				scaleI = 0.7f + (0.3f * completion);
				alphaI = completion;

				tlRoot.setLeftMargin((int) (pX + (dispX * completion)));
				alItems.get(intAnimItem).getParentView().setViewScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setViewScaleY(scaleO);
				alItems.get(intAnimItem).getParentLabel().setAlphaLevel(alphaO);
				alItems.get(intAnimItem).getChildContainer().setAlphaLevel(alphaO);
				alItems.get(intAnimItem).getChildContainer().invalidate();
				alItems.get(intNextItem).getParentView().setViewScaleX(scaleI);
				alItems.get(intNextItem).getParentView().setViewScaleY(scaleI);
				alItems.get(intNextItem).getParentLabel().setAlphaLevel(alphaI);
				alItems.get(intNextItem).getChildContainer().setAlphaLevel(alphaI);
				alItems.get(intNextItem).getChildContainer().invalidate();

				if (completion == 1.0f) {
					alItems.get(intAnimItem).getChildContainer().setVisibility(View.INVISIBLE);
				}

				break;
			case ANIM_HIDE_MENU_HALF:
			case ANIM_SHOW_MENU_HALF:
			case ANIM_HIGHLIGHT_MENU_PRE:
			case ANIM_HIGHLIGHT_MENU_POS:
				if (intAnimType == ANIM_HIDE_MENU_HALF || intAnimType == ANIM_HIGHLIGHT_MENU_PRE) {
					alphaI = 1.0f - completion;
					alphaO = 1.0f - (0.5f * completion);
					alphaA = completion;
				} else if (intAnimType == ANIM_SHOW_MENU_HALF
						|| intAnimType == ANIM_HIGHLIGHT_MENU_POS) {
					alphaI = completion;
					alphaO = 0.5f + (0.5f * completion);
					alphaA = 1.0f - completion;
				}

				((XPMB_ImageView) getRootActivity().findViewById(R.id.ivSubmenuShown))
						.setAlphaLevel(alphaA);
				tlRoot.setLeftMargin((int) (pX + (dispX * completion)));
				alItems.get(cMenuItem).getChildContainer()
						.setLeftMargin((int) (pY + (dispY * completion)));

				if (intAnimType == ANIM_SHOW_MENU_HALF || intAnimType == ANIM_HIDE_MENU_HALF) {
					alItems.get(cMenuItem).getParentLabel().setAlphaLevel(alphaI);
				}
				for (XPMBMenuItem xmihf : alItems) {
					int idx = alItems.indexOf(xmihf);

					if (idx != cMenuItem) {
						xmihf.getParentView().setAlphaLevel(alphaI);
					}
				}
				for (XPMBMenuSubitem xms : alItems.get(cMenuItem).getSubitems()) {
					int idy = alItems.get(cMenuItem).getIndexOf(xms);

					if (idy == alItems.get(cMenuItem).getSelectedSubitem()) {
						if (intAnimType == ANIM_SHOW_MENU_HALF
								|| intAnimType == ANIM_HIDE_MENU_HALF) {
							xms.getParentContainer().setAlphaLevel(alphaI);
						} else {
							xms.getParentLabel().setAlphaLevel(alphaI);
						}
					} else {
						xms.getParentContainer().setAlphaLevel(alphaO);
						if (cMenuItem == 0) {
							xms.getParentLabel().setAlphaLevel(alphaI);
						}
					}
				}
				break;
			case ANIM_NONE:
			default:
				break;
			}
		}

		@Override
		public void onAnimationCancel(Animator arg0) {
			getRootActivity().enableTouchEvents(true);

		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			getRootActivity().enableTouchEvents(true);

		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
			getRootActivity().enableTouchEvents(false);
		}
	};

	private ArrayList<XPMBMenuItem> alItems = null;
	private int cMenuItem = 0;
	private XmlResourceParser xrpRes = null;
	private XPMB_LinearLayout tlRoot = null;
	private boolean firstBackPress = false, isFocused = true;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;

	public XPMBMenu(XmlResourceParser source, Handler messageBus, ViewGroup rootView,
			XPMB_Activity root) {
		super(root, messageBus, rootView);
		xrpRes = source;

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);
	}

	@Override
	public void doInit() {

		try {
			int eventType = xrpRes.getEventType();
			boolean done = false;
			XPMBMenuItem cItem = null;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String cName = null;

				switch (eventType) {
				case XmlResourceParser.START_DOCUMENT:
					alItems = new ArrayList<XPMBMenuItem>();
					break;
				case XmlResourceParser.START_TAG:
					cName = xrpRes.getName();
					if (cName.equals("item")) {
						cItem = new XPMBMenuItem(xrpRes.getAttributeValue(null, "id"));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cItem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
					}
					if (cName.equals("subitem")) {
						XPMBMenuSubitem cSubitem = new XPMBMenuSubitem(xrpRes.getAttributeValue(
								null, "id"), xrpRes.getAttributeValue(null, "type"));
						String cAtt = xrpRes.getAttributeValue(null, "icon");
						if (cAtt != null) {
							cSubitem.setIcon(cAtt);
						} else {
							cItem.setIcon("ui_xmb_default_icon");
						}
						cAtt = xrpRes.getAttributeValue(null, "exec");
						if (cAtt != null) {
							Intent cIntent = new Intent(Intent.ACTION_MAIN);
							cIntent.setComponent(ComponentName.unflattenFromString(cAtt));
							cSubitem.setExecIntent(cIntent);
						}
						cAtt = xrpRes.getAttributeValue(null, "submenu");
						if (cAtt != null) {
							cSubitem.setSubmenu(cAtt);
						}
						cItem.addSubItem(cSubitem);
					}
					break;
				case XmlResourceParser.END_TAG:
					cName = xrpRes.getName();
					if (cName.equals("item")) {
						alItems.add(cItem);
					}
					break;
				}
				eventType = xrpRes.next();
			}
			xrpRes.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO Find a correct use for this procedure or give it a GTFO
	// private void doCenterOnMenuItemPre() {
	// XPMBMenuItem xmi = alItems.get(cMenuItem);

	// xmi.getParentView().setScaleX(0.7f);
	// xmi.getParentView().setScaleY(0.7f);
	// xmi.getParentLabel().setAlpha(0.0f);
	// xmi.getChildContainer().setVisibility(View.INVISIBLE);
	// xmi.getChildContainer().setAlpha(0.0f);
	// }

	private void centerOnNearestMenuItem() {
		float cPosX = tlRoot.getLeftMargin();
		int destItem = ((int) (pxFromDip(56) - cPosX) / pxFromDip(90));
		if (destItem < 0) {
			destItem = 0;
		} else if (destItem > (alItems.size() - 1)) {
			destItem = (alItems.size() - 1);
		}
		centerOnMenuItem(destItem);
	}

	private void centerOnNearestSubmenuItem() {
		float cPosY = alItems.get(cMenuItem).getChildContainer().getTopMargin();
		int destItem = ((int) (pxFromDip(56) - cPosY) / pxFromDip(80));
		if (destItem < 0) {
			destItem = 0;
		} else if (destItem > (alItems.get(cMenuItem).getNumSubItems() - 1)) {
			destItem = (alItems.get(cMenuItem).getNumSubItems() - 1);
		}
		centerOnSubmenuItem(destItem);
	}

	private void centerOnMenuItem(int index) {
		aUIAnimatorW.setArguments(new float[] { index });
		aUIAnimatorW.setAnimationType(ANIM_CENTER_ON_MENUITEM);
		aUIAnimator.start();
		cMenuItem = index;
	}

	private void centerOnSubmenuItem(int index) {
		aUIAnimatorW.setArguments(new float[] { index });
		aUIAnimatorW.setAnimationType(ANIM_CENTER_ON_SUBMENUITEM);
		aUIAnimator.start();
		alItems.get(cMenuItem).setSelectedSubItem(index);
	}

	private OnTouchListener mTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			if (isFocused) {
				getRootActivity().setTouchedChildView(arg0);
			}
			return false;
		}
	};

	@Override
	public void sendClickEventToView(View v) {
		if (v.getTag() != null) {
			int[] tags = (int[]) v.getTag();
			if (tags[1] == -1) {
				centerOnMenuItem(tags[0]);
			} else {
				execCustItem(tags[1]);
			}
		}
	}

	@Override
	public void parseInitLayout() {

		tlRoot = new XPMB_LinearLayout(getRootView().getContext());
		tlRoot.setOrientation(LinearLayout.HORIZONTAL);
		RelativeLayout.LayoutParams rootP = new RelativeLayout.LayoutParams(
				pxFromDip(90 * alItems.size()), pxFromDip(80));
		rootP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rootP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rootP.leftMargin = pxFromDip(56);
		rootP.topMargin = pxFromDip(48);
		tlRoot.setLayoutParams(rootP);

		for (XPMBMenuItem xmi : alItems) {
			int idx = alItems.indexOf(xmi);

			XPMB_RelativeLayout cCont = new XPMB_RelativeLayout(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());
			cCont.setId(getNextID());

			// Setup Item Container
			LinearLayout.LayoutParams cContP = new LinearLayout.LayoutParams(pxFromDip(80),
					pxFromDip(80));
			cCont.setLayoutParams(cContP);

			// Setup Item Icon
			RelativeLayout.LayoutParams cIconP = new RelativeLayout.LayoutParams(pxFromDip(80),
					pxFromDip(80));
			cIconP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			cIconP.addRule(RelativeLayout.CENTER_HORIZONTAL);
			cIcon.setLayoutParams(cIconP);
			cIcon.resetScaleBase();
			if (idx > 0) {
				cIcon.setViewScaleX(0.7f);
				cIcon.setViewScaleY(0.7f);
			}
			cIcon.setImageDrawable(getRootView().getResources().getDrawable(
					getRootView().getResources().getIdentifier("drawable/" + xmi.getIcon(), null,
							getRootView().getContext().getPackageName())));
			cIcon.setTag(new int[] { idx, -1 });
			cIcon.setOnTouchListener(mTouchListener);

			// Setup Item Label
			RelativeLayout.LayoutParams cLabelP = new RelativeLayout.LayoutParams(pxFromDip(90),
					pxFromDip(20));
			cLabelP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			cLabel.setLayoutParams(cLabelP);
			cLabel.setText(xmi.getID());
			cLabel.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Small);
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(4, 0, 0, Color.WHITE);
			if (idx > 0) {
				cLabel.setAlphaLevel(0.0f);
			}
			cLabel.setTag(new int[] { idx, -1 });
			cLabel.setOnTouchListener(mTouchListener);

			// Add everything to their holder classes and containers
			cCont.addView(cIcon);
			cCont.addView(cLabel);
			xmi.setParentView(cIcon);
			xmi.setParentLabel(cLabel);
			// xmi.setParentContainer(cCont);
			tlRoot.addView(cCont);

			// Setup Subitems
			if (xmi.getNumSubItems() != 0) {
				XPMB_TableLayout cSubCont = new XPMB_TableLayout(getRootView().getContext());

				// Setup Subitems Root Container
				RelativeLayout.LayoutParams cSubContP = new RelativeLayout.LayoutParams(
						pxFromDip(416), pxFromDip(80 + (80 * xmi.getNumSubItems())));
				cSubContP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				cSubContP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				cSubContP.leftMargin = pxFromDip(56);
				cSubContP.topMargin = pxFromDip(48);
				cSubCont.setLayoutParams(cSubContP);
				if (idx > 0) {
					cSubCont.setAlphaLevel(0.0f);
					cSubCont.setVisibility(View.INVISIBLE);
				}

				for (XPMBMenuSubitem xsi : xmi.getSubitems()) {
					int idy = xmi.getIndexOf(xsi);

					XPMB_TableRow cSCont = new XPMB_TableRow(getRootView().getContext());
					XPMB_ImageView cSIcon = new XPMB_ImageView(getRootView().getContext());
					XPMB_TextView cSLabel = new XPMB_TextView(getRootView().getContext());
					cSIcon.setId(getNextID());
					cSLabel.setId(getNextID());
					cSCont.setId(getNextID());

					// Setup Subitem Container
					TableLayout.LayoutParams cSContP = new TableLayout.LayoutParams(pxFromDip(416),
							pxFromDip(80));
					if (idy == 0) {
						cSContP.topMargin = pxFromDip(80);
					}
					cSCont.setLayoutParams(cSContP);

					// Setup Subitem Icon
					TableRow.LayoutParams cSIconP = new TableRow.LayoutParams((int) pxFromDip(80),
							(int) pxFromDip(80));
					cSIconP.column = 0;
					cSIcon.setLayoutParams(cSIconP);
					cSIcon.setImageDrawable(getRootView().getResources().getDrawable(
							getRootView().getResources().getIdentifier("drawable/" + xsi.getIcon(),
									null, getRootView().getContext().getPackageName())));
					cSIcon.setTag(new int[] { idx, idy });
					cSIcon.setOnTouchListener(mTouchListener);

					// Setup Subitem Label
					TableRow.LayoutParams cSLabelP = new TableRow.LayoutParams(pxFromDip(320),
							pxFromDip(80));
					cSIconP.column = 1;
					cSLabel.setLayoutParams(cSLabelP);
					cSLabel.setText(xsi.getID());
					cSLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
					cSLabel.setTextAppearance(getRootView().getContext(),
							android.R.style.TextAppearance_Medium);
					cSLabel.setShadowLayer(8, 0, 0, Color.WHITE);
					if (idx > 0 && idy > 0) {
						cSLabel.setAlphaLevel(0.0f);
					}
					cSLabel.setTag(new int[] { idx, idy });
					cSLabel.setOnTouchListener(mTouchListener);

					// Add everything to their holder classes and containers
					cSCont.addView(cSIcon);
					cSCont.addView(cSLabel);
					cSubCont.addView(cSCont);
					xsi.setParentView(cSIcon);
					xsi.setParentLabel(cSLabel);
					xsi.setParentContainer(cSCont);

				}
				getRootView().addView(cSubCont);
				xmi.setChildContainer(cSubCont);
				cSubCont.invalidate();
			}
		}
		getRootView().addView(tlRoot);
		tlRoot.invalidate();
	}

	@Override
	public void sendKeyDown(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_LEFT:
			firstBackPress = false;
			moveLeft();
			break;
		case XPMB_Main.KEYCODE_RIGHT:
			firstBackPress = false;
			moveRight();
			break;
		case XPMB_Main.KEYCODE_UP:
			firstBackPress = false;
			moveUp();
			break;
		case XPMB_Main.KEYCODE_DOWN:
			firstBackPress = false;
			moveDown();
			break;
		case XPMB_Main.KEYCODE_CROSS:
			firstBackPress = false;
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			if (!firstBackPress) {
				firstBackPress = true;
				Toast tst = Toast.makeText(getRootActivity().getWindow().getContext(),
						getRootActivity().getString(R.string.strBackKeyHint), Toast.LENGTH_SHORT);
				tst.show();
			} else {
				getRootActivity().requestActivityEnd();
			}
			break;
		}
	}

	private void moveRight() {
		if (cMenuItem == (alItems.size() - 1)) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_RIGHT);
		aUIAnimator.start();

		++cMenuItem;
	}

	private void moveLeft() {
		if (cMenuItem == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_LEFT);
		aUIAnimator.start();

		--cMenuItem;
	}

	private void moveDown() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == (alItems.get(cMenuItem).getNumSubItems() - 1)) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_DOWN);
		aUIAnimator.start();

		alItems.get(cMenuItem).setSelectedSubItem(alItems.get(cMenuItem).getSelectedSubitem() + 1);
	}

	private void moveUp() {
		if (alItems.get(cMenuItem).getSelectedSubitem() == 0) {
			return;
		}

		aUIAnimatorW.setAnimationType(ANIM_MENU_MOVE_UP);
		aUIAnimator.start();

		alItems.get(cMenuItem).setSelectedSubItem(alItems.get(cMenuItem).getSelectedSubitem() - 1);
	}

	private void execCustItem(int index) {
		final XPMBMenuSubitem xsi = alItems.get(cMenuItem).getSubItem(index);
		switch (xsi.getType()) {
		case XPMBMenuSubitem.TYPE_EXEC:
			Intent cExInt = xsi.getExecIntent();
			if (getRootActivity().isActivityAvailable(cExInt)) {
				getRootActivity().showLoadingAnim(true);
				getRootActivity().postIntentStartWait(new IntentFinishedListener() {
					@Override
					public void onFinished(Intent intent) {
						getRootActivity().showLoadingAnim(false);
					}
				}, cExInt);
			} else {
				Toast tst = Toast.makeText(
						getRootActivity().getWindow().getContext(),
						getRootActivity().getString(R.string.strAppNotInstalled).replace("%s",
								cExInt.getComponent().getPackageName()), Toast.LENGTH_SHORT);
				tst.show();
			}
			break;
		case XPMBMenuSubitem.TYPE_SUBMENU:
			doPreExecute();
			getMessageBus().postDelayed(new Runnable() {

				@Override
				public void run() {
					getRootActivity().preloadSubmenu(xsi.getSubmenu());
				}

			}, 260);
			break;
		}
	}

	private void execSelectedItem() {
		execCustItem(alItems.get(cMenuItem).getSelectedSubitem());
	}

	private void doPreExecute() {
		isFocused = false;
		XPMBMenuSubitem xsi = alItems.get(cMenuItem).getSubItem(
				alItems.get(cMenuItem).getSelectedSubitem());

		switch (xsi.getFlags()) {
		case XPMBMenuSubitem.FLAG_MENU_HIDE_HALF:
			aUIAnimatorW.setAnimationType(ANIM_HIDE_MENU_HALF);
			aUIAnimator.start();
			break;
		case XPMBMenuSubitem.FLAG_MENU_HIDE_FULL:
			aUIAnimatorW.setAnimationType(ANIM_HIDE_MENU_FULL);
			aUIAnimator.start();
			break;
		case XPMBMenuSubitem.FLAG_MENU_HIGHLIGHT_ITEM:
			aUIAnimatorW.setAnimationType(ANIM_HIGHLIGHT_MENU_PRE);
			aUIAnimator.start();
			break;
		}
	}

	@Override
	public void postExecuteFinished() {
		XPMBMenuSubitem xsi = alItems.get(cMenuItem).getSubItem(
				alItems.get(cMenuItem).getSelectedSubitem());

		switch (xsi.getFlags()) {
		case XPMBMenuSubitem.FLAG_MENU_HIDE_HALF:
			aUIAnimatorW.setAnimationType(ANIM_SHOW_MENU_HALF);
			aUIAnimator.start();
			break;
		case XPMBMenuSubitem.FLAG_MENU_HIDE_FULL:
			aUIAnimatorW.setAnimationType(ANIM_SHOW_MENU_FULL);
			aUIAnimator.start();
			break;
		case XPMBMenuSubitem.FLAG_MENU_HIGHLIGHT_ITEM:
			aUIAnimatorW.setAnimationType(ANIM_HIGHLIGHT_MENU_POS);
			aUIAnimator.start();
			break;
		}
		isFocused = true;
	}

	@Override
	public void doCleanup() {
		tlRoot.removeAllViews();
		getRootView().removeView(tlRoot);
	}
}
