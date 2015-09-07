package com.rajasharan.camerapeekpreview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.rajasharan.camera.CameraPeekPreview;

public class MainActivity extends AppCompatActivity implements CameraPeekPreview.OnPictureTakenListener {

    private ImageView mImageView;
    private CameraPeekPreview mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = (ImageView) findViewById(R.id.imageview);
        mRoot = (CameraPeekPreview) findViewById(R.id.camera_peek);
        mRoot.setOnPictureTakenListener(this);
    }

    @Override
    public void onPictureTaken(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }
}
