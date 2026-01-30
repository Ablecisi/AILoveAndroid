package com.ailianlian.ablecisi.activity;

import android.annotation.SuppressLint;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.databinding.ActivityWebviewBinding;

/**
 * ailianlian
 * com.ailianlian.ablecisi.activity
 * WebViewActivity <br>
 * WebView展示页面
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/15
 * 星期日
 * 17:30
 */
public class WebViewActivity extends BaseActivity<ActivityWebviewBinding> {

    @Override
    protected ActivityWebviewBinding getViewBinding() {
        return ActivityWebviewBinding.inflate(getLayoutInflater());
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initView() {
        String title = getIntent().getStringExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE);
        String assetPath = getIntent().getStringExtra(ExtrasConstant.EXTRA_ASSET_PATH);
        if (title != null) setTitle(title);

        WebView webView = binding.webView;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        // 设置WebView的缓存模式
        webView.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
        // 设置WebView的缩放控件
        webView.getSettings().setBuiltInZoomControls(true);
        if (assetPath == null || assetPath.isEmpty()) {
            // 如果没有提供assetPath，则用字符串构造一个默认的HTML内容
            webView.loadData(
                    "<! DOCTYPE html>\n" +
                            "<html lang=\"zh_CN\">\n" +
                            "<head>\n" +
                            "    <meta charset=\"UTF-8\">\n" +
                            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                            "    <title>404</title>\n" +
                            "</head>\n" +
                            "\n" +
                            "<body>\n" +
                            "    <h1>404 NOT Found</h1>\n" +
                            "    <p>这是一个默认的WebView页面。</p>\n" +
                            "</body>\n" +
                            "</html>",
                    "text/html", "UTF-8");
            return;
        }
        if (assetPath.startsWith("http://") || assetPath.startsWith("https://")) {
            // 如果是网络地址，直接加载
            webView.loadUrl(assetPath);
            return;
        }
        webView.loadUrl("file:///android_asset/" + assetPath);
    }

    @Override
    protected void initData() {
    }

    @Override
    protected void setListeners() {
    }
}
