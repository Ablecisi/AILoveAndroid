package com.ailianlian.ablecisi.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.viewmodel.LoginViewModel;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 设置页面
 */
public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SwitchCompat switchMessages;
    private SwitchCompat switchCommunity;
    private SwitchCompat switchDarkMode;
    private TextView textCacheSize;
    private TextView textVersion;
    private TextView textLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 初始化Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // 初始化SharedPreferences
        preferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // 初始化视图
        initViews();

        // 设置点击事件
        setupClickListeners();

        // 加载设置
        loadSettings();
    }

    private void initViews() {
        // 初始化开关
        switchMessages = findViewById(R.id.switchMessages);
        switchCommunity = findViewById(R.id.switchCommunity);
        switchDarkMode = findViewById(R.id.switchDarkMode);

        // 初始化文本视图
        textCacheSize = findViewById(R.id.textCacheSize);
        textVersion = findViewById(R.id.textVersion);
        textLanguage = findViewById(R.id.textLanguage);

        // 设置缓存大小
        calculateCacheSize();

        // 设置版本信息
        setVersionInfo();
    }

    private void setupClickListeners() {
        // 个人资料
        findViewById(R.id.layoutProfile).setOnClickListener(v -> {
            // 跳转到个人资料页面
            finish(); // 直接返回，因为个人资料页面是主页的一个标签页
        });

        // 账号安全
        findViewById(R.id.layoutSecurity).setOnClickListener(v -> {
            Toast.makeText(this, "账号安全功能尚未实现", Toast.LENGTH_SHORT).show();
        });

        // 隐私设置
        findViewById(R.id.layoutPrivacy).setOnClickListener(v -> {
            Toast.makeText(this, "隐私设置功能尚未实现", Toast.LENGTH_SHORT).show();
        });

        // 消息通知开关
        switchMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("message_notifications", isChecked).apply();
            Toast.makeText(this, isChecked ? "已开启消息通知" : "已关闭消息通知", Toast.LENGTH_SHORT).show();
        });

        // 社区通知开关
        switchCommunity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("community_notifications", isChecked).apply();
            Toast.makeText(this, isChecked ? "已开启社区通知" : "已关闭社区通知", Toast.LENGTH_SHORT).show();
        });

        // 语言设置
        findViewById(R.id.layoutLanguage).setOnClickListener(v -> {
            showLanguageDialog();
        });

        // 深色模式开关
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // 启用深色模式
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); // 跟随系统模式
            }
        });

        // 清除缓存
        findViewById(R.id.layoutClearCache).setOnClickListener(v -> {
            showClearCacheDialog();
        });

        // 关于我们
        findViewById(R.id.layoutAbout).setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, "关于我们");
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "about.html");
            startActivity(intent);
        });

        // 用户协议
        findViewById(R.id.layoutTerms).setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, "用户协议");
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "terms.html");
            startActivity(intent);
        });

        // 隐私政策
        findViewById(R.id.layoutPrivacyPolicy).setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, "隐私政策");
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "privacy.html");
            startActivity(intent);
        });

        // 版本信息
        findViewById(R.id.layoutVersion).setOnClickListener(v -> {
            Toast.makeText(this, "当前版本: " + textVersion.getText(), Toast.LENGTH_SHORT).show();
        });

        // 退出登录
        findViewById(R.id.buttonLogout).setOnClickListener(v -> {
            showLogoutDialog();
        });
    }

    private void loadSettings() {
        // 加载通知设置
        switchMessages.setChecked(preferences.getBoolean("message_notifications", true));
        switchCommunity.setChecked(preferences.getBoolean("community_notifications", true));

        // 加载深色模式设置
        boolean darkMode = preferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(darkMode);

        // 加载语言设置
        String language = preferences.getString("language", "zh");
        if (language.equals("zh")) {
            textLanguage.setText(R.string.settings_language_chinese);
        } else {
            textLanguage.setText(R.string.settings_language_english);
        }
    }

    private void showLanguageDialog() {
        String[] languages = {getString(R.string.settings_language_chinese), getString(R.string.settings_language_english)};
        int checkedItem = preferences.getString("language", "zh").equals("zh") ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String language = which == 0 ? "zh" : "en";
                    preferences.edit().putString("language", language).apply();
                    textLanguage.setText(languages[which]);
                    dialog.dismiss();
                    Toast.makeText(this, "语言设置已更新，重启应用后生效", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_clear_cache)
                .setMessage("确定要清除缓存吗？")
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    clearCache();
                    calculateCacheSize();
                    Toast.makeText(this, R.string.settings_cache_cleared, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.settings_logout_confirm)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    // 退出登录
                    LoginViewModel loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
                    loginViewModel.logout();
                    // 返回登录页面
                    Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
                    // 跳转到登录页面
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void calculateCacheSize() {
        try {
            File cacheDir = getCacheDir();
            File externalCacheDir = getExternalCacheDir();
            long size = getFolderSize(cacheDir);
            if (externalCacheDir != null) {
                size += getFolderSize(externalCacheDir);
            }
            textCacheSize.setText(formatSize(size));
        } catch (Exception e) {
            e.printStackTrace();
            textCacheSize.setText("0MB");
        }
    }

    private long getFolderSize(File file) {
        long size = 0;
        if (file != null && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        size += getFolderSize(f);
                    } else {
                        size += f.length();
                    }
                }
            }
        }
        return size;
    }

    private String formatSize(long size) {
        if (size <= 0) {
            return "0MB";
        }
        
        DecimalFormat df = new DecimalFormat("#.##");
        float result;
        
        if (size < 1024) {
            return size + "B";
        } else if (size < 1048576) {
            result = (float) size / 1024;
            return df.format(result) + "KB";
        } else {
            result = (float) size / 1048576;
            return df.format(result) + "MB";
        }
    }

    private void clearCache() {
        try {
            File cacheDir = getCacheDir();
            File externalCacheDir = getExternalCacheDir();
            deleteDir(cacheDir);
            if (externalCacheDir != null) {
                deleteDir(externalCacheDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.exists()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private void setVersionInfo() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            textVersion.setText(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            textVersion.setText("1.0.0");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 