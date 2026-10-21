package com.liquidglass.ime;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.util.function.Consumer;

/** Owns only the IME window's background blur, never the host application's content. */
final class BlurController implements AutoCloseable {
    private final Window window;
    private final WindowManager manager;
    private final SharedPreferences prefs;
    private final GradientDrawable background;
    private final Consumer<Boolean> listener = enabled -> apply();
    private boolean registered;
    private final View dock;
    BlurController(Context context, Window window, View dock) {
        this.window = window; this.dock = dock;
        manager = context.getSystemService(WindowManager.class);
        prefs = context.getSharedPreferences("glass", Context.MODE_PRIVATE);
        // The window background's outline is the compositor's blur region.
        background = GlassStyle.surface(0x7014222B, GlassStyle.dp(context, 22), 0x448EA9AA);
        window.setBackgroundDrawable(background);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND | WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        if (Build.VERSION.SDK_INT >= 31) {
            manager.addCrossWindowBlurEnabledListener(context.getMainExecutor(), listener);
            registered = true;
        }
        apply();
    }
    void apply() {
        int radius = prefs.getInt("radius", 72);
        boolean available = Build.VERSION.SDK_INT >= 31 && manager.isCrossWindowBlurEnabled();
        boolean active = available && radius > 0;
        int opacity = active ? prefs.getInt("opacity", 42) : 96;
        background.setColor(Color.argb(Math.round(opacity * 2.55f), 20, 32, 39));
        if (Build.VERSION.SDK_INT >= 31) window.setBackgroundBlurRadius(active ? radius : 0);
        dock.setContentDescription(active ? "实时背景模糊已启用，半径 " + radius + " 像素" : "背景模糊未启用，使用深色玻璃底板");
        Log.i("GlassBlur", "enabled=" + active + " supportedNow=" + available + " radiusPx=" + radius);
    }
    @Override public void close() {
        if (Build.VERSION.SDK_INT >= 31) {
            if (registered) manager.removeCrossWindowBlurEnabledListener(listener);
            window.setBackgroundBlurRadius(0);
        }
        registered = false;
    }
}
