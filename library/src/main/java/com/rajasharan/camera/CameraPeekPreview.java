package com.rajasharan.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaActionSound;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rajasharan on 9/6/15.
 */
public class CameraPeekPreview extends ViewGroup implements TextureView.SurfaceTextureListener,
        View.OnTouchListener, Camera.PictureCallback, Camera.ShutterCallback, Camera.AutoFocusCallback {
    private static final String TAG = "CameraPeekPreview";
    private static final String LOG_TAG = "LOG_CameraPeekPreview";
    private static final float TOLERANCE = 0.0001f;

    private TextureView mTextureView;
    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private boolean mDisplayInversed;
    private float mAspectRatio;
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
        mDisplayInversed = false;

        mMaxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150f,
                context.getResources().getDisplayMetrics());

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        mAspectRatio = (float)size.x / (float)size.y;
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

        if (mPreviewSize != null) {
            int previewWidthSpec = MeasureSpec.makeMeasureSpec(mPreviewSize.width, MeasureSpec.EXACTLY);
            int previewHeightSpec = MeasureSpec.makeMeasureSpec(mPreviewSize.height, MeasureSpec.EXACTLY);

            if (mDisplayInversed) {
                mTextureView.measure(previewHeightSpec, previewWidthSpec);
            }
            else {
                mTextureView.measure(previewWidthSpec, previewHeightSpec);
            }
        }
        else {
            mTextureView.measure(widthMeasureSpec, heightMeasureSpec);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mPreviewSize != null) {
            int w = mDisplayInversed? mPreviewSize.height : mPreviewSize.width;
            int h = mDisplayInversed? mPreviewSize.width : mPreviewSize.height;

            int _l = l;
            int _t = mTouchYaxis == -1? t : mTouchYaxis;
            int _r = _l + w;
            int _b = mTouchYaxis == -1? _t + h : mTouchYaxis + h;

            mTextureView.layout(_l, _t, _r, _b);
        }
        else {
            mTextureView.layout(l, t, r, b);
        }

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
            mDisplayInversed = true;
        }

        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> pictureSizes = params.getSupportedPictureSizes();
        Collections.sort(pictureSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return rhs.width - lhs.width;
            }
        });
        for (Camera.Size size: pictureSizes) {
            float ratio = mDisplayInversed?
                    (float)size.height / (float)size.width:
                    (float)size.width / (float)size.height;

            if (Math.abs(ratio - mAspectRatio) <= TOLERANCE) {
                params.setPictureSize(size.width, size.height);
                mCamera.setParameters(params);
                break;
            }
        }

        List<Camera.Size> previewSizes = params.getSupportedPreviewSizes();
        Collections.sort(previewSizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int ret = lhs.width - rhs.width;
                return ret == 0? lhs.height - rhs.height: ret;
            }
        });
        for (Camera.Size size: previewSizes) {
            int w = mDisplayInversed? size.height : size.width;
            if (w >= width) {
                mPreviewSize = size;
                params.setPreviewSize(size.width, size.height);
                mCamera.setParameters(params);
                break;
            }
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
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchYaxis != -1) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchYaxis != -1 && y > 0) {
                    mCamera.autoFocus(this);
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
            if (mDisplayInversed) {
                m.setRotate(90);
            }
            Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, false);
            mListener.onPictureTaken(bitmap);
        }
        camera.startPreview();
    }

    @Override
    public void onShutter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.playSoundEffect(MediaActionSound.SHUTTER_CLICK);
        }
        else {
            this.playSoundEffect(SoundEffectConstants.CLICK);
        }
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

    public void logCameraParams() {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();

            Log.d(LOG_TAG, "List<PictureSizes>: ");
            for (Camera.Size size: params.getSupportedPictureSizes()) {
                Log.d(LOG_TAG, String.format("PictureSize: (%s, %s)", size.width, size.height));
            }

            Camera.Size pictureSize = params.getPictureSize();
            Log.d(LOG_TAG, String.format("current PictureSize: (%s, %s)", pictureSize.width, pictureSize.height));

            Log.d(LOG_TAG, String.format("List<PreviewSizes>: "));
            for (Camera.Size size: params.getSupportedPreviewSizes()) {
                Log.d(LOG_TAG, String.format("PreviewSize: (%s, %s)", size.width, size.height));
            }

            Camera.Size previewSize = params.getPreviewSize();
            Log.d(LOG_TAG, String.format("current PreviewSize: (%s, %s)", previewSize.width, previewSize.height));

            Log.d(LOG_TAG, "ColorEffects: " + params.getColorEffect());

            params.setAutoExposureLock(true);
            Log.d(LOG_TAG, String.format("exposureLockSupported: %s, value: %s",
                    params.isAutoExposureLockSupported(), params.getAutoExposureLock()));

            Camera.CameraInfo info = new Camera.CameraInfo();
            for (int i=0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, info);
                Log.d(LOG_TAG, String.format("Camera_%s Facing: %s, Orientation: %s", i, info.facing, info.orientation));
            }
        }

        Log.d(LOG_TAG, String.format("ViewSize: (%s, %s) Rect: (%s,%s - %s,%s)", getWidth(), getHeight(),
                getLeft(), getTop(), getRight(), getBottom()));

        Log.d(LOG_TAG, String.format("TextureViewSize: (%s, %s) Rect: (%s,%s - %s,%s)",
                mTextureView.getWidth(), mTextureView.getHeight(),
                mTextureView.getLeft(), mTextureView.getTop(), mTextureView.getRight(), mTextureView.getBottom()));

    }
}
