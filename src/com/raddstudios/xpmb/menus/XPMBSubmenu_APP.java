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
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Activity.IntentFinishedListener;
import com.raddstudios.xpmb.utils.XPMB_Layout;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_TableLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TableRow;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

public class XPMBSubmenu_APP extends XPMB_Layout {

	class XPMBSubmenuItem_APP {

		private Drawable drAppIcon = null;
		private String strAppName = null;
		private Intent intAppIntent = null;
		private XPMB_ImageView ivParentView = null;
		private XPMB_TextView tvParentLabel = null;
		private TableRow vgParentContainer = null;

		public XPMBSubmenuItem_APP(String appName, Drawable appIcon, Intent appIntent) {
			strAppName = appName;
			drAppIcon = appIcon;
			intAppIntent = appIntent;
		}

		public String getAppName() {
			return strAppName;
		}

		public Drawable getAppIcon() {
			return drAppIcon;
		}

		public Intent getAppIntent() {
			return intAppIntent;
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

		public TableRow getParentContainer() {
			return vgParentContainer;
		}

		public void setParentContainer(TableRow parentContainer) {
			this.vgParentContainer = parentContainer;
		}
	}

	private ArrayList<XPMBSubmenuItem_APP> alItems = null;
	private int intSelItem = 0;
	//private boolean isFocused = true;
	private XPMB_TextView tv_no_app = null;

	private XPMB_TableLayout tlRoot = null;

	public XPMBSubmenu_APP(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView, 0x1000);

		alItems = new ArrayList<XPMBSubmenuItem_APP>();
	}

	@Override
	public void doInit() {
		PackageManager pm = getRootActivity().getPackageManager();
		Intent filter = new Intent(Intent.ACTION_MAIN);
		filter.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> ri = pm.queryIntentActivities(filter, PackageManager.GET_META_DATA);
		for (ResolveInfo r : ri) {
			if (r.activityInfo.packageName.equals(getRootActivity().getPackageName()) ||
					r.activityInfo.packageName.equals("com.android.vending") ||
					r.activityInfo.packageName.equals("com.vektor.romdownloader")) {
				continue;
			}
			
			alItems.add(new XPMBSubmenuItem_APP(r.loadLabel(pm).toString(), r.loadIcon(pm), pm
					.getLaunchIntentForPackage(r.activityInfo.packageName)));
		}
	}
	
	@Override
	public void parseInitLayout() {
		if (alItems.size() == 0) {
			tv_no_app = new XPMB_TextView(getRootView().getContext());
			RelativeLayout.LayoutParams lp_ng = new RelativeLayout.LayoutParams(pxFromDip(320),
					pxFromDip(100));
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp_ng.leftMargin = pxFromDip(48);
			lp_ng.topMargin = pxFromDip(128);
			tv_no_app.setLayoutParams(lp_ng);
			tv_no_app.setText(getRootActivity().getText(R.string.strNoApps));
			tv_no_app.setTextColor(Color.WHITE);
			tv_no_app.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_app.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_app.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			getRootView().addView(tv_no_app);
			return;
		}

		tlRoot = new XPMB_TableLayout(getRootView().getContext());
		RelativeLayout.LayoutParams rootP = new RelativeLayout.LayoutParams(pxFromDip(396),
				pxFromDip(160 + (50 * alItems.size())));
		rootP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rootP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rootP.leftMargin = pxFromDip(48);
		rootP.topMargin = pxFromDip(88);
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_APP xsi : alItems) {
			int idx = alItems.indexOf(xsi);

			XPMB_TableRow cCont = new XPMB_TableRow(getRootView().getContext());
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cCont.setId(getNextID());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());

			// Setup Container
			TableLayout.LayoutParams cContP = new TableLayout.LayoutParams(pxFromDip(386),
					pxFromDip(50));
			cCont.setLayoutParams(cContP);

			// Setup Icon
			TableRow.LayoutParams cIconP = new TableRow.LayoutParams(pxFromDip(50), pxFromDip(50));
			cIconP.column = 0;
			if (idx == 0) {
				cIconP.bottomMargin = pxFromDip(16);
				cIconP.topMargin = pxFromDip(16);
			}
			cIcon.setLayoutParams(cIconP);
			cIcon.resetScaleBase();

			if (idx == 0) {
				cIcon.setViewScaleX(2.56f);
				cIcon.setViewScaleY(2.56f);
			}
			cIcon.setImageDrawable(xsi.getAppIcon());

			// Setup Label
			TableRow.LayoutParams cLabelParams = new TableRow.LayoutParams((int) pxFromDip(320),
					(int) pxFromDip(50));
			cLabelParams.leftMargin = pxFromDip(16);
			cLabelParams.column = 1;
			cLabelParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			cLabel.setLayoutParams(cLabelParams);

			cLabel.setText(xsi.getAppName());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			if (idx != 0) {
				cLabel.setAlphaLevel(0.0f);
			}

			// Add everything to their parent containers and holders
			cCont.addView(cIcon);
			cCont.addView(cLabel);
			tlRoot.addView(cCont);
			xsi.setParentView(cIcon);
			xsi.setParentLabel(cLabel);
			xsi.setParentContainer(cCont);
		}
		// Prevent Image scale changes to distort layout during animations
		XPMB_TableRow tlFiller = new XPMB_TableRow(getRootView().getContext());
		XPMB_ImageView ivFiller = new XPMB_ImageView(getRootView().getContext());
		XPMB_TextView tvFiller = new XPMB_TextView(getRootView().getContext());
		TableRow.LayoutParams iv_f_lp = new TableRow.LayoutParams(pxFromDip(128), pxFromDip(128));
		TableRow.LayoutParams tv_f_lp = new TableRow.LayoutParams(pxFromDip(320), pxFromDip(128));
		iv_f_lp.column = 0;
		tv_f_lp.column = 1;
		ivFiller.setLayoutParams(iv_f_lp);
		tvFiller.setLayoutParams(tv_f_lp);
		tlFiller.addView(ivFiller);
		tlFiller.addView(tvFiller);
		tlRoot.addView(tlFiller);
		getRootView().addView(tlRoot);
	}

	@Override
	public void sendKeyUp(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_DOWN:
			moveDown();
			break;
		case XPMB_Main.KEYCODE_UP:
			moveUp();
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

	private void moveDown() {
		if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
			return;
		}

		final float pY = tlRoot.getTopMargin();
		final int intAnimItem = intSelItem;

		ValueAnimator va_md = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_md.setInterpolator(new DecelerateInterpolator());
		va_md.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				int posY = (int) (pY - (pxFromDip(50) * completion));
				float scaleO = 2.56f - (1.56f * completion);
				float scaleI = 1.0f + (1.56f * completion);
				float alphaO = 1.0f - completion;
				float alphaI = completion;
				int marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				int marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setTopMargin(posY);
				alItems.get(intAnimItem).getParentView().setViewScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setViewScaleY(scaleO);
				alItems.get(intAnimItem).getParentView().setTopMargin(marginO);
				alItems.get(intAnimItem).getParentView().setBottomMargin(marginO);
				alItems.get(intAnimItem).getParentLabel().setAlphaLevel(alphaO);
				alItems.get(intAnimItem + 1).getParentView().setViewScaleX(scaleI);
				alItems.get(intAnimItem + 1).getParentView().setViewScaleY(scaleI);
				alItems.get(intAnimItem + 1).getParentLabel().setAlphaLevel(alphaI);
				alItems.get(intAnimItem + 1).getParentView().setTopMargin(marginI);
				alItems.get(intAnimItem + 1).getParentView().setBottomMargin(marginI);
			}
		});

		va_md.setDuration(150);
		getRootActivity().lockKeys(true);
		va_md.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		++intSelItem;
	}

	private void moveUp() {
		if (intSelItem == 0 || alItems.size() == 0) {
			return;
		}

		final int pY = tlRoot.getTopMargin();
		final int intAnimItem = intSelItem;

		ValueAnimator va_mu = ValueAnimator.ofFloat(0.0f, 1.0f);
		va_mu.setInterpolator(new DecelerateInterpolator());
		va_mu.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator arg0) {
				float completion = (Float) arg0.getAnimatedValue();

				int posY = (int) (pY + (pxFromDip(50) * completion));
				float scaleO = 2.56f - (1.56f * completion);
				float scaleI = 1.0f + (1.56f * completion);
				float alphaO = 1.0f - completion;
				float alphaI = completion;
				int marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				int marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setTopMargin(posY);
				alItems.get(intAnimItem).getParentView().setViewScaleX(scaleO);
				alItems.get(intAnimItem).getParentView().setViewScaleY(scaleO);
				alItems.get(intAnimItem).getParentView().setTopMargin(marginO);
				alItems.get(intAnimItem).getParentView().setBottomMargin(marginO);
				alItems.get(intAnimItem).getParentLabel().setAlphaLevel(alphaO);
				alItems.get(intAnimItem - 1).getParentView().setViewScaleX(scaleI);
				alItems.get(intAnimItem - 1).getParentView().setViewScaleY(scaleI);
				alItems.get(intAnimItem - 1).getParentLabel().setAlphaLevel(alphaI);
				alItems.get(intAnimItem - 1).getParentView().setTopMargin(marginI);
				alItems.get(intAnimItem - 1).getParentView().setBottomMargin(marginI);
			}
		});

		va_mu.setDuration(150);
		getRootActivity().lockKeys(true);
		va_mu.start();
		getMessageBus().postDelayed(new Runnable() {

			@Override
			public void run() {
				getRootActivity().lockKeys(false);
			}

		}, 160);

		--intSelItem;
	}

	private void execSelectedItem() {
		getRootActivity().showLoadingAnim(true);
		getRootActivity().postIntentStartWait(new IntentFinishedListener() {
			@Override
			public void onFinished(Intent intent) {
				getRootActivity().showLoadingAnim(false);
			}
		}, alItems.get(intSelItem).getAppIntent());
	}

	@Override
	public void doCleanup() {
		if (alItems.size() == 0 && tv_no_app != null) {
			getRootView().removeView(tv_no_app);
			return;
		}
		if (tlRoot != null) {
			getRootView().removeView(tlRoot);
		}
	}

	// TODO implement requestDestroy() to release any resources used
}
