package yoctobyte.yoctomp.fragments;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.data.Track;

public class MediaPlayerFragment extends Fragment{
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
        try {
            mediaPlayer.setDataSource(getActivity(), track.getUri());
            mediaPlayer.prepare();
        } catch (IOException e) {
            // something went wrong?
        }
        mediaPlayer.start();
    }

    //public void pause() {mediaPlayer.pause();}
    //public boolean isPlaying() {return mediaPlayer.isPlaying();}
}
