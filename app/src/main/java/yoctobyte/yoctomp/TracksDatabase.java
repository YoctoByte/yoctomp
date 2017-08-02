package yoctobyte.yoctomp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;


public class TracksDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME = "db_yoctomp";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME_ALL_TRACKS = "all_tracks";           // This table contains all tracks and their metadata.
    private static final String TABLE_NAME_TABLE_NAMES = "table_names";         // This table contains all other table names except "all_tracks".
    private static final String TABLE_NAME_LOCAL_MUSIC = "tracks_local";        // This table only contains references to track ids of the "all_tracks" table.
    private static final String TABLE_NAME_EXTERNAL_MUSIC = "tracks_external";  // This table contains references of tracks that are not stored locally. e.g. youtube songs. The actual metadata is still stored in the "all_tracks" table.
    private static final String TABLE_NAME_SOURCES = "sources";                 // This table contains the local sources for tracks, added by the user.
    private static final String TABLE_PREFIX_PLAYLIST = "playlist_";

    private static final String[] PRIMARY_COLUMN_NAMES = {"id", "uri", "title", "album", "artist", "length"};
    private static final String SQL_CREATE_PRIMARY_TABLE = "CREATE TABLE IF NOT EXISTS all_tracks(id INTEGER PRIMARY KEY AUTOINCREMENT, uri VARCHAR, title VARCHAR, album VARCHAR, artist VARCHAR, length INTEGER, referenced_by VARCHAR);";
    private static final String SQL_CREATE_SECONDARY_TABLE = "CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT, db_id INTEGER, FOREIGN KEY(db_id) REFERENCES all_tracks(id));";
    private static final String SQL_CREATE_TABLE_NAMES_TABLE = "CREATE TABLE IF NOT EXISTS table_names(id INTEGER PRIMARY KEY AUTOINCREMENT, table_name VARCHAR);";


    public static String getTableNameLocalMusic() {return TABLE_NAME_LOCAL_MUSIC;}
    public static String getTableNameExternalMusic() {return TABLE_NAME_EXTERNAL_MUSIC;}
    public static String getTableNameSources() {return TABLE_NAME_SOURCES;}


    public TracksDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.d("TracksDatabase", "Constructor called");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d("TracksDatabase", "onCreate called");

        sqLiteDatabase.execSQL(SQL_CREATE_PRIMARY_TABLE);
        sqLiteDatabase.execSQL(String.format(SQL_CREATE_SECONDARY_TABLE, TABLE_NAME_LOCAL_MUSIC));
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_NAMES_TABLE);

        ContentValues contentValues = new ContentValues();
        contentValues.put("table_name", TABLE_NAME_LOCAL_MUSIC);
        sqLiteDatabase.insert(TABLE_NAME_TABLE_NAMES, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sqlDropLocalMusic = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME_LOCAL_MUSIC);
        String sqlDropAllTracks = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME_ALL_TRACKS);
        String sqlDropTableNames = String.format("DROP TABLE IF EXISTS %s;", TABLE_NAME_TABLE_NAMES);
        sqLiteDatabase.execSQL(sqlDropLocalMusic);
        sqLiteDatabase.execSQL(sqlDropAllTracks);
        sqLiteDatabase.execSQL(sqlDropTableNames);

        onCreate(sqLiteDatabase);
    }

    public TrackTable newPlaylist(String playlistName) {
        String tableName = TABLE_PREFIX_PLAYLIST + playlistName;
        return newTable(tableName);
    }

    public TrackTable getPlaylist(String playlistName) {
        String tableName = TABLE_PREFIX_PLAYLIST + playlistName;
        return getTable(tableName);
    }

    public boolean deletePlaylist(String playlistName) {
        String tableName = TABLE_PREFIX_PLAYLIST + playlistName;
        return deleteTable(tableName);
    }

    private ArrayList<String> getTableNames() {
        ArrayList<String> tableNames = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"table_name"};
        Cursor cursor = db.query(TABLE_NAME_TABLE_NAMES, columns, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                tableNames.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tableNames;
    }

    public TrackTable newTable(String tableName) {
        ArrayList<String> tableNames = getTableNames();
        if (!tableNames.contains(tableName)) {
            SQLiteDatabase db = getWritableDatabase();
            String sqlCreatePlaylist = String.format(SQL_CREATE_SECONDARY_TABLE, tableName);
            db.execSQL(sqlCreatePlaylist);

            ContentValues contentValues = new ContentValues();
            contentValues.put("table_name", tableName);
            db.insert(TABLE_NAME_TABLE_NAMES, null, contentValues);
            db.close();
        }
        return new TrackTable(tableName);
    }

    public TrackTable getTable(String tableName) {
        ArrayList<String> tableNames = getTableNames();
        if (!tableNames.contains(tableName)) {
            Log.d("getTable", tableName + " does not exist");
            return null;
        }
        return new TrackTable(tableName);
    }

    //TODO
    public boolean deleteTable(String tableName) {
        return true;
    }

    private long getId(Track track) {
        long id;

        String uri = track.getUri().toString().replace("'", "''");
        String columns = "id,uri";
        String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE uri='" + uri + "';";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        } else {
            id = -1;
        }
        cursor.close();
        db.close();
        return id;
    }

    public long addTrack(Track track) {
        long id = getId(track);
        if (id != -1) {
            return id;
        }
        Log.d("addTrack", "Added " + track.getUri().toString());
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", track.getTitle().replace("'", "''"));
        contentValues.put("album", track.getAlbum().replace("'", "''"));
        contentValues.put("artist", track.getArtist().replace("'", "''"));
        contentValues.put("uri", track.getUri().toString().replace("'", "''"));
        contentValues.put("length", track.getLength());

        id = db.insert(TABLE_NAME_ALL_TRACKS, null, contentValues);
        db.close();
        track.setId(id);
        return id;
    }

    private Track readTrack(long dbId) {
        String columns = TextUtils.join(",", PRIMARY_COLUMN_NAMES);
        String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE id=" + dbId + ";";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        Track track = new Track(Uri.parse(cursor.getString(1)));
        track.setId(cursor.getLong(0));
        track.setTitle(cursor.getString(2));
        track.setAlbum(cursor.getString(3));
        track.setArtist(cursor.getString(4));
        track.setLength(cursor.getInt(5));
        cursor.close();
        db.close();
        return track;
    }

    private ArrayList<Track> readTracks(ArrayList<Long> dbIds) {
        ArrayList<Track> tracks = new ArrayList<>();

        String stringDbIds = TextUtils.join(",", dbIds);
        String columns = TextUtils.join(",", PRIMARY_COLUMN_NAMES);
        String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE id IN (" + stringDbIds + ");";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

        if (cursor.moveToFirst()) {
            do {
                Track track = new Track(Uri.parse(cursor.getString(1)));
                track.setId(cursor.getLong(0));
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

    public long getSize() {
        SQLiteDatabase db = getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME_ALL_TRACKS);
        db.close();
        return count;
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

        public Track readTrack(long id) {
            String columns = "id,db_id";
            String sqlQuery = "SELECT " + columns + " FROM " + tableName + " WHERE id=" + id + ";";
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

            if (cursor == null) {
                return null;
            }
            cursor.moveToFirst();
            long dbId = cursor.getLong(1);
            cursor.close();
            return TracksDatabase.this.readTrack(dbId);
        }

        public ArrayList<Track> readTracks() {
            String columns = "id,db_id";
            String sqlQuery = "SELECT " + columns + " FROM " + tableName + ";";
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

            ArrayList<Long> dbIds = new ArrayList<>();
            if (cursor.moveToFirst()) {
                do {
                    long dbId = cursor.getLong(1);
                    dbIds.add(dbId);
                } while (cursor.moveToNext());
            }
            cursor.close();
            return TracksDatabase.this.readTracks(dbIds);
        }

        private long getId(long dbId) {
            String columns = "id,db_id";
            String sqlQuery = "SELECT " + columns + " FROM " + tableName + " WHERE db_id=" + dbId + ";";
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

            long id;
            if (cursor.moveToFirst()) {
                id = cursor.getLong(0);
            } else {
                id = -1;
            }
            cursor.close();
            db.close();
            return id;
        }

        public Track newTrack(Uri uri) {
            Track track = new Track(uri);
            addTrack(track);
            return track;
        }

        public long addTrack(Track track) {
            long db_id = TracksDatabase.this.addTrack(track);
            if (db_id == -1) {
                return -1;
            }

            // Check if the id returned by the main track table already is in the current table
            long id = getId(db_id);
            if (id != -1) {
                return id;
            }

            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("db_id", db_id);
            id = db.insert(tableName, null, contentValues);
            db.close();
            return id;
        }
    }

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

            updateDatabase();
        }

        private void updateDatabase() {
            Log.d("updateDatabse", title);

            ContentValues contentValues = new ContentValues();
            contentValues.put("title", title);
            contentValues.put("album", album);
            contentValues.put("artist", artist);
            contentValues.put("length", length);

            SQLiteDatabase db = getWritableDatabase();
            db.update(TABLE_NAME_ALL_TRACKS, contentValues, "id="+id, null);
        }

        public long getId() {return id;}
        public String getArtist() {if (artist != null) return artist; else return "";}
        public String getAlbum() {if (album != null) return album; else return "";}
        public String getTitle() {if (title != null) return title; else return "";}
        public Uri getUri() {return uri;}
        public int getLength() {return length;}

        public void setId(long id) {this.id = id;}
        public void setArtist(String artist) {this.artist = artist;}
        public void setAlbum(String album) {this.album = album;}
        public void setTitle(String title) {this.title = title;}
        public void setUri(Uri uri) {this.uri = uri;}
        public void setLength(int length) {this.length = length;}
    }
}
