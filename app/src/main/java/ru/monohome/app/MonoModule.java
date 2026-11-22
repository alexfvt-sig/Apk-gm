package ru.monohome.app;

import android.content.pm.LauncherActivityInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.Build;
import android.util.Log;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public final class MonoModule extends XposedModule {
    private static final String TAG = "MonoIcons";
    private static final String ONE_UI_HOME = "com.sec.android.app.launcher";

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!ONE_UI_HOME.equals(param.getPackageName())) return;

        Set<Method> methods = new HashSet<>();
        try {
            methods.add(LauncherActivityInfo.class.getDeclaredMethod("getIcon", int.class));
        } catch (Throwable ignored) { }
        try {
            methods.add(PackageItemInfo.class.getDeclaredMethod("loadIcon", PackageManager.class));
        } catch (Throwable ignored) { }

        // Cover the icon-returning PackageManager methods used by launcher builds
        // that do not call LauncherActivityInfo.getIcon directly.
        try {
            Class<?> pm = Class.forName("android.app.ApplicationPackageManager");
            for (Method method : pm.getDeclaredMethods()) {
                String name = method.getName();
                if ((name.equals("getActivityIcon") || name.equals("getApplicationIcon"))
                        && Drawable.class.isAssignableFrom(method.getReturnType())) {
                    methods.add(method);
                }
            }
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Could not inspect ApplicationPackageManager", t);
        }

        int installed = 0;
        for (Method method : methods) {
            try {
                hook(method).intercept(chain -> gray(chain.proceed()));
                installed++;
            } catch (Throwable t) {
                log(Log.WARN, TAG, "Could not hook " + method.getName(), t);
            }
        }
        log(Log.INFO, TAG, "Installed " + installed + " grayscale icon hooks in One UI Home");
    }

    private static Object gray(Object value) {
        if (!(value instanceof Drawable)) return value;
        Drawable drawable = (Drawable) value;
        if (drawable instanceof GrayscaleIconDrawable) return drawable;

        Drawable source = drawable;
        try {
            Drawable.ConstantState state = drawable.getConstantState();
            if (state != null) {
                source = state.newDrawable().mutate();
            } else {
                source = drawable.mutate();
            }
        } catch (Throwable ignored) {
            source = drawable;
        }
        return new GrayscaleIconDrawable(source);
    }

    private static final class GrayscaleIconDrawable extends DrawableWrapper {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        GrayscaleIconDrawable(Drawable child) {
            super(child);
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0f);
            paint.setColorFilter(new ColorMatrixColorFilter(matrix));
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            if (bounds.isEmpty()) {
                super.draw(canvas);
                return;
            }
            int save = canvas.saveLayer(
                    bounds.left, bounds.top, bounds.right, bounds.bottom, paint);
            super.draw(canvas);
            canvas.restoreToCount(save);
        }
    }
}
