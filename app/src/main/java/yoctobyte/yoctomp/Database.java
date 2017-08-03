package yoctobyte.yoctomp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "db_yoctomp";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME_ALL_TRACKS = "all_tracks";           // This table contains all tracks and their metadata.
    private static final String TABLE_NAME_LOCAL_TRACKS = "tracks_local";       // This is one of the tables that only contain references to track ids of the "all_tracks" table.
    private static final String TABLE_NAME_EXTERNAL_TRACKS = "tracks_external"; // This table contains references of tracks that are not stored locally. e.g. youtube songs. The actual metadata is still stored in the "all_tracks" table.
    private static final String TABLE_NAME_LOCAL_LIBRARIES = "local_libraries"; // This table contains the locations of the libraries added by the user.
    private static final String TABLE_PREFIX_PLAYLIST = "playlist_";
    private static final String[] NON_PERMITTED_TABLE_NAMES = {TABLE_NAME_ALL_TRACKS, TABLE_NAME_LOCAL_LIBRARIES};

    private static final String SQL_CREATE_TABLE_PRIMARY = "CREATE TABLE IF NOT EXISTS all_tracks(id INTEGER PRIMARY KEY AUTOINCREMENT, uri VARCHAR, metadata VARCHAR);";
    private static final String SQL_CREATE_TABLE_REFERENCE = "CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT, db_id INTEGER, FOREIGN KEY(db_id) REFERENCES all_tracks(id));";
    private static final String SQL_CREATE_TABLE_LOCAL_LIBRARIES = "CREATE TABLE IF NOT EXISTS local_libraries(id INTEGER PRIMARY KEY AUTOINCREMENT, uri VARCHAR);";

    private static final String[] COLUMN_NAMES_PRIMARY = {"id", "uri", "metadata"};
    private static final String[] COLUMN_TYPES_PRIMARY = {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR", "VARCHAR"};
    private static final String[] COLUMN_NAMES_REFERENCE = {"id", "db_id"};
    private static final String[] COLUMN_TYPES_REFERENCE = {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER"};
    private static final String[] COLUMN_NAMES_LOCAL_LIBRARY = {"id", "uri"};
    private static final String[] COLUMN_TYPES_LOCAL_LIBRARY = {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR"};

    public TrackTable getTableLocalTracks() {return new TrackTable(TABLE_NAME_LOCAL_TRACKS);}
    public TrackTable getTableExternaltracks() {return new TrackTable(TABLE_NAME_EXTERNAL_TRACKS);}
    public TrackTable getTablePlaylist(String playlistName) {return new TrackTable(TABLE_PREFIX_PLAYLIST + playlistName);}

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_PRIMARY);
        sqLiteDatabase.execSQL(String.format(SQL_CREATE_TABLE_REFERENCE, TABLE_NAME_LOCAL_TRACKS));
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_LOCAL_LIBRARIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        onCreate(sqLiteDatabase);

        for (int i=0; i<Math.min(COLUMN_NAMES_PRIMARY.length, COLUMN_TYPES_PRIMARY.length); i++) {
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME_ALL_TRACKS + " ADD COLUMN " + COLUMN_NAMES_PRIMARY[i] + " " + COLUMN_TYPES_PRIMARY[i] + ";");
        }
        for (int i=0; i<Math.min(COLUMN_NAMES_LOCAL_LIBRARY.length, COLUMN_TYPES_LOCAL_LIBRARY.length); i++) {
            sqLiteDatabase.execSQL("ALTER TABLE " + TABLE_NAME_LOCAL_LIBRARIES + " ADD COLUMN " + COLUMN_NAMES_LOCAL_LIBRARY[i] + " " + COLUMN_TYPES_LOCAL_LIBRARY[i] + ";");
        }
    }

    private class AllTracksTable {
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

        private long addTrack(Track track) {
            long id = getId(track);
            if (id != -1) {
                return id;
            }
            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("uri", track.getUri().toString());
            String json = track.toJSON();
            Log.d("addTrack json", json);
            contentValues.put("metadata", json);

            id = db.insert(TABLE_NAME_ALL_TRACKS, null, contentValues);
            db.close();
            track.setId(id);
            return id;
        }

        @Nullable
        private Track getTrack(long dbId) {
            String columns = TextUtils.join(",", COLUMN_NAMES_PRIMARY);
            String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE id=" + dbId + ";";

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

            if (cursor == null) {
                return null;
            }
            cursor.moveToFirst();
            Track track = new Track(Uri.parse(cursor.getString(1)));
            Map metadata = new Gson().fromJson(cursor.getString(2), Map.class);
            track.setTitle((String) metadata.get("title"));
            track.setAlbum((String) metadata.get("album"));
            track.setArtist((String) metadata.get("artist"));
            track.setGenre((String) metadata.get("genre"));
            track.setYear(Long.parseLong((String) metadata.get("year")));
            track.setLength(Long.parseLong((String) metadata.get("length")));
            track.setBitrate((String) metadata.get("bitrate"));

            cursor.close();
            db.close();
            return track;
        }

        //TODO
        private void deleteTrack(long dbId) {

        }

        private ArrayList<Track> getTracks(ArrayList<Long> dbIds) {
            ArrayList<Track> tracks = new ArrayList<>();

            String stringDbIds = TextUtils.join(",", dbIds);
            String columns = TextUtils.join(",", COLUMN_NAMES_PRIMARY);
            String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE id IN (" + stringDbIds + ");";

            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

            if (cursor.moveToFirst()) {
                do {
                    Track track = new Track(Uri.parse(cursor.getString(1)));
                    Log.d("getTracks string", cursor.getString(2));
                    Map metadata = new Gson().fromJson(cursor.getString(2), Map.class);
                    Log.d("getTracks", metadata.toString());
                    track.setTitle((String) metadata.get("title"));
                    track.setAlbum((String) metadata.get("album"));
                    track.setArtist((String) metadata.get("artist"));
                    track.setGenre((String) metadata.get("genre"));
                    try {track.setYear(Long.parseLong((String) metadata.get("year")));} catch (NumberFormatException e) {track.setYear(0);}
                    try {track.setLength(Long.parseLong((String) metadata.get("length")));} catch (NumberFormatException e) {track.setLength(0);}
                    track.setBitrate((String) metadata.get("bitrate"));

                    tracks.add(track);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return tracks;
        }

        private long sizeOf() {
            SQLiteDatabase db = getReadableDatabase();
            long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME_ALL_TRACKS);
            db.close();
            return count;
        }
    }

    public class TrackTable {
        private String tableName;
        AllTracksTable allTracksTable = new AllTracksTable();

        private TrackTable(String name) {
            tableName = name;

            if (Arrays.asList(NON_PERMITTED_TABLE_NAMES).contains(name)) {
                throw new java.lang.RuntimeException("TrackTable name can't be " + name);
            }

            // If table does not exist in database, create it:
            SQLiteDatabase db = getWritableDatabase();
            String sqlCreateTable = String.format(SQL_CREATE_TABLE_REFERENCE, tableName);
            db.execSQL(sqlCreateTable);
            db.close();
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
            return allTracksTable.getTrack(dbId);
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
            return allTracksTable.getTracks(dbIds);
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
            if (addTrack(track) == -1) {
                return null;
            }
            return track;
        }

        public long addTrack(Track track) {
            long db_id = allTracksTable.addTrack(track);
            if (db_id == -1) {
                return -1;
            }

            // Check if the id returned by the main track table already is in the current table
            long id = getId(db_id);
            if (id != -1) {
                return -1;
            }

            SQLiteDatabase db = getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("db_id", db_id);
            id = db.insert(tableName, null, contentValues);
            db.close();
            return id;
        }
    }

    public class LocalLibraries {
        private final String tableName = TABLE_NAME_LOCAL_LIBRARIES;

        public LocalLibraries() {}

    }

    public class Track {
        private String artist, album, title, genre, bitrate;
        private long id, length, year;
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
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            String yearString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
            try {if (yearString != null) year = Long.parseLong(yearString);} catch (NumberFormatException e) {year = 0;}
            String lengthString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            try {if (lengthString != null) length = Long.parseLong(lengthString);} catch (NumberFormatException e) {length = 0;}
            bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

            updateDatabase();
        }

        private void updateDatabase() {
            ContentValues contentValues = new ContentValues();
            contentValues.put("uri", uri.toString());
            contentValues.put("metadata", toJSON());

            SQLiteDatabase db = getWritableDatabase();
            db.update(TABLE_NAME_ALL_TRACKS, contentValues, "id="+id, null);
            db.close();
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
        public String getAlbum() {if (album != null) return album; else return "";}
        public String getTitle() {if (title != null) return title; else return "";}
        public Uri getUri() {return uri;}
        public long getLength() {return length;}

        public void setId(long id) {this.id = id;}
        public void setTitle(String title) {this.title = title;}
        public void setAlbum(String album) {this.album = album;}
        public void setArtist(String artist) {this.artist = artist;}
        public void setGenre(String genre) {this.genre = genre;}
        public void setYear(long year) {this.year = year;}
        public void setLength(long length) {this.length = length;}
        public void setBitrate(String bitrate) {this.bitrate = bitrate;}
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
    }
}
