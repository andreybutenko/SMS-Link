package ninja.andrey.smslink.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ninja.andrey.smslink.SyncService;

/**
 * Created by Andrey on 10/7/2017.
 */

public class MmsSync {
    private static final String TAG = "MMS_SYNC";
    private SyncService syncService;
    private ContentResolver contentResolver;

    public MmsSync(SyncService syncService) {
        this.syncService = syncService;
        this.contentResolver = syncService.getContentResolver();
    }
    
    public void syncMmsTexts() {
        Cursor mmsCursor = contentResolver.query(Uri.parse("content://mms"), new String[]{"*"}, null, null, null);
        CursorHelper.iterate(mmsCursor, new CursorHelper.IterateListener() {
            @Override
            public void onItemParsed(JSONObject textData) {
                parseMms(textData);
            }
        });
    }

    private void parseMms(final JSONObject textData) {
        try {
            Cursor mmsCursor = contentResolver.query(Uri.parse("content://mms/part"), new String[]{"*"}, "mid=" + textData.getString("_id"), null, null);
            final String address = getMmsAddr(textData);
            CursorHelper.iterateAll(mmsCursor, new CursorHelper.IterateAllListener() {
                @Override
                public void onItemsParsed(List<JSONObject> partsData) {
                    Log.d(TAG, String.valueOf(partsData));
                    JSONObject parameters = new JSONObject();
                    JSONArray parts = new JSONArray();

                    try {
                        int i = 0;
                        for(JSONObject partData : partsData) {
                            if(partData.getString("ct").equals("text/plain")) {
                                partData.put("body", getMmsText(partData));
                            }
                            else if(partData.getString("ct").equals("image/jpeg")) {
                                Bitmap image = getMmsImg(partData);
                                String encodedImage = encodeToBase64(image, Bitmap.CompressFormat.JPEG, 100);
                                partData.put("image", encodedImage);
                            }
                            parts.put(i, partData);
                            i++;
                        }

                        parameters.put("address", address);
                        parameters.put("message", textData);
                        parameters.put("parts", parts);

                        syncService.sendMms(parameters);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getMmsAddr(JSONObject textData) {
        try {
            Cursor mmsCursor = contentResolver.query(Uri.parse("content://mms/" + textData.getString("_id") + "/addr"), null, null, null, null);
            if(mmsCursor.moveToFirst()) {
                int index = mmsCursor.getColumnIndex("address");
                if(index > -1 && index < mmsCursor.getColumnCount()) {
                    return mmsCursor.getString(index);
                }
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getMmsImg(JSONObject partData) {
        try {
            Uri uri = Uri.parse("content://mms/part/" + partData.getString("_id"));
            InputStream in = null;
            Bitmap bitmap = null;

            in = contentResolver.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(in);
            if(in != null)
                in.close();

            return bitmap;
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getMmsText(JSONObject partData) {
        try {
            return partData.getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // from https://stackoverflow.com/a/9768973/1110858
    private String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }
}
