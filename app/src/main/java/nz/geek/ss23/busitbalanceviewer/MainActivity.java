package nz.geek.ss23.busitbalanceviewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private NfcAdapter mAdapter;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            showMessage(R.string.error, R.string.no_nfc);
            finish();
            return;
        }

        resolveIntent(getIntent());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    private void showMessage(int title, int message) {
        mDialog.setTitle(title);
        mDialog.setMessage(getText(message));
        mDialog.show();
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        try {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                MifareClassic tag = MifareClassic.get(tagFromIntent);
                Toast.makeText(getApplicationContext(), "Mifare detected!", Toast.LENGTH_SHORT).show();

                tag.connect();

                int sectorCount = tag.getSectorCount(); // Get the number of sectors for this tag
                int tagSize = tag.getSize(); // Get the tag size
                Log.d("SECTOR", Integer.toString(sectorCount));
                Log.d("TAGSIZE", Integer.toString(tagSize));
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
}
