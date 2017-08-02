package yoctobyte.yoctomp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;


public class LocalMusicFragment extends Fragment {
    static final int CHOOSE_DIRECTORY_REQUEST = 42;

    static ArrayList<HashMap<String, String>> tracks = new ArrayList<>();
    ListView listview;
    SimpleAdapter simpleAdapter;


    public LocalMusicFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);

        listview = view.findViewById(R.id.localMusicListview);
        Log.d("LocalMusicFragment", String.valueOf(listview));
        populatePlaylist();
        simpleAdapter = new SimpleAdapter(getActivity(), tracks, R.layout.listview_track,
                new String[] {"title", "artist", "length"}, new int[] {R.id.trackTitle, R.id.trackArtist, R.id.trackLength});
        listview.setAdapter(simpleAdapter);

        return view;
    }

    private void populatePlaylist() {
        TracksDatabase db = new TracksDatabase(getActivity());
        TracksDatabase.TrackTable localMusicTable = db.getTable(TracksDatabase.getTableNameLocalMusic());

        tracks = new ArrayList<>();
        HashMap<String, String> temp = new HashMap<>();
        temp.put("title", "test title");
        temp.put("artist", "test artist");
        temp.put("length", "1:23");
        tracks.add(temp);

        for (TracksDatabase.Track track: localMusicTable.readTracks()) {
            Log.d("", "track read");
            temp = new HashMap<>();
            if (track.getTitle().equals("")) {
                temp.put("title", track.getUri().getLastPathSegment());
            } else {
                temp.put("title", track.getTitle());
            }
            temp.put("artist", track.getArtist());
            temp.put("length", showLength(track.getLength()));
            tracks.add(temp);
        }
    }

    private String showLength(long milliSeconds) {
        long seconds = (milliSeconds+999)/1000;
        long minutes = seconds/60;
        seconds -= 60 * minutes;
        long hours = minutes/60;
        minutes -= 60 * hours;
        String result = "";

        if (hours != 0){
            result += hours + ":";
            if (minutes < 10) {
                result += "0";
            }
        }
        result += minutes + ":";
        if (seconds < 10) {
            result += "0";
        }
        result += seconds;
        return result;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the main; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.local_music, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.localMusic_addSource) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, CHOOSE_DIRECTORY_REQUEST);
            return true;
        } else if (id == R.id.localMusic_deleteDatabase) {
            getActivity().deleteDatabase("db_yoctomp");
            return true;
        } else if (id == R.id.localMusic_manageSources) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == CHOOSE_DIRECTORY_REQUEST) {
            if (resultData == null) {
                return;
            }
            Uri treeUri = resultData.getData();
            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri);

            TracksDatabase db = new TracksDatabase(getActivity());
            TracksDatabase.TrackTable localMusicTable = db.getTable(TracksDatabase.getTableNameLocalMusic());

            for (DocumentFile file: pickedDir.listFiles()) {
                Log.d("onActivityResult", "uri: " + file.getUri() + ", type: " + file.getType() + ", size: " + file.length());
                TracksDatabase.Track track = localMusicTable.newTrack(file.getUri());
                track.findMetadata(getActivity());
            }
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}