package com.rajasharan.camerapeekpreview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by rajasharan on 9/13/15.
 */
public class ImageView extends View {

    private Bitmap mScaledBitmap;
    private Point mDisplaySize;

    public ImageView(Context context) {
        this(context, null);
    }

    public ImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDisplaySize = new Point();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(mDisplaySize);
    }

    public void setImageBitmap(Bitmap source) {
        if (mScaledBitmap != null && !mScaledBitmap.isRecycled()) {
            mScaledBitmap.recycle();
        }
        mScaledBitmap = Bitmap.createScaledBitmap(source, mDisplaySize.x, mDisplaySize.y, false);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScaledBitmap != null) {
            canvas.drawBitmap(mScaledBitmap, getLeft(), getTop(), null);
        }
    }
}
