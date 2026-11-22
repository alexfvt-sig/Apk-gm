package ru.monohome.app;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(13, 14, 16);
    private static final int CARD = Color.rgb(27, 29, 33);
    private static final int WHITE = Color.rgb(246, 246, 247);
    private static final int MUTED = Color.rgb(156, 160, 168);

    private final List<AppEntry> allApps = new ArrayList<>();
    private final List<AppEntry> shownApps = new ArrayList<>();
    private GridAdapter adapter;
    private GridView grid;
    private TextView countView;
    private TextView homeButton;
    private TextView emptyView;
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildScreen();
        loadApps();
        updateHomeButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (homeButton != null) updateHomeButton();
    }

    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(20), dp(12), dp(20), dp(12));
        if (Build.VERSION.SDK_INT >= 35) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                view.setPadding(dp(20) + bars.left, dp(12) + bars.top,
                        dp(20) + bars.right, dp(12) + bars.bottom);
                return insets;
            });
        }

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("MonoHome", 29, WHITE, true);
        heading.addView(title);
        TextView subtitle = text("Чёрно-белый экран приложений", 14, MUTED, false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(3);
        heading.addView(subtitle, subtitleParams);
        root.addView(heading);

        homeButton = text("Сделать главным экраном", 14, WHITE, true);
        homeButton.setGravity(Gravity.CENTER);
        homeButton.setPadding(dp(16), 0, dp(16), 0);
        homeButton.setBackground(roundRect(CARD, dp(15)));
        LinearLayout.LayoutParams homeParams = new LinearLayout.LayoutParams(-1, dp(50));
        homeParams.topMargin = dp(18);
        root.addView(homeButton, homeParams);
        homeButton.setOnClickListener(v -> requestHomeRole());

        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setTextSize(15);
        searchBox.setTextColor(WHITE);
        searchBox.setHintTextColor(MUTED);
        searchBox.setHint("Поиск приложений");
        searchBox.setPadding(dp(16), 0, dp(16), 0);
        searchBox.setBackground(roundRect(CARD, dp(15)));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(50));
        searchParams.topMargin = dp(13);
        root.addView(searchBox, searchParams);

        LinearLayout captionRow = new LinearLayout(this);
        captionRow.setGravity(Gravity.CENTER_VERTICAL);
        countView = text("Приложения", 13, MUTED, true);
        captionRow.addView(countView, new LinearLayout.LayoutParams(0, -2, 1));
        TextView actionHint = text("нажмите, чтобы открыть", 12, MUTED, false);
        captionRow.addView(actionHint);
        LinearLayout.LayoutParams captionParams = new LinearLayout.LayoutParams(-1, -2);
        captionParams.topMargin = dp(18);
        captionParams.bottomMargin = dp(8);
        root.addView(captionRow, captionParams);

        grid = new GridView(this);
        grid.setNumColumns(4);
        grid.setHorizontalSpacing(dp(4));
        grid.setVerticalSpacing(dp(8));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setSelector(android.R.color.transparent);
        grid.setClipToPadding(false);
        grid.setPadding(0, dp(4), 0, dp(8));
        adapter = new GridAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((parent, view, position, id) -> openApp(shownApps.get(position)));
        root.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1));

        emptyView = text("Приложения не найдены", 15, MUTED, false);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setVisibility(View.GONE);
        root.addView(emptyView, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView footer = text("Значки серые только внутри MonoHome.", 12, MUTED, false);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(-1, dp(30));
        footerParams.topMargin = dp(4);
        root.addView(footer, footerParams);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterApps(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
        setContentView(root);
    }

    private void loadApps() {
        allApps.clear();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> results;
        if (Build.VERSION.SDK_INT >= 33) {
            results = getPackageManager().queryIntentActivities(query,
                    PackageManager.ResolveInfoFlags.of(0));
        } else {
            results = getPackageManager().queryIntentActivities(query, 0);
        }
        for (ResolveInfo info : results) {
            if (info.activityInfo == null) continue;
            String packageName = info.activityInfo.packageName;
            if (packageName.equals(getPackageName())) continue;
            CharSequence label = info.loadLabel(getPackageManager());
            Drawable icon = info.loadIcon(getPackageManager());
            if (label == null || icon == null) continue;
            Intent launch = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            allApps.add(new AppEntry(label.toString(), packageName, launch, icon));
        }
        Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(allApps, (left, right) -> collator.compare(left.label, right.label));
        filterApps(searchBox == null ? "" : searchBox.getText().toString());
    }

    private void filterApps(String query) {
        shownApps.clear();
        String needle = query.trim().toLowerCase(Locale.ROOT);
        for (AppEntry app : allApps) {
            if (needle.isEmpty() || app.label.toLowerCase(Locale.ROOT).contains(needle)) {
                shownApps.add(app);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
        if (countView != null) countView.setText("Приложения · " + shownApps.size());
        if (emptyView != null) {
            boolean empty = shownApps.isEmpty();
            emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            grid.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
    }

    private void openApp(AppEntry app) {
        try {
            startActivity(app.launchIntent);
        } catch (Exception ignored) {
            // An app may have been removed after the list was loaded.
            loadApps();
        }
    }

    private void requestHomeRole() {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roleManager = getSystemService(RoleManager.class);
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME), 41);
                return;
            }
        }
        try {
            startActivity(new Intent("android.settings.HOME_SETTINGS"));
        } catch (Exception ignored) {
            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        }
    }

    private boolean isDefaultHome() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo home = getPackageManager().resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);
        return home != null && home.activityInfo != null
                && getPackageName().equals(home.activityInfo.packageName);
    }

    private void updateHomeButton() {
        boolean selected = isDefaultHome();
        homeButton.setText(selected ? "MonoHome выбран главным экраном ✓" : "Сделать главным экраном");
        homeButton.setEnabled(!selected);
        homeButton.setAlpha(selected ? 0.72f : 1f);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateHomeButton();
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private GradientDrawable roundRect(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static final class AppEntry {
        final String label;
        final String packageName;
        final Intent launchIntent;
        final Drawable icon;

        AppEntry(String label, String packageName, Intent launchIntent, Drawable icon) {
            this.label = label;
            this.packageName = packageName;
            this.launchIntent = launchIntent;
            this.icon = icon;
        }
    }

    private final class GridAdapter extends BaseAdapter {
        private final ColorMatrixColorFilter grayscale;
        GridAdapter() {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0f);
            grayscale = new ColorMatrixColorFilter(matrix);
        }
        @Override public int getCount() { return shownApps.size(); }
        @Override public Object getItem(int position) { return shownApps.get(position); }
        @Override public long getItemId(int position) { return position; }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout cell;
            ImageView icon;
            TextView label;
            if (convertView instanceof LinearLayout) {
                cell = (LinearLayout) convertView;
                icon = (ImageView) cell.getChildAt(0);
                label = (TextView) cell.getChildAt(1);
            } else {
                cell = new LinearLayout(MainActivity.this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER_HORIZONTAL);
                cell.setPadding(dp(2), dp(5), dp(2), dp(5));
                icon = new ImageView(MainActivity.this);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(54), dp(54));
                cell.addView(icon, iconParams);
                label = text("", 11, WHITE, false);
                label.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                label.setMaxLines(2);
                label.setEllipsize(android.text.TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, dp(34));
                labelParams.topMargin = dp(5);
                cell.addView(label, labelParams);
                cell.setLayoutParams(new GridView.LayoutParams(-1, dp(105)));
            }
            AppEntry app = shownApps.get(position);
            icon.setImageDrawable(app.icon);
            icon.setColorFilter(grayscale);
            label.setText(app.label);
            return cell;
        }
    }
}
