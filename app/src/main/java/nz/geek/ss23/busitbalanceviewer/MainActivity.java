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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private NfcAdapter mAdapter;
    private AlertDialog mDialog;

    TextView balanceView;

    private byte[] key = new byte[] { (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0xff, (byte)0xff, (byte)0xff };

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

        this.balanceView = (TextView)findViewById(R.id.balanceView);

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

                //Toast.makeText(getApplicationContext(), "Card detected!", Toast.LENGTH_SHORT).show();
                Log.d("BBV", "Card detected");

                tag.connect();

                // Authenticate to the card in the sector we'll be reading from
                if (!tag.authenticateSectorWithKeyA(2, this.key)) {
                    Log.d("AUTH", "Authentication failed. Not Busit?");
                    Toast.makeText(getApplicationContext(), "Invalid card detected.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(getApplicationContext(), "Reading card...", Toast.LENGTH_SHORT).show();
                }

                // Read out the bytes we require (sector 2, block 1)
                byte[] blockData = tag.readBlock(tag.sectorToBlock(2) + 1);

                // Reconstruct the balance from our block
                int balance =
                        (blockData[9]  << 0)&0x000000ff |
                        (blockData[10] << 8)&0x0000ff00;

                Log.i("DEBUG", String.format("9: 0x%x - 10: 0x%x", blockData[9], blockData[10]));

                int cents = balance % 100;
                int dollars = balance / 100;

                this.balanceView.setText(String.format("$%d.%02d", dollars, cents));
                this.balanceView.setVisibility(View.VISIBLE);

                //Toast.makeText(getApplicationContext(), String.format("Balance: $%d.%02d", dollars, cents), Toast.LENGTH_SHORT).show();

                /*

                int sectorCount = tag.getSectorCount(); // Get the number of sectors for this tag
                int tagSize = tag.getSize(); // Get the tag size
                Log.d("SECTOR", Integer.toString(sectorCount));
                Log.d("TAGSIZE", Integer.toString(tagSize));

                if (!tag.authenticateSectorWithKeyA(0, this.key)) {
                    Log.d("AUTH", "Authentication failed. Not Busit?");
                    Toast.makeText(getApplicationContext(), "Invalid card detected.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "Authenticated!", Toast.LENGTH_SHORT).show();

                // Loop over every sector and authenticate and get the data
                Log.d("DEBUG", String.format("Reading %d sectors", sectorCount));
                for (int i = 0; i < sectorCount; i++) {
                    if (!tag.authenticateSectorWithKeyA(i, this.key)) {
                        Log.d("AUTH", "Authentication failed. Not Mifare?");
                        Toast.makeText(getApplicationContext(), "Invalid card detected.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Loop over every block
                    int blockCount = tag.getBlockCountInSector(i);
                    Log.d("DEBUG", String.format("Reading %d blocks", blockCount));
                    for (int j = 0; j < blockCount; j++) {
                        try {
                            Log.d("DEBUG", String.format("Reading block: %d", tag.sectorToBlock(i) + j));
                            byte[] blockData = tag.readBlock(tag.sectorToBlock(i) + j);
                            for (int k = 0; k < blockData.length; k++) {
                                Log.i("DATA", String.format("%d %d %d: 0x%x", i, j, k, blockData[k]));
                            }
                        } catch (Exception e) {
                            Log.i("ERROR", "Temporary error reading card");
                            // re-authenticate
                            tag.authenticateSectorWithKeyA(i, this.key);
                        }
                    }
                }
                // Retrieve the balance data
                */

            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Unspecified error", Toast.LENGTH_SHORT).show();
            Log.i("ERROR", e.toString());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }
}
