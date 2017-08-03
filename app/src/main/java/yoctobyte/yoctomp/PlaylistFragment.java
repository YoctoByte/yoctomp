package yoctobyte.yoctomp;


import android.support.v4.app.ListFragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class PlaylistFragment extends ListFragment {
    protected ArrayList<HashMap<String, String>> tracks = new ArrayList<>();
    private SimpleAdapter simpleAdapter;
    private String playlistName;
    private MediaPlayer mediaPlayer;


    public PlaylistFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mediaPlayer = new MediaPlayer();

        simpleAdapter = new SimpleAdapter(getActivity(), tracks, R.layout.item_playlist,
                new String[] {"title", "artist", "length"}, new int[] {R.id.trackTitle, R.id.trackArtist, R.id.trackLength});
        setListAdapter(simpleAdapter);

        if (tracks.size() == 0) {
            populatePlaylist();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d("onListItemClick", String.valueOf(getSelectedItemId()));
        Log.d("onListItemClick", String.valueOf(getSelectedItemPosition()));
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) actionBar.setTitle(playlistName);
    }

    private void populatePlaylist() {
        if (playlistName == null) {
            return;
        }
        Database db = new Database(getActivity());
        Database.TrackTable playlistTable = db.getTablePlaylist(playlistName);

        tracks = new ArrayList<>();
        for (Database.Track track: playlistTable.readTracks()) {
            updateTracks(track);
        }

        if (simpleAdapter != null) simpleAdapter.notifyDataSetChanged();
    }

    protected void updateTracks(Database.Track track) {
        HashMap<String, String> temp = new HashMap<>();
        if (track.getTitle().equals("")) {
            temp.put("title", uriToFilename(track.getUri()));
        } else {
            temp.put("title", track.getTitle());
        }
        temp.put("artist", track.getArtist());
        temp.put("length", track.getLengthRepr());
        tracks.add(temp);
    }

    protected String uriToFilename(Uri uri) {
        String[] segments = uri.getPath().split("/");
        return segments[segments.length-1];
    }

    public interface OnFragmentInteractionListener {}
}