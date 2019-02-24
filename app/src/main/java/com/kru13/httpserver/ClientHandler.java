package com.kru13.httpserver;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class ClientHandler extends Thread {

    public static String NEWLINE = "\r\n";
    private ServerSocket serverSocket;
    private boolean bRunning = false;

    public ClientHandler(ServerSocket socket)
    {
        this.serverSocket = socket;
    }

    public void run() {

        try {

            bRunning = true;
            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");

                OutputStream o = s.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

                ArrayList<String> responses = new ArrayList<String>();
                String response;
                while(!(response = in.readLine()).isEmpty())
                {
                    responses.add(response);
                    Log.d("SERVER",response);
                }
                if(!responses.isEmpty())
                {
                    String header[] = responses.get(0).split(" ");
                    if (header[0].toUpperCase().equals("GET"))
                    {
                        String fileName = header[1].substring(header[1].lastIndexOf("/")+1);
                        File outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),fileName);
                        if (outFile.exists())
                        {
                            out.write("HTTP/1.0 200 OK"+ NEWLINE);
                            out.write("Date: "+Calendar.getInstance().getTime()+ NEWLINE);
                            out.write("Server: localhost/12345"+ NEWLINE);
                            out.write("Content-Length: " + String.valueOf(outFile.length())+ NEWLINE);
                            out.write("Connection: Closed"+ NEWLINE);
                            out.write(NEWLINE);
                            out.flush();

                            byte[] buf = new byte[1024];
                            int len = 0;
                            FileInputStream fis = new FileInputStream(outFile);
                            while((len = fis.read(buf)) > 0)
                            {
                                o.write(buf,0,len);
                            }

                        }
                        else
                        {
                            File notFoundFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"notFound.html");
                            out.write(header[2] + " 404 Not Found"+ NEWLINE);
                            out.write("Date: "+Calendar.getInstance().getTime()+ NEWLINE);
                            out.write("Server: localhost/12345"+ NEWLINE);
                            out.write("Content-Length: " + String.valueOf(notFoundFile.length())+ NEWLINE);
                            out.write("Connection: Closed"+ NEWLINE);
                            //out.write("Content-Type: text/html"+ NEWLINE);
                            out.write(NEWLINE);
                            out.flush();

                            byte[] buf = new byte[1024];
                            int len = 0;
                            FileInputStream fis = new FileInputStream(notFoundFile);
                            while((len = fis.read(buf)) > 0)
                            {
                                o.write(buf,0,len);
                            }

                            Log.d("SERVER","File not found");
                        }
                    }
                    else if(header[0].toUpperCase().equals("PUT"))
                    {
                        Log.d("SERVER","Put methode");

                    }
                    else
                    {
                        Log.d("SERVER","bad request methode!");
                    }

                }
                else
                {

                }

                s.close();
                Log.d("SERVER", "Socket Closed");
            }
        }
        catch (Exception e)
        {
            Log.d("SERVER",e.toString());
        }
        finally {
            serverSocket = null;
            bRunning = false;
        }
    }
}
