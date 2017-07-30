package yoctobyte.yoctomp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TracksDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "db_yoctomp";
    private static final int DB_VERSION = 1;

    private static final String TABLE_ALL_TRACKS = "all_tracks";
    private static final String TABLE_NAME_LOCAL_MUSIC = "tracks_local";
    private static final String TABLE_PREFIX_PLAYLIST = "playlist_";

    private static final String[] PRIMARY_COLUMN_NAMES = {"id", "location", "title", "album", "artist", "length"};
    private static final String SQL_CREATE_PRIMARY_TABLE = "CREATE TABLE IF NOT EXISTS all_tracks(id INTEGER PRIMARY KEY AUTOINCREMENT, location VARCHAR, title VARCHAR, album VARCHAR, artist VARCHAR, length INTEGER, referenced_by VARCHAR);";
    private static final String SQL_CREATE_SECONDARY_TABLE = "CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT, db_id INTEGER, FOREIGN KEY(db_id) REFERENCES all_tracks(id));";

    private Map<String, TrackTable> tables = new HashMap<>();


    public TracksDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_PRIMARY_TABLE);
        String sqlCreateLocalMusic = String.format(SQL_CREATE_SECONDARY_TABLE, TABLE_NAME_LOCAL_MUSIC);
        sqLiteDatabase.execSQL(sqlCreateLocalMusic);
    }

    public TrackTable newPlaylist(String playlistName) {
        String tableName = TABLE_PREFIX_PLAYLIST + playlistName;
        return newTable(tableName);
    }

    public TrackTable getPlaylist(String playlistName) {
        String tableName = TABLE_PREFIX_PLAYLIST + playlistName;
        return getTable(tableName);
    }

    public TrackTable newTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        if (tables.containsKey(tableName)) {
            db.close();
            return tables.get(tableName);
        } else {
            String sqlCreatePlaylist = String.format(SQL_CREATE_SECONDARY_TABLE, tableName);
            db.execSQL(sqlCreatePlaylist);
            db.close();
            TrackTable trackTable = new TrackTable(tableName);
            tables.put(tableName, trackTable);
            return trackTable;
        }
    }

    public TrackTable getTable(String tableName) {
        SQLiteDatabase db = getWritableDatabase();
        if (tables.containsKey(tableName)) {
            db.close();
            return tables.get(tableName);
        } else {
            db.close();
            return null;
        }
    }

    public long addTrack(Track track) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("title", track.getTitle());
        contentValues.put("album", track.getAlbum());
        contentValues.put("artist", track.getArtist());
        contentValues.put("location", track.getLocation());
        contentValues.put("length", track.getLength());

        long id = db.insert(TABLE_ALL_TRACKS, null, contentValues);
        db.close();
        return id;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sqlDropLocalMusic = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME_LOCAL_MUSIC);
        String sqlDropAllTracks = String.format("DROP TABLE IF EXISTS %s;", TABLE_ALL_TRACKS);
        sqLiteDatabase.execSQL(sqlDropLocalMusic);
        sqLiteDatabase.execSQL(sqlDropAllTracks);

        onCreate(sqLiteDatabase);
    }

    public class TrackTable {
        private String tableName;

        public TrackTable(String name) {
            tableName = name;
        }

        public long getSize() {
            SQLiteDatabase db = getReadableDatabase();
            long count = DatabaseUtils.queryNumEntries(db, tableName);
            db.close();
            return count;
        }

        public Track readTrack(int id) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, PRIMARY_COLUMN_NAMES, "id=?", new String[]{String.valueOf(id)}, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            Track track = new Track(cursor.getString(1));
            track.setId(cursor.getInt(0));
            track.setTitle(cursor.getString(2));
            track.setAlbum(cursor.getString(3));
            track.setArtist(cursor.getString(4));
            track.setLength(cursor.getInt(5));
            cursor.close();
            db.close();
            return track;
        }

        public ArrayList<Track> listTracks() {
            ArrayList<Track> tracks = new ArrayList<>();

            SQLiteDatabase db = getReadableDatabase();
            String query = String.format("SELECT * FROM %s;", tableName);
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Track track = new Track(cursor.getString(1));
                    track.setId(cursor.getInt(0));
                    track.setTitle(cursor.getString(2));
                    track.setAlbum(cursor.getString(3));
                    track.setArtist(cursor.getString(4));
                    track.setLength(cursor.getInt(5));

                    tracks.add(track);
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return tracks;
        }

        public long addTrack(Track track) {
            long db_id = TracksDatabase.this.addTrack(track);
            if (db_id == -1) {
                return -1;
            }
            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("db_id", db_id);
            long id = db.insert(tableName, null, contentValues);
            db.close();
            return id;
        }
    }

    public class Track {
        private String artist, album, title, location;
        private int length, id;

        public Track(String location) {
            this.location = location; }

        public int getId() {return id;}
        public String getArtist() {return artist;}
        public String getAlbum() {return album;}
        public String getTitle() {return title;}
        public String getLocation() {return location;}
        public int getLength() {return length;}

        public void setId(int id) {this.id = id;}
        public void setArtist(String artist) {this.artist = artist;}
        public void setAlbum(String album) {this.album = album;}
        public void setTitle(String title) {this.title = title;}
        public void setLocation(String location) {this.location = location;}
        public void setLength(int length) {this.length = length;}
    }
}
