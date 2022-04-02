package com.nqm.event_manager.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.nqm.event_manager.R;
import com.nqm.event_manager.fragments.ManageSalaryFragment;
import com.nqm.event_manager.fragments.ManageEmployeeFragment;
import com.nqm.event_manager.fragments.ManageEventFragment;
import com.nqm.event_manager.fragments.MoreSettingsFragment;
import com.nqm.event_manager.utils.DatabaseAccess;

public class RootActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    NavigationView navigationView;
    Toolbar toolbar;
    TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        if (!DatabaseAccess.isAllDataLoaded(getApplicationContext())) {
            Intent splashIntent = new Intent(this, SplashActivity.class);
            startActivity(splashIntent);
            finish();
        }
        setContentView(R.layout.activity_root);
        initView();
    }

    private void initView() {

        //Init toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        toggle.setDrawerIndicatorEnabled(false);
//        toggle.setHomeAsUpIndicator(R.drawable.ic_list);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

//        toggle.setToolbarNavigationClickListener(v -> {
//            if (drawer.isDrawerVisible(GravityCompat.START)) {
//                drawer.closeDrawer(GravityCompat.START);
//            } else {
//                drawer.openDrawer(GravityCompat.START);
//            }
//        });

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        userNameTextView = navigationView.getHeaderView(0).findViewById(R.id.nav_user_name_text_view);
        String userDisplayName = LogInActivity.getFirebaseAuth().getCurrentUser().getEmail();
        userNameTextView.setText(userDisplayName);
        userNameTextView.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Bạn có muốn đăng xuất?")
                    .setPositiveButton("Đồng ý", (dialog, which) -> {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(this, LogInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
//        Toast.makeText(this, "user display name = " + userDisplayName, Toast.LENGTH_SHORT).show();

        //open event management as default.
        openManageEventFragment();

        setMenuItemChecked();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            super.onBackPressed();
        } else {
            drawer.openDrawer(GravityCompat.START);
        }
    }

    private void setMenuItemChecked() {
        MenuItem menuItem = navigationView.getMenu().findItem(R.id.nav_event);
        menuItem.setChecked(true);
    }

    public void ReplaceFragment(Fragment newFragment) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.root_content, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_event) {
            openManageEventFragment();
        } else if (id == R.id.nav_employee) {
            openManageEmployeeFragment();
        } else if (id == R.id.nav_calculate_salaries) {
            openCalculateSalaryFragment();
        } else if (id == R.id.nav_settings) {
            openSettingsActivity();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void openManageEventFragment() {

        // Replace root view to default fragment
        toolbar.setTitle(R.string.manage_event_fragment_label);
        Fragment newFragment = new ManageEventFragment();
        ReplaceFragment(newFragment);
    }

    private void openManageEmployeeFragment() {
        // Replace root view to default fragment
        toolbar.setTitle(R.string.manage_employee_fragment_label);
        Fragment newFragment = new ManageEmployeeFragment();
        ReplaceFragment(newFragment);
    }

    private void openCalculateSalaryFragment() {
        // Replace root view to default fragment
        toolbar.setTitle(R.string.calculate_salary_fragment_label);
        Fragment newFragment = new ManageSalaryFragment();
        ReplaceFragment(newFragment);
    }

    private void openSettingsActivity() {
        toolbar.setTitle(R.string.more_settings_fragment_label);
        Fragment newFragment = new MoreSettingsFragment();
        ReplaceFragment(newFragment);
    }

}
