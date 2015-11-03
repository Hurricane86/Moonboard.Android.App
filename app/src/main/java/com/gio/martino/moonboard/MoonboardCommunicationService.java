package com.gio.martino.moonboard;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MoonboardCommunicationService extends Service {

    public class LocalBinder extends Binder {
        MoonboardCommunicationService getService() {
            return MoonboardCommunicationService.this;
        }
    }

    private class ConnectThread extends Thread {
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        @Override
        public void run()
        {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null)
                return;

            do
            {
                if (!bluetoothAdapter.isEnabled())
                    bluetoothAdapter.enable();

                bluetoothAdapter.startDiscovery();
                do
                {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                while(bluetoothAdapter.isDiscovering());

                do
                {
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice d : pairedDevices)
                        {
                            if (d.getName().equals(deviceName))
                            {
                                device = d;
                                break;
                            }
                        }
                    }

                    try
                    {
                        Thread.sleep(500);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
                while(device == null);

                try
                {
                    socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                }
                catch(IOException e)
                {
                }

                if(bluetoothAdapter.isDiscovering())
                    bluetoothAdapter.cancelDiscovery();

                try
                {
                    socket.connect();
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    connectedThread = new ConnectedThread();
                    connectedThread.start();

                    break;
                }
                catch(IOException connectException)
                {
                }

                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                }

                try {
                    socket.close();
                } catch (IOException e) {}
                device = null;

            }while(!socket.isConnected());

        }
    }

    private class ConnectedThread extends Thread
    {
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            while (true)
            {
                try
                {
                    bytes += inputStream.read(buffer, bytes, buffer.length - bytes);

                    if(bytes >= 6)
                    {
                        //@TODO: check HEADER bytes...
                        //...
                        byte messageType = buffer[4];
                        byte messageLength = buffer[5];

                        if(bytes - 6 >= messageLength)
                        {
                            messageHandler.obtainMessage(messageType, 0, 0, buffer).sendToTarget();

                            int begin = 6+messageLength;
                            System.arraycopy(buffer.clone(), begin, buffer, 0, buffer.length - begin );
                            bytes -= begin;
                        }
                    }
                }
                catch (IOException e)
                {
                    break;
                }
            }
        }
    }

    private final IBinder binder = new LocalBinder();
    private final String deviceName = "Moonboard";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private volatile OutputStream outputStream;
    private volatile InputStream inputStream;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private Handler messageHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(messageReceiver != null)
                messageReceiver.onMessageReceived(msg.what, (byte[]) msg.obj);
        }
    };

    public interface MessageReceiver
    {
        void onMessageReceived(int messageType, byte[] buffer);
    }

    public static byte MESSAGE_TYPE_SET_PROBLEM = 0x01;
    public static byte MESSAGE_TYPE_SET_COLORS = 0x02;
    public MessageReceiver messageReceiver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        refresh();
    }

    public void refresh()
    {
        if(connectThread == null)
            connectThread = new ConnectThread();

        if(!connectThread.isAlive())
            connectThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        closeBT();

        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        closeBT();

        super.onTaskRemoved(rootIntent);
    }

    private void closeBT()
    {
        try
        {
            if(outputStream != null)
                outputStream.close();
            if(inputStream != null)
                inputStream.close();
            if(socket != null)
                socket.close();

            bluetoothAdapter.disable();

        }
        catch (IOException ex)
        {
        }
    }

    public boolean isAlive()
    {
        return outputStream != null && inputStream != null;
    }

    public boolean send(byte messageType, byte[] payload) {
        //Assert.assertTrue(payload.length <= 64);

        // first 4 bytes are the header
        // 5th byte: message type
        // 6th byte: payload size
        if (!send(new byte[]
                {(byte) 0xEF,
                        (byte) 0xFE,
                        (byte) 0xFF,
                        (byte) 0xAA,

                        messageType,
                        (byte) payload.length
                }))
            return false;

        if (!send(payload))
            return false;

        return true;
    }

    private boolean send(byte[] data)
    {
        if (!isAlive())
            return false;

        try
        {
            outputStream.write(data);
            outputStream.flush();
        }
        catch (IOException ex)
        {
            return false;
        }

        return true;
    }

}
