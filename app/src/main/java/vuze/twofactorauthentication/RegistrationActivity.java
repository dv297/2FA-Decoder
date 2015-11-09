package vuze.twofactorauthentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationActivity extends AppCompatActivity {

    @Bind(R.id.key_container)
    EditText keyContainer;

    KeyDataSource keySource;
    String keys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        keySource = new KeyDataSource(this);
        keySource.open();

        KeyGenerator kgen = null;
        try {
            kgen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            kgen.init(128, sr);
            SecretKey skey = kgen.generateKey();
            keys = Base64.encodeToString(skey.getEncoded(), Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        keySource.insertKey(keys.toString());
        keyContainer.setText(keys.toString());
        Log.i("New Key", keys.toString());
    }

    @OnClick(R.id.submit_button)
    public void closeRegistration() {
        keySource.close();
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}
