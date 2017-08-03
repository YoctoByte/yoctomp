package yoctobyte.yoctomp;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class PlaylistFragment extends Fragment {
    protected ArrayList<HashMap<String, String>> tracks = new ArrayList<>();
    private ListView listview;
    private SimpleAdapter simpleAdapter;
    private OnFragmentInteractionListener listener;
    private String playlistName;


    public PlaylistFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        listview = view.findViewById(R.id.playlistView);
        simpleAdapter = new SimpleAdapter(getActivity(), tracks, R.layout.playlist_track,
                new String[] {"title", "artist", "length"}, new int[] {R.id.trackTitle, R.id.trackArtist, R.id.trackLength});

        listview.setAdapter(simpleAdapter);
        populatePlaylist();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnHeadlineSelectedListener");
        }
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
            tracks.add(populateMap(track));
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

    protected HashMap<String, String> populateMap(Database.Track track) {
        HashMap<String, String> result = new HashMap<>();
        if (track.getTitle().equals("")) {
            result.put("title", uriToFilename(track.getUri()));
        } else {
            result.put("title", track.getTitle());
        }
        result.put("artist", track.getArtist());
        result.put("length", track.getLengthRepr());
        return result;
    }

    protected String uriToFilename(Uri uri) {
        String[] segments = uri.getPath().split("/");
        return segments[segments.length-1];
    }

    public interface OnFragmentInteractionListener {}
}