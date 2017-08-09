package yoctobyte.yoctomp.data;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Track {
    private String artist, album, title, genre, bitrate;
    private long id, length, year;
    private Uri uri;
    private Database database;

    public Track(Database database, Uri uri) {
        this.database = database;
        this.uri = uri;
    }

    public void findMetadata(Context context) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        Log.d("findMetadata", uri.getPath());
        retriever.setDataSource(context, uri);

        title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        String yearString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        try {if (yearString != null) year = Long.parseLong(yearString);} catch (NumberFormatException e) {year = 0;}
        String lengthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        try {if (lengthString != null) length = Long.parseLong(lengthString);} catch (NumberFormatException e) {length = 0;}
        bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

        updateDatabase();
    }

    private void updateDatabase() {
        database.updateTrack(this);
    }

    public static Track fromMap(Database database, Uri uri, Map metadata) {
        Track track = new Track(database, uri);
        track.title = (String) metadata.get("title");
        track.album = (String) metadata.get("album");
        track.artist = (String) metadata.get("artist");
        track.genre = (String) metadata.get("genre");
        try {track.year = Long.parseLong((String) metadata.get("year"));} catch (NumberFormatException e) {track.year = 0;}
        try {track.length = Long.parseLong((String) metadata.get("length"));} catch (NumberFormatException e) {track.length = 0;}
        track.bitrate = (String) metadata.get("bitrate");
        track.updateDatabase();
        return track;
    }

    public String toJSON() {
        return new JSONObject(toMap()).toString();
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        if (title != null) map.put("title", title);
        if (album != null) map.put("album", album);
        if (artist != null) map.put("artist", artist);
        if (genre != null) map.put("genre", genre);
        if (year != 0) map.put("year", String.valueOf(year));
        if (length != 0) map.put("length", String.valueOf(length));
        if (bitrate != null) map.put("bitrate", bitrate);

        return map;
    }

    public long getId() {return id;}
    public String getArtist() {if (artist != null) return artist; else return "";}
    //public String getAlbum() {if (album != null) return album; else return "";}
    public String getTitle() {if (title != null) return title; else return "";}
    public Uri getUri() {return uri;}
    //public long getLength() {return length;}
    public String getLengthRepr() {
        long seconds = (length+999)/1000;
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

    public void setId(long id) {this.id = id;}
    public void setTitle(String title) {this.title = title; updateDatabase();}
    public void setAlbum(String album) {this.album = album; updateDatabase();}
    public void setArtist(String artist) {this.artist = artist; updateDatabase();}
    public void setGenre(String genre) {this.genre = genre; updateDatabase();}
    public void setYear(long year) {this.year = year; updateDatabase();}
    public void setLength(long length) {this.length = length; updateDatabase();}
    public void setBitrate(String bitrate) {this.bitrate = bitrate; updateDatabase();}
}