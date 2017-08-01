package ganteng.hendrawd.popularmovies.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

import ganteng.hendrawd.popularmovies.R;

/**
 * Draw line is buggy for layertype hardware and lollipop+
 * and divider inconsistency of listview among android versions
 * need to be replaced with this method instead to use default divider
 * @author by hendrawd on 11/4/15
 */
public class ContinuedLineView extends View {

    private Paint paint;
    private Path path;
    @ColorInt
    private int lineColor = Color.BLACK;

    public ContinuedLineView(Context context) {
        super(context);
        init(context);
    }

    public ContinuedLineView(Context context, AttributeSet as) {
        super(context, as);

        TypedArray a = context.obtainStyledAttributes(as, R.styleable.ContinuedLineView);

        String color = a.getString(R.styleable.ContinuedLineView_android_color);
        lineColor = Color.parseColor(color);

        a.recycle();

        init(context);
    }

    public ContinuedLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ContinuedLineView, defStyle, 0);

        String color = a.getString(R.styleable.ContinuedLineView_android_color);
        lineColor = Color.parseColor(color);

        a.recycle();

        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setAntiAlias(true);
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = 1 * (metrics.densityDpi / 160f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        path = new Path();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setStrokeWidth(getHeight());
        paint.setColor(lineColor);
        path.reset();
        path.moveTo(getHeight(), getHeight() / 2);
        path.lineTo(getWidth() - getHeight(), getHeight() / 2);
        canvas.drawPath(path, paint);
    }

    public void setLineColor(@ColorInt int color){
        this.lineColor = color;
        invalidate();
    }
}
