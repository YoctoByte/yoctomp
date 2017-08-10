package yoctobyte.yoctomp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;


public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "db_yoctomp";
    private static final int DB_VERSION = 1;

    private Context context;

    protected static final String TABLE_NAME_ALL_TRACKS = "all_tracks";           // This table contains all tracks and their metadata.
    protected static final String TABLE_NAME_LOCAL_TRACKS = "tracks_local";       // This is one of the tables that only contain references to track ids of the "all_tracks" table.
    //protected static final String TABLE_NAME_EXTERNAL_TRACKS = "tracks_external"; // This table contains references of tracks that are not stored locally. e.g. youtube songs. The actual metadata is still stored in the "all_tracks" table.
    protected static final String TABLE_NAME_LOCAL_LIBRARIES = "local_libraries"; // This table contains the locations of the libraries added by the user.
    protected static final String TABLE_PREFIX_PLAYLIST = "playlist_";
    protected static final String[] NON_PERMITTED_TABLE_NAMES = {TABLE_NAME_ALL_TRACKS, TABLE_NAME_LOCAL_LIBRARIES};

    protected static final String SQL_CREATE_TABLE_PRIMARY = "CREATE TABLE IF NOT EXISTS all_tracks(id INTEGER PRIMARY KEY AUTOINCREMENT, uri VARCHAR, metadata VARCHAR);";
    protected static final String SQL_CREATE_TABLE_REFERENCE = "CREATE TABLE IF NOT EXISTS %s(id INTEGER PRIMARY KEY AUTOINCREMENT, db_id INTEGER, FOREIGN KEY(db_id) REFERENCES all_tracks(id));";
    protected static final String SQL_CREATE_TABLE_LOCAL_LIBRARIES = "CREATE TABLE IF NOT EXISTS local_libraries(id INTEGER PRIMARY KEY AUTOINCREMENT, uri VARCHAR);";

    private static final String[] COLUMN_NAMES_PRIMARY = {"id", "uri", "metadata"};
    private static final String[] COLUMN_TYPES_PRIMARY = {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR", "VARCHAR"};
    //private static final String[] COLUMN_NAMES_REFERENCE = {"id", "db_id"};
    //private static final String[] COLUMN_TYPES_REFERENCE = {"INTEGER PRIMARY KEY AUTOINCREMENT", "INTEGER"};
    private static final String[] COLUMN_NAMES_LOCAL_LIBRARY = {"id", "uri"};
    private static final String[] COLUMN_TYPES_LOCAL_LIBRARY = {"INTEGER PRIMARY KEY AUTOINCREMENT", "VARCHAR"};

    public TrackTable getTableLocalTracks() {return new TrackTable(context, TABLE_NAME_LOCAL_TRACKS);}
    //public TrackTable getTableExternaltracks() {return new TrackTable(context, TABLE_NAME_EXTERNAL_TRACKS);}
    public TrackTable getTablePlaylist(String playlistName) {return new TrackTable(context, TABLE_PREFIX_PLAYLIST + playlistName);}

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
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

    private long getId(Track track) {
        //Returns -1 if id not found so this method can be used to check if a track already exists in the database.

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

    protected long addTrack(Track track) {
        long id = getId(track);
        if (id != -1) {
            return id;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("uri", track.getUri().toString());
        String json = track.toJSON();
        contentValues.put("metadata", json);

        id = db.insert(TABLE_NAME_ALL_TRACKS, null, contentValues);
        db.close();
        track.setId(id);
        return id;
    }

    protected Track getTrack(long dbId) {
        String columns = TextUtils.join(",", COLUMN_NAMES_PRIMARY);
        String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE id=" + dbId + ";";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        Map metadata = new Gson().fromJson(cursor.getString(2), Map.class);
        Track track = Track.fromMap(this, Uri.parse(cursor.getString(1)), metadata);

        cursor.close();
        db.close();
        return track;
    }

    protected ArrayList<Track> getTracks(ArrayList<Long> dbIds) {
        ArrayList<Track> tracks = new ArrayList<>();

        String stringDbIds = TextUtils.join(",", dbIds);
        String columns = TextUtils.join(",", COLUMN_NAMES_PRIMARY);
        String sqlQuery = "SELECT " + columns + " FROM " + TABLE_NAME_ALL_TRACKS + " WHERE id IN (" + stringDbIds + ");";

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[] {});

        if (cursor.moveToFirst()) {
            do {
                Map metadata = new Gson().fromJson(cursor.getString(2), Map.class);
                Track track = Track.fromMap(this, Uri.parse(cursor.getString(1)), metadata);
                tracks.add(track);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tracks;
    }

    public void updateTrack(Track track) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uri", track.getUri().toString());
        contentValues.put("metadata", track.toJSON());

        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME_ALL_TRACKS, contentValues, "id="+track.getId(), null);
        db.close();
    }

}
