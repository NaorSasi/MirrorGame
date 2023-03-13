package com.mirrorgame;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    //time
    Calendar c = Calendar.getInstance();
    Date d = c.getTime();
    SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss.SSS");
    String currentDate = df.format(d);

    String folderKey;

    private  final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    public DriveServiceHelper (Drive mDriveService){
        this.mDriveService = mDriveService;
    }

    public Task<String> createFile (String filePath, String name) {
        return Tasks.call(mExecutor, () -> {

            File fileMetaData = new File();
            fileMetaData.setName(name + "-" + currentDate);
            fileMetaData.setDescription("Naor");
            fileMetaData.setMimeType("text/plain");
            fileMetaData.setParents(Collections.singletonList(folderKey));
            java.io.File file = new java.io.File(filePath);
            FileContent mediaContent = new FileContent("text/plain", file);
            File upFile = null;
            try {
                upFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (upFile == null) {
                throw new Exception("Null result When requesting file creation");
            }
            return upFile.getId();
        });
    }

    public Task<String> createFolder(String id) {
        try {
            Drive.Files.List result = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and name='"+id+"'").setSpaces("drive");
//            FileList file = result.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Tasks.call(mExecutor, () -> {
            File folderMetadata = new File();
            folderMetadata.setName(id);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
    //        folderMetadata.setParents(Collections.singletonList(folderKey את התקיית אב));
            File myFolder = null;
            try {
                myFolder = mDriveService.files().create(folderMetadata)
                        .setFields("id")
                        .execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (myFolder == null) {
                throw new IOException("Null result when requesting folder creation");
            }
            folderKey = myFolder.getId();
            return folderKey;
        });
    }

}
