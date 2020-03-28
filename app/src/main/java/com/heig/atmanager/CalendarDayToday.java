package com.heig.atmanager;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.text.style.LineBackgroundSpan;

import androidx.annotation.NonNull;

/**
 * Author : Stéphane Bottin
 * Date   : 22.03.2020
 *
 * Graphic for today's calendar notification
 */
public class CalendarDayToday  implements LineBackgroundSpan {

    private static final int HEIGHT = 50;
    private static final int PADDING = 10;
    private static final int CORNER_RADIUS = 20;

    int color;

    public CalendarDayToday(int color) {
        this.color = color;
    }

    @Override
    public void drawBackground(
            @NonNull Canvas canvas, Paint paint,
            int left, int right, int top, int baseline, int bottom,
            @NonNull CharSequence charSequence,
            int start, int end, int lineNum
    ) {
        paint.setColor(color);

        // Draw line
        /*canvas.drawRect(left + HORIZONTAL_PADDING,
                bottom + VERTICAL_OFFSET,
                right - HORIZONTAL_PADDING,
                bottom + VERTICAL_OFFSET + LINE_HEIGHT, paint);*/
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(left + PADDING, top - HEIGHT + PADDING,
                right - PADDING, bottom + HEIGHT - PADDING, CORNER_RADIUS,
                CORNER_RADIUS, paint);

        // Set color to white for the text that will be drawn next
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
    }
}
