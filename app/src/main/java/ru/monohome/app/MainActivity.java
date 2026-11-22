package ru.monohome.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(20, 21, 23);
    private static final int PANEL = Color.rgb(36, 38, 41);
    private static final int TEXT = Color.rgb(245, 245, 245);
    private static final int MUTED = Color.rgb(185, 187, 190);

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setContentView(makeScreen());
    }

    private View makeScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        scroll.addView(page);

        TextView title = text("MonoIcons", 30, TEXT, true);
        page.addView(title);

        TextView subtitle = text("Монохромные значки в One UI Home", 16, MUTED, false);
        LinearLayout.LayoutParams subtitleParams = params(-1, -2);
        subtitleParams.topMargin = dp(6);
        page.addView(subtitle, subtitleParams);

        TextView description = text(
                "Модуль переводит значки приложений в оттенки серого прямо в стандартном лаунчере Samsung. "
                        + "Он обрабатывает значок при загрузке, поэтому новые установленные приложения тоже получают фильтр.",
                15, TEXT, false);
        description.setLineSpacing(dp(4), 1f);
        addCard(page, description, 22);

        TextView requirement = text(
                "Нужны root, Magisk и LSPosed с поддержкой API 102. "
                        + "Без системного модуля Android не разрешает менять значки внутри One UI Home.",
                14, Color.rgb(224, 224, 224), true);
        requirement.setLineSpacing(dp(4), 1f);
        addCard(page, requirement, 14);

        TextView steps = text(
                "Как включить\n"
                        + "1. Установи этот APK.\n"
                        + "2. В LSPosed включи MonoIcons. Область действия уже указана для One UI Home.\n"
                        + "3. Перезапусти телефон или One UI Home.\n"
                        + "4. Если старые значки остались цветными, перезагрузи One UI Home, чтобы он перечитал кэш.",
                15, TEXT, false);
        steps.setLineSpacing(dp(5), 1f);
        addCard(page, steps, 14);

        Button open = new Button(this);
        open.setText("Открыть LSPosed");
        open.setTextColor(TEXT);
        open.setAllCaps(false);
        open.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(75, 77, 80)));
        LinearLayout.LayoutParams buttonParams = params(-1, dp(52));
        buttonParams.topMargin = dp(20);
        page.addView(open, buttonParams);
        open.setOnClickListener(v -> openLsposed());

        TextView note = text(
                "Этот APK не разблокирует загрузчик и не устанавливает root. На Samsung разблокировка загрузчика может стереть данные и изменить состояние Knox.",
                12, MUTED, false);
        note.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams noteParams = params(-1, -2);
        noteParams.topMargin = dp(18);
        page.addView(note, noteParams);

        return scroll;
    }

    private void addCard(LinearLayout page, View child, int topMarginDp) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(15), dp(16), dp(15));
        card.setBackgroundColor(PANEL);
        LinearLayout.LayoutParams cardParams = params(-1, -2);
        cardParams.topMargin = dp(topMarginDp);
        page.addView(card, cardParams);
        card.addView(child);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private void openLsposed() {
        Intent launch = getPackageManager().getLaunchIntentForPackage("org.lsposed.manager");
        if (launch != null) {
            startActivity(launch);
            return;
        }
        Toast.makeText(this, "LSPosed Manager не найден", Toast.LENGTH_LONG).show();
    }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
