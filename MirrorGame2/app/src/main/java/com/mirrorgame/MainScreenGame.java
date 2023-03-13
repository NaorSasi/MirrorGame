package com.mirrorgame;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class MainScreenGame extends AppCompatActivity {

    TextView nameid, viewtouch, Timer;
    String trail, name, id, fileName;
    int timeMillis;
    ImageView pointBlue, pointRed;
    File path;
    FileOutputStream write;
    float maxWidth;
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    ServerClass1 serverClass;
    ClientClass1 clientClass;
    boolean isHost, P2P=false;
    final int second = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_game);
        nameid = findViewById(R.id.nameAndId);
        getDataFromPreviousScreen();
        setNameid(name + " - " + id);
        nameid.setText(R.string.startwith);
        fileName = name+id;
        createFile(fileName);
        pointRed = findViewById(R.id.imageViewPointRed);
        pointBlue = findViewById(R.id.imageViewPointBlue);
        viewtouch = findViewById(R.id.textViewtouch);
        Timer = findViewById(R.id.editTime);


        ConstraintLayout myLayout = (ConstraintLayout) findViewById(R.id.screen2);
        if (trail.equals(getString(R.string.P2P))){
            Button buttonStart = findViewById(R.id.Start);
            buttonStart.performClick();
        }
        touchListener(myLayout);

    }

    public void start(View view) {
        Button buttonStart = findViewById(R.id.Start);
        buttonStart.setVisibility(View.INVISIBLE);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            maxWidth = dm.widthPixels;
            try {
                file(trail);
                Toast.makeText(getApplicationContext(), getString(R.string.readFile) + " " + trail, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void file (String file) throws IOException {
        int idFile=-1;
//        String[] trailData = {getString(R.string.dot1),getString(R.string.dot2),getString(R.string.dot3),getString(R.string.dot4),getString(R.string.dot5),getString(R.string.dot6),getString(R.string.dot7),getString(R.string.dot8),getString(R.string.dot9),getString(R.string.dot10),getString(R.string.dot11) };
/*        switch (file) {
            case "dot1":
                idFile = R.raw.dot1;
                break;
            case "dot2":
                idFile = R.raw.dot2;
                break;
            case "dot3":
                idFile = R.raw.dot3;
                break;
            case "dot4":
                idFile = R.raw.dot4;
                break;
            case "dot5":
                idFile = R.raw.dot5;
                break;
            case "dot6":
                idFile = R.raw.dot6;
                break;
            case "dot7":
                idFile = R.raw.dot7;
                break;
            case "dot8":
                idFile = R.raw.dot8;
                break;
            case "dot9":
                idFile = R.raw.dot9;
                break;
            case "dot10":
                idFile = R.raw.dot10;
                break;
            case "dot11":
                idFile = R.raw.dot11;
                break;
            default:
                Toast.makeText(getApplicationContext(), "File does not exist: " + file, Toast.LENGTH_SHORT).show();
        }*/

        if (file.equals(getString(R.string.dot1))) {
            idFile = R.raw.dot1;
        }else if (file.equals(getString(R.string.dot2))) {
            idFile = R.raw.dot2;
        }else if (file.equals(getString(R.string.dot3))) {
            idFile = R.raw.dot3;
        }else if (file.equals(getString(R.string.dot4))) {
            idFile = R.raw.dot4;
        }else if (file.equals(getString(R.string.dot5))) {
            idFile = R.raw.dot5;
        }else if (file.equals(getString(R.string.dot6))) {
            idFile = R.raw.dot6;
        }else if (file.equals(getString(R.string.dot7))) {
            idFile = R.raw.dot7;
        }else if (file.equals(getString(R.string.dot8))) {
            idFile = R.raw.dot8;
        }else if (file.equals(getString(R.string.dot9))) {
            idFile = R.raw.dot9;
        }else if (file.equals(getString(R.string.dot10))) {
            idFile = R.raw.dot10;
        }else if (file.equals(getString(R.string.dot11))) {
            idFile = R.raw.dot11;
        }else if(file.equals(getString(R.string.P2P))){
            String sOc = getIntent().getExtras().getString("serverOrclient");
            Button stop = findViewById(R.id.buttonStop);
            P2P= true;
            if (sOc.equals(getString(R.string.host))) {
                isHost = true;
                //serverClass = new ServerClass1(viewtouch, getString(R.string.keyNextScreen));
                serverClass = getIntent().getParcelableExtra("server");
                //serverClass =(ServerClass1) getIntent().getSerializableExtra("server");
                serverClass.setNextButton(stop);
                serverClass.setScreenGame(true);
                serverClass.setPointRed(pointRed);
                serverClass.setViewtouch(viewtouch);
                //serverClass.setStop(false);
                serverClass.start();

            } else if (sOc.equals(getString(R.string.client))) {
                isHost = false;
                //InetAddress hostAddress = (InetAddress) getIntent().getExtras().getSerializable("client");
                //clientClass = new ClientClass1(hostAddress,  viewtouch, stop, getString(R.string.keyNextScreen));
                clientClass = getIntent().getParcelableExtra("client");
                //clientClass = (ClientClass1) getIntent().getSerializableExtra("client");
                clientClass.setNextButton(stop);
                clientClass.setScreenGame(true);
                clientClass.setPointRed(pointRed);
                clientClass.setViewtouch(viewtouch);
                //clientClass.setStop(false);
                clientClass.start();
            }
        }else {
            Toast.makeText(getApplicationContext(), "File does not exist: " + file, Toast.LENGTH_SHORT).show();
        }

        if (idFile != -1) {
            int fIdFile = idFile;
            pointRed.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    timerStart();
                    // readFile(idFile);
                    InputStream is = getResources().openRawResource(fIdFile);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                    String line = "";
                    float x;
                    try {
                        while ((line = reader.readLine()) != null) {
                            String[] data = line.split(",");
                            if (data[0].length() > 0) {
                                x = Float.parseFloat(data[0]);
                                if (x < 0) {
                                    x = 0;
                                } else if (x > 1) {
                                    x = 1;
                                }
                                x = x * maxWidth;
                                pointRed.setX(x);
                                Thread.sleep(timeMillis);
                            }
                        }
                        pointRed.setVisibility(View.INVISIBLE);
                    } catch (IOException e) {
                        Log.e("Read File", "Error reading data file on line " + line, e);
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    private void timerStart(){
        Timer.setVisibility(View.VISIBLE);
        int startsec = 0;
        while(startsec<=second){
            try {
                Timer.setText(R.string.startwith + " " + (second - startsec) + " " + R.string.seconds);
                startsec++;
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        Timer.setVisibility(View.INVISIBLE);
    }
    /*
    public void readFile (int idFile){
        InputStream is = getResources().openRawResource(idFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        float x;
        try {
            while ((line = reader.readLine()) != null) {
//                TimeUnit.MILLISECONDS.sleep(1);
                String[] data = line.split(",");
                if (data[0].length()>0) {
                    x = Float.parseFloat(data[0]);
                    if (x<0){
                        x=0;
                    }else if (x>1){
                        x=1;
                    }
                    x = x * maxWidth;
                    redPoint(x);
                }
            }
            nameid.setText(line);
        } catch (IOException e) {
            Log.e("Read File","Error reading data file on line " + line, e);
            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        pointRed.setVisibility(View.INVISIBLE);
    }

    private void redPoint(float x){
        pointRed.setVisibility(View.VISIBLE);
        pointRed.setX(x);
    }

     */
    private void createFile(String fileName){
        path = getApplicationContext().getFilesDir();
        try{
            write = new FileOutputStream(new File(path, fileName +".txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveWrite(View view, float x, float y) {
        float yh = viewtouch.getY();
        float h = viewtouch.getHeight();
        float xw = viewtouch.getX();
        float w = viewtouch.getWidth();
        float maxHeight = h + yh;
        maxWidth = w + xw;
        String location = x/maxWidth + "," + y/maxHeight;
        if(x>=xw && x<=xw+w && y>=yh && y<=yh+h){
            pointBlue.setVisibility(View.VISIBLE);
            pointBlue.setX(x);
            location += ", OK, ";
            if (P2P){
                sendP2P(location);
            }
        }else{
            pointBlue.setVisibility(View.INVISIBLE);
            location += ", BAD, ";
        }
        location += df.format(Calendar.getInstance().getTime()) + "\n";
        try {
            write.write(location.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendP2P(String msg) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (msg != null && isHost) {
                    serverClass.write(msg.getBytes());
                } else if (msg != null && !isHost) {
                    clientClass.write(msg.getBytes());
                }
            }
        });
    }
    private void getDataFromPreviousScreen(){
        name = getIntent().getExtras().getString("Name");
        id = getIntent().getExtras().getString("Id");
        trail = getIntent().getExtras().getString("trail");
        timeMillis = Integer.parseInt(getIntent().getExtras().getString("timeMillis"));
    }

    private void setNameid(String val){
        nameid.setText(val);
    }

    private void touchListener(View view){
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = (float) motionEvent.getX();
                float y = (float) motionEvent.getY();
                saveWrite(view,x, y);
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("TochEvents", "Action Down");
                        break;
                    case  MotionEvent.ACTION_UP:
                        Log.i("TochEvents", "Action Up");
                        pointBlue.setVisibility(View.INVISIBLE);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("TochEvents", "Action Move (" + x +"' "+ y+ ")");
                        break;
                }

                return true;
            }
        });
    }

    public void comfigureUploadScreen(View view){
        if (P2P){
            sendP2P(getString(R.string.keyNextScreen));
        }
        if (P2P && isHost){
            serverClass.setStop(true);
        }else if (P2P){
            clientClass.setStop(true);
        }
        Intent secScreGa = new Intent(MainScreenGame.this, MainActivityUpload.class);
        try{
            write.close();
            Toast.makeText(getApplicationContext(),getString(R.string.nameFile) +": " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        secScreGa.putExtra("Id", getIntent().getExtras().getString("Id"));
        secScreGa.putExtra("Name", name);
        secScreGa.putExtra("FileName", fileName);
        secScreGa.putExtra("FileLocation", path);
        startActivity(secScreGa);
        finish();
    }
}

