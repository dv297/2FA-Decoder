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
    KeyDataSource keySource; // SQLite database to hold persistent storage of encryption key
    SecretKeySpec secretKey; // Java's object for secret key
    public static final String cipherType = "AES/CBC/PKCS5Padding";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        TypefaceProvider.registerDefaultIconSets();

        // Key Source contains the encryption key
        // KeyDataSource allows us to obtain this key
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

    // Initiate barcode scanning on Scan button click
    @OnClick(R.id.scan_button)
    public void scanBarcode() {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setBeepEnabled(false);
            integrator.initiateScan();
    }


    /**
     * Response from the barcode scanner activity
     * The individual parameters are brought to override Android's onActivityResult,
     * however, the results are parsed in accordance with instructions for
     * zxing-android-embedded
     * @param requestCode N/A, app currently only has three activities and the only
     *                    activity started from MainActivity is the barcode activity
     * @param resultCode N/A, not used
     * @param data N/A
     */
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
                // The barcode encodes two parts delimited by a colon:
                // 1. The initialization vector
                // 2. The cipher text
                String[] barcodeElements = barcodeText.split(":");
                String plainText = decrypt(barcodeElements[1], barcodeElements[0]);
                if(plainText != null) {
                    instructions.setText("Enter the following pin into the extension");
                    valueTextview.setText(plainText);
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

    /**
     * Decrypts the message sent from the extension
     * @param cipherText The cipher text sent by the extension
     * @param ivS The initialization vector used to prevent repeated plain texts from
     *            returning the same cipher text
     * @return returns the plain text or null if there is an error
     */
    public String decrypt(String cipherText, String ivS) {
        AlgorithmParameterSpec iv = new IvParameterSpec(hexStringToByteArray(ivS));
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

    /**
     * Converts the string version of the key to a SecretKeySpec used for Java
     * @param myKey The string representation of the key
     */
    public void setKey(String myKey) {
        MessageDigest sha = null;
        try {

            byte[] key = myKey.getBytes("UTF-8");
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

    /**
     * Converts a string representation of hex to a usable byte array
     * @param s String representation of hex
     * @return Byte array representation of hex
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}