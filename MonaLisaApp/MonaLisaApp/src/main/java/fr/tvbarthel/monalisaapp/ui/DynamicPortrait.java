package fr.tvbarthel.monalisaapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import fr.tvbarthel.monalisaapp.Eye;

public class DynamicPortrait extends ImageView {

    private static final String TAG = DynamicPortrait.class.getName();

    private Paint mPaint;
    private Eye mLeftEye;
    private Eye mRightEye;


    public DynamicPortrait(Context context, Drawable drawable) {
        super(context);

        mPaint = new Paint();
        mPaint.setStrokeWidth(15f);
        mPaint.setColor(Color.WHITE);

        this.setImageDrawable(drawable);
    }

    public void setEyesModel(Eye left, Eye right) {
        mLeftEye = left;
        mRightEye = right;
    }

    @Override
    protected void onDisplayHint(int hint) {
        super.onDisplayHint(hint);

        if (hint == VISIBLE) {
            Log.e(TAG, "VISIBLE");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float middleW = canvas.getWidth();
        float middleH = canvas.getHeight();

        if (mLeftEye != null) {
            //draw left eye
            canvas.drawPoint(mLeftEye.getCenterX() * middleW + mLeftEye.getOrientationX(),
                    mLeftEye.getCenterY() * middleH + mRightEye.getOrientationY(), mPaint);
        }

        if (mRightEye != null) {
            //draw right eye
            canvas.drawPoint(mRightEye.getCenterX() * middleW + mRightEye.getOrientationX(),
                    mRightEye.getCenterY() * middleH + mRightEye.getOrientationY(), mPaint);
        }

    }
}
