package com.example.adams.ptuxiaki_xwris_tabs;


import android.content.ContentProviderOperation;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;


public class SaveContact extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_contact);


        Intent intent = getIntent();
        String qrmsgName = intent.getStringExtra("strName");
        String qrmsgEmail = intent.getStringExtra("strEmail");
        String qrmsgNumber = intent.getStringExtra("strNumber");
        String qrmsgAddress = intent.getStringExtra("strAddress");
        String qrmsgCompany = intent.getStringExtra("company");
        //Toast.makeText(this, qrmsg+"to pire" ,Toast.LENGTH_LONG).show();
        showContact(qrmsgName, qrmsgEmail, qrmsgNumber, qrmsgAddress, qrmsgCompany);
        addListener(qrmsgName, qrmsgEmail, qrmsgNumber, qrmsgAddress, qrmsgCompany);


    }

    private void addListener(final String qrmsgName, final String qrmsgEmail, final String qrmsgNumber, final String qrmsgAddress, final String qrmsgCompany) {
        ImageButton save = findViewById(R.id.saveButton);


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveContact(qrmsgName, qrmsgEmail, qrmsgNumber, qrmsgAddress, qrmsgCompany);
                //Toast.makeText(getApplicationContext(),"To button doulevei", Toast.LENGTH_LONG).show();


            }
        });
    }

    public void showContact(String qrmsgName, String qrmsgEmail, String qrmsgNumber, String qrmsgAddress, String qrmsgCompany) {

        final TextView svFname = findViewById(R.id.svFname);
        final TextView svCmp = findViewById(R.id.svCmp);
        final TextView svAddr = findViewById(R.id.svAddr);
        final TextView svNumber = findViewById(R.id.svNumber);
        final TextView svEmail = findViewById(R.id.svEmail);


        svFname.setText(qrmsgName);
        svEmail.setText(qrmsgEmail);
        svNumber.setText(qrmsgNumber);
        svAddr.setText(qrmsgAddress);
        svCmp.setText(qrmsgCompany);

    }


    public void saveContact(String qrmsgName, String qrmsgEmail, String qrmsgNumber, String qrmsgAddress, String qrmsgCompany) {
        String DisplayName = qrmsgName;
        String MobileNumber = qrmsgNumber;
        String emailID = qrmsgEmail;
        String company = qrmsgCompany;
        String address = qrmsgAddress;

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //Names
        if (DisplayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());
        }

        // Mobile Number
        if (MobileNumber != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //Email
        if (emailID != null) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        // Organization
        if (!company.equals("")) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
                    .build());
        }

        //Address
        if (!address.equals("")) {
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address)
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK)
                    .build());
        }

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(getApplicationContext(), "Η Επαφή αποθηκεύτηκε με επιτυχία", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}
