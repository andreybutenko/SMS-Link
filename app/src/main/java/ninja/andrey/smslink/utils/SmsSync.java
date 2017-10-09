package ninja.andrey.smslink.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import org.json.JSONObject;

import ninja.andrey.smslink.SyncService;

/**
 * Created by Andrey on 10/7/2017.
 */

public class SmsSync {
    private static final String TAG = "SMS_SYNC";
    private SyncService syncService;
    private ContentResolver contentResolver;

    public SmsSync(SyncService syncService) {
        this.syncService = syncService;
        this.contentResolver = syncService.getContentResolver();
    }

    public void syncSmsTexts() {
        Cursor smsCursor = contentResolver.query(Uri.parse("content://sms"), new String[]{"*"}, null, null, null);
        CursorHelper.iterate(smsCursor, new CursorHelper.IterateListener() {
            @Override
            public void onItemParsed(JSONObject textData) {
                syncService.sendSms(textData);
            }
        });
    }
}
