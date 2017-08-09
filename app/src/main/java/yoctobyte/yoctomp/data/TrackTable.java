package yoctobyte.yoctomp.data;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;

public class TrackTable extends Database {
    private String tableName;

    public TrackTable(Context context, String name) {
        super(context);
        tableName = name;

        if (Arrays.asList(NON_PERMITTED_TABLE_NAMES).contains(name)) {
            throw new java.lang.RuntimeException("TrackTable name can't be " + name);
        }

        // If table does not exist in database, create it:
        SQLiteDatabase sqlDb = getWritableDatabase();
        String sqlCreateTable = String.format(SQL_CREATE_TABLE_REFERENCE, tableName);
        sqlDb.execSQL(sqlCreateTable);
        sqlDb.close();
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
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{});

        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();
        long dbId = cursor.getLong(1);
        cursor.close();
        return getTrack(dbId);
    }

    public ArrayList<Track> readTracks() {
        String columns = "id,db_id";
        String sqlQuery = "SELECT " + columns + " FROM " + tableName + ";";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{});

        ArrayList<Long> dbIds = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                long dbId = cursor.getLong(1);
                dbIds.add(dbId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return getTracks(dbIds);
    }

    private long getId(long dbId) {
        String columns = "id,db_id";
        String sqlQuery = "SELECT " + columns + " FROM " + tableName + " WHERE db_id=" + dbId + ";";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sqlQuery, new String[]{});

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
        Track track = new Track(this, uri);
        if (addTrack(track) == -1) {
            return null;
        }
        return track;
    }

    public long addTrack(Track track) {
        long db_id = super.addTrack(track);
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