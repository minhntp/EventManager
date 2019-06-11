package com.nqm.event_manager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.FirebaseApp;
import com.nqm.event_manager.R;
import com.nqm.event_manager.fragments.ManageSalaryFragment;
import com.nqm.event_manager.fragments.ManageEmployeeFragment;
import com.nqm.event_manager.fragments.ManageEventFragment;
import com.nqm.event_manager.fragments.MoreSettingsFragment;

public class RootActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    NavigationView navigationView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_root);
        initView();
    }

    @Override
    protected void onResume() {

        super.onResume();
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
