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

package com.raddstudios.xpmb.utils.backports;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.raddstudios.xpmb.R;

public class XPMB_RelativeLayout extends RelativeLayout implements XPMB_View {

	private float mAlpha = 1.0f, mScaleX = 1.0f, mScaleY = 1.0f;
	private int baseWidth = 0, baseHeight = 0;

	public XPMB_RelativeLayout(Context context) {
		super(context);
	}

	public XPMB_RelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_RelativeLayout, 0, 0);

		try {
			mAlpha = a.getFloat(
					R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_RelativeLayout_alpha, 1.0f);
			mScaleX = a.getFloat(
					R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_RelativeLayout_scaleX, 1.0f);
			mScaleX = a.getFloat(
					R.styleable.com_raddstudios_xpmb_utils_backports_XPMB_RelativeLayout_scaleY, 1.0f);
		} finally {
			a.recycle();
		}
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		canvas.saveLayerAlpha(0, 0, canvas.getWidth(), canvas.getHeight(), (int) (255 * mAlpha),
				Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		super.dispatchDraw(canvas);
	}

	@Override
	public void setAlphaLevel(float value) {
		mAlpha = value;
	}

	@Override
	public float getAlphaLevel() {
		return mAlpha;
	}

	@Override
	public void resetScaleBase() {
		baseWidth = super.getLayoutParams().width;
		baseHeight = super.getLayoutParams().height;
	}

	private void updateScaledLayoutParams(ViewGroup.LayoutParams params) {
		if (params != null) {
			if (params.width != ViewGroup.LayoutParams.MATCH_PARENT
					|| params.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
				params.width = (int) (baseWidth * mScaleX);
			}
			if (params.height != ViewGroup.LayoutParams.MATCH_PARENT
					|| params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
				params.height = (int) (baseHeight * mScaleY);
			}
			super.setLayoutParams(params);
		}
	}

	@Override
	public void setViewScaleX(float scale) {
		mScaleX = scale;
		updateScaledLayoutParams(super.getLayoutParams());
	}

	@Override
	public float getViewScaleX() {
		return mScaleX;
	}

	@Override
	public void setViewScaleY(float scale) {
		mScaleY = scale;
		updateScaledLayoutParams(super.getLayoutParams());
	}

	@Override
	public float getViewScaleY() {
		return mScaleY;
	}

	@Override
	public void setTopMargin(int top) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).topMargin = top;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setBottomMargin(int bottom) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).bottomMargin = bottom;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setLeftMargin(int left) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).leftMargin = left;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public void setRightMargin(int right) {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			((MarginLayoutParams) lp).rightMargin = right;
			super.setLayoutParams(lp);
		}
	}

	@Override
	public int getTopMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).topMargin;
		}
		return 0;
	}

	@Override
	public int getBottomMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).bottomMargin;
		}
		return 0;
	}

	@Override
	public int getLeftMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).leftMargin;
		}
		return 0;
	}

	@Override
	public int getRightMargin() {
		ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) super.getLayoutParams();
		if (lp instanceof MarginLayoutParams) {
			return ((MarginLayoutParams) lp).rightMargin;
		}
		return 0;
	}
}
