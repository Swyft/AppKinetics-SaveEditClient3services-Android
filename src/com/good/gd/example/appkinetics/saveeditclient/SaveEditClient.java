/*
 *  This file contains sample code that is licensed according to the BlackBerry Dynamics SDK terms and conditions.
 *  (c) 2017 BlackBerry Limited. All rights reserved.
 */

package com.good.gd.example.appkinetics.saveeditclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.good.gd.GDAndroid;
import com.good.gd.GDServiceProvider;
import com.good.gd.GDServiceType;
import com.good.gd.GDStateListener;
import com.good.gd.file.File;
import com.good.gd.file.FileInputStream;
import com.good.gd.file.FileOutputStream;
import com.good.gd.icc.GDICCForegroundOptions;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveEditClient extends Activity implements GDStateListener {

    private static final String TAG = SaveEditClient.class.getSimpleName();
    // private static final String SERVICE_NAME = "com.good.gdservice.edit-file";
    private static final String SERVICE_NAME = "com.swyftmobile.smsf.create-contact";
    private static final String SERVICE_VERSION = "1.0.0.0";
    // private static final String SERVICE_METHOD = "editFile";
    private static final String SERVICE_METHOD = "importFile";
    private static final String FILE_NAME = "DataFile.txt";
    private static final String VCARD_FILE_NAME = "christopherdonato.vcf";

    private static final String CONTACT_SERVICE_NAME = "com.swyftmobile.smsf.create-contact";
    private static final String NOTE_SERVICE_NAME = "com.swyftmobile.smsf.create-note";
    private static final String DOCUMENT_SERVICE_NAME = "com.swyftmobile.smsf.create-document";

    private TextView dataView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GDAndroid.getInstance().activityInit(this);
        GDSaveEditClientListener.getInstance().setOnReceiveAttachmentsEventListener(
                new GDSaveEditClientListener.OnReceiveAttachmentsEventListener() {
                    @Override
                    public void onReceiveAttachments(final String[] attachments) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (attachments != null) {
                                    refreshData(attachments[0]);
                                }
                            }
                        });
                    }
                }
        );
        setContentView(R.layout.activity_main);
        dataView = findViewById(R.id.data_view);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_contact:
                sendContact();
                return true;
            case R.id.action_send_document:
                sendDocument();
                return true;
            case R.id.action_send_note:
                sendNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void processSendActionSelected() {
        showDialog();
    }

    @Override
    public void onAuthorized() {
        if (GDSaveEditClientListener.getInstance().getPendingAttachments() != null) {
            refreshData(GDSaveEditClientListener.getInstance().getPendingAttachments()[0]);
        } else {
            final File file = getTextFile();

            final String text = readDataFromFile(file.getAbsolutePath());

            dataView.setText(text);
        }
    }

    private void showDialog() {
        final List<GDServiceProvider> services = getServiceProviders();
        final List<String> serviceNames = getServiceNames(services);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.send_to_label)
                .setAdapter(
                        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, serviceNames),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int which) {
                                sendFile(services.get(which).getAddress());
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                // User cancelled the dialog
                            }
                        }
                ).setCancelable(true);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private List<GDServiceProvider> getServiceProviders() {
        final List<GDServiceProvider> serviceProviders = GDAndroid.getInstance()
                .getServiceProvidersFor(SERVICE_NAME, SERVICE_VERSION,
                        GDServiceType.GD_SERVICE_TYPE_APPLICATION);

        for (final GDServiceProvider serviceProvider : serviceProviders) {
            if (serviceProvider.getAddress().equals(getPackageName())) {
                serviceProviders.remove(serviceProvider);
                break;
            }
        }
        return serviceProviders;
    }

    private List<GDServiceProvider> getServiceProviders(String serviceName) {
        final List<GDServiceProvider> serviceProviders = GDAndroid.getInstance()
                .getServiceProvidersFor(serviceName, SERVICE_VERSION,
                        GDServiceType.GD_SERVICE_TYPE_APPLICATION);

        for (final GDServiceProvider serviceProvider : serviceProviders) {
            if (serviceProvider.getAddress().equals(getPackageName())) {
                serviceProviders.remove(serviceProvider);
                break;
            }
        }
        return serviceProviders;
    }

    private List<String> getServiceNames(final List<GDServiceProvider> serviceProviders) {
        final List<String> serviceNames = new ArrayList<String>();
        for (final GDServiceProvider serviceProvider : serviceProviders) {
            serviceNames.add(serviceProvider.getName());
        }
        return serviceNames;
    }

    private void sendFile(final String serviceId) {
        final File file = getTextFile();

        try {
            GDServiceClient.sendTo(serviceId,
                    SERVICE_NAME,
                    SERVICE_VERSION,
                    SERVICE_METHOD,
                    null,
                    new String[]{file.getAbsolutePath()},
                    GDICCForegroundOptions.PreferPeerInForeground);
        } catch (final GDServiceException gdServiceException) {
            Log.e(TAG, gdServiceException.getMessage());
        }
    }

    private void sendContact(final String serviceId) {
        final File file = getVCardFile();

        try {
            GDServiceClient.sendTo(serviceId,
                    CONTACT_SERVICE_NAME,
                    SERVICE_VERSION,
                    SERVICE_METHOD,
                    null,
                    new String[] { file.getAbsolutePath()},
                    GDICCForegroundOptions.PreferPeerInForeground);
        } catch (final GDServiceException gdServiceException) {
            Log.e(TAG, gdServiceException.getMessage());
        }
    }

    private File getTextFile() {
        try {
            final InputStream inputStream = getAssets().open(FILE_NAME);
            final File file = new File("/", FILE_NAME);

            if (!file.exists()) {
                file.createNewFile();
            } else {
                return file;
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                fileOutputStream.write(buf, 0, len);
            }
            inputStream.close();
            fileOutputStream.close();

            return file;
        } catch (final IOException ioException) {
            return null;
        }
    }

    private File getVCardFile() {
        try {
            // VCARD_FILE_NAME
            final InputStream inputStream = getAssets().open(VCARD_FILE_NAME);
            final File file = new File("/", VCARD_FILE_NAME);

            if (!file.exists()) {
                file.createNewFile();
            } else {
                return file;
            }

            final FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte[] buf = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                fileOutputStream.write(buf, 0, len);
            }
            inputStream.close();
            fileOutputStream.close();

            return file;
        } catch (final IOException ioException) {
            return null;
        }
    }

    private void refreshData(final String filePath) {
        final String editedText = readDataFromFile(filePath);
        dataView.setText(editedText);

        try {
            final File file = new File("/", FILE_NAME);

            final FileOutputStream overWritingFile = new FileOutputStream(file.getAbsolutePath());
            overWritingFile.write(editedText.getBytes());
            overWritingFile.flush();
            overWritingFile.close();
        } catch (final IOException ioException) {
            Log.e(TAG, ioException.getMessage());
        }
    }

    private String readDataFromFile(final String filePath) {
        String dataFromFile = "";
        byte data[];
        try {
            final InputStream inputStream = new FileInputStream(filePath);
            if (inputStream.available() > 0) {
                data = new byte[inputStream.available()];
                inputStream.read(data);
                dataFromFile = new String(data, "UTF-8");
                inputStream.close();
            }

        } catch (final IOException ioException) {
            dataFromFile = "File read error";
        }
        return dataFromFile;
    }

    private void sendContact() {
        List<GDServiceProvider> serviceProviderList = getServiceProviders(CONTACT_SERVICE_NAME);
        final List<String> serviceNames = getServiceNames(serviceProviderList);
        String serviceAddress = serviceProviderList.get(0).getAddress();
        sendContact(serviceAddress);
    }

    private void sendDocument() {
        List<GDServiceProvider> serviceProviderList = getServiceProviders(DOCUMENT_SERVICE_NAME);
        String serviceAddress = serviceProviderList.get(0).getAddress();
        sendDocumentKinetics(serviceAddress);
    }

    private void sendNote() {
        List<GDServiceProvider> serviceProviderList = getServiceProviders(NOTE_SERVICE_NAME);
        String serviceAddress = serviceProviderList.get(0).getAddress();
        sendNoteKinetics(serviceAddress);
    }

    private void sendDocumentKinetics(String serviceAddress) {
        final File file = getTextFile();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("Filename", "DataFile.txt");
        params.put("Mimetype", "text/plain");

        try {
            GDServiceClient.sendTo(serviceAddress,
                    DOCUMENT_SERVICE_NAME,
                    SERVICE_VERSION,
                    SERVICE_METHOD,
                    params,
                    new String[] { file.getAbsolutePath()},
                    GDICCForegroundOptions.PreferPeerInForeground);
        } catch (final GDServiceException gdServiceException) {
            Log.e(TAG, gdServiceException.getMessage());
        }
    }

    private void sendNoteKinetics(String serviceAddress) {
        final File file = getTextFile();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("Title", "This is my title");
        params.put("Body", "This is my sample body for this note.");

        try {
            GDServiceClient.sendTo(serviceAddress,
                    NOTE_SERVICE_NAME,
                    SERVICE_VERSION,
                    SERVICE_METHOD,
                    params,
                    null,
                    GDICCForegroundOptions.PreferPeerInForeground);
        } catch (final GDServiceException gdServiceException) {
            Log.e(TAG, gdServiceException.getMessage());
        }
    }

    @Override
    public void onLocked() {
        Log.d(TAG, "MainActivity.onLocked()");
    }

    @Override
    public void onWiped() {
        Log.d(TAG, "MainActivity.onWiped()");
    }

    @Override
    public void onUpdateConfig(final Map<String, Object> stringObjectMap) {
        Log.d(TAG, "MainActivity.onUpdateConfig()");
    }

    @Override
    public void onUpdatePolicy(final Map<String, Object> stringObjectMap) {
        Log.d(TAG, "MainActivity.onUpdatePolicy()");
    }

    @Override
    public void onUpdateServices() {
        Log.d(TAG, "MainActivity.onUpdateServices()");
    }

    @Override
    public void onUpdateEntitlements() {
        Log.d(TAG, "onUpdateEntitlements()");
    }
}