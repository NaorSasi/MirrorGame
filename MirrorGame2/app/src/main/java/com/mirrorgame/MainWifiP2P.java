package com.mirrorgame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.InetAddresses;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainWifiP2P extends AppCompatActivity {
    String name, id, time="0";

    Button aSwitch, discoverButton, sendButton, nextButton;
    ListView listView;
    TextView messageTextView, connectionStatus;
    EditText typeMsg;
    Thread thread;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    Socket socket;
    //ServerClass serverClass;
    //ClientClass clientClass;
    ServerClass1 serverClass;
    ClientClass1 clientClass;
    boolean isHost, nextScreen=false, stopThred=false, backScreen= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wifi_p2_p);
        getDataFromPreviousScreen();
        connectingComponents();
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
                        connectionStatus.setText(getString(R.string.connected)+": "+device.deviceAddress);
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
                if (nextScreen){
                    msg =getString(R.string.keyNextScreen);
                }else if(backScreen){
                    msg = getString(R.string.keyBackScreen);
                }else {
                    msg = typeMsg.getText().toString();
                    typeMsg.setText("");
                }
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if(msg!=null && isHost){
                            serverClass.write(msg.getBytes());
                        }else if (msg!=null && !isHost){
                            clientClass.write(msg.getBytes());
                        }
                    }
                });
            }
        });
    }


    private void connectingComponents() {
        nextButton = (Button) findViewById(R.id.buttonNext);
        aSwitch =(Button) findViewById(R.id.onOff);
        discoverButton = (Button) findViewById(R.id.discover);
        sendButton = (Button) findViewById(R.id.sendButton);
        listView= (ListView) findViewById(R.id.peerListView);
        messageTextView = (TextView) findViewById(R.id.readMsg);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        typeMsg = (EditText) findViewById(R.id.writeMsg);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.equals(peers)){
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
                int index = 0;
                for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                listView.setAdapter(adapter);
                if (peers.size() == 0){
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
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connectionStatus.setText(getString(R.string.host));
                isHost = true;
                stopThred = true;
                //serverClass = new ServerClass();
                serverClass = new ServerClass1(messageTextView, getString(R.string.keyNextScreen),getString(R.string.keyBackScreen), discoverButton);
                thread = new Thread(serverClass);
                thread.start();
                sendButton.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
            }else if (wifiP2pInfo.groupFormed){
                connectionStatus.setText(getString(R.string.client));
                isHost = false;
                stopThred = true;
                //clientClass = new ClientClass(groupOwnerAddress);
                clientClass = new ClientClass1(groupOwnerAddress, messageTextView, nextButton,getString(R.string.keyBackScreen), getString(R.string.pointRedINVISIBLEN), discoverButton);
                thread = new Thread(clientClass);
                thread.start();
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


    private void getDataFromPreviousScreen(){
        name = getIntent().getExtras().getString("Name");
        id = getIntent().getExtras().getString("Id");
    }

    public void comfigureNextButton(View view) {
        if(isHost && !nextScreen) {
            nextScreen = true;
            sendButton.performClick();
        }

        Intent secScreGa = new Intent(MainWifiP2P.this, MainScreenGame.class);
       // Bundle bundle = new Bundle();
        if (isHost){
            secScreGa.putExtra("serverOrclient", getString(R.string.host));
            //bundle.putSerializable("server", serverClass);
            //secScreGa.putExtras(bundle);
         //   secScreGa.putExtra("server", serverClass);
            //serverClass.setStop(true);
        }else{
            secScreGa.putExtra("serverOrclient", getString(R.string.client));
            //secScreGa.putExtra("client", clientClass.getHostAddress());
            //bundle.putSerializable("client", serverClass);
            //secScreGa.putExtras(bundle);
         //   secScreGa.putExtra("client", clientClass);
            //clientClass.setStop(true);
        }
        secScreGa.putExtra("trail", getString(R.string.P2P))
        .putExtra("Name", name)
        .putExtra("Id", id)
        .putExtra("timeMillis", time);

        startActivity(secScreGa);
        finish();
    }

    public void comfigurePreviousButton(View view) {
        if (stopThred){
            backScreen = true;
            sendButton.performClick();
            if(isHost){
                serverClass.setStop(true);
            }else{
                clientClass.setStop(true);
            }
        }
        Intent secScreGa = new Intent(MainWifiP2P.this, MainActivity.class);
        secScreGa.putExtra("Name", name);
        secScreGa.putExtra("Id", id);
        startActivity(secScreGa);
        finish();

    }

/*    public class ServerClass extends Thread{
        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler((Looper.getMainLooper()));
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    while (socket!=null){
                        try {
                            bytes = inputStream.read(buffer);
                            if (bytes>0){
                                int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMsg = new String(buffer, 0, finalBytes);
                                        messageTextView.setText(tempMsg);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    public class ClientClass extends  Thread{
        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }
        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888),500);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    while(socket!=null){
                        try {
                            bytes = inputStream.read(buffer);
                            if (bytes>0){
                                int finalBytes = bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer, 0, finalBytes);
                                        messageTextView.setText(tempMSG);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }*/
}