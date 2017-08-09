package yoctobyte.yoctomp.data;


import android.content.Context;

public class PlaylistTable extends TrackTable {
    PlaylistTable(Context context, String name) {
        super(context, TABLE_PREFIX_PLAYLIST + name);
    }
}
