package com.example.adams.ptuxiaki_xwris_tabs;


import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.Result;
import com.google.zxing.client.result.VCardResultParser;
import java.nio.charset.Charset;
import java.util.ArrayList;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;


public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    ImageButton search;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        addListenerNFC();
        addListenerScan();

        addSpinner();

        checkStorage();

        checkNFC();

    }


    private void addListenerNFC() {
        final TextView tvFname = findViewById(R.id.tvFname);
        final TextView tvCmp = findViewById(R.id.tvCmp);
        final TextView tvAddr = findViewById(R.id.tvAddr);
        final TextView tvNumber = findViewById(R.id.tvNumber);
        final TextView tvEmail = findViewById(R.id.tvEmail);

        search = findViewById(R.id.writeNFC);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), WriteTag.class);


                intent.putExtra("email", tvEmail.getText().toString());
                intent.putExtra("name", tvFname.getText().toString());
                intent.putExtra("number", tvNumber.getText().toString());
                intent.putExtra("company", tvCmp.getText().toString());
                intent.putExtra("address", tvAddr.getText().toString());

                startActivity(intent);

            }
        });

    }


    private void addListenerScan() {
        search = findViewById(R.id.scanButton);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scannerView = new ZXingScannerView(MainActivity.this);
                setContentView(scannerView);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkPermission()) {
                        Toast.makeText(MainActivity.this, "Scan QR code", Toast.LENGTH_LONG).show();
                        scannerView.setResultHandler(MainActivity.this);
                        setContentView(scannerView);
                        scannerView.startCamera();

                    } else {
                        requestPermission();
                    }
                }


            }
        });
    }

    private void checkStorage() {
        if (isExternalStorageWritable() == false) {

            Toast.makeText(MainActivity.this, "Έχουμε θέμα", Toast.LENGTH_SHORT).show();
        }
    }


    /* Add a spinner with contacts */
    public void addSpinner() {
        final ArrayList<Contact> listOfContact;
        final Spinner spinner = findViewById(R.id.contact_spinner);
        final TextView tvFname = findViewById(R.id.tvFname);
        final TextView tvCmp = findViewById(R.id.tvCmp);
        final TextView tvAddr = findViewById(R.id.tvAddr);
        final TextView tvNumber = findViewById(R.id.tvNumber);
        final TextView tvEmail = findViewById(R.id.tvEmail);


        listOfContact = loadContacts();

        final ArrayAdapter<Contact> spin_adapter = new ArrayAdapter<Contact>(this, R.layout.spinner_layout, listOfContact);

        spin_adapter.setDropDownViewResource(R.layout.spinner_layout);
        spinner.setAdapter(spin_adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {

                    Toast.makeText(getApplication(), "Διάλεξε μια επαφή", Toast.LENGTH_LONG).show();


                } else {
                    ArrayList<Contact> listOfContact = loadContacts();
                    String fName = listOfContact.get(i).firstName;
                    String number = listOfContact.get(i).number;
                    String email = listOfContact.get(i).email;
                    String company = listOfContact.get(i).company;
                    String address = listOfContact.get(i).address;

                    Toast.makeText(getApplicationContext(), adapterView.getItemAtPosition(i) + " Contact has selected", Toast.LENGTH_LONG).show();


                    tvFname.setText(fName);
                    tvNumber.setText(number);
                    tvEmail.setText(email);
                    tvAddr.setText(address);
                    tvCmp.setText(company);

                    writeNFC(fName, number, email, address, company);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
    }

    /* Load Contacts */
    public ArrayList<Contact> loadContacts() {
        ArrayList<Contact> listOfContact = new ArrayList<Contact>();

        listOfContact.add(new Contact("Διάλεξε μια επαφή", "", "", "", ""));

        Cursor cursor_Contacts = null;
        ContentResolver contentResolver = getContentResolver();

        try {
            cursor_Contacts = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        } catch (Exception ex) {
            Log.e("Error on Contact", ex.getMessage());
        }


        if (cursor_Contacts.getCount() > 0) {


            while (cursor_Contacts.moveToNext()) {

                Contact contact = new Contact("", "", "", "", "");
                String contact_id = cursor_Contacts.getString(cursor_Contacts.getColumnIndex(ContactsContract.Contacts._ID));
                String contact_name = cursor_Contacts.getString(cursor_Contacts.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                contact.firstName = contact_name;

                int hasPhoneNumber = Integer.parseInt(cursor_Contacts.getString(cursor_Contacts.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if (hasPhoneNumber > 0) {

                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {

                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact.number = phoneNumber;
                    }

                    phoneCursor.close();

                }

                Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{contact_id}, null);

                while (emailCursor.moveToNext()) {
                    String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                    contact.email = email;
                }
                emailCursor.close();

                Cursor addrCursor = contentResolver.query(Uri.parse(String.valueOf(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI)), null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{contact_id}, null);

                while (addrCursor.moveToNext()) {
                    String address = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
                    contact.address = address;
                }
                addrCursor.close();

                Cursor cmpCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, null, ContactsContract.CommonDataKinds.Organization.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{contact_id, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}, null);

                while (cmpCursor.moveToNext()) {
                    String company = cmpCursor.getString(cmpCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));
                    contact.company = company;

                }
                cmpCursor.close();

                listOfContact.add(contact);

            }
        }


        return listOfContact;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Check if NFC is enable
    public void checkNFC() {
        NfcAdapter mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

            // Toast.makeText(getContext(),  "NFC available!!!", Toast.LENGTH_SHORT).show();

        } else {

            Toast.makeText(this, "NFC not available!!!", Toast.LENGTH_SHORT).show();

        }
    }

    //push NFC message
    public void writeNFC(String fName, String number, String email, String address, String company) {
        NfcAdapter nfcAdapter1 = NfcAdapter.getDefaultAdapter(getApplicationContext());
        NdefRecord[] rec = new NdefRecord[1];

        rec[0] = createVcardRecord(fName, number, email, address, company);
        NdefMessage msg = new NdefMessage(rec);
        nfcAdapter1.setNdefPushMessage(msg, this);
    }

    //create NFC message
    public NdefRecord createVcardRecord(String fName, String number, String email, String address, String company) {
        //final TextView tvMessage = view.findViewById(R.id.tvNFCContact);
        //final TextView tvFname = getView().findViewById(R.id.tvFname);
        // String fname= WelcomeTab.instantiate(getContext(),getActivity());

        //fName=tvFname.getText().toString();


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

    private boolean checkPermission() {

        return (ContextCompat.checkSelfPermission(MainActivity.this, CAMERA) == PackageManager.PERMISSION_GRANTED);

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    public void onRequestPermissionsResult(int requestCode, String permission[], int grantResults[]) {

        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                displayAlertMessage("You need to allow access for both permission",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(final Result result) {
        final String scanResult = result.getText();
        VCardResultParser temp = new VCardResultParser();
        String displayContact = temp.parse(result).getDisplayResult();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                setContentView(R.layout.activity_main);
                addSpinner();
                addListenerScan();
                addListenerNFC();

            }
        });

        builder.setNeutralButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Result temp = result;
                Intent intent = new Intent(getApplicationContext(), SaveContact.class);
                intent.putExtra("temp", String.valueOf(temp));

                VCardResultParser pars = new VCardResultParser();
                StringBuilder builderName = new StringBuilder();
                StringBuilder builderEmail = new StringBuilder();
                StringBuilder builderNumber = new StringBuilder();
                StringBuilder builderAddress = new StringBuilder();


                String strEmail;
                String strNumber;
                String strName;
                String strAddress;
                String[] name = pars.parse(temp).getNames();
                String[] email = pars.parse(temp).getEmails();
                String[] number = pars.parse(temp).getPhoneNumbers();
                String company = pars.parse(temp).getOrg();
                String[] address = pars.parse(temp).getAddresses();

                for (String s : name) {
                    builderName.append(s);
                }
                strName = builderName.toString();

                if (email == null) {


                    strEmail = "";

                } else {

                    for (String s : email) {
                        builderEmail.append(s);
                    }
                    strEmail = builderEmail.toString();


                }

                if (number == null) {

                    strNumber = "";
                } else {
                    for (String s : number) {
                        builderNumber.append(s);
                    }
                    strNumber = builderNumber.toString();
                }

                if (address == null) {


                    strAddress = "";

                } else {

                    for (String s : address) {
                        builderAddress.append(s);
                    }
                    strAddress = builderAddress.toString();


                }

                intent.putExtra("strEmail", strEmail);
                intent.putExtra("strName", strName);
                intent.putExtra("strNumber", strNumber);
                intent.putExtra("company", company);
                intent.putExtra("strAddress", strAddress);

                //Toast.makeText(getApplicationContext(),str+ "Ayto einai to company",Toast.LENGTH_LONG).show();
                //Toast.makeText(getApplicationContext(),temp,Toast.LENGTH_LONG).show();
                startActivity(intent);


            }
        });
        builder.setMessage(displayContact);
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onBackPressed() {

        setContentView(R.layout.activity_main);
        addSpinner();
        addListenerScan();
        addListenerNFC();

    }

}
