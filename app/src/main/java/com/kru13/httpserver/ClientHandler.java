package com.kru13.httpserver;

import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class ClientHandler extends Thread {

    public static String NEWLINE = "\r\n";
    private ServerSocket serverSocket;
    private boolean bRunning = false;

    private Handler messageHandler;

    CameraManager cameraManager;
    Camera cameraInstance;


    public ClientHandler(ServerSocket socket, Handler handler, CameraManager cameraManager, Camera cameraInstance)
    {
        this.serverSocket = socket;
        this.messageHandler = handler;
        this.cameraManager = cameraManager;
        this.cameraInstance = cameraInstance;
    }

    public void run() {

        try {

            bRunning = true;
            while (bRunning) {

                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                (new ClientHandler(serverSocket,messageHandler, cameraManager, cameraInstance)).start();
                Log.d("SERVER", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter res = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                ArrayList<String> responses = new ArrayList<String>();
                String response;
                    String req = in.readLine();
                    String[] reqData = req.split("\\s+");
                    String reqestType = reqData[0];
                    String requestedFile = reqData[1];
                    ResMessage resMessage = new ResMessage();
                    if (reqestType.equals("GET"))
                    {
                        File outFile = null;
                        if(requestedFile.equalsIgnoreCase("/webcam")){
                            outFile = new File(makeCameraPhoto());
                        }
                        else {
                            outFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), requestedFile);
                        }
                        if (outFile.exists())
                        {

                            res.write( "HTTP/1.1 200 OK"+ NEWLINE);
                            res.write("Date: "+Calendar.getInstance().getTime()+ NEWLINE);
                            res.write("Server: localhost:12345"+ NEWLINE);
                            res.write("Content-Length: " + String.valueOf(outFile.length())+ NEWLINE);
                            res.write("Connection: Closed"+ NEWLINE);
                            res.write(NEWLINE);
                            res.flush();

                            byte[] buf = new byte[1024];
                            int len = 0;
                            FileInputStream fis = new FileInputStream(outFile);
                            while((len = fis.read(buf)) > 0)
                            {
                                o.write(buf,0,len);
                            }

                            resMessage.Size = outFile.length();
                            resMessage.FileName = requestedFile;
                            resMessage.Host = s.getRemoteSocketAddress().toString();
                            resMessage.ResponseType = "404 NOT FOUND";
                        }
                        else
                        {
                            File notFoundFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"not-found.html");
                            res.write("HTTP/1.1 200 OK"+ NEWLINE);
                            res.write("Date: "+Calendar.getInstance().getTime()+ NEWLINE);
                            res.write("Server: localhost/12345"+ NEWLINE);
                            res.write("Content-Length: " + String.valueOf(notFoundFile.length())+ NEWLINE);
                            res.write("Connection: Closed"+ NEWLINE);
                            res.write("Content-Type: text/html"+ NEWLINE);
                            res.write(NEWLINE);
                            res.flush();
                            byte[] buf = new byte[1024];
                            int len = 0;
                            FileInputStream fis = new FileInputStream(notFoundFile);
                            while((len = fis.read(buf)) > 0)
                            {
                                o.write(buf,0,len);
                            }
                            resMessage.Size = notFoundFile.length();
                            resMessage.FileName = requestedFile;
                            resMessage.Host = s.getRemoteSocketAddress().toString();
                            resMessage.ResponseType = "404 NOT FOUND";

                            Log.d("SERVER","File not found");
                        }
                    }
                   /* else if(request.Method.toUpperCase().equals("PUT"))
                    {
                        Log.d("SERVER","Put methode");

                    }*/
                    else
                    {
                        Log.d("SERVER","bad request methode!");
                    }

                Message msg = messageHandler.obtainMessage();
                Bundle bundle = new Bundle();
                bundle.putSerializable("REQUEST",(Serializable)resMessage);
                msg.setData(bundle);
                messageHandler.sendMessage(msg);


                s.close();
                Log.d("SERVER", "Socket Closed");
            }
        }
        catch (Exception e)
        {
            Log.d("SERVER ERROR",e.toString());
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }

    private String makeCameraPhoto() throws Exception {
        if (cameraManager == null) {
            throw new Exception("NO CAMERA");
        }

        takePicture();
        String photoPath = cameraManager.getLastFile() != null ?
                cameraManager.getLastFile().getPath() : "/error";


        if (photoPath.startsWith("/storage/sdcard/")) {
            photoPath = photoPath.substring("/storage/sdcard".length());
        }

        return photoPath;

    }

    public void takePicture() {
        if (cameraManager == null) {
            Log.d("takePictureFromCamera", "no camera hardware found");
            // todo return info
            return;
        }


        if (cameraInstance == null) {
            Log.d("takePictureFromCamera", "could not get access to camera");
            return;
        }

        cameraInstance.takePicture(null, null, this.cameraManager);

       /* try {
            // sync time
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Log.d("takePictureFromCamera", "sync interrupted");
        }*/

        //cameraInstance.release();
    }
}