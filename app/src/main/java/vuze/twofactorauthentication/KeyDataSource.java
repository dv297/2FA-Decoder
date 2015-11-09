package vuze.twofactorauthentication;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import vuze.twofactorauthentication.MySQLiteHelper;


/**
 * Created by Daniel Vu on 6/18/2015.
 */
public class KeyDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    public KeyDataSource(Context context){
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void insertKey(String key){
        ContentValues values = new ContentValues();
                values.put(MySQLiteHelper.COLUMN_PASSWORD, key);
        // We only want one key at a time
        database.execSQL("DELETE FROM " + MySQLiteHelper.KEY_TABLE + " WHERE 1");
        long insertId = database.insert(MySQLiteHelper.KEY_TABLE, null, values);
    }

    public String getKey(){
        Cursor resultSet = database.rawQuery("Select * from " + MySQLiteHelper.KEY_TABLE, null);
        if(resultSet != null){
            resultSet.moveToFirst();
            return resultSet.getString(0);
        }
        return null;
    }

    public boolean isKeyPresent(){
        Cursor resultSet = database.rawQuery("Select * from " + MySQLiteHelper.KEY_TABLE, null);
        return resultSet.getCount() != 0;
    }

    public void clearTable(){
        database.execSQL("DELETE FROM " + MySQLiteHelper.KEY_TABLE+ " WHERE 1");
    }
}
