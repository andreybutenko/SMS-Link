package ninja.andrey.smslink;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import ninja.andrey.smslink.utils.ContactSync;
import ninja.andrey.smslink.utils.CursorHelper;
import ninja.andrey.smslink.utils.MmsSync;
import ninja.andrey.smslink.utils.SmsSync;

/*
    References:
    - https://stackoverflow.com/a/36200825/1110858
    - https://stackoverflow.com/questions/8447735/android-sms-type-constants
 */

public class SyncService extends Service {
    private static final String TAG = "SYNC_SERVICE";
    private Socket socket;
    private static SyncService instance;
    private List<SyncStatus> listeners = new LinkedList<>();

    private static final String REQUEST_DIRECTORY = "REQUEST_DIRECTORY";

    public SyncService() {
        Log.d(TAG, "Started!");
        instance = this;
        try {
            socket = IO.socket("http://192.168.1.123");
            socket.connect();
        } catch (URISyntaxException e) {
            Log.d(TAG, "Exception");
            e.printStackTrace();
        }
        Log.d(TAG, "Successfully connected!");

        socket.on(REQUEST_DIRECTORY, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        });
    }

    public static SyncService getInstance() {
        Log.d(TAG, "Got instance... " + String.valueOf(instance));
        return instance;
    }

    public void authenticate() {
        Log.d(TAG, "Authenticating");
        socket.emit("AUTH", Preferences.getAuthToken(this));
    }

    public void sendMessage(String string) {
        Log.d(TAG, "Sending a message");
        socket.emit("MESSAGE", string);
    }

    public void sendMessage(JSONObject data) {
        Log.d(TAG, "Sending a message");
        socket.emit("MESSAGE", data);
    }

    public void sendSms(JSONObject sms) {
        Log.d(TAG, "Sending a sms");
        socket.emit("SMS", sms);
    }

    public void sendMms(JSONObject mms) {
        Log.d(TAG, "Sending a mms");
        socket.emit("MMS", mms);
    }

    public void sendContact(JSONObject contact) {
        Log.d(TAG, "Sending a contact");
        socket.emit("CONTACT", contact);
    }

    public void syncAllTexts() {
        MmsSync mmsSync = new MmsSync(this);
        mmsSync.syncMmsTexts();

        SmsSync smsSync = new SmsSync(this);
        smsSync.syncSmsTexts();
    }

    public void syncAllContacts() {
        ContactSync contactSync = new ContactSync(this);
        contactSync.syncContacts();
    }

    public void addListener(SyncStatus listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for(SyncStatus listener : listeners) {
            // listener.onProgress();
        }
    }

    public interface SyncStatus {
        void onSetup(int mmsCount, int smsCount, int contactCount);
        void onProgress(int mmsComplete, int smsComplete, int contactComplete);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
