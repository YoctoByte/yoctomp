package yoctobyte.yoctomp.activities;


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

    private SparseArray<Fragment> activeFragments = new SparseArray<>();
    private SparseArray<Class> fragmentClasses = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Preload fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment;
        Class fragmentClass;

        fragmentClasses.put(R.id.drawer_home, HomeFragment.class);
        fragmentClasses.put(R.id.drawer_local_music, LocalMusicFragment.class);
        fragmentClasses.put(R.id.drawer_create_playlist, CreatePlaylistFragment.class);
        fragmentClasses.put(R.id.nav_settings, SettingsFragment.class);
        fragmentClasses.put(R.id.drawer_info, InfoFragment.class);

        for (int i=0; i<fragmentClasses.size(); i++) {
            int navId = fragmentClasses.keyAt(i);
            fragmentClass = fragmentClasses.get(navId);
            try {
                fragment = (Fragment) fragmentClass.newInstance();
                fragmentManager.beginTransaction().attach(fragment).commit();
                activeFragments.put(navId, fragment);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        fragmentManager.beginTransaction().replace(R.id.main_content, activeFragments.get(R.id.drawer_home)).commit();

        // Create drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        FragmentManager fragmentManager;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        for (int i=0; i<fragmentClasses.size(); i++) {
            int navId = fragmentClasses.keyAt(i);
            if (item.getItemId() == navId) {
                if (activeFragments.indexOfKey(navId) >= 0) {
                    fragment = activeFragments.get(navId);
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();
                } else {
                    fragmentClass = fragmentClasses.get(navId);
                    try {
                        fragment = (Fragment) fragmentClass.newInstance();
                        fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();
                        activeFragments.put(navId, fragment);
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
}