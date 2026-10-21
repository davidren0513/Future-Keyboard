package com.liquidglass.ime;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

final class GlassStyle {
    static final int INK = Color.rgb(242, 247, 247), MINT = Color.rgb(129, 216, 184);
    static int dp(Context c, float value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    static GradientDrawable surface(int color, float radius, int stroke) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius);
        if (stroke != 0) d.setStroke(1, stroke);
        return d;
    }
}
