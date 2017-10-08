package ninja.andrey.smslink.utils;

import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrey on 10/7/2017.
 */

public class CursorHelper {
    private static final String TAG = "CURSOR_HELPER";

    public static void iterate(Cursor cursor, IterateListener iteratorListener) {
        if(cursor.moveToFirst()) {
            while(cursor.moveToNext()) {
                JSONObject data = new JSONObject();

                for(int i = 0; i < cursor.getColumnCount(); i++) {
                    try {
                        data.put(cursor.getColumnName(i), cursor.getString(i));
                    } catch (JSONException e) {
                        Log.d(TAG, "Error");
                        e.printStackTrace();
                    }
                }

                iteratorListener.onItemParsed(data);
            }
        }
        cursor.close();
    }

    public static void iterateAll(Cursor cursor, IterateAllListener iteratorListener) {
        List<JSONObject> results = new ArrayList<>();
        if(cursor.moveToFirst()) {
            while(cursor.moveToNext()) {
                JSONObject data = new JSONObject();

                for(int i = 0; i < cursor.getColumnCount(); i++) {
                    try {
                        data.put(cursor.getColumnName(i), cursor.getString(i));
                    } catch (JSONException e) {
                        Log.d(TAG, "Error");
                        e.printStackTrace();
                    }
                }

                results.add(data);
            }
        }
        cursor.close();
        iteratorListener.onItemsParsed(results);
    }

    public static List<JSONObject> iterateImmediately(Cursor cursor) {
        List<JSONObject> results = new ArrayList<>();
        if(cursor.moveToFirst()) {
            while(cursor.moveToNext()) {
                JSONObject data = new JSONObject();

                for(int i = 0; i < cursor.getColumnCount(); i++) {
                    try {
                        data.put(cursor.getColumnName(i), cursor.getString(i));
                    } catch (JSONException e) {
                        Log.d(TAG, "Error");
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, String.valueOf(data));

                results.add(data);
            }
        }
        cursor.close();

        return results;
    }

    public interface IterateListener {
        void onItemParsed(JSONObject data);
    }

    public interface IterateAllListener {
        void onItemsParsed(List<JSONObject> data);
    }
}
