package com.example.adams.ptuxiaki_xwris_tabs;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

public class WriteTag extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    ImageButton nfc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_write_tag);
        nfcAdapter = nfcAdapter.getDefaultAdapter(this);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String number = intent.getStringExtra("number");
        String address = intent.getStringExtra("address");
        String company = intent.getStringExtra("company");
        showContact(name, email, number, address, company);
        addListener(name, email, number, address, company);


    }

    private void addListener(final String name, final String email, final String number, final String address, final String company) {
        nfc = findViewById(R.id.writeTag);

        nfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (myTag == null) {
                        Toast.makeText(getApplicationContext(), "Error1: during writing, is the NFC tag close enough to your device?", Toast.LENGTH_LONG).show();
                    } else {
                        write(name, number, email, address, company, myTag);
                        Toast.makeText(getApplicationContext(), "Contact written to the NFC tag successfully!", Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Error2: during writing, is the NFC tag close enough to your device?", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (FormatException e) {
                    Toast.makeText(getApplicationContext(), "Error3: during writing, is the NFC tag close enough to your device?", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};


    }


    @Override
    public void onPause() {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        WriteModeOn();
    }


    //Enable Write
    private void WriteModeOn() {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    //Disable Write
    private void WriteModeOff() {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }


    //Read Tag
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        return;

    }


    //Write Tag
    private void write(String fName, String number, String email, String address, String company, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = {createVcardRecord(fName, number, email, address, company)};
        NdefMessage message = new NdefMessage(records);
        // Get an instance of Ndef for the tag.
        Ndef ndef = Ndef.get(tag);
        // Enable I/O
        ndef.connect();
        // Write the message
        ndef.writeNdefMessage(message);
        // Close the connection
        ndef.close();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }


    private void showContact(String name, String email, String number, String address, String company) {
        TextView svFname = findViewById(R.id.wrFname);
        TextView svCmp = findViewById(R.id.wrCmp);
        TextView svAddr = findViewById(R.id.wrAddr);
        TextView svNumber = findViewById(R.id.wrNumber);
        TextView svEmail = findViewById(R.id.wrEmail);


        svFname.setText(name);
        svEmail.setText(email);
        svNumber.setText(number);
        svAddr.setText(address);
        svCmp.setText(company);
    }


    //create NFC message
    public NdefRecord createVcardRecord(String fName, String number, String email, String address, String company) {
        String payloadStr = "BEGIN:VCARD" + "\n" + "VERSION:2.1" + "\n" + "FN:" + fName + "\n" + "ORG:" + company + "\n" + "TEL:" + number + "\n" + "ADR:" + address + "\n" + "EMAIL:" + email + "\n" + "END:VCARD";
        byte[] uriField = payloadStr.getBytes(Charset.forName("UTF-8"));
        byte[] payload = new byte[uriField.length + 1];
        System.arraycopy(uriField, 0, payload, 1, uriField.length);
        NdefRecord nfcRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                "text/vcard".getBytes(),
                new byte[0],
                payload);
        //tvMessage.setText(payloadStr);

        return nfcRecord;
    }


}