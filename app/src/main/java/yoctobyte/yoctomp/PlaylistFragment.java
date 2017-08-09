package yoctobyte.yoctomp;


import android.content.Context;
import android.support.v4.app.ListFragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

        String[] keys = {"title", "artist", "length"};
        int[] ids = {R.id.trackTitle, R.id.trackArtist, R.id.trackLength};
        simpleAdapter = new CustomAdapter(getActivity(), tracks, R.layout.item_playlist, keys, ids);
        setListAdapter(simpleAdapter);

        if (tracks.size() == 0) {
            populatePlaylist();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Log.d("onListItemClick", String.valueOf(getSelectedItemId()));
        Log.d("onListItemClick", String.valueOf(getSelectedItemPosition()));
        super.onListItemClick(listView, view, position, id);
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

    private class CustomAdapter extends SimpleAdapter {
        public CustomAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("getView", "is called");
            return super.getView(position, convertView, parent);
        }
    }

    public interface OnFragmentInteractionListener {}
}