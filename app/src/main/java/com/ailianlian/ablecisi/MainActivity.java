package com.ailianlian.ablecisi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.ailianlian.ablecisi.activity.LoginActivity;
import com.ailianlian.ablecisi.constant.LoginSharedPreferencesConstant;
import com.ailianlian.ablecisi.databinding.ActivityMainBinding;
import com.ailianlian.ablecisi.viewmodel.LoginViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private LoginViewModel loginViewModel;
    private ActivityMainBinding binding;
    private NavController navController;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // 初始化ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        
        // 显示欢迎信息
        String userName = loginViewModel.getUserName();
        if (userName != null) {
            if (userName.equals(LoginSharedPreferencesConstant.NOT_LOGGED_IN)) {
                Toast.makeText(this, "对不起，您未登录！", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(this, userName + " 欢迎回来！", Toast.LENGTH_SHORT).show();
        }
        BottomNavigationView bottomNavigationView = binding.bottomNavigation;

        // 获取NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // 设置底部导航
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * 退出登录
     */
    private void logout() {
        loginViewModel.logout();
        navigateToLoginActivity();
    }
    
    /**
     * 跳转到登录页面
     */
    private void navigateToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
} 