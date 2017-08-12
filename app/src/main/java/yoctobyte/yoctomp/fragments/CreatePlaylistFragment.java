package yoctobyte.yoctomp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.interfaces.FragmentStateListener;


public class CreatePlaylistFragment extends Fragment {
    private FragmentStateListener fragmentStateListener;

    public CreatePlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_playlist, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentStateListener = (FragmentStateListener) context;
            fragmentStateListener.onFragmentAttach(this);
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentStateListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentStateListener.onFragmentDetach(this);
        fragmentStateListener = null;
    }
}