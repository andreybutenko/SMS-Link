package ninja.andrey.smslink.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import ninja.andrey.smslink.SyncService;

/**
 * Created by Andrey on 10/7/2017.
 */

public class ContactSync {
    private static final String TAG = "CONTACT_SYNC";
    private SyncService syncService;
    private ContentResolver contentResolver;

    public ContactSync(SyncService syncService) {
        this.syncService = syncService;
        this.contentResolver = syncService.getContentResolver();
    }

    public void syncContacts() {
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        CursorHelper.iterate(cursor, new CursorHelper.IterateListener() {
            @Override
            public void onItemParsed(JSONObject contactData) {
                try {
                    contactData.put("phone", getPhoneNumber(contactData));
                    if(contactData.has("photo_uri")) {
                        Bitmap contactPhoto = getContactImage(contactData);
                        String encodedImage = encodeToBase64(contactPhoto, Bitmap.CompressFormat.JPEG, 100);
                        contactData.put("photo", encodedImage);
                    }
                    syncService.sendContact(contactData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void syncContactRecipientIds()  {
        // Associate `recipient_ids` from conversations with contacts
        Cursor cursor = contentResolver.query(Uri.parse("content://mms-sms/canonical-addresses"), null, null, null, null);
        CursorHelper.iterate(cursor, new CursorHelper.IterateListener() {
            @Override
            public void onItemParsed(JSONObject conversationData) {
                syncService.sendContactRecipientIds(conversationData);
            }
        });
    }

    private String getPhoneNumber(JSONObject contactData) {
        try {
            return contactData.getString(ContactsContract.CommonDataKinds.Phone.NUMBER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getContactImage(JSONObject contactData) {
        try {
            Uri uri = Uri.parse(contactData.getString("photo_uri"));
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

    // from https://stackoverflow.com/a/9768973/1110858
    private String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }
}
