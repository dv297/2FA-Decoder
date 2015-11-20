package vuze.twofactorauthentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    @Bind(R.id.value_textview)
    TextView valueTextview;
    @Bind(R.id.instructions)
    TextView instructions;

    KeyDataSource keySource;
    SecretKeySpec secretKey;
    byte[] key;
    String cipherType = "AES/CBC/PKCS5Padding";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TypefaceProvider.registerDefaultIconSets();

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            valueTextview.setText(savedInstanceState.getString("keyValue"));
        }


        keySource = new KeyDataSource(this);
        keySource.open();
        if (keySource.isKeyPresent()) {
            String keyString = keySource.getKey();
            setKey(keyString);
        } else {
            Intent registrationIntent = new Intent(this, RegistrationActivity.class);
            startActivity(registrationIntent);
            keySource.close();
            finish();
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("keyValue", valueTextview.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

        @OnClick(R.id.scan_button)
    public void scanBarcode() {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d(TAG, "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "Scanned");
                String barcodeText = result.getContents();
                String[] barcodeElements = barcodeText.split(":");
                String plainText = decrypt(barcodeElements[1], barcodeElements[0]);
                if(plainText != null) {
                    valueTextview.setText(plainText);
                    instructions.setText("Enter the following pin into the extension");
                }


            }
        } else {
            Log.d(TAG, "Error");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public String decrypt(String cipherText) {
                try {
            Cipher cipher = Cipher.getInstance(cipherType);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String plainText = (new String(cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT))));
            return plainText;
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public String decrypt(String cipherText, String ivS) {
        AlgorithmParameterSpec iv = new IvParameterSpec(Base64.decode(ivS, Base64.DEFAULT));
        try {
            Cipher cipher = Cipher.getInstance(cipherType);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            String plainText = (new String(cipher.doFinal(Base64.decode(cipherText, Base64.DEFAULT))));
            return plainText;
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(cipherType);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            valueTextview.setText(Base64.encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")), Base64.DEFAULT));
            Log.i("CIPHER TEXT: ", valueTextview.getText() + "");
        } catch (Exception e) {
            Toast.makeText(this, "Error while encrypting: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(Base64.decode(myKey, Base64.DEFAULT), "AES");


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}