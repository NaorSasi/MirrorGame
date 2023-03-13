package com.mirrorgame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainScreenP2P extends AppCompatActivity {

    ServerClass1 serverClass;
    ClientClass1 clientClass;
    TextView nameid, viewtouch, messageTextView, connectionStatus, Timer;
    String trail, name, id, fileName, time = "0", screen = "NaorSasi", nameDotNow;
    int timeMillis, numberforDot;
    ImageView pointBlue, pointRed;
    File path;
    FileOutputStream write;
    float maxWidth, xRed, yRed;
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
    boolean isHost, P2P = false, getData = false, startrun= true, local = true,waitforend = false;
    //final int second = 10;
    int second = 10;
    Button aSwitch, discoverButton, sendButton, nextButton;
    ListView listView;
    EditText typeMsg;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray, listTrail;
    WifiP2pDevice[] deviceArray;
    FirebaseDatabase database;
    DatabaseReference myrefDot;
    boolean nextScreen = false, stopThred = false, backScreen = false;
    Bundle saveState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        saveState = savedInstanceState;
        super.onCreate(savedInstanceState);
        connectingComponents();
        database = FirebaseDatabase.getInstance();
        myrefDot = database.getReference().child("Dotlist");

        // setContentView(R.layout.activity_main_screen_game);
       // setContentView(R.layout.activity_main_wifi_p2_p);
        if (screen.equals("NaorSasi")){
            screen = getIntent().getExtras().getString("screen");
        }
        if (screen.equals(getString(R.string.WifiP2P))){
            mainWifiP2P();
        }else if(screen.equals(getString(R.string.screenGame))){
            mainScreenGame();
        }
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (screen.equals(getString(R.string.screenGame))) {
            mainScreenGame();
        }
        // setContentView(R.layout.activity_main_screen_game);
    }

    private void palette (int sOp){
        if (sOp == 1){
            nameid = findViewById(R.id.nameAndId);
            pointRed = findViewById(R.id.imageViewPointRed);
            pointBlue = findViewById(R.id.imageViewPointBlue);
            viewtouch = findViewById(R.id.textViewtouch);
            Timer = findViewById(R.id.editTime);

        }else if(sOp == 2){
            nextButton = (Button) findViewById(R.id.buttonNext);
            aSwitch = (Button) findViewById(R.id.onOff);
            discoverButton = (Button) findViewById(R.id.discover);
            sendButton = (Button) findViewById(R.id.sendButton);
            listView = (ListView) findViewById(R.id.peerListView);
            messageTextView = (TextView) findViewById(R.id.readMsg);
            connectionStatus = (TextView) findViewById(R.id.connectionStatus);
            typeMsg = (EditText) findViewById(R.id.writeMsg);
        }
    }

    private void mainScreenGame() {
        //screen = (getString(R.string.WifiP2P));
        setContentView(R.layout.activity_main_screen_game);
        palette(1);
        getDataFromPreviousScreen();
        setNameid(name + " - " + id);
        fileName = name + id;
        createFile(fileName);
        ConstraintLayout myLayout = (ConstraintLayout) findViewById(R.id.screen2);
        if (trail.equals(getString(R.string.P2P))) {
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
            if (local) {
                file(trail);
            }else{
                runFirebase();
            }
            Toast.makeText(getApplicationContext(), getString(R.string.readFile) + " " + trail, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void runFirebase() {
        if (listTrail == null) {
            Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (String dot : listTrail) {
                        while (waitforend) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        waitforend = true;
                        DataDot(dot);
                    }
                    Button stop = findViewById(R.id.buttonStop);
                    stop.performClick();
                }
            }).start();
        }
    }
    private void DataDot(String dot) {
        DatabaseReference ref = myrefDot.child(dot).child("Points");
        Query query = ref.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float[] dotDataX = getXfirebase(dataSnapshot);
                nameDotNow = dot;
                numberforDot = 0;
                runRedPointDB(dotDataX);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
                Log.e("Error Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }
    private void runRedPointDB(float[] dataX) {
        timerStart();
        pointRed.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // readFile(idFile);
                while (!startrun) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    for (float x : dataX) {
                        if (x < 0) {
                            x = 0;
                        } else if (x > 1) {
                            x = 1;
                        }
                        x = x * maxWidth;
                        pointRed.setX(x);
                        xRed = x;
                        yRed = pointRed.getY();
                        Thread.sleep(timeMillis);
                    }
                    startrun = false;
                    pointRed.setVisibility(View.INVISIBLE);
                } catch (InterruptedException e) {
                    Log.e("firebase", "Error data from firebase", e);
                    e.printStackTrace();
                }
                waitforend = false;
            }
        }).start();
    }

    private float[] getXfirebase(DataSnapshot dataSnapshot){
        int len = (int)dataSnapshot.getChildrenCount();
        float[] dataFirebase = new float[len];
        float value;
        int i = 0;
        for(DataSnapshot child: dataSnapshot.getChildren()){
            DataSnapshot x = child.child("Item1");
            value = x.getValue(float.class);
            Log.i("Firebase", "Value is: " + value);
            dataFirebase[i++] = value;
        }
        return dataFirebase;
    }
    private void firebase(String trail){
        DatabaseReference myref =database.getReference().child("Traillist").child(trail).child("keydot");
        Query query = myref.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listTrail = firebasechange(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();

                Log.e("Error Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }
    private String[] firebasechange(DataSnapshot dataSnapshot){
        int len = (int)dataSnapshot.getChildrenCount();
        String[] dataFirebase = new String[len];
        String value;
        int i = 0;
        for(DataSnapshot child: dataSnapshot.getChildren()){
            value = (String)(child.getValue());
            Log.i("Firebase", "Value is: " + value);
            dataFirebase[i++] = value;
        }
        return dataFirebase;
    }
    private void file(String file) throws IOException {
        int idFile = -1;
        if (file.equals(getString(R.string.dot1))) {
            idFile = R.raw.dot1;
        } else if (file.equals(getString(R.string.dot2))) {
            idFile = R.raw.dot2;
        } else if (file.equals(getString(R.string.dot3))) {
            idFile = R.raw.dot3;
        } else if (file.equals(getString(R.string.dot4))) {
            idFile = R.raw.dot4;
        } else if (file.equals(getString(R.string.dot5))) {
            idFile = R.raw.dot5;
        } else if (file.equals(getString(R.string.dot6))) {
            idFile = R.raw.dot6;
        } else if (file.equals(getString(R.string.dot7))) {
            idFile = R.raw.dot7;
        } else if (file.equals(getString(R.string.dot8))) {
            idFile = R.raw.dot8;
        } else if (file.equals(getString(R.string.dot9))) {
            idFile = R.raw.dot9;
        } else if (file.equals(getString(R.string.dot10))) {
            idFile = R.raw.dot10;
        } else if (file.equals(getString(R.string.dot11))) {
            idFile = R.raw.dot11;
        } else if (file.equals(getString(R.string.P2P))) {
            Button stop = findViewById(R.id.buttonStop);
            P2P = true;
            if (isHost) {
                serverClass.setNextButton(stop);
                serverClass.setScreenGame(true);
                serverClass.setPointRed(pointRed);
                serverClass.setViewtouch(viewtouch);

            } else {
                clientClass.setNextButton(stop);
                clientClass.setScreenGame(true);
                clientClass.setPointRed(pointRed);
                clientClass.setViewtouch(viewtouch);
            }
        } else {
            Toast.makeText(getApplicationContext(), "File does not exist: " + file, Toast.LENGTH_SHORT).show();
        }
        if (idFile != -1) {
            runRedPointFile(idFile);
        }
    }
    public void runRedPointFile(int idFile) {
        timerStart();
        pointRed.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // readFile(idFile);
                while (!startrun) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                InputStream is = getResources().openRawResource(idFile);
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
                            xRed = x;
                            yRed = pointRed.getY();
                            Thread.sleep(timeMillis);
                        }
                    }
                    startrun = false;
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

    private void createFile(String fileName) {
        path = getApplicationContext().getFilesDir();
        try {
            write = new FileOutputStream(new File(path, fileName + ".txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveWrite(float x, float y, String move) {
        if(startrun) {
            float yh = viewtouch.getY();
            float h = viewtouch.getHeight();
            float xw = viewtouch.getX();
            float w = viewtouch.getWidth();
            float maxHeight = h + yh;
            maxWidth = w + xw;
            String location = xRed/ maxWidth + "," + yRed/y / maxHeight + "," + x / maxWidth + "," + y / maxHeight;
            if (x >= xw && x <= xw + w && y >= yh && y <= yh + h) {
                pointBlue.setVisibility(View.VISIBLE);
                pointBlue.setX(x);
                location += ", OK, ";
            } else {
                pointBlue.setVisibility(View.INVISIBLE);
                location += ", BAD, ";
            }

            location += df.format(Calendar.getInstance().getTime());
            String loc = location + "," + move;
            if (!local){
                database.getReference().child("Test").child(trail).child(fileName).child(nameDotNow).child(numberforDot+"").setValue(location);
                numberforDot++;
            }
            location += "\n";
            if (P2P) {
                sendP2P(loc);
            }
            if (move.equals("ACTION_UP")) {
                pointBlue.setVisibility(View.INVISIBLE);
                if (P2P) {
                    sendP2P(loc);
                }
            }
            try {
                write.write(location.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    private void getDataFromPreviousScreen() {
        if (!getData) {
            getData = true;
            name = getIntent().getExtras().getString("Name");
            id = getIntent().getExtras().getString("Id");
            trail = getIntent().getExtras().getString("trail");
            local = (boolean) getIntent().getExtras().getSerializable("local");
            timeMillis = Integer.parseInt(getIntent().getExtras().getString("timeMillis"));
            second = Integer.parseInt(getIntent().getExtras().getString("timer"));
            if (!local){
                firebase(trail);
            }
        }
    }
    private void getDataFromPreviousScreen1() {
        if(!getData) {
            getData = true;
            name = getIntent().getExtras().getString("Name");
            id = getIntent().getExtras().getString("Id");
            trail = getString(R.string.P2P);
        }
    }

    private void setNameid(String val) {
        nameid.setText(val);
    }

    private void touchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = (float) motionEvent.getX();
                float y = (float) motionEvent.getY();
                String move = null;
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i("TochEvents", "Action Down (" + x + "' " + y + ")");
                        move = "ACTION_DOWN";
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i("TochEvents", "Action Up (" + x + "' " + y + ")");
                        move = "ACTION_UP";
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i("TochEvents", "Action Move (" + x + "' " + y + ")");
                        move = "ACTION_MOVE";
                        break;

                }

                saveWrite(x, y, move);
                return true;
            }
        });
    }
/*
    private void pointRedINVISIBLE(){
        if (P2P) {
            String redINVISIBLEN = "-1,-1,BAD, ns";
            sendP2P(redINVISIBLEN);
        }
    }
*/
    public void comfigureUploadScreen(View view) {
        if (P2P) {
            sendP2P(getString(R.string.keyNextScreen));
        }
        if (P2P && isHost) {
            serverClass.setStop(true);
        } else if (P2P) {
            clientClass.setStop(true);
        }
        Intent secScreGa = new Intent(MainScreenP2P.this, MainActivityUpload.class);
        try {
            write.close();
            Toast.makeText(getApplicationContext(), getString(R.string.nameFile) + ": " + fileName, Toast.LENGTH_SHORT).show();
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

    private void mainWifiP2P() {
        setContentView(R.layout.activity_main_wifi_p2_p);
        getDataFromPreviousScreen1();
        palette(2);
        exqListener();
    }

    private void exqListener() {
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(intent, 1);
            }
        });

        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButton.setVisibility(View.INVISIBLE);
                sendButton.setVisibility(View.INVISIBLE);
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText(getString(R.string.DiscoveryStarted));
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText(getString(R.string.DiscoverynotStarted));
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final WifiP2pDevice device = deviceArray[i];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText(getString(R.string.connected) + ": " + device.deviceAddress);
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText(getString(R.string.notConnected));
                    }
                });
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                String msg;
                if (nextScreen) {
                    msg = getString(R.string.keyNextScreen);
                } else if (backScreen) {
                    backScreen = false;
                    msg = getString(R.string.keyBackScreen);
                } else {
                    msg = typeMsg.getText().toString();
                    typeMsg.setText("");
                }
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
        });
    }


    private void connectingComponents() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.equals(peers)) {
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
                if (peers.size() == 0) {
                    connectionStatus.setText(getString(R.string.noDeviceFound));
                    return;
                }
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                connectionStatus.setText(getString(R.string.host));
                isHost = true;
                stopThred = true;
                //serverClass = new ServerClass();
                serverClass = new ServerClass1(messageTextView, getString(R.string.keyNextScreen),getString(R.string.keyBackScreen), discoverButton);
                serverClass.start();
                sendButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            } else if (wifiP2pInfo.groupFormed) {
                connectionStatus.setText(getString(R.string.client));
                isHost = false;
                stopThred = true;
                //clientClass = new ClientClass(groupOwnerAddress);
                clientClass = new ClientClass1(groupOwnerAddress, messageTextView, nextButton, getString(R.string.keyNextScreen),getString(R.string.keyBackScreen), discoverButton);
                clientClass.start();
                sendButton.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


    public void comfigureNextButton(View view) {
        if (isHost && !nextScreen) {
            nextScreen = true;
            sendButton.performClick();
        }
        screen = getString(R.string.screenGame);
        onRestart();
    }

    public void comfigurePreviousButton(View view) {
        if (stopThred) {
            backScreen = true;
            sendButton.performClick();
            if (isHost) {
                serverClass.setStop(true);
            } else {
                clientClass.setStop(true);
            }
        }
        Intent secScreGa = new Intent(MainScreenP2P.this, MainActivity.class);
        secScreGa.putExtra("Name", name);
        secScreGa.putExtra("Id", id);
        startActivity(secScreGa);
        finish();
    }

    private void timerStart() {
        Timer.setText("");
        Timer.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!startrun) {
                    int startsec = 0;
                    while (startsec <= second) {
                        try {
                            Timer.setText(getString(R.string.startwith) + " " + (second - startsec) + " " + getString(R.string.seconds));
                            startsec++;
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Timer.setVisibility(View.INVISIBLE);
                    startrun = true;
                }
            }
        }).start();
    }
}