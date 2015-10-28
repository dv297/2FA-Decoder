package vuze.twofactorauthentication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

/**
 * Created by Daniel Vu on 10/28/2015.
 */
public class FragmentBarcode extends BarCodeScannerFragment {


    private OnBarcodeFragmentInteractionListener mListener;
    public interface OnBarcodeFragmentInteractionListener {
        void onBarcodeFragmentInteraction(String patientID);
    }

    public FragmentBarcode() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setmCallBack(new BarCodeScannerFragment.IResultCallback() {
            @Override
            public void result(Result lastResult) {
                String patientID = lastResult.toString();
                Log.v("zxingfragmentlib", patientID);
//                Toast.makeText(getActivity(), patientID, Toast.LENGTH_SHORT).show();
                if (mListener != null) {
                    mListener.onBarcodeFragmentInteraction(patientID);
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnBarcodeFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnBarcodeFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
