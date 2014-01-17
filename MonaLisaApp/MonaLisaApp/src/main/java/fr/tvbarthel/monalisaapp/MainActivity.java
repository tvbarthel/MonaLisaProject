package fr.tvbarthel.monalisaapp;

import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();
    private Camera mCamera;
    private FaceDetectionPreview mFaceDetectionPreview;
    private FrameLayout mPreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = getCameraInstance();

        if (mCamera == null) {
            this.finish();
        }

        mFaceDetectionPreview = new FaceDetectionPreview(this, mCamera);
        mPreview = (FrameLayout) findViewById(R.id.container);

        mPreview.addView(mFaceDetectionPreview);

        mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
            @Override
            public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                if (faces.length > 0) {
                    Log.d("FaceDetection", "face detected: " + faces.length +
                            " Face 1 Location X: " + faces[0].rect.centerX() +
                            "Y: " + faces[0].rect.centerY());
                }
            }
        });

        setCameraDisplayOrientation(mCamera);
    }


    @Override
    protected void onPause() {
        super.onPause();
        releasePreview();
        releaseCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * used to adapt camera preview to the current device orientation
     * @param camera
     */
    public void setCameraDisplayOrientation(android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int degrees = 0;
        int currentRotation = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        switch (currentRotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * A safe way to get an instance of the front camera
     */
    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());

        }
        return c;
    }

    /**
     * remove the preview
     */
    private void releasePreview(){
        if(mFaceDetectionPreview != null){
            mPreview.removeView(mFaceDetectionPreview);
            mFaceDetectionPreview = null;
        }
    }

    /**
     * release the camera for the other app
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            FrameLayout preview = (FrameLayout) findViewById(R.id.container);
            preview.removeView(mFaceDetectionPreview);
        }
    }
}
