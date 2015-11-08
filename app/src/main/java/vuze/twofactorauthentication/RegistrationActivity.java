package vuze.twofactorauthentication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.security.GeneralSecurityException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationActivity extends AppCompatActivity {

    @Bind(R.id.key_container)
    EditText keyContainer;

    KeyDataSource keySource;
    AesCbcWithIntegrity.SecretKeys keys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        keySource = new KeyDataSource(this);
        keySource.open();
        //Toast.makeText(this, "Generating new key", Toast.LENGTH_SHORT).show();
        try {
            keys = AesCbcWithIntegrity.generateKey();
            keySource.insertKey(keys.toString());
            keyContainer.setText(keys.toString());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.submit_button)
    public void closeRegistration(){
        keySource.close();
        finish();
    }


}
