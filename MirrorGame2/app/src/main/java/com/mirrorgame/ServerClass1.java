package com.mirrorgame;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerClass1 extends Thread{
    ServerSocket serverSocket;
    Socket socket;
    TextView messageTextView;
    private InputStream inputStream;
    private OutputStream outputStream;
    Button nextButton, discover;
    boolean screenGame =false, stop = false;
    ImageView pointRed;
    String keyNextScreen,keybackScreen, time;
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    public ServerClass1(TextView messageTextView, String keyNextScreen, String keybackScreen, Button discover) {
        this.keyNextScreen = keyNextScreen;
        this.messageTextView = messageTextView;
        this.keybackScreen = keybackScreen;
        this.discover = discover;
        time = df.format(Calendar.getInstance().getTime());
        // messageTextView.setText(time.toString());
    }

    protected ServerClass1(Parcel in) {
        screenGame = in.readByte() != 0;
        stop = in.readByte() != 0;
        keyNextScreen = in.readString();
    }


    public void write(byte[] bytes){
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setViewtouch(TextView viewtouch) {
        this.messageTextView = viewtouch;
    }
    public void setPointRed(ImageView pointRed) {
        this.pointRed = pointRed;
    }
    public void setNextButton(Button nextButton) {
        this.nextButton = nextButton;
    }
    public void setScreenGame(boolean screenGame) {
        this.screenGame = screenGame;
    }
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        try {
            stop = false;
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
                while (socket!=null && !stop){
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes>0){
                            int finalBytes = bytes;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    String tempMsg = new String(buffer, 0, finalBytes);
                                    if (tempMsg.equals(keyNextScreen)) {
                                        if (!stop) {
                                            nextButton.performClick();
                                            if (screenGame) {
                                                stop = true;
                                                write(tempMsg.getBytes());
                                                close();
                                            }else{
                                                close();
                                            }
                                        }
                                    } else if (tempMsg.equals(keybackScreen)) {
                                        stop = true;
                                        discover.performClick();
                                        messageTextView.setText("");
                                        try {
                                            close();
                                        }catch (Exception e){
                                            Log.e("Server", "Server not shut down");
                                        }
                                    } else if (!screenGame) {
                                        messageTextView.setText(tempMsg);
                                    } else if (screenGame) {
                                        tempMsg = tempMsg.replace(" ", "");
                                        String[] data = tempMsg.split(",");
                                        float x;
                                        if (data[0].length() > 0) {
                                            x = Float.parseFloat(data[0]);
                                            if (x < 0) {
                                                x = 0;
                                            } else if (x > 1) {
                                                x = 1;
                                            }
                                            float xw = messageTextView.getX();
                                            float w = messageTextView.getWidth();
                                            float maxWidth = w + xw;
                                            x = x * maxWidth;
                                            String time = data[3];
                                            if (data[2].equals("BAD")) {
                                                pointRed(true, x, time);
                                            } else if (data[4].equals("ACTION_UP")) {
                                                pointRed(true, x, time);
                                            } else {
                                                pointRed(false, x, time);
                                            }
                                        }
                                    }
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

    public synchronized void pointRed(boolean INVISIBLE, float x, String timedata){
        try {
            if (df.parse(timedata).after(df.parse(this.time))) {
                if (INVISIBLE) {
                    pointRed.setVisibility(View.INVISIBLE);
                } else {
                    pointRed.setVisibility(View.VISIBLE);
                    pointRed.setX(x);
                }
                time = timedata;
                //pointRed.setVisibility(View.INVISIBLE);
            }else if(timedata.equals(time) && INVISIBLE){
                pointRed.setVisibility(View.INVISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public void close() {
        try {
            inputStream.close();
            outputStream.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

