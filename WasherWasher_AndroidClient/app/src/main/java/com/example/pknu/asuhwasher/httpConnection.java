package com.example.pknu.asuhwasher;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by PKNU on 2017-08-07.
 */

public class httpConnection {
    HttpURLConnection httpCon = null;
    JSONObject json_out = new JSONObject();

    OutputStream os;
    InputStream is;

    String urlstr = "http://210.119.12.75:7878/";
    String data_in = "";
    String data_out = "";

    public void Connecthttp() {
        //String urlstr = "http://210.119.12.75:7878/";

        try{
            URL urlCon = new URL(urlstr);
            httpCon = (HttpURLConnection) urlCon.openConnection();

            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            httpCon.setDoOutput(true);  // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoInput(true); // InputStream으로 서버로 부터 응답을 받겠다는 옵션.

            // 종료
            //httpCon.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void data_out() {
        try {
            try {
                // ********** data out **********
                // json -> string -> byte
                //json_out = new JSONObject();
                json_out.put("Pass", "ok");

                data_out = json_out.toString();
                System.out.println(data_out);

                os = httpCon.getOutputStream();
                os.write(data_out.getBytes("utf-8"));
                os.flush();

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } catch (IOException oe) {
            oe.printStackTrace();
        }


    }

    public void data_in() {
        try {
            // ********** data in **********
            is = httpCon.getInputStream();
            if(is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                data_in = sb.toString();
                System.out.println(data_in);

            } else {
                data_in = "Did not Work";
                System.out.println(data_in);
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }

    }
}
