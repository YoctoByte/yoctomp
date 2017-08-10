package yoctobyte.yoctomp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.data.Database;
import yoctobyte.yoctomp.data.Track;
import yoctobyte.yoctomp.data.TrackTable;


public class LocalMusicFragment extends PlaylistFragment {
    private static final int CHOOSE_DIRECTORY_REQUEST = 42;
    SimpleAdapter simpleAdapter;


    public LocalMusicFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (tracks.size() == 0) {
            populatePlaylist();
        }

        simpleAdapter = new SimpleAdapter(getActivity(), tracks, R.layout.item_playlist,
                new String[] {"title", "artist", "length"}, new int[] {R.id.trackTitle, R.id.trackArtist, R.id.trackLength});
        setListAdapter(simpleAdapter);
    }

    private void populatePlaylist() {
        Database db = new Database(getActivity());
        TrackTable playlistTable = db.getTableLocalTracks();

        tracks = new ArrayList<>();
        for (Track track: playlistTable.readTracks()) {
            updateTracks(track);
        }
        if (simpleAdapter != null) simpleAdapter.notifyDataSetChanged();
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
        } else if (id == R.id.local_music_delete_database) {
            getActivity().deleteDatabase("db_yoctomp");
            tracks.clear();
            simpleAdapter.notifyDataSetChanged();
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
            new readLibrary().execute(treeUri);
        }
    }

    private class readLibrary extends AsyncTask<Uri, Void, String> {
        private TrackTable localTracksTable;

        @Override
        protected String doInBackground(Uri... treeUri) {
            Database db = new Database(getActivity());
            localTracksTable = db.getTableLocalTracks();

            DocumentFile pickedDir = DocumentFile.fromTreeUri(getActivity(), treeUri[0]);
            scanDirectory(pickedDir);

            return null;
        }

        private void scanDirectory(DocumentFile directory) {
            for (DocumentFile file: directory.listFiles()) {
                if (file.isDirectory()) {
                    scanDirectory(file);
                } else if (file.isFile()) {
                    Track track = localTracksTable.newTrack(file.getUri());
                    if (track == null) {
                        continue;
                    }
                    track.findMetadata(getActivity());
                    updateTracks(track);
                    publishProgress();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            ListView listView = getListView();
            Boolean isAtBottom;

            isAtBottom = (listView.getLastVisiblePosition() >= listView.getCount() - 2);
            simpleAdapter.notifyDataSetChanged();
            if (isAtBottom) {
                listView.smoothScrollToPosition(listView.getCount() - 1);
            }
        }
    }

    public interface OnFragmentInteractionListener {}
}