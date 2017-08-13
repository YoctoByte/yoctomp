package yoctobyte.yoctomp.fragments;


import android.content.Context;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.adapters.PlaylistAdapter;
import yoctobyte.yoctomp.data.Database;
import yoctobyte.yoctomp.data.Track;
import yoctobyte.yoctomp.data.TrackTable;
import yoctobyte.yoctomp.interfaces.FragmentStateListener;


public class PlaylistFragment extends ListFragment {
    protected boolean initialized = false;
    protected FragmentStateListener fragmentStateListener;
    protected PlaylistAdapter playlistAdapter;
    protected OnPlaylistInteractionListener listener;

    protected ArrayList<Track> tracks = new ArrayList<>();  // This list won't handle track deletion correctly...
    private String playlistName;


    public PlaylistFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!initialized) {
            Log.d("PlaylistFragment", "onCreate is called");

            setRetainInstance(true);

            playlistAdapter = new PlaylistAdapter(getActivity());
            setListAdapter(playlistAdapter);
        }

        initialized = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        view.setVerticalScrollBarEnabled(false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnPlaylistInteractionListener) context;
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

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d("onListItemClick", position + " " + id);
        Log.d("onListItemClick", listView.getItemAtPosition(position).toString());
        super.onListItemClick(listView, view, position, id);
        listener.onTrackClicked(tracks.get(position));
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) actionBar.setTitle(playlistName);

        if (playlistAdapter.isEmpty()) populatePlaylist();
    }

    private void populatePlaylist() {
        if (playlistName == null) {
            return;
        }
        Database db = new Database(getActivity());
        TrackTable playlistTable = db.getTablePlaylist(playlistName);

        playlistAdapter.clear();
        tracks.clear();
        for (Track track: playlistTable.readTracks()) {
            playlistAdapter.addTrack(track);
            tracks.add(track);
        }

        playlistAdapter.notifyDataSetChanged();
    }

    public interface OnPlaylistInteractionListener {
        void onTrackClicked(Track track);
    }
}