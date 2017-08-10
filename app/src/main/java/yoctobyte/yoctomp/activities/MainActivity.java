package yoctobyte.yoctomp.activities;


import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;

import yoctobyte.yoctomp.data.Track;
import yoctobyte.yoctomp.fragments.MediaPlayerFragment;
import yoctobyte.yoctomp.fragments.PlaylistFragment;
import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.fragments.SettingsFragment;
import yoctobyte.yoctomp.fragments.CreatePlaylistFragment;
import yoctobyte.yoctomp.fragments.HomeFragment;
import yoctobyte.yoctomp.fragments.InfoFragment;
import yoctobyte.yoctomp.fragments.LocalMusicFragment;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        CreatePlaylistFragment.OnFragmentInteractionListener,
        InfoFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        PlaylistFragment.OnFragmentInteractionListener {

    private FragmentManager fragmentManager;
    private Class currentFragment = HomeFragment.class;

    private ArrayList<Class> fragmentClasses = new ArrayList<>();
    private HashMap<Class, Fragment> activeFragments = new HashMap<>();
    private SparseArray<Class> drawerFragments = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "onCreate is called");

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerFragments.put(R.id.drawer_home, HomeFragment.class);
        drawerFragments.put(R.id.drawer_local_music, LocalMusicFragment.class);
        drawerFragments.put(R.id.drawer_create_playlist, CreatePlaylistFragment.class);
        drawerFragments.put(R.id.nav_settings, SettingsFragment.class);
        drawerFragments.put(R.id.drawer_info, InfoFragment.class);

        fragmentClasses.add(HomeFragment.class);
        fragmentClasses.add(LocalMusicFragment.class);
        fragmentClasses.add(CreatePlaylistFragment.class);
        fragmentClasses.add(SettingsFragment.class);
        fragmentClasses.add(InfoFragment.class);
        fragmentClasses.add(MediaPlayerFragment.class);

        // Preload fragments
        fragmentManager = getSupportFragmentManager();
        for (Class fragmentClass: fragmentClasses) {
            try {
                Fragment fragment = (Fragment) fragmentClass.newInstance();
                fragmentManager.beginTransaction().attach(fragment).commit();
                activeFragments.put(fragmentClass, fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Load currently active fragment
        fragmentManager.beginTransaction().replace(R.id.mainContent, activeFragments.get(currentFragment)).commit();

        // Create drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("MainActivity", "onConfigurationChanged is called");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Log.d("onNavigationItemSelect", "is called");
        Fragment fragment;
        Class fragmentClass;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        for (int i = 0; i< drawerFragments.size(); i++) {
            int navId = drawerFragments.keyAt(i);
            fragmentClass = drawerFragments.get(navId);
            if (item.getItemId() == navId) {
                if (activeFragments.containsKey(fragmentClass)) {
                    fragment = activeFragments.get(fragmentClass);
                    fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();
                    currentFragment = fragmentClass;
                } else {
                    fragmentClass = drawerFragments.get(navId);
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();
                        activeFragments.put(fragmentClass, fragment);
                        currentFragment = fragmentClass;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onTrackClicked(Track track) {
        fragmentManager.beginTransaction().replace(R.id.mainContent, activeFragments.get(MediaPlayerFragment.class)).commit();
        currentFragment = MediaPlayerFragment.class;
    }
}