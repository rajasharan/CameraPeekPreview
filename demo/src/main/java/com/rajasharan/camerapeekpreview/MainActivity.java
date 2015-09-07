package com.rajasharan.camerapeekpreview;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rajasharan.camera.CameraPeekPreview;

public class MainActivity extends AppCompatActivity implements CameraPeekPreview.OnPictureTakenListener {

    private TextView mTextview;
    private ImageView mImageView;
    private CameraPeekPreview mRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextview = (TextView) findViewById(R.id.textview);
        mImageView = (ImageView) findViewById(R.id.imageview);

        mRoot = (CameraPeekPreview) findViewById(R.id.camera_peek);
        mRoot.setOnPictureTakenListener(this);
    }

    @Override
    public void onPictureTaken(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_hide:
                mTextview.setVisibility(View.GONE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
