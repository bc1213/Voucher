package com.g12.apple.appleTvLogin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import com.g12.apple.appleTvLogin.utils.VoucherLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AppleTvConnection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;

    private static final int LONG_BYTE_COUNT = Long.SIZE/Byte.SIZE;
//    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static final ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;

    private static final String TAG = "AppleTvConnection";

    private Socket mSocket;
    private int mPort = -1;

    public AppleTvConnection(Handler handler) {
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);
    }

    public void tearDown() {
        if(mChatClient != null){
            mChatClient.tearDown();
        }
        if(mChatServer != null){
            mChatServer.tearDown();
        }
    }

    public void connectToServer(InetAddress address, int port) {
        mChatClient = new ChatClient(address, port);
    }

    public void sendMessage(String msg) {
        if (mChatClient != null) {
            mChatClient.sendMessage(msg);
        }
    }
    
    public int getLocalPort() {
        return mPort;
    }
    
    public void setLocalPort(int port) {
        mPort = port;
    }
    

    public synchronized void updateMessages(String msg, boolean local) {
        if (local) {
            msg = "me: " + msg;
        } else {
            msg = "them: " + msg;
        }

        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);

        Message message = new Message();
        message.setData(messageBundle);
        mUpdateHandler.sendMessage(message);

    }

    private synchronized void setSocket(Socket socket) {
        VoucherLog.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                    VoucherLog.d(TAG,"Socket getting closed sync");
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
                VoucherLog.d(TAG,"Socket getting closed teadr");
            } catch (IOException ioe) {
                VoucherLog.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        VoucherLog.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        VoucherLog.d(TAG, "Connected.");
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                    VoucherLog.e(TAG, "Error creating ServerSocket: "+ e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            Log.d(TAG, "Creating chatClient "+address.getHostName()+ " port: "+port);
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<String> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<String>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(TAG, "Client-side socket initialized.");

                    } else {
                        Log.d(TAG, "Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(TAG, "Initializing socket failed, IOE.", e);
                }

                while (true) {
                    try {
                        String msg = mMessageQueue.take();
                        sendMessage(msg);
                    } catch (InterruptedException ie) {
                        Log.d(TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {

            @Override
            public void run() {
                BufferedReader input;

                try {

                    Socket socket = getSocket();
                    if (socket == null) {
                        Log.d(TAG, "Socket is null, waat?");
                    } else if (socket.getInputStream() == null) {
                        Log.d(TAG, "Socket output stream is null, waat?");
                    }

//                    if(socket.isClosed()){
//                        Log.d(TAG, "Socket is closed, wat?");
//                    }
//
//                    if(socket.isBound()){
//                        Log.d(TAG, "Socket is bound, wat?");
//                    }

                    input = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    while (!Thread.currentThread().isInterrupted()) {

                        String messageStr = null;
                        messageStr = input.readLine();
                        if (messageStr != null) {
                            Log.d(TAG, "Read from the stream: " + messageStr);
                            updateMessages(messageStr, false);
                        } else {
                            Log.d(TAG, "The nulls! The nulls!");
                            break;
                        }
                    }
                    input.close();

                } catch (IOException e) {
                    Log.e(TAG, "Server loop error: ", e);
                }
            }
        }

        public void tearDown() {
            try {
                getSocket().close();
                Log.d(TAG,"Socket getting closed");
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        public void sendMessage(String msg) {
            try {
                Socket socket = getSocket();
                if (socket == null) {
                    Log.d(TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(TAG, "Socket output stream is null, wtf?");
                }

                sendBytes(socket,msg.getBytes());
                updateMessages(msg, true);
            } catch (UnknownHostException e) {
                Log.d(TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(TAG, "Error3", e);
            }
            Log.d(TAG, "Client sent message: " + msg);
        }
    }

    private void sendBytes(Socket socket, byte[] bytesToSend) throws IOException {
        OutputStream os = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        int bytesToSendLength = bytesToSend.length;
        byte[] bytesToSendLengthBytes = ByteBuffer.allocate(LONG_BYTE_COUNT).order(BYTE_ORDER).putLong(bytesToSendLength).array();
        dos.write(bytesToSendLengthBytes);
        dos.write(bytesToSend);
        Log.d(TAG, "Info-"+bytesToSendLength);
    }

    int htonl(int value) {
        if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
            return value;
        }
        return Integer.reverseBytes(value);
    }

    public long ntohll(long convert) {
        ByteBuffer bbuf = ByteBuffer.allocate(8);
        bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.putLong(convert);
        bbuf.order(ByteOrder.LITTLE_ENDIAN);
        return bbuf.getLong(0);
    }
}
