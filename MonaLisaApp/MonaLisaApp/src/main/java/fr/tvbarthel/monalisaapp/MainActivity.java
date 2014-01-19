package fr.tvbarthel.monalisaapp;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import fr.tvbarthel.monalisaapp.ui.DynamicPortrait;
import fr.tvbarthel.monalisaapp.ui.FaceDetectionPreview;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getName();
    private Camera mCamera;
    private FaceDetectionPreview mFaceDetectionPreview;
    private DynamicPortrait mPortrait;
    private FrameLayout.LayoutParams mPortraitParams;
    private FrameLayout mPreview;
    private Eye mLeftEye;
    private Eye mRightEye;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPortrait = new DynamicPortrait(this, getResources().getDrawable(R.drawable.mona_lisa));
        mPortraitParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPortraitParams.gravity = Gravity.CENTER;

        mLeftEye = new Eye(0.36f, 0.28f, 10f);
        mRightEye = new Eye(0.50f, 0.28f, 10f);
        mPortrait.setEyesModel(mLeftEye,mRightEye);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CameraAsyncTask().execute();
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
     *
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
    private void releasePreview() {
        if (mFaceDetectionPreview != null) {
            mPreview.removeView(mFaceDetectionPreview);
        }
        mPreview.removeView(mPortrait);
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

    private class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

        @Override
        protected Camera doInBackground(Void... params) {
            return getCameraInstance();
        }

        @Override
        protected void onPostExecute(Camera camera) {
            super.onPostExecute(camera);

            mCamera = camera;

            if (mCamera == null) {
                MainActivity.this.finish();
            }

            mFaceDetectionPreview = new FaceDetectionPreview(MainActivity.this, mCamera);
            mPreview = (FrameLayout) findViewById(R.id.container);

            mPreview.addView(mFaceDetectionPreview);
            mPreview.addView(mPortrait, mPortraitParams);

            mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    if (faces.length > 0) {
                        float relativeY = -((float) faces[0].rect.centerX()) / ((float) mFaceDetectionPreview.getMeasuredWidth());
                        float relativeX = -((float) faces[0].rect.centerY()) / ((float) mFaceDetectionPreview.getMeasuredHeight()/2);
                        Log.d("FaceDetection", "face detected: " + faces.length +
                                " Face 1 Location X: " + relativeX + "Y: " + relativeY);
                        mLeftEye.addOrientation(relativeX,relativeY);
                        mRightEye.addOrientation(relativeX,relativeY);
                    }
                    mPortrait.invalidate();
                }
            });

            setCameraDisplayOrientation(mCamera);
        }
    }
}
