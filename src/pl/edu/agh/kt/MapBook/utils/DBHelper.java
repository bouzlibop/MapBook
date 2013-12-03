package pl.edu.agh.kt.MapBook.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: adba
 * Date: 03.12.13
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mapBookDB";

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_POSITION_TABLE = "CREATE TABLE position ( "+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "screenX NUM NOT NULL, "+
                "screenY NUM NOT NULL, "+
                "latitude TEXT NOT NULL, "+
                "longitude TEXT NOT NULL );";
        String CREATE_MAPA_TABLE = "CREATE TABLE mapa ( "+
                "id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "title TEXT NOT NULL, "+
                "imagePath TEXT NOT NULL, "+
                "scale TEXT NOT NULL, "+
                "position1 INTEGER, "+
                "position2 INTEGER, "+
                "FOREIGN KEY (position1) REFERENCES position(id), "+
                "FOREIGN KEY (position2) REFERENCES position(id));";
        Log.d("DB", "Creating tables");
        db.execSQL(CREATE_POSITION_TABLE);
        db.execSQL(CREATE_MAPA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS mapa");
        db.execSQL("DROP TABLE IF EXISTS position");
        Log.d("DB","Upgrading database");
        this.onCreate(db);
    }

    public void addPosition(Position position){
        Log.d("add Position", "Value, x: "+position.getScreenX()+" y: "+position.getScreenY());
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("screenX", position.getScreenX());
        values.put("screenY", position.getScreenY());
        values.put("latitude", position.getLatitude());
        values.put("longitude", position.getLongitude());

        db.insert("position", null, values);
    }
}
