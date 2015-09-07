package com.rajasharan.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by rajasharan on 9/6/15.
 */
public class CameraPeekPreview extends ViewGroup implements TextureView.SurfaceTextureListener,
        View.OnTouchListener, Camera.PictureCallback, Camera.ShutterCallback, Camera.AutoFocusCallback {
    private static final String TAG = "CameraPeekPreview";

    private TextureView mTextureView;
    private Camera mCamera;
    private Drawable mCameraIcon;
    private int mTouchYaxis;
    private int mMaxHeight;
    private OnPictureTakenListener mListener;

    public CameraPeekPreview(Context context) {
        this(context, null);
    }

    public CameraPeekPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPeekPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTextureView = new TextureView(context);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setOnTouchListener(this);
        mCameraIcon = context.getResources().getDrawable(android.R.drawable.ic_menu_camera);
        mTouchYaxis = -1;
        mMaxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f,
                context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(mTextureView, 0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        for (int i=0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof TextureView) {
                continue;
            }

            if (mTouchYaxis != -1) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(mTouchYaxis, MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            }
            v.measure(widthMeasureSpec, heightMeasureSpec);
        }

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);

        mTextureView.measure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mTextureView.layout(l, t, r, b);

        for (int i=0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof TextureView) {
                continue;
            }
            v.setBackgroundColor(Color.WHITE);
            if (mTouchYaxis != -1) {
                v.layout(l, t, r, mTouchYaxis);
            } else {
                v.layout(l, t, r, b);
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int x = (getLeft() + getRight())/2 - mCameraIcon.getIntrinsicWidth()/2;
        int y = mTouchYaxis == -1 ?
                getBottom() - mCameraIcon.getIntrinsicHeight():
                mTouchYaxis - mCameraIcon.getIntrinsicHeight();

        mCameraIcon.setBounds(x, y, x + mCameraIcon.getIntrinsicWidth(), y + mCameraIcon.getIntrinsicHeight());
        mCameraIcon.draw(canvas);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();
        mCamera.startPreview();
        if (getMeasuredWidth() < getMeasuredHeight()) {
            mCamera.setDisplayOrientation(90);
        }

        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException e) {
            Log.e(TAG, "Camera setPreviewTexture(surface) failed", e);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return checkIfTouchInsideCameraIcon(x, y);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return checkIfTouchInsideCameraIcon(x, y);
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (y < getHeight() - mMaxHeight) {
                    mTouchYaxis = getHeight() - mMaxHeight;
                } else {
                    mTouchYaxis = y;
                }
                requestLayout();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private boolean checkIfTouchInsideCameraIcon(int x, int y) {
        return mCameraIcon.getBounds().contains(x, y);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int y = (int) event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchYaxis != -1 && y > mTouchYaxis) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchYaxis != -1 && y > mTouchYaxis) {
                    mCamera.autoFocus(this);
                    //mCamera.takePicture(this, null, null, this);
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
        if (mListener != null) {
            Matrix m = new Matrix();
            m.setRotate(90);
            Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, false);
            mListener.onPictureTaken(bitmap);
        }
        b.recycle();
        camera.startPreview();
    }

    @Override
    public void onShutter() {
        this.playSoundEffect(MediaActionSound.SHUTTER_CLICK);
    }

    public void setOnPictureTakenListener(OnPictureTakenListener listener) {
        mListener = listener;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        camera.takePicture(this, null, null, this);
    }

    public interface OnPictureTakenListener {
        void onPictureTaken(Bitmap bitmap);
    }
}
