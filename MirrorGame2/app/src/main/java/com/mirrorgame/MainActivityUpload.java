package com.mirrorgame;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivityUpload extends AppCompatActivity {

    String filename, location,name,id;
    File path;
    DriveServiceHelper driveServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_upload);
        nameFileAndPath();
        try {
            readFile(filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectionToDrive(View view){
        requestSingIn();
    }

    private void readFile (String filename) throws IOException{
        Spinner spinner = (Spinner) findViewById(R.id.spinnerPoint);
        String read;
        List<String> arraySpinner = new ArrayList<String>();
        StringBuffer buffer = new StringBuffer();
        FileInputStream fis = new FileInputStream(new File(path, filename +".txt"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        if (fis!=null) {
            while ((read = reader.readLine()) != null) {
                //   buffer.append(Read + "\n" );
                arraySpinner.add(read.toString());
            }
        }
        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp);
        fis.close();
    }

    private void nameFileAndPath(){
        id = getIntent().getExtras().getString("Id");
        name = getIntent().getExtras().getString("Name");
        filename = getIntent().getExtras().getString("FileName");
        TextView showNameFile = findViewById(R.id.textViewNameFile);
        showNameFile.setText(filename);
        path = (File) getIntent().getExtras().getSerializable("FileLocation");
        //    String location1 = path.getAbsolutePath() +"/"+filename+".txt";
        //    showNameFile.setText(location1);
        location = path.getAbsolutePath() + "/" + filename + ".txt";
    }

    private void requestSingIn(){
        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), 200);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 200:
                if(resultCode == RESULT_OK){
                    handleSignInIntent(data);
                }
                break;
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential
                                .usingOAuth2(MainActivityUpload.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());
                        Drive googleDriveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(), credential)
                                .setApplicationName("Mirror Game").build();
                        driveServiceHelper = new DriveServiceHelper(googleDriveService);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void uploadFile(View view) {
        ProgressBar progressBar2 = (ProgressBar) findViewById(R.id.progressBarUpload2);
        Button btup = (Button) findViewById(R.id.buttonUpload);
        TextView showupload = (TextView) findViewById(R.id.textViewUpload);
        showupload.setText(R.string.uploading);
        if (driveServiceHelper != null) {
            String s;
            btup.setEnabled(false);
            progressBar2.setVisibility(View.VISIBLE);
            driveServiceHelper.createFolder(id).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            Toast.makeText(getApplicationContext(),R.string.createFolderSuccessfully , Toast.LENGTH_LONG).show();
                            driveServiceHelper.createFile(location, filename).addOnSuccessListener(new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            disButBar(progressBar2, btup);
                                            showupload.setText(R.string.uploadedSuccessfully);
                                            Toast.makeText(getApplicationContext(), R.string.uploadedSuccessfully, Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            disButBar(progressBar2, btup);
                                            showupload.setText(R.string.uploadedFail);
                                            Toast.makeText(getApplicationContext(), R.string.checkAPI, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            disButBar(progressBar2, btup);
                            Toast.makeText(getApplicationContext(), R.string.createFolderFail, Toast.LENGTH_LONG).show();
                        }
                    });
        }else{
            showupload.setText(R.string.driveServiceHelperFail);
            Toast.makeText(getApplicationContext(), R.string.driveServiceHelperFail, Toast.LENGTH_LONG).show();
        }
    }

    public void disButBar(ProgressBar progressBar2, Button btup){
        btup.setEnabled(true);
        progressBar2.setVisibility(View.INVISIBLE);
    }
    public void comfigureNextButton(View view){
        Intent secScreGa = new Intent(MainActivityUpload.this, MainActivity.class);
        secScreGa.putExtra("Name", name);
        secScreGa.putExtra("Id", id);
        startActivity(secScreGa);
        finish();
    }

    public void closeApp(View view) {
        finish();
        System.exit(0);
    }
}
