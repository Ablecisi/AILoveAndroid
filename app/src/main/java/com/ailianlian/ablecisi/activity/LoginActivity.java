package com.ailianlian.ablecisi.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.lifecycle.ViewModelProvider;

import com.ailianlian.ablecisi.MainActivity;
import com.ailianlian.ablecisi.R;
import com.ailianlian.ablecisi.baseclass.BaseActivity;
import com.ailianlian.ablecisi.constant.ExtrasConstant;
import com.ailianlian.ablecisi.constant.LoginTokenSecretConstant;
import com.ailianlian.ablecisi.databinding.ActivityLoginBinding;
import com.ailianlian.ablecisi.viewmodel.LoginViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.mobile.auth.gatewayauth.AuthRegisterViewConfig;
import com.mobile.auth.gatewayauth.AuthUIConfig;
import com.mobile.auth.gatewayauth.PhoneNumberAuthHelper;
import com.mobile.auth.gatewayauth.PreLoginResultListener;
import com.mobile.auth.gatewayauth.ResultCode;
import com.mobile.auth.gatewayauth.TokenResultListener;

import java.util.Objects;

/**
 * 登录页面
 * 基于welcome.html原型设计，只提供手机号登录功能
 */
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {
    private LoginViewModel loginViewModel;
    private TokenResultListener mTokenResultListener;
    private PhoneNumberAuthHelper mPhoneNumberAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (loginViewModel.isLoggedIn()) {

            navigateToMainActivity();// 如果已登录，直接跳转到主页
            finish();
        }

    }

    @Override
    protected ActivityLoginBinding getViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView() {
        // 初始化ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        // 手机号登录助手初始化
        mTokenResultListener = new TokenResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                showToast("获取token成功: " + s);
            }

            @Override
            public void onTokenFailed(String s) {
                showToast("获取token失败: " + s);
            }
        };
        mPhoneNumberAuthHelper = PhoneNumberAuthHelper.getInstance(getApplicationContext(), mTokenResultListener);
    }

    @Override
    protected void initData() {
        observeLoginResult(); // 观察登录结果
        observeLoadingState();// 观察加载状态
    }

    @Override
    protected void setListeners() {
        // 登录按钮
        binding.btnLogin.setOnClickListener(v ->
                login()
        );

        // 服务条款和隐私政策
        binding.tvTerms.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, "服务条款");
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "terms.html");
            startActivity(intent);
        });

        binding.tvPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, "隐私政策");
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "privacy.html");
            startActivity(intent);
        });

        binding.tvLicense.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(ExtrasConstant.EXTRA_WEB_VIEW_TITLE, "开源协议");
            intent.putExtra(ExtrasConstant.EXTRA_ASSET_PATH, "https://raw.githubusercontent.com/Ablecisi/Nexa-Campus/refs/heads/master/LICENSE");
            startActivity(intent);
        });
    }

    /**
     * 观察登录结果
     */
    private void observeLoginResult() {
        loginViewModel.getLoginResult().observe(this, result -> {
            if (result.isSuccess()) {
                navigateToMainActivity();// 登录成功，跳转到主页
            } else {
                showToast(result.getMessage());// 登录失败，显示错误信息
            }
        });
    }

    /**
     * 观察加载状态
     */
    private void observeLoadingState() {
        loginViewModel.getIsLoading().observe(this, re -> {
            isLoading(re);
        });
    }

    /**
     * 跳转到主页
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void login() {
        // 弹出对话框，获取手机号
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_input_phone);
        // 设置对话框背景透明
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(true); // 设置对话框可取消
        // 设置对话框宽高
        if (dialog.getWindow() != null) {
            dialog.getWindow()
                    .setLayout(
                            (int) (getResources().getDisplayMetrics().widthPixels * 0.9), // 宽度为屏幕宽度的80%
                            WindowManager.LayoutParams.WRAP_CONTENT // 高度自适应内容
                    );
        }
        TextInputEditText etPhone = dialog.findViewById(R.id.et_phone); // 获取手机号输入框
        TextInputEditText etPassword = dialog.findViewById(R.id.et_password); // 获取密码输入框
        if (etPhone.getText() == null) {
            etPhone.setText("");
        }
        if (etPassword.getText() == null) {
            etPassword.setText("");
        }
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(btn_confirm -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (phone.isEmpty()) {
                showToast("手机号不能为空");
                etPhone.requestFocus(); // 设置焦点到手机号输入框
            } else if (password.isEmpty()) {
                showToast("密码不能为空");
                etPassword.requestFocus(); // 设置焦点到密码输入框
            } else if (!phone.matches("\\d{11}")) { // 简单的手机号格式验证
                showToast("请输入有效的11位手机号");
                etPhone.requestFocus(); // 设置焦点到手机号输入框
            } else if (password.length() < 6) { // 简单的密码长度验证
                showToast("密码长度不能少于6位");
                etPassword.requestFocus(); // 设置焦点到密码输入框
            } else {
                dialog.dismiss();
                loginViewModel.login(phone, password, true);
            }
        });
        dialog.findViewById(R.id.btn_cancel).setOnClickListener(btn_cancel -> dialog.dismiss());
        dialog.show();
    }

    @Deprecated
    private void loginByPhone() {
        mPhoneNumberAuthHelper.getReporter().setLoggerEnable(true);
        mPhoneNumberAuthHelper.setAuthSDKInfo(LoginTokenSecretConstant.AUTH_SDK_INFO());
        //2、检测环境
        mPhoneNumberAuthHelper.checkEnvAvailable(PhoneNumberAuthHelper.SERVICE_TYPE_LOGIN);

        //3.1、加速拉起一键登录页面
        //在不是一进app就需要登录的场景 建议调用此接口 加速拉起一键登录页面
        //等到用户点击登录的时候 授权页可以秒拉
        //预取号的成功与否不影响一键登录功能，所以不需要等待预取号的返回。
        int timeout = 10000; // 设置超时时间为10秒
        mPhoneNumberAuthHelper.accelerateLoginPage(timeout, new PreLoginResultListener() {
            @Override
            public void onTokenSuccess(String s) {
                showToast("加速成功");//加速成功业务逻辑处理
            }

            @Override
            public void onTokenFailed(String s, String s1) {
                showToast("加速失败");//加速失败业务逻辑处理
            }
        });

        //3.2、添加授权页属性，调用获取登录Token接口，可以立马弹起授权页

        mPhoneNumberAuthHelper.setUIClickListener((code, context, jsonString) -> {
            //此处进行授权页各控件点击事件监控，根据不同code区分控件
            showToast("点击了授权页控件: " + code);
            if (ResultCode.CODE_ERROR_USER_CANCEL.equals(code)) {
                mPhoneNumberAuthHelper.quitLoginPage();
            }
        });
        // 4、设置授权页配置
        mPhoneNumberAuthHelper.removeAuthRegisterXmlConfig();
        // 移除默认的注册view配置
        mPhoneNumberAuthHelper.removeAuthRegisterViewConfig();
        //添加自定义切换其他登录方式
        mPhoneNumberAuthHelper.addAuthRegistViewConfig("", new AuthRegisterViewConfig.Builder()
                //此处需改为用户自定义添加的view
                .setView(null)
                .setRootViewId(AuthRegisterViewConfig.RootViewId.ROOT_VIEW_ID_BODY)
                .setCustomInterface(context -> {
                    //此处可进行用户自定义view点击事件业务逻辑处理
                }).build());
        int authPageOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        //updateScreenSize(authPageOrientation);
        mPhoneNumberAuthHelper.setAuthUIConfig(new AuthUIConfig.Builder()
                .setAppPrivacyOne("《自定义隐私协议》", "https://www.baidu.com")
                .setAppPrivacyTwo("《自定义隐私协议》2", "https://baijiahao.baidu.com/s?id=1693920988135022454&wfr=spider&for=pc")
                .setAppPrivacyThree("《自定义隐私协议》3", "http://www.npc.gov.cn/zgrdw/npc/cwhhy/13jcwh/node_35014.htm")
                .setAppPrivacyColor(Color.GRAY, Color.parseColor("#002E00")) // 设置协议文字颜色和选中颜色
                //隐藏默认切换其他登录方式
                .setSwitchAccHidden(false)
                //隐藏默认Toast
                .setLogBtnToastHidden(true)

                //自定义协议页跳转协议，需要在清单文件配置自定义intent-filter，不需要自定义协议页，则请不要配置ProtocolAction
                .setProtocolAction("com.aliqin.mytel.protocolWeb")

                //沉浸式状态栏
                .setNavColor(Color.parseColor("#026ED2"))
                .setStatusBarColor(Color.parseColor("#026ED2"))
                .setWebViewStatusBarColor(Color.parseColor("#026ED2"))
                .setLightColor(true) // 设置授权页标题颜色为浅色
                .setBottomNavColor(Color.TRANSPARENT)// 设置底部导航栏颜色为透明
                .setWebNavTextSizeDp(20) // 设置WebView标题文字大小
                //图片或者xml的传参方式为不包含后缀名的全称 需要文件需要放在drawable或drawable-xxx目录下 in_activity.xml, mytel_app_launcher.png
                .setAuthPageActIn("in_activity", "out_activity") // 授权页背景图片
                .setAuthPageActOut("in_activity", "out_activity") // 授权页背景图片
                .setProtocolShakePath("protocol_shake") // 协议抖动动画
                .setVendorPrivacyPrefix("《") // 协议前缀
                .setVendorPrivacySuffix("》") // 协议后缀
                .setPageBackgroundPath("page_background_color") // 授权页背景图片
                .setLogoImgPath("mytel_app_launcher") // 授权页logo图片
                //一键登录按钮三种状态背景示例login_btn_bg.xml
                .setLogBtnBackgroundPath("login_btn_bg") // 一键登录按钮背景
                .setScreenOrientation(authPageOrientation) // 设置授权页屏幕方向
                .create()); // 设置授权页UI配置
        //用户控制返回键及左上角返回按钮效果
        mPhoneNumberAuthHelper.userControlAuthPageCancel();
        //用户禁用utdid
        mPhoneNumberAuthHelper.prohibitUseUtdid();
        //授权页是否跟随系统深色模式
        mPhoneNumberAuthHelper.setAuthPageUseDayLight(true);
        //授权页物理返回键禁用
        //mPhoneNumberAuthHelper.closeAuthPageReturnBack(true);
        //横屏水滴屏全屏适配
        mPhoneNumberAuthHelper.keepAuthPageLandscapeFullSreen(true);
        //授权页扩大协议按钮选择范围至我已阅读并同意
        mPhoneNumberAuthHelper.expandAuthPageCheckedScope(true);
        mPhoneNumberAuthHelper.setAuthListener(mTokenResultListener);
        mPhoneNumberAuthHelper.getLoginToken(this, timeout);
    }
}