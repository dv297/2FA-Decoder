package vuze.twofactorauthentication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.scottyab.aescrypt.AESCrypt;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tozny.crypto.android.AesCbcWithIntegrity.encrypt;

//import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;


public class MainActivity extends AppCompatActivity {
    //implements FragmentBarcode.OnBarcodeFragmentInteractionListener {

    @Bind(R.id.value_textview)
    EditText valueTextview;

    KeyDataSource keySource;
    @Bind(R.id.scan_button)
    Button scanButton;

    String keyString = "u/Gu5posvwDsXUnV5Zaq4g==";
    String ivString = "5D9r9ZVzEYYgha93/aUK2w==";

    AlgorithmParameterSpec iv = new IvParameterSpec(Base64.decode(ivString, Base64.DEFAULT));

    SecretKeySpec secretKey;
    private static byte[] key;
    private String plainText = "NJG4emn3";
    String cipherType = "AES/CBC/PKCS5Padding";


//    BarCodeScannerFragment mScannerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setKey(keyString);
        encrypt(plainText);
    }

    @OnClick(R.id.scan_button)
    public void scanBarcode() {
        new IntentIntegrator(this).initiateScan(); // `this` is the current Activity
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(this, "Cipher: " + result.getContents(), Toast.LENGTH_LONG).show();
                Log.d("MainActivity", "Scanned");
                String messageAfterDecrypt = decrypt(result.getContents());
                Toast.makeText(this, "Scanned: " + messageAfterDecrypt, Toast.LENGTH_LONG).show();
                valueTextview.setText(messageAfterDecrypt);

            }
        } else {
            Log.d("MainActivity", "Weird");
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public String decrypt(String strToDecrypt)
    {

        try
        {
            Cipher cipher = Cipher.getInstance(cipherType);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            String plainText = (new String(cipher.doFinal(Base64.decode(strToDecrypt, Base64.DEFAULT))));
            Toast.makeText(this, plainText, Toast.LENGTH_SHORT).show();
            return plainText;

        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public String encrypt(String strToEncrypt)
    {
        try
        {
            Cipher cipher = Cipher.getInstance(cipherType);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);


            valueTextview.setText(Base64.encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")), Base64.DEFAULT));
            Log.i("CIPHER TEXT: " , valueTextview.getText() + "");

        }
        catch (Exception e)
        {

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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
