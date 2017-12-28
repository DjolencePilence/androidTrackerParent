package com.example.djole.parrentapp;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

/**
 * Created by Djole on 25-Dec-17.
 */

public class GetLocationDownloadTask extends AsyncTask<String,Void,String> {
    @Override
    protected String doInBackground(String... strings) {


        String result = "";
        URL url;
        HttpURLConnection urlConnection;
        try {
            url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream is = urlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(is);

            int data = inputStreamReader.read();
            while(data != -1){
                char curr = (char) data;
                result += curr;
                data = inputStreamReader.read();
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result != null) {
            try {
                JSONObject locationObject = new JSONObject(result);
                JSONObject jsonObject = locationObject.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                double longitude, latitude;
                longitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");
                latitude = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");
                MapsActivity.moveToLocationZoom(latitude, longitude, 15);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
