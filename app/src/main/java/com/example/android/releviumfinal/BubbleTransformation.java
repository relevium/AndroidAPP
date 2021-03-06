package com.example.android.releviumfinal;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

public class BubbleTransformation implements com.squareup.picasso.Transformation {
    private static final int outerMargin = 30;
    private static final int triangleMargin = 10;
    private final String color;
    private final int margin;  // dp

    // margin is the board in dp
    public BubbleTransformation(final int margin) {
        this.margin = margin;
        this.color = "#284ae0";
    }

    public BubbleTransformation(final int margin, final String color) {
        this.margin = margin;
        this.color = color;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        float r = size / 2f;

        Bitmap output = Bitmap.createBitmap(size + triangleMargin, size + triangleMargin, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
        paintBorder.setColor(Color.parseColor(color));
        paintBorder.setStrokeWidth(margin);
        canvas.drawCircle(r, r, r - margin, paintBorder);

        Paint trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trianglePaint.setStrokeWidth(2);
        trianglePaint.setColor(Color.parseColor(color));
        trianglePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        trianglePaint.setAntiAlias(true);
        Path triangle = new Path();
        triangle.setFillType(Path.FillType.EVEN_ODD);
        triangle.moveTo(size - margin, size / 2);
        triangle.lineTo(size / 2, size + triangleMargin);
        triangle.lineTo(margin, size / 2);
        triangle.close();
        canvas.drawPath(triangle, trianglePaint);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        canvas.drawCircle(r, r, r - outerMargin, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        return "rounded";
    }
}