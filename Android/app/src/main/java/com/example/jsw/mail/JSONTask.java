package com.example.jsw.mail;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class JSONTask extends AsyncTask<String, String, String> {
    private String JsonData;
    public void setJsonData(String JsonData){
        this.JsonData = JsonData;
    }

    @Override
    protected String doInBackground(String... urls) {
       try{

           JSONObject jsonObject = new JSONObject(JsonData);
           HttpURLConnection con = null;
           BufferedReader reader = null;
           try{

               URL url  = new URL(urls[0]);
               //server connection
               con = (HttpURLConnection) url.openConnection();
               con.setRequestMethod("POST");
               con.setRequestProperty("Cache-Control", "no-cache");
               con.setRequestProperty("Content-Type", "application/json ");
               con.setRequestProperty("Accpet", "text/html");
               con.setDoOutput(true);
               con.setDoInput(true);
               con.connect();


               //send data to server
               OutputStream outputStream = con.getOutputStream();
               BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
               bufferedWriter.write(jsonObject.toString());
               bufferedWriter.flush();
               bufferedWriter.close();

               //get data from server
               InputStream inputStream = con.getInputStream();
               reader = new BufferedReader(new InputStreamReader(inputStream));
               StringBuffer stringBuffer = new StringBuffer();

               String line = "";
               while((line = reader.readLine()) != null){
                   stringBuffer.append(line);
               }
               return stringBuffer.toString();

           } catch (ProtocolException e) {
               e.printStackTrace();
           } catch (MalformedURLException e) {
               e.printStackTrace();
           } catch (IOException e) {
               e.printStackTrace();
           } finally{
               if(con!=null){
                   con.disconnect();
               }
               try{
                   if(reader !=null){
                       reader.close();
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       } catch (JSONException e) {
           e.printStackTrace();
       }
        return null;
    }
}
