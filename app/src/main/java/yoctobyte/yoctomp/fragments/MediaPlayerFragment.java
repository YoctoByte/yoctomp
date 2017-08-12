package yoctobyte.yoctomp.fragments;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.data.Track;
import yoctobyte.yoctomp.interfaces.FragmentStateListener;

public class MediaPlayerFragment extends Fragment{
    private FragmentStateListener fragmentStateListener;
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_player, container, false);
        view.setVerticalScrollBarEnabled(false);
        return view;
    }

    public void playTrack(Track track) {
        Uri myUri = track.getUri();
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getContext(), myUri);
            mediaPlayer.prepare();
        } catch (IOException e) {
            // something went wrong?
        }
        mediaPlayer.start();
    }

    public void pause() {

    }

    public void play() {

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
