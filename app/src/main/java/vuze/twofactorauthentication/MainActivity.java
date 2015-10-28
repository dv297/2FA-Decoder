package vuze.twofactorauthentication;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements FragmentBarcode.OnBarcodeFragmentInteractionListener {


    @Bind(R.id.value_textview)
    TextView valueTextview;

    BarCodeScannerFragment mScannerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        FragmentManager fm = getSupportFragmentManager();
        mScannerFragment = (BarCodeScannerFragment) fm.findFragmentById(R.id.barcode_scanner);

    }

    @Override
    public void onBarcodeFragmentInteraction(String barcodeText) {
        Toast.makeText(this, barcodeText, Toast.LENGTH_SHORT).show();
    }
}
