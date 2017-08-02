package yoctobyte.yoctomp;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;
import android.view.View;


public class Track {
    private String artist, album, title;
    private int length;
    private long id;
    private Uri uri;

    public Track(Uri uri) {
        this.uri = uri;
    }

    public void findMetadata(Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Log.d("findMetadata", uri.getPath());
        retriever.setDataSource(context, uri);

        title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        //length = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        //retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        //retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        //retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
    }

    public long getId() {return id;}
    public String getArtist() {return artist;}
    public String getAlbum() {return album;}
    public String getTitle() {return title;}
    public Uri getUri() {return uri;}
    public int getLength() {return length;}

    public void setId(long id) {this.id = id;}
    public void setArtist(String artist) {this.artist = artist;}
    public void setAlbum(String album) {this.album = album;}
    public void setTitle(String title) {this.title = title;}
    public void setUri(Uri uri) {this.uri = uri;}
    public void setLength(int length) {this.length = length;}
}