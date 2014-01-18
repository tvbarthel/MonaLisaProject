package fr.tvbarthel.monalisaapp.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DynamicEyeView extends View {

    private static final String TAG = DynamicEyeView.class.getName();

    private Paint mPaint;

    public DynamicEyeView(Context context) {
        super(context);

        mPaint = new Paint();
        mPaint.setStrokeWidth(30f);
        mPaint.setColor(Color.WHITE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float middleW = canvas.getWidth() / 2;
        float middleH = canvas.getHeight() / 2;

        canvas.drawPoint(middleW - 30, middleH - 10, mPaint);
        canvas.drawPoint(middleW + 30, middleH - 10, mPaint);
    }
}
