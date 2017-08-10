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

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.adapters.PlaylistAdapter;
import yoctobyte.yoctomp.data.Database;
import yoctobyte.yoctomp.data.Track;
import yoctobyte.yoctomp.data.TrackTable;


public class PlaylistFragment extends ListFragment {
    protected PlaylistAdapter playlistAdapter;
    protected OnFragmentInteractionListener listener;
    private String playlistName;


    public PlaylistFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        if (playlistAdapter == null) {
            playlistAdapter = new PlaylistAdapter(getActivity());
            setListAdapter(playlistAdapter);
        }
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
            listener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d("onListItemClick", position + " " + id);
        Log.d("onListItemClick", listView.getItemAtPosition(position).toString());
        super.onListItemClick(listView, view, position, id);
        listener.onTrackClicked(playlistAdapter.getTrack(position));
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
        for (Track track: playlistTable.readTracks()) {
            playlistAdapter.addTrack(track);
        }

        playlistAdapter.notifyDataSetChanged();
    }

    public interface OnFragmentInteractionListener {
        void onTrackClicked(Track track);
    }
}