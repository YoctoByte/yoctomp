package yoctobyte.yoctomp;

import android.net.Uri;


public class Track {
    private String artist, album, title;
    private int length;
    private long id;
    private Uri uri;

    public Track(Uri uri) {
        this.uri = uri;
    }

    //TODO
    public void findMetadata() {
        //https://stackoverflow.com/questions/11327954/how-to-extract-metadata-from-mp3
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