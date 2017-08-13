package yoctobyte.yoctomp.activities;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;

import java.util.HashMap;
import java.util.Map;

import yoctobyte.yoctomp.data.Track;
import yoctobyte.yoctomp.fragments.MediaPlayerFragment;
import yoctobyte.yoctomp.fragments.SettingsFragment;
import yoctobyte.yoctomp.fragments.CreatePlaylistFragment;
import yoctobyte.yoctomp.fragments.HomeFragment;
import yoctobyte.yoctomp.fragments.InfoFragment;
import yoctobyte.yoctomp.fragments.LocalMusicFragment;
import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.interfaces.OnTrackInteractionListener;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnTrackInteractionListener{

    private FragmentManager fragmentManager;

    private static final String HOME_FRAGMENT_TAG = "HomeFragment";
    private static final String LOCAL_MUSIC_FRAGMENT_TAG = "LocalMusicFragment";
    private static final String CREATE_PLAYLIST_FRAGMENT_TAG = "CreatePlaylisFragment";
    private static final String SETTINGS_FRAGMENT_TAG = "SettingsFragment";
    private static final String INFO_FRAGMENT_TAG = "InfoFragment";
    private static final String MEDIA_PLAYER_FRAGMENT_TAG = "MediaPlayerFragment";
    // private static final String PLAYLIST_TAG_PREFIX = "PlaylistFragment_";

    private HashMap<String, Fragment> activeFragments = new HashMap<>();  // Keys are fragmentClasses.
    private SparseArray<String> drawerFragments = new SparseArray<>();  // Keys are ids of the drawer items.
    private HashMap<String, Class> fragmentClasses = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MainActivity", "onCreate is called");

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        fragmentClasses.put(HOME_FRAGMENT_TAG, HomeFragment.class);
        fragmentClasses.put(LOCAL_MUSIC_FRAGMENT_TAG, LocalMusicFragment.class);
        fragmentClasses.put(CREATE_PLAYLIST_FRAGMENT_TAG, CreatePlaylistFragment.class);
        fragmentClasses.put(SETTINGS_FRAGMENT_TAG, SettingsFragment.class);
        fragmentClasses.put(INFO_FRAGMENT_TAG, InfoFragment.class);
        fragmentClasses.put(MEDIA_PLAYER_FRAGMENT_TAG, MediaPlayerFragment.class);

        drawerFragments.put(R.id.drawer_home, HOME_FRAGMENT_TAG);
        drawerFragments.put(R.id.drawer_local_music, LOCAL_MUSIC_FRAGMENT_TAG);
        drawerFragments.put(R.id.drawer_create_playlist, CREATE_PLAYLIST_FRAGMENT_TAG);
        drawerFragments.put(R.id.nav_settings, SETTINGS_FRAGMENT_TAG);
        drawerFragments.put(R.id.drawer_info, INFO_FRAGMENT_TAG);

        // Preload fragments
        fragmentManager = getSupportFragmentManager();
        for (Map.Entry<String, Class> entry: fragmentClasses.entrySet()) {
            String fragmentTag = entry.getKey();
            Class fragmentClass = entry.getValue();
            try {
                Fragment fragment = (Fragment) fragmentClass.newInstance();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.attach(fragment);
                transaction.commit();
                activeFragments.put(fragmentTag, fragment);
            } catch (Exception e) {
                Log.d("MainActivity", fragmentTag + " not preloaded");
                // Fragment not preloaded
            }
        }

        // Load home fragment into UI
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.mainContent, activeFragments.get(HOME_FRAGMENT_TAG), HOME_FRAGMENT_TAG);
        transaction.commit();
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        String fragmentTag;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);

        int navId = item.getItemId();
        fragmentTag = drawerFragments.get(navId);
        if (item.getItemId() == navId) {
            if (activeFragments.containsKey(fragmentTag)) {
                Log.d("onNavigationItemSelect", "contains key");
                fragment = activeFragments.get(fragmentTag);
                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment, fragmentTag).commit();
            } else {
                Log.d("onNavigationItemSelect", "does not contain key");
                try {
                    fragment = (Fragment) fragmentClasses.get(fragmentTag).newInstance();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.mainContent, fragment, fragmentTag);
                    transaction.commit();
                    activeFragments.put(fragmentTag, fragment);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    @Override
    public void onTrackPlayed(Track track) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment;
        if (activeFragments.containsKey(MEDIA_PLAYER_FRAGMENT_TAG)) {
            fragment = activeFragments.get(MEDIA_PLAYER_FRAGMENT_TAG);
        } else {
            try {
                fragment = (Fragment) fragmentClasses.get(MEDIA_PLAYER_FRAGMENT_TAG).newInstance();
            } catch (Exception e) {
                fragment = null;
            }
        }
        if (fragment != null) {
            transaction.replace(R.id.mainContent, fragment, MEDIA_PLAYER_FRAGMENT_TAG);
            transaction.commit();
            MediaPlayerFragment mediaPlayerFragment = (MediaPlayerFragment) activeFragments.get(MEDIA_PLAYER_FRAGMENT_TAG);
            mediaPlayerFragment.playTrack(track);
        }
    }
}