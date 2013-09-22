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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.raddstudios.xpmb.R;
import com.raddstudios.xpmb.XPMBServices;
import com.raddstudios.xpmb.XPMBServices.MediaPlayerControl;
import com.raddstudios.xpmb.XPMB_Main;
import com.raddstudios.xpmb.menus.utils.XPMBSubmenuItem;
import com.raddstudios.xpmb.utils.XPMB_Activity;
import com.raddstudios.xpmb.utils.XPMB_Layout;
import com.raddstudios.xpmb.utils.backports.XPMB_ImageView;
import com.raddstudios.xpmb.utils.backports.XPMB_RelativeLayout;
import com.raddstudios.xpmb.utils.backports.XPMB_TextView;

public class XPMBSubmenu_MUSIC extends XPMB_Layout {

	class XPMBSubmenuItem_MUSIC_Metadata {
		private File fTrackPath = null;
		private String strName = null, strAuthor = null, strAlbum = null;
		private Bitmap drwAlbumCover = null;

		public XPMBSubmenuItem_MUSIC_Metadata(String path, String name, String author, String album) {
			fTrackPath = new File(path);
			strName = name;
			strAuthor = author;
			strAlbum = album;
		}

		public String getTrackName() {
			return strName;
		}

		public String getTrackAuthor() {
			return strAuthor;
		}

		public String getTrackAlbum() {
			return strAlbum;
		}

		public void setTrackAlbumCover(Bitmap cover) {
			drwAlbumCover = cover;
		}

		public Bitmap getTrackAlbumCover() {
			return drwAlbumCover;
		}

		public void setTrackPath(String path) {
			fTrackPath = new File(path);
		}

		public File getTrackPath() {
			return fTrackPath;
		}
	}

	class XPMBSubmenuItem_MUSIC extends XPMBSubmenuItem {
		private XPMBSubmenuItem_MUSIC_Metadata xsimmMetadata = null;

		public XPMBSubmenuItem_MUSIC(XPMBSubmenuItem_MUSIC_Metadata trackInfo) {
			super(trackInfo.getTrackName() + "\r\n" + trackInfo.getTrackAuthor(), trackInfo
					.getTrackAlbumCover());
			xsimmMetadata = trackInfo;
		}

		public XPMBSubmenuItem_MUSIC_Metadata getTrackMetadata() {
			return xsimmMetadata;
		}
	}

	private final int ANIM_NONE = -1, ANIM_MENU_MOVE_UP = 0, ANIM_MENU_MOVE_DOWN = 1,
			ANIM_CENTER_ON_ITEM = 2, ANIM_SHOW_MEDIA_CONTROLS = 3, ANIM_HIDE_MEDIA_CONTROLS = 4,
			ANIM_CENTER_ON_ITEM_DELAYED = 5;

	private class UIAnimatorWorker implements AnimatorUpdateListener, AnimatorListener {

		private int intAnimType = -1;
		private int intAnimItem = -1, intNextItem = -1;
		private float pY = 0, destY = 0;
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
			mOwner.setStartDelay(0);
			intAnimType = type;
			intAnimItem = intSelItem;

			switch (type) {
			case ANIM_MENU_MOVE_UP:
				mOwner.setDuration(150);
				pY = tlRoot.getTopMargin();
				intNextItem = intAnimItem - 1;
				destY = pxFromDip(122) - (pxFromDip(60) * intNextItem);
				break;
			case ANIM_MENU_MOVE_DOWN:
				mOwner.setDuration(150);
				pY = tlRoot.getTopMargin();
				intNextItem = intAnimItem + 1;
				destY = pxFromDip(122) - (pxFromDip(60) * intNextItem);
				break;
			case ANIM_CENTER_ON_ITEM_DELAYED:
				mOwner.setStartDelay(3000);
				mOwner.setDuration(250);
				pY = tlRoot.getTopMargin();
				destY = pxFromDip(122) - (pxFromDip(60) * intSelItem);
				break;
			case ANIM_CENTER_ON_ITEM:
				mOwner.setDuration(150);
				pY = tlRoot.getTopMargin();
				intNextItem = (int) mArgs[0];
				destY = pxFromDip(122) - (pxFromDip(60) * intNextItem);
				break;
			case ANIM_SHOW_MEDIA_CONTROLS:
				mOwner.setDuration(200);
				rlPlayerControls.setVisibility(View.VISIBLE);
				break;
			case ANIM_HIDE_MEDIA_CONTROLS:
				mOwner.setDuration(800);
				break;
			}
		}

		@Override
		public void onAnimationUpdate(ValueAnimator arg0) {
			float completion = (Float) arg0.getAnimatedValue();

			float dispY = 0, alphaO = 0, alphaI = 0;
			int marginO = 0, marginI = 0;

			switch (intAnimType) {
			case ANIM_MENU_MOVE_UP:
			case ANIM_MENU_MOVE_DOWN:
			case ANIM_CENTER_ON_ITEM:
				dispY = destY - pY;
				alphaO = 1.0f - (0.5f * completion);
				alphaI = 0.5f + (0.5f * completion);
				marginO = (int) (pxFromDip(16) - (pxFromDip(16) * completion));
				marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setTopMargin((int) (pY + (dispY * completion)));
				getView(alItems.get(intAnimItem).getParentView()).setTopMargin(marginO);
				getView(alItems.get(intAnimItem).getParentView()).setBottomMargin(marginO);
				getView(alItems.get(intAnimItem).getParentLabel()).setAlphaLevel(alphaO);
				getView(alItems.get(intNextItem).getParentView()).setTopMargin(marginI);
				getView(alItems.get(intNextItem).getParentView()).setBottomMargin(marginI);
				getView(alItems.get(intNextItem).getParentLabel()).setAlphaLevel(alphaI);
				break;
			case ANIM_CENTER_ON_ITEM_DELAYED:
				dispY = destY - pY;
				alphaI = 0.5f + (0.5f * completion);
				marginI = (int) (pxFromDip(16) * completion);

				tlRoot.setTopMargin((int) (pY + (dispY * completion)));
				getView(alItems.get(intAnimItem).getParentView()).setTopMargin(marginI);
				getView(alItems.get(intAnimItem).getParentView()).setBottomMargin(marginI);
				getView(alItems.get(intAnimItem).getParentLabel()).setAlphaLevel(alphaI);
				break;
			case ANIM_SHOW_MEDIA_CONTROLS:
				alphaI = completion;

				rlPlayerControls.setAlphaLevel(alphaI);
				rlPlayerControls.invalidate();
				break;
			case ANIM_HIDE_MEDIA_CONTROLS:
				alphaO = 1.0f - completion;

				rlPlayerControls.setAlphaLevel(alphaO);
				rlPlayerControls.invalidate();
				if (completion == 1.0f) {
					rlPlayerControls.setVisibility(View.INVISIBLE);
				}
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

	private final String LIST_ITEMS = "Subitems", COLLECTION_COVERS = "AlbumArtList",
			COLLECTION_CONFIG = "MusicConfig";
	private final String CONFIG_SELECTED_ITEM = "intSelItem",
			CONFIG_CURRENT_TRACK = "intLastPlayed";

	private ArrayList<XPMBSubmenuItem_MUSIC> alItems = null;
	private Hashtable<String, Bitmap> htCovers = null;
	private int intSelItem = 0, intLastPlayed = -1;

	private boolean isFocused = true;

	private XPMB_TextView tv_no_music = null;
	private MediaPlayerControl mpPlayer = null;
	private View vwPlayerControlsRoot = null;
	private XPMB_RelativeLayout rlPlayerControls = null, tlRoot = null;
	private XPMB_TextView tvCurPos = null, tvTotalLen = null;
	private XPMB_ImageView ivPlayStatus = null;
	private ProgressBar pbTrackPos = null;

	private ValueAnimator aUIAnimator = null;
	private UIAnimatorWorker aUIAnimatorW = null;
	private RapidScroller ttFastScroll = null;

	public XPMBSubmenu_MUSIC(XPMB_Activity root, Handler messageBus, ViewGroup rootView) {
		super(root, messageBus, rootView, 0x1000);

		aUIAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
		aUIAnimator.setInterpolator(new DecelerateInterpolator());
		aUIAnimator.setDuration(150);
		aUIAnimatorW = new UIAnimatorWorker(aUIAnimator);
		aUIAnimator.addUpdateListener(aUIAnimatorW);
		aUIAnimator.addListener(aUIAnimatorW);

		mpPlayer = getRootActivity().getPlayerControl();
		mpPlayer.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				if ((intLastPlayed + 1) < alItems.size()) {
					doCenterOnItemPre();
					execCustItem(intLastPlayed + 1);
					centerOnItem(intLastPlayed);
				} else {
					doShowPlayerControls(false);
				}
			}
		});

		new Timer().scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				getMessageBus().post(new Runnable() {

					@Override
					public void run() {
						if (rlPlayerControls != null
								&& mpPlayer.getPlayerStatus() == XPMBServices.MediaPlayerControl.STATE_PLAYING) {
							pbTrackPos.setMax(mpPlayer.getDuration());
							tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
							pbTrackPos.setProgress(mpPlayer.getCurrentPosition());
							tvCurPos.setText(getTimeString(mpPlayer.getCurrentPosition()));
						}
					}
				});
			}
		}, 0, 100);
		ttFastScroll = new RapidScroller();
		new Timer().scheduleAtFixedRate(ttFastScroll, 0, 50);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
		alItems = (ArrayList<XPMBSubmenuItem_MUSIC>) getRootActivity().getStorage().getList(
				LIST_ITEMS);
		htCovers = (Hashtable<String, Bitmap>) getRootActivity().getStorage().getCollection(
				COLLECTION_COVERS);

		if (alItems != null) {
			return;
		}

		getRootActivity().getStorage().createList(LIST_ITEMS);
		getRootActivity().getStorage().createCollection(COLLECTION_COVERS);
		getRootActivity().getStorage().createCollection(COLLECTION_CONFIG);
		alItems = (ArrayList<XPMBSubmenuItem_MUSIC>) getRootActivity().getStorage().getList(
				LIST_ITEMS);
		htCovers = (Hashtable<String, Bitmap>) getRootActivity().getStorage().getCollection(
				COLLECTION_COVERS);
		String[] projection = new String[] { MediaStore.MediaColumns.DATA,
				MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ID };
		Cursor mCur = getRootActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
		mCur.moveToFirst();

		while (mCur.isAfterLast() == false) {
			if (mCur.getString(0).startsWith(
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
							.getAbsolutePath())) {
				alItems.add(new XPMBSubmenuItem_MUSIC(new XPMBSubmenuItem_MUSIC_Metadata(mCur
						.getString(0), mCur.getString(1), mCur.getString(2), mCur.getString(3))));

				long albumId = mCur.getLong(4);
				String strAlbumId = String.valueOf(albumId);

				try {
					if (!htCovers.containsKey(strAlbumId)) {
						Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
						Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
						htCovers.put(strAlbumId, Bitmap.createScaledBitmap(MediaStore.Images.Media
								.getBitmap(getRootView().getContext().getContentResolver(),
										albumArtUri), 96, 96, false));
					}
					alItems.get(alItems.size() - 1).getTrackMetadata()
							.setTrackAlbumCover(htCovers.get(strAlbumId));

				} catch (Exception e) {
					// Log.d(getClass().getSimpleName(),
					// e.getLocalizedMessage(), e);
					alItems.get(alItems.size() - 1)
							.getTrackMetadata()
							.setTrackAlbumCover(
									BitmapFactory.decodeResource(getRootView().getResources(),
											R.drawable.ui_xmb_default_music_icon));
				}
			}
			mCur.moveToNext();
		}
		mCur.close();
	}

	private void doCenterOnItemPre() {
		getView(alItems.get(intSelItem).getParentView()).setTopMargin(0);
		getView(alItems.get(intSelItem).getParentView()).setBottomMargin(0);
		getView(alItems.get(intSelItem).getParentLabel()).setAlphaLevel(0.5f);
	}

	private void doCenterOnItemPos() {
		aUIAnimator.cancel();
		getView(alItems.get(intSelItem).getParentLabel()).setAlphaLevel(1.0f);
		aUIAnimatorW.setAnimationType(ANIM_CENTER_ON_ITEM_DELAYED);
		aUIAnimator.start();
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

	@Override
	public void parseInitLayout() {

		if (alItems.size() == 0) {
			tv_no_music = new XPMB_TextView(getRootView().getContext());
			RelativeLayout.LayoutParams lp_ng = new RelativeLayout.LayoutParams(pxFromDip(320),
					pxFromDip(100));
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			lp_ng.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			lp_ng.leftMargin = pxFromDip(108);
			lp_ng.topMargin = pxFromDip(128);
			tv_no_music.setLayoutParams(lp_ng);
			tv_no_music.setText(getRootActivity().getText(R.string.strNoMusic));
			tv_no_music.setTextColor(Color.WHITE);
			tv_no_music.setShadowLayer(16, 0, 0, Color.WHITE);
			tv_no_music.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			tv_no_music.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			getRootView().addView(tv_no_music);
			return;
		}

		intSelItem = (Integer) getRootActivity().getStorage().getObject(COLLECTION_CONFIG,
				CONFIG_SELECTED_ITEM, 0);
		intLastPlayed = (Integer) getRootActivity().getStorage().getObject(COLLECTION_CONFIG,
				CONFIG_CURRENT_TRACK, -1);

		tlRoot = new XPMB_RelativeLayout(getRootView().getContext());
		RelativeLayout.LayoutParams rootP = new RelativeLayout.LayoutParams(pxFromDip(396),
				pxFromDip(32 + (60 * alItems.size())));
		rootP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		rootP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rootP.leftMargin = pxFromDip(108);
		rootP.topMargin = pxFromDip(122 - (60 * intSelItem));
		tlRoot.setLayoutParams(rootP);

		for (XPMBSubmenuItem_MUSIC xsi : alItems) {
			int idy = alItems.indexOf(xsi);
			XPMB_ImageView cIcon = new XPMB_ImageView(getRootView().getContext());
			XPMB_TextView cLabel = new XPMB_TextView(getRootView().getContext());
			cIcon.setId(getNextID());
			cLabel.setId(getNextID());

			// Setup Icon
			RelativeLayout.LayoutParams cIconParams = new RelativeLayout.LayoutParams(
					pxFromDip(60), pxFromDip(60));
			if (idy == 0) {
				cIconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				cIconParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			} else {
				cIconParams.addRule(RelativeLayout.BELOW, cIcon.getId() - 2);
			}
			if (idy == intSelItem) {
				cIconParams.topMargin = pxFromDip(16);
				cIconParams.bottomMargin = pxFromDip(16);
			}
			cIcon.setLayoutParams(cIconParams);
			cIcon.setTag(idy);
			cIcon.setImageBitmap(xsi.getTrackMetadata().getTrackAlbumCover());
			cIcon.setOnTouchListener(mTouchListener);

			// Setup Label
			RelativeLayout.LayoutParams cLabelParams = new RelativeLayout.LayoutParams(
					pxFromDip(320), pxFromDip(60));
			cLabelParams.leftMargin = pxFromDip(16);
			cLabelParams.addRule(RelativeLayout.RIGHT_OF,cIcon.getId());
			cLabelParams.addRule(RelativeLayout.ALIGN_TOP,cIcon.getId());
			cLabel.setLayoutParams(cLabelParams);
			cLabel.setTag(idy);
			cLabel.setText(xsi.getLabel());
			cLabel.setTextColor(Color.WHITE);
			cLabel.setShadowLayer(16, 0, 0, Color.WHITE);
			cLabel.setTextAppearance(getRootView().getContext(),
					android.R.style.TextAppearance_Medium);
			cLabel.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
			if (idy != intSelItem) {
				cLabel.setAlphaLevel(0.5f);
			}
			cLabel.setOnTouchListener(mTouchListener);

			xsi.setParentView(cIcon.getId());
			xsi.setParentLabel(cLabel.getId());

			tlRoot.addView(cIcon);
			tlRoot.addView(cLabel);
		}

		getRootView().addView(tlRoot);

		vwPlayerControlsRoot = ((LayoutInflater) getRootView().getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.mediaplayer_control,
				getRootView());
		rlPlayerControls = (XPMB_RelativeLayout) vwPlayerControlsRoot.findViewById(R.id.playerc_l);
		tvCurPos = (XPMB_TextView) vwPlayerControlsRoot.findViewById(R.id.tvCurPos);
		tvTotalLen = (XPMB_TextView) vwPlayerControlsRoot.findViewById(R.id.tvTotalLen);
		pbTrackPos = (ProgressBar) vwPlayerControlsRoot.findViewById(R.id.pbCurPos);
		ivPlayStatus = (XPMB_ImageView) vwPlayerControlsRoot.findViewById(R.id.ivPlayStatus);

		rlPlayerControls.setAlphaLevel(0.0f);

		switch (mpPlayer.getPlayerStatus()) {
		case XPMBServices.MediaPlayerControl.STATE_PLAYING:
			ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
					R.drawable.ui_status_media_play));
			pbTrackPos.setMax(mpPlayer.getDuration());
			tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
			doShowPlayerControls(true);
			break;
		case XPMBServices.MediaPlayerControl.STATE_PAUSED:
			ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
					R.drawable.ui_status_media_pause));
			pbTrackPos.setMax(mpPlayer.getDuration());
			tvTotalLen.setText("/ " + getTimeString(mpPlayer.getDuration()));
			rlPlayerControls.setAlphaLevel(1.0f);
			doShowPlayerControls(false);
			break;
		}
	}

	@Override
	public void sendKeyDown(int keyCode) {
		switch (keyCode) {
		case XPMB_Main.KEYCODE_LEFT:
			moveLeft();
			break;
		case XPMB_Main.KEYCODE_RIGHT:
			moveRight();
			break;
		case XPMB_Main.KEYCODE_UP:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveUp();
			}
			break;
		case XPMB_Main.KEYCODE_DOWN:
			if (ttFastScroll.isEnabled()) {
				ttFastScroll.setEnabled(false);
			} else {
				moveDown();
			}
			break;
		case XPMB_Main.KEYCODE_CROSS:
			execSelectedItem();
			break;
		case XPMB_Main.KEYCODE_CIRCLE:
			getRootActivity().requestUnloadSubmenu();
			break;
		case XPMB_Main.KEYCODE_SHOULDER_LEFT:
			if (intSelItem == 0 || alItems.size() == 0) {
				break;
			} else {
				execCustItem(intLastPlayed - 1);
				centerOnItem(intLastPlayed);
			}
			break;
		case XPMB_Main.KEYCODE_SHOULDER_RIGHT:
			if (intSelItem == (alItems.size() - 1) || alItems.size() == 0) {
				break;
			} else {
				execCustItem(intLastPlayed + 1);
				centerOnItem(intLastPlayed);
			}
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

	private void moveLeft() {
		if (mpPlayer.getPlayerStatus() != XPMBServices.MediaPlayerControl.STATE_NOT_INITIALIZED) {
			if (mpPlayer.getCurrentPosition() - 2500 > 0) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() - 2500);
			}
			return;
		}
		getRootActivity().requestUnloadSubmenu();
	}

	private void moveRight() {
		if (mpPlayer.getPlayerStatus() != XPMBServices.MediaPlayerControl.STATE_NOT_INITIALIZED) {
			if (mpPlayer.getCurrentPosition() + 2500 < mpPlayer.getDuration()) {
				mpPlayer.seekTo(mpPlayer.getCurrentPosition() + 2500);
			}
		}
	}

	private void moveDown() {
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

	private void doShowPlayerControls(boolean show) {
		if (show) {
			aUIAnimatorW.setAnimationType(ANIM_SHOW_MEDIA_CONTROLS);
		} else {
			aUIAnimatorW.setAnimationType(ANIM_HIDE_MEDIA_CONTROLS);
		}
		aUIAnimator.start();
	}

	private void execCustItem(int index) {
		if (index == intLastPlayed) {
			if (mpPlayer.getPlayerStatus() == XPMBServices.MediaPlayerControl.STATE_PLAYING) {
				ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
						R.drawable.ui_status_media_pause));
				mpPlayer.pause();
				doShowPlayerControls(false);
			} else {
				ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
						R.drawable.ui_status_media_play));
				mpPlayer.play();
				doShowPlayerControls(true);
			}
			return;
		} else {
			if (mpPlayer.getPlayerStatus() == XPMBServices.MediaPlayerControl.STATE_PLAYING) {
				mpPlayer.stop();
			}
		}

		mpPlayer.setMediaSource(alItems.get(index).getTrackMetadata().getTrackPath()
				.getAbsolutePath());
		ivPlayStatus.setImageDrawable(getRootActivity().getResources().getDrawable(
				R.drawable.ui_status_media_play));
		doShowPlayerControls(true);
		intLastPlayed = index;

	}

	public void execSelectedItem() {
		execCustItem(intSelItem);
	}

	private String getTimeString(long millis) {
		StringBuffer buf = new StringBuffer();

		int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
		int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

		buf.append(String.format("%02d", minutes)).append(":")
				.append(String.format("%02d", seconds));

		return buf.toString();
	}

	@Override
	public void doCleanup() {
		if (alItems.size() == 0 && tv_no_music != null) {
			getRootView().removeView(tv_no_music);
			return;
		}
		if (ivPlayStatus != null) {
			getRootView().removeView(ivPlayStatus);
		}
		if (tlRoot != null) {
			getRootView().removeView(tlRoot);
		}
		if (rlPlayerControls != null) {
			getRootView().removeView(rlPlayerControls);
		}
		getRootActivity().getStorage().putObject(COLLECTION_CONFIG, CONFIG_SELECTED_ITEM,
				intSelItem);
		getRootActivity().getStorage().putObject(COLLECTION_CONFIG, CONFIG_CURRENT_TRACK,
				intLastPlayed);
	}

	@Override
	public void requestDestroy() {
		if (mpPlayer != null) {
			if (mpPlayer.getPlayerStatus() != XPMBServices.MediaPlayerControl.STATE_NOT_INITIALIZED) {
				mpPlayer.stop();
			}
			mpPlayer.release();
		}
		if (tlRoot != null) {
			tlRoot.removeAllViews();
			getRootView().removeView(tlRoot);
		}
		if (alItems != null) {
			alItems.clear();
		}
		getRootActivity().getStorage().removeCollection(COLLECTION_CONFIG);
		getRootActivity().getStorage().removeCollection(COLLECTION_COVERS);

		tlRoot = null;
		rlPlayerControls = null;
		alItems = null;
	}
}
