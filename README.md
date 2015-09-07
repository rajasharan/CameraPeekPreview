# Android Camera Peek Preview
Easily peek into the camera preview and snap pictures

## Demo
![](/screencast.gif)

## Usage
Add your layout file inside `CameraPeekPreview` using the `include` tag

[activity_main.xml](/demo/src/main/res/layout/activity_main.xml)
```xml
<com.rajasharan.camera.CameraPeekPreview
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/camera_peek"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >
    <include layout="@layout/sample_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
</com.rajasharan.camera.CameraPeekPreview>
```

#### Setup `OnPictureTakenListener` to receive Bitmap
[MainActivity.java](/demo/src/main/java/com/rajasharan/camerapeekpreview/MainActivity.java)
```java
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
```

## [License](/LICENSE)
    The MIT License (MIT)
