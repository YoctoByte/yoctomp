package yoctobyte.yoctomp.adapters;


import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import yoctobyte.yoctomp.R;
import yoctobyte.yoctomp.data.Track;


public class PlaylistAdapter extends SimpleAdapter {
    private static ArrayList<HashMap<String, String>> tracks = new ArrayList<>();

    public PlaylistAdapter(Context context) {
        super(context, tracks, R.layout.item_playlist, new String[]{"title", "artist", "length"}, new int[] {R.id.trackTitle, R.id.trackArtist, R.id.trackLength});
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("getView", "is called");
        return super.getView(position, convertView, parent);
    }

    public void clear() {
        tracks.clear();
    }

    public void addTrack(Track track) {
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

    private String uriToFilename(Uri uri) {
        String[] segments = uri.getPath().split("/");
        return segments[segments.length-1];
    }
}