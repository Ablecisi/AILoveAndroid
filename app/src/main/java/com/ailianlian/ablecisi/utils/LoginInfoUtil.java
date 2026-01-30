package com.ailianlian.ablecisi.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ailianlian.ablecisi.activity.LoginActivity;
import com.ailianlian.ablecisi.constant.LoginSharedPreferencesConstant;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * ailianlian
 * com.ailianlian.ablecisi.utils
 * LoginInfoUtil <br>
 * 登录信息工具类
 *
 * @author Ablecisi
 * @version 1.0
 * 2025/6/16
 * 星期一
 * 01:04
 */
public class LoginInfoUtil {

    private static final String userInterestsPrefConstant = "user_interests";
    private static final String userInterestsKeyConstant = "interests";

    /**
     * 保存登录信息到SharedPreferences
     *
     * @param context   上下文
     * @param loginType 登录类型
     * @param userId    用户ID
     * @param userToken 用户令牌
     */
    public static void saveLoginInfo(Context context, String loginType, String userId, String name, String userToken, String username, String avatarUrl) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(LoginSharedPreferencesConstant.KEY_LOGIN_TYPE, loginType); // 保存登录类型
        editor.putString(LoginSharedPreferencesConstant.KEY_USER_ID, userId); // 保存用户ID
        editor.putString(LoginSharedPreferencesConstant.KEY_TOKEN, userToken); // 保存用户令牌
        editor.putString(LoginSharedPreferencesConstant.KEY_USERNAME, username); // 保存用户名
        editor.putString(LoginSharedPreferencesConstant.KEY_NAME, name);
        editor.putString(LoginSharedPreferencesConstant.KEY_AVATAR_URL, avatarUrl); // 保存用户头像URL
        editor.apply();
    }

    /**
     * 获取登录类型
     *
     * @param context 上下文
     * @return 登录类型，如果未登录则返回null
     */
    public static String getLoginType(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LoginSharedPreferencesConstant.KEY_LOGIN_TYPE, "未登录");
    }

    /**
     * 获取用户ID
     *
     * @param context 上下文
     * @return 用户ID，如果未登录则返回null
     */
    public static String getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LoginSharedPreferencesConstant.KEY_USER_ID, "-1");
    }

    /**
     * 获取用户昵称
     *
     * @param context 上下文
     * @return 用户昵称，如果未登录则返回"游客"
     */
    public static String getName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LoginSharedPreferencesConstant.KEY_NAME, "游客");
    }

    /**
     * 获取用户名
     *
     * @param context 上下文
     * @return 用户名，如果未登录则返回null
     */
    public static String getUserName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LoginSharedPreferencesConstant.KEY_USERNAME, LoginSharedPreferencesConstant.NOT_LOGGED_IN);
    }

    /**
     * 获取用户令牌
     *
     * @param context 上下文
     * @return 用户令牌，如果未登录则返回null
     */
    public static String getUserToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LoginSharedPreferencesConstant.KEY_TOKEN, "");
    }

    /**
     * 获取用户头像URL
     *
     * @param context 上下文
     * @return 用户头像URL，如果未登录则返回null
     */
    public static String getAvatarUrl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(LoginSharedPreferencesConstant.KEY_AVATAR_URL, "");
    }

    /**
     * 检查用户是否已登录
     *
     * @param context 上下文
     * @return 如果已登录返回true，否则返回false
     */
    public static boolean isLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.contains(LoginSharedPreferencesConstant.KEY_USER_ID) &&
                sharedPreferences.contains(LoginSharedPreferencesConstant.KEY_TOKEN);
    }

    /**
     * 清除登录信息
     *
     * @param context 上下文
     */
    public static void clearLoginInfo(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LoginSharedPreferencesConstant.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LoginSharedPreferencesConstant.KEY_LOGIN_TYPE);
        editor.remove(LoginSharedPreferencesConstant.KEY_USER_ID);
        editor.remove(LoginSharedPreferencesConstant.KEY_TOKEN);
        editor.apply();
    }

    /**
     * 退出登录并跳转到登录页面
     *
     * @param context 上下文
     */
    public static void logout(Context context) {
        clearLoginInfo(context);
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        // 如果需要，可以在这里添加其他清理操作，比如清除缓存等
    }

    /**
     * 保存用户兴趣到SharedPreferences
     *
     * @param application 应用上下文
     * @param interests   用户兴趣列表
     */
    public static void saveUserInterests(Application application, List<String> interests) {
        SharedPreferences sharedPreferences = application.getSharedPreferences(userInterestsPrefConstant, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(userInterestsKeyConstant, JsonUtil.toJson(interests));
        editor.apply();
    }

    /**
     * 获取用户兴趣列表
     *
     * @param context 上下文
     * @return 用户兴趣列表
     */
    public static List<String> getUserInterests(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userInterestsPrefConstant, Context.MODE_PRIVATE);
        String interestsJson = sharedPreferences.getString(userInterestsKeyConstant, "[]");
        return JsonUtil.fromJson(interestsJson, new TypeToken<List<String>>() {
        }.getType());
    }
}
