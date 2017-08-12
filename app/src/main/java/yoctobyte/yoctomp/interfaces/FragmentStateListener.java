package yoctobyte.yoctomp.interfaces;


import android.support.v4.app.Fragment;

public interface FragmentStateListener {
    void onFragmentAttach(Fragment fragment);
    void onFragmentDetach(Fragment fragment);
}
