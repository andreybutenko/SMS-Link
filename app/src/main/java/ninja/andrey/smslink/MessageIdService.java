package ninja.andrey.smslink;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * Created by Andrey on 10/6/2017.
 */

public class MessageIdService extends FirebaseInstanceIdService {
    private static final String TAG = "MESSAGE_ID_SERVICE";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        Preferences.setMessageToken(this, refreshedToken);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            sendRegistrationToServer(user, refreshedToken);
        }
    }

    public static void sendRegistrationToServer(FirebaseUser user, String token) {
        UserModel userModel = new UserModel(user.getEmail(), token);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference()
                .child("users")
                .child(user.getUid())
                .setValue(userModel);
    }
}