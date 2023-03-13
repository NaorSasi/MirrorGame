package com.mirrorgame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private MainScreenP2P mActivity1;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainWifiP2P mActivity;
    private boolean screenAndP2P;

    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainWifiP2P mActivity){
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity = mActivity;
        screenAndP2P = false;

    }
    public WiFiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainScreenP2P mActivity){
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mActivity1 = mActivity;
        screenAndP2P = true;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                if (screenAndP2P) {
                    mManager.requestPeers(mChannel, mActivity1.peerListListener);
                } else {
                    mManager.requestPeers(mChannel, mActivity.peerListListener);
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    if (screenAndP2P) {
                        mManager.requestConnectionInfo(mChannel, mActivity1.connectionInfoListener);
                    } else {
                        mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
                    }
                } else {
                    if (screenAndP2P) {
                        mActivity1.connectionStatus.setText(R.string.notConnected);
                    } else {
                        mActivity.connectionStatus.setText(R.string.notConnected);
                    }
                }
            }
        }
    }

}
