package vuze.twofactorauthentication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Daniel Vu on 9/22/2015.
 */
public class MySQLiteHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "twofactor.db";
    private static final int DATABASE_VERSION = 1;


    public static final String KEY_TABLE= "key_table";
    public static final String COLUMN_PASSWORD = "password";

        public static final String[] allColumns = {COLUMN_PASSWORD};

    private static final String KEY_TABLE_CREATION = "create table "
            + KEY_TABLE + "("+ COLUMN_PASSWORD
            + " text primary key not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(KEY_TABLE_CREATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + KEY_TABLE);
        onCreate(db);
    }
}