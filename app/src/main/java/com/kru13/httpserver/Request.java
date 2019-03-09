package com.kru13.httpserver;

import java.util.ArrayList;

public class Request
{
    public String Method;
    public String Path;
    public String FileName;
    public String HttpVersion;
    public String Host;
    public String Connection;
    public String Purpose;
    public String Upgrade_Insecure_Requests;
    public String User_Agent;
    public String Accept;
    public String Accept_Encoding;
    public String Accept_Language;


    public static Request ParseRequest(ArrayList<String> responses)
    {
        if (responses.isEmpty())
            return null;

        Request request = new Request();
        for (String res:responses) {

            if(res.contains("GET") || res.contains("POST"))
            {
                String header[] = res.split(" ");
                request.Method = header[0];
                request.Path = header[1];
                request.HttpVersion = header[2];
                request.FileName = header[1].substring(header[1].lastIndexOf("/")+1);

            }else if(res.contains("Host:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Host = param;
            }else if(res.contains("Connection:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Connection = param;
            }else if(res.contains("Purpose:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Purpose = param;
            }else if(res.contains("Upgrade-Insecure-Requests:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Upgrade_Insecure_Requests = param;
            }else if(res.contains("User-Agent:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.User_Agent = param;
            }else if(res.contains("Accept:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Accept = param;
            }else if(res.contains("Accept-Encoding:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Accept_Encoding = param;
            }else if(res.contains("Accept-Language:"))
            {
                String param = res.substring(res.indexOf(" "));
                request.Accept_Language = param;
            }

        }

        return request;
    }
}