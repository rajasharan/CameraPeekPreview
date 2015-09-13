package com.rajasharan.camerapeekpreview;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rajasharan.camera.CameraPeekPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements CameraPeekPreview.OnPictureTakenListener, Handler.Callback {
    private static final int MSG_SAVE_BITMAP = 0;

    private TextView mTextview;
    private ImageView mImageView;
    private CameraPeekPreview mCameraPeek;
    private HandlerThread mBackgroundThread;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBackgroundThread = new HandlerThread("Disk-IO-Thread");
        mBackgroundThread.start();

        mHandler = new Handler(mBackgroundThread.getLooper(), this);

        mTextview = (TextView) findViewById(R.id.textview);
        mImageView = (ImageView) findViewById(R.id.imageview);

        mCameraPeek = (CameraPeekPreview) findViewById(R.id.camera_peek);
        mCameraPeek.setOnPictureTakenListener(this);
    }

    @Override
    public void onPictureTaken(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        Message msg = mHandler.obtainMessage(MSG_SAVE_BITMAP, bitmap);
        mHandler.sendMessage(msg);
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
            case R.id.action_log:
                mCameraPeek.logCameraParams();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_SAVE_BITMAP: {
                final Bitmap bitmap = (Bitmap) msg.obj;

                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File cameraDir = new File(dir, "Camera");
                final File file = new File(cameraDir, "PeekPreview_" + System.currentTimeMillis() + ".jpg");

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            scanIntent.setData(Uri.fromFile(file));
                            sendBroadcast(scanIntent);
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "FileNotFoundException: " + file, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "IOException: " + file, Toast.LENGTH_LONG).show();
                }
                return true;
            }
        }
        return false;
    }
}
