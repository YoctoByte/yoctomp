package yoctobyte.yoctomp;

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
import android.view.MenuItem;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        CreatePlaylistFragment.OnFragmentInteractionListener,
        AccountFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        PlaylistFragment.OnFragmentInteractionListener{

    private HashMap<Integer, Fragment> activeFragments = new HashMap<>();
    private HashMap<Integer, Class> fragmentClasses = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentClasses.put(R.id.nav_home, HomeFragment.class);
        fragmentClasses.put(R.id.nav_local_music, LocalMusicFragment.class);
        fragmentClasses.put(R.id.nav_create_playlist, CreatePlaylistFragment.class);
        fragmentClasses.put(R.id.nav_settings, SettingsFragment.class);
        fragmentClasses.put(R.id.nav_account, AccountFragment.class);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = HomeFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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
        Fragment fragment;
        Class fragmentClass;
        FragmentManager fragmentManager;

        for (HashMap.Entry<Integer, Class> entry: fragmentClasses.entrySet()) {
            int navId = entry.getKey();
            if (item.getItemId() == navId) {
                if (activeFragments.containsKey(navId)) {
                    fragment = activeFragments.get(navId);
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.main_content, fragment).commit();
                } else {
                    fragmentClass = entry.getValue();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}