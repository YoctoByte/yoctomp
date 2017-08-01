package yoctobyte.yoctomp;


import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class playlistListView extends ListFragment {

    static final ArrayList<HashMap<String,String>> playlist = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_local_music, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), playlist, R.layout.listview_track,
                new String[] {"title", "artist", "length"}, new int[] {R.id.trackTitle, R.id.trackArtist, R.id.trackLength});
        populatePlaylist();
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    private void populatePlaylist() {
        TracksDatabase db = new TracksDatabase(getActivity());
        TracksDatabase.TrackTable localMusicTable = db.getTable(TracksDatabase.getTableNameLocalMusic());

        HashMap<String, String> temp = new HashMap<>();
        temp.put("title", "test title");
        temp.put("artist", "test artist");
        temp.put("length", "1:23");
        playlist.add(temp);
        for (Track track: localMusicTable.readTracks()) {
            temp = new HashMap<>();
            temp.put("title", track.getTitle());
            temp.put("artist", track.getArtist());
            temp.put("length", String.valueOf(track.getLength()));
            playlist.add(temp);
        }
    }
}
