package ninja.andrey.smslink.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import ninja.andrey.smslink.SyncService;

/**
 * Created by Andrey on 10/9/2017.
 */

public class ConversationSync {
    private static final String TAG = "CONVO_SYNC";
    private SyncService syncService;
    private ContentResolver contentResolver;

    public ConversationSync(SyncService syncService) {
        this.syncService = syncService;
        this.contentResolver = syncService.getContentResolver();
    }

    public void syncConversations() {
        Cursor conversationCursor = contentResolver.query(Uri.parse("content://mms-sms/conversations?simple=true"), null, null, null, null);
        CursorHelper.iterate(conversationCursor, new CursorHelper.IterateListener() {
            @Override
            public void onItemParsed(JSONObject conversationData) {
                syncService.sendConversation(conversationData);
            }
        });
    }
}
