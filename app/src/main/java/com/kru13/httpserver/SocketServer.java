package com.kru13.httpserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Environment;
import android.util.Log;

public class SocketServer extends Thread {
	
	ServerSocket serverSocket;
	public final int port = 12345;
	boolean bRunning;
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}
	
	public void run() {
		
		try {
        	Log.d("SERVER", "Creating Socket");
			serverSocket = new ServerSocket(port);
			ClientHandler client = new ClientHandler(serverSocket);
			client.run();
			//client.join();

		} catch (IOException e) {
			if (serverSocket != null && serverSocket.isClosed())
				Log.d("SERVER", "Normal exit");
			else {
				Log.d("SERVER", "Error");
				e.printStackTrace();
			}
		}
        try {
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
            	Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept(); 
                Log.d("SERVER", "Socket Accepted");
				File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				StringBuilder res = new StringBuilder();
				StringBuilder resContent = new StringBuilder();
				OutputStream o = s.getOutputStream();
	        	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
	        	BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

	            String req = in.readLine();
				String[] reqData = req.split("\\s+");
				String reqestType = reqData[0];
				String requestedFile = reqData[1];
				File file = null;
				if(requestedFile.equalsIgnoreCase("/index.html")) {
					file = new File(sdcard, "index.html");
				}
				else {
					file = new File(sdcard, "not-found.html");
				}

					try {
						BufferedReader br = new BufferedReader(new FileReader(file));
						String line;

						while ((line = br.readLine()) != null) {
							resContent.append(line);
							resContent.append('\n');
						}
						br.close();
					}
					catch (IOException e) {
						//You'll need to add proper error handling here
					}

				res.append("HTTP/1.1 200 OK\n" +
						"Date: Mon, 27 Jul 2009 12:28:53 GMT\n" +
						"Server: Apache/2.2.14 (Win32)\n" +
						"Last-Modified: Wed, 22 Jul 2009 19:15:56 GMT\n" +
						"Content-Type: text/html\n" +
						"Connection: Closed\n\n");
				res.append(resContent);
	            out.write(res.toString());
                out.flush();
	            
                s.close();
                Log.d("SERVER", "Socket Closed");
            }
        } 
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        }
        finally {
        	serverSocket = null;
        	bRunning = false;
        }
    }

}
