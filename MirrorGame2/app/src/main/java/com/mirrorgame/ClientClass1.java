package com.mirrorgame;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientClass1 extends Thread {
    String hostAdd, keyNextScreen, keybackScreen, time;
    Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    TextView messageTextView;
    Button nextButton, discover;
    boolean screenGame = false, stop = false;
    ImageView pointRed;
    InetAddress hostAddress;
    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");


    public ClientClass1(InetAddress hostAddress, TextView messageTextView, Button nextButton, String keyNextScreen, String keybackScreen, Button discover) {
        this.hostAddress = hostAddress;
        this.nextButton = nextButton;
        hostAdd = hostAddress.getHostAddress();
        socket = new Socket();
        this.messageTextView = messageTextView;
        this.keyNextScreen = keyNextScreen;
        this.keybackScreen = keybackScreen;
        this.discover = discover;
        time = df.format(Calendar.getInstance().getTime());
    }

    protected ClientClass1(Parcel in) {
        hostAdd = in.readString();
        keyNextScreen = in.readString();
        screenGame = in.readByte() != 0;
        stop = in.readByte() != 0;
    }


    public void write(byte[] bytes) {
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InetAddress getHostAddress() {
        return hostAddress;
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
            socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
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
                while (socket != null && !stop) {
                    try {
                        bytes = inputStream.read(buffer);
                        if (bytes > 0) {
                            int finalBytes = bytes;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    String tempMSG = new String(buffer, 0, finalBytes);

                                    if (tempMSG.equals(keyNextScreen)) {
                                        if (!stop) {
                                            if (screenGame) {
                                                stop = true;
                                                write(tempMSG.getBytes());
                                                close();
                                            }
                                            nextButton.performClick();
                                        }else{
                                            close();
                                        }
                                    } else if (tempMSG.equals(keybackScreen)) {
                                        // if (!stop) {
                                        stop = true;
                                        discover.performClick();
                                        messageTextView.setText("");
                                        close();
                                        //    write(tempMSG.getBytes());
                                        // }
                                    } else if (!screenGame) {
                                        messageTextView.setText(tempMSG);
                                    } else if (screenGame) {
                                        tempMSG = tempMSG.replace(" ", "");
                                        String[] data = tempMSG.split(",");
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

    public synchronized void pointRed(boolean INVISIBLE, float x, String timedata) {
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
            } else if (timedata.equals(time) && INVISIBLE) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
