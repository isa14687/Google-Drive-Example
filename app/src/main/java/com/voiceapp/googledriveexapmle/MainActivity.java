package com.voiceapp.googledriveexapmle;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * Google  Drive 範例
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "result";
    private final static int REQUEST_MAIN = 1;
    private final static int REQUEST_CREATE_FILE = 2;
    private GoogleApiClient client;
    private Button bt1;
    private Button bt2;
    private Button bt3;
    private Button bt4;
    private Button bt5;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bt1 = (Button) findViewById(R.id.bt1);
        bt2 = (Button) findViewById(R.id.bt2);
        bt3 = (Button) findViewById(R.id.bt3);
        bt4 = (Button) findViewById(R.id.bt4);
        bt5 = (Button) findViewById(R.id.bt5);
        bt1.setOnClickListener(this);
        bt2.setOnClickListener(this);
        bt3.setOnClickListener(this);
        bt4.setOnClickListener(this);
        bt5.setOnClickListener(this);
        init();
    }

    /**
     * 初始化物件
     */
    private void init() {
        client = new GoogleApiClient.Builder(this)
                //選擇API
                .addApi(Drive.API)
                        //讓使用者存取file
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(connectListener())
                .addOnConnectionFailedListener(connectionFailedListener())
                .build();
    }

    /**
     * Connect 成功的CallBack事件
     */
    private GoogleApiClient.ConnectionCallbacks connectListener() {
        return new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.e(TAG, "onConnected");
                switch (index) {
                    case 1:
                        createNewFolder();
                        break;
                    case 2:
                        CreateFile();
                        break;
                    case 3:
                        openFile();
                        break;
                    case 4:
                        createFileInFolder();
                        break;
                    case 5:
                        searchFile();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.e(TAG, "onConnectionSuspended");
            }
        };
    }

    /**
     * Connect 失敗的CallBack事件
     */
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener() {
        return new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.e(TAG, "onConnectionFailed");
                if (!connectionResult.hasResolution()) {
                    GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, connectionResult.getErrorCode(), 0).show();
                    return;
                }
                try {
                    //允許存取的Dialog
                    connectionResult.startResolutionForResult(MainActivity.this, 2);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        };
    }
//
//    /**
//     * 按鈕的Click事件
//     */
//    private View.OnClickListener clickListener() {
//        return new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                client.connect();
//            }
//        };
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MAIN) {
            if (resultCode == RESULT_OK) {
                Log.e(TAG, "result_ok");
                DriveId driveId = (DriveId) data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                Log.e(TAG, "" + driveId);
                DriveFile file = driveId.asDriveFile();
                file.open(client, DriveFile.MODE_READ_ONLY, null)
                        .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                if (!driveContentsResult.getStatus().isSuccess()) {
                                    Log.e(TAG, "is not successful");
                                    return;
                                }
                                BufferedReader reader = new BufferedReader(new InputStreamReader(driveContentsResult.getDriveContents().getInputStream()));
                                StringBuffer buffer = new StringBuffer();
                                String line;
                                try {
                                    while ((line = reader.readLine()) != null) {
                                        buffer.append(line + "\n");
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.e(TAG, buffer.toString());
                                client.disconnect();

                            }
                        });
//                file.delete(client).setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(Status status) {
//                        if (status.isSuccess()) {
//                            Log.e(TAG, "delete successful");
//                            client.disconnect();
//                        }
//                    }
//                });
            } else {
                Log.e(TAG, "result_fail");
            }
        } else if (requestCode == REQUEST_CREATE_FILE) {
            client.disconnect();
        }
    }

    /**
     * Create Folder
     */
    private void createNewFolder() {
        MetadataChangeSet metadata = new MetadataChangeSet.Builder()
                .setTitle("new folder")
                .build();

        Drive.DriveApi.getRootFolder(client)
                .createFolder(client, metadata)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(DriveFolder.DriveFolderResult driveFolderResult) {
                        if (!driveFolderResult.getStatus().isSuccess()) {
                            Log.e(TAG, "fail");
                            return;
                        }
                        Log.e(TAG, "successful");
                        client.disconnect();
                    }
                });
    }

    /**
     * 建立新檔案
     */
    private void CreateFile() {

        Drive.DriveApi.newDriveContents(client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                if (!driveContentsResult.getStatus().isSuccess()) {
                    Log.e(TAG, "faild");
                    return;
                }
                OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();
                try {
                    StringReader stringReader = new StringReader("hello world");
                    BufferedReader reader = new BufferedReader(stringReader);
                    String line = reader.readLine();

                    while (line != null) {
                        byte[] b = (line + "\n").getBytes("UTF8");
                        outputStream.write(b);
                        line = reader.readLine();
                    }
                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setMimeType("text/plain")
                            .setTitle("title")
                            .build();

                    IntentSender sender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(driveContentsResult.getDriveContents())
                            .build(client);

                    startIntentSenderForResult(sender, REQUEST_CREATE_FILE, null, 0, 0, 0);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("result", e.toString());
                    client.disconnect();
                }
            }
        });
    }

    /**
     * Search File
     */
    private void searchFile() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .build();

        Drive.DriveApi.query(client, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                for (int i = 0; i < metadataBufferResult.getMetadataBuffer().getCount(); i++) {
                    Log.e(TAG, metadataBufferResult.getMetadataBuffer().get(i).getTitle());
                }
                client.disconnect();
            }
        });
    }

    /**
     * Open File
     */
    private void openFile() {
        IntentSender sender = Drive.DriveApi.newOpenFileActivityBuilder()
                .setMimeType(new String[]{"text/plain", "text/csv"})
                .build(client);

        try {
            startIntentSenderForResult(sender, REQUEST_MAIN, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create Folder in new Folder
     */
    private void createFileInFolder() {
        final MetadataChangeSet metadata = new MetadataChangeSet.Builder()
                .setTitle("new folder")
                .build();
        final MetadataChangeSet mmetadata = new MetadataChangeSet.Builder()
                .setTitle("new file")
                .build();
        Drive.DriveApi.getRootFolder(client)
                .createFolder(client, metadata)
                .setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {
                    @Override
                    public void onResult(final DriveFolder.DriveFolderResult driveFolderResult) {
                        if (!driveFolderResult.getStatus().isSuccess()) {
                            Log.e(TAG, "fail");
                            return;
                        }
                        Drive.DriveApi.newDriveContents(client)
                                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                    @Override
                                    public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                                        driveFolderResult.getDriveFolder().getDriveId().asDriveFolder()
                                                .createFile(client, mmetadata, driveContentsResult.getDriveContents());
                                        client.disconnect();

                                    }
                                });

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt1:
                index = 1;
                break;
            case R.id.bt2:
                index = 2;
                break;
            case R.id.bt3:
                index = 3;
                break;
            case R.id.bt4:
                index = 4;
                break;
            case R.id.bt5:
                index = 5;
                break;
        }
        client.connect();
    }
}
