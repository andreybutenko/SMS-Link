package ninja.andrey.smslink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import ninja.andrey.smslink.models.Text;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final int GOOGLE_SIGN_IN_RESULT = 10000;
    private static final String TAG = "SPLASH_ACTIVITY";

    private FirebaseAuth firebaseAuth;
    private GoogleApiClient googleApiClient;

    ProgressDialog progressDialog;

    Button signinEmail;
    Button signinGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
/*
        TelephonyManager tMgr = (TelephonyManager)mAppContext.getSystemService(Context.TELEPHONY_SERVICE);
        String mPhoneNumber = tMgr.getLine1Number();
        */

        firebaseAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signinEmail = (Button) findViewById(R.id.btn_signin_email);
        signinGoogle = (Button) findViewById(R.id.btn_signin_google);

        signinGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_RESULT);
            }
        });

        ///

        startService(new Intent(this, SyncService.class));

        signinEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SyncService syncService = SyncService.getInstance();
                syncService.authenticate();
                // syncService.sendMessage();
                //syncService.syncAllTexts();
                syncService.syncAllContacts();
            }
        });

        ///
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "Authenticated successfully");
                        if (task.isSuccessful()) {
                            final FirebaseUser user = firebaseAuth.getCurrentUser();
                            user.getIdToken(true)
                                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                                            dismissProgressDialog();
                                            if (task.isSuccessful()) {
                                                String idToken = task.getResult().getToken();
                                                Toast.makeText(SplashActivity.this, "Signed in " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                                Log.d(TAG, "Got id token");
                                                afterAuthentication(user, idToken);
                                            } else {
                                                Log.w(TAG, "Authenticated unsuccessfully", task.getException());
                                                displayDialog("Sign in error", "We were not able to sign you in! Try using another form of sign in.");
                                            }
                                        }
                                    });
                        } else {
                            Log.w(TAG, "Authenticated unsuccessfully", task.getException());
                            displayDialog("Sign in error", "We were not able to sign you in! Try using another form of sign in.");
                        }
                    }
                });
    }

    private void afterAuthentication(FirebaseUser user, String idToken) {
        Preferences.setAuthToken(this, idToken);
        MessageIdService.sendRegistrationToServer(user, Preferences.getMessageToken(this));
    }

    private void displayDialog(String title, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(description)
                .setPositiveButton("Ok", null);
        builder.create().show();
    }

    private void displayProgressDialog() {
        progressDialog = ProgressDialog.show(this, "Loading", "Please wait");
    }

    private void dismissProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_RESULT) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                displayProgressDialog();
                GoogleSignInAccount account = result.getSignInAccount();
                Log.d(TAG, "Signed in successfully");
                firebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "Cancelled Google sign in");
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
