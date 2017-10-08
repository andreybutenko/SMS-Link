package ninja.andrey.smslink;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Andrey on 10/6/2017.
 */

public class Preferences {
    private static final String SMS_LINK = "SMS_LINK";
    private static final String MESSAGE_TOKEN =  "MESSAGE_TOKEN";
    private static final String AUTH_TOKEN = "AUTH_TOKEN";

    public static String getMessageToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SMS_LINK, context.MODE_PRIVATE);
        return sharedPreferences.getString(MESSAGE_TOKEN, "");
    }

    public static void setMessageToken(Context context, String messageToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SMS_LINK, context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(MESSAGE_TOKEN, messageToken)
                .apply();
    }

    public static String getAuthToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SMS_LINK, context.MODE_PRIVATE);
        return sharedPreferences.getString(AUTH_TOKEN, "");
    }

    public static void setAuthToken(Context context, String authToken) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SMS_LINK, context.MODE_PRIVATE);
        sharedPreferences.edit()
                .putString(AUTH_TOKEN, authToken)
                .apply();
    }
}
