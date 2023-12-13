package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by PKNU on 2017-08-16.
 */

public class MyinfoActivity extends AppCompatActivity   {
    InputStream is = null;
    String data_in = "";
    JSONObject jsonlogin;

    TextView sname, wnum, state;
    Button open_btn, del_btn;
    String userno, bookingno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfo);

        sname = (TextView) findViewById(R.id.shopname_txt);
        wnum = (TextView) findViewById(R.id.washernum_txt);
        state = (TextView) findViewById(R.id.washerstate_txt);
        open_btn = (Button) findViewById(R.id.open_btn);
        del_btn = (Button) findViewById(R.id.del_btn);

        Intent intent = getIntent();
        userno = intent.getStringExtra("UserNo");

        data_AsyncTask data_asyncTask = new data_AsyncTask();
        data_asyncTask.execute();

        open_btn.setEnabled(false);

        // 빨래 완료 > 세탁기 열림 메시지 주기
        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endThread endThread = new endThread();
                endThread.start();
            }
        });

        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delThread delThread = new delThread();
                delThread.start();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public class data_AsyncTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            //String urlstr = "http://210.119.12.75:7878/phone/myLaundry";
            String urlstr = "http://192.168.0.150:8080/phone/myLaundry";
            //String urlstr = basicurl.makeurl() + "myBookmark";

            String data_out = "";
            try {
                URL urlCon = new URL(urlstr);
                HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();

                httpCon.setRequestProperty("Accept", "application/json");
                httpCon.setRequestProperty("Content-type", "application/json");


                httpCon.setDoOutput(true);  // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
                httpCon.setDoInput(true); // InputStream으로 서버로 부터 응답을 받겠다는 옵션.

                try {
                    // data out
                    // json -> string -> byte
                    jsonlogin = new JSONObject();
                    jsonlogin.put("UserNo", userno);

                    data_out = jsonlogin.toString();
                    System.out.println(data_out);

                    OutputStream os = httpCon.getOutputStream();
                    os.write(data_out.getBytes("UTF-8"));
                    os.flush();

                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

                // data in
                is = httpCon.getInputStream();
                if(is != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
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
                    Log.e("main_data_in >>", data_in);


                } else {
                    data_in = "Did not Work";
                    Log.e("main_data_in >>", data_in);
                }

                // 종료
                httpCon.disconnect();


            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return data_in;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setData();
        }
    }

    public void setData() {
        try {
            JSONObject obj = new JSONObject(data_in);

            if(obj.getString("Delegate").equals("3")) {
                state.setText("세탁기를 예약해주세요");
                sname.setText("세탁기를 예약해주세요");
                wnum.setText("세탁기를 예약해주세요");
            } else {
                if (obj.getString("BookingType").equals("0")) {
                    state.setText("세탁기 할당 대기중");
                    sname.setText("세탁기 할당 대기중");
                    wnum.setText("세탁기 할당 대기중");
                } else if (obj.getString("BookingType").equals("1")) {
                    state.setText("사용중");
                    sname.setText(obj.getString("ShopName"));
                    wnum.setText(obj.getString("WasherNum"));
                } else if (obj.getString("BookingType").equals("2")) {
                    state.setText("사용종료.. 수거 대기중");
                    sname.setText(obj.getString("ShopName"));
                    wnum.setText(obj.getString("WasherNum"));
                    open_btn.setEnabled(true);
                }
            }
            bookingno = obj.getString("BookingNo");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void Connecthttp() {
        //String urlstr = "http://210.119.12.75:7878/phone/end";
        String urlstr = "http://192.168.0.150:8080/phone/end";
        String data_out = "";

        try {
            URL urlCon = new URL(urlstr);
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();

            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            httpCon.setDoOutput(true);  // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoInput(true); // InputStream으로 서버로 부터 응답을 받겠다는 옵션.

            try {
                // data out
                // json -> string -> byte
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("BookingNo", bookingno);

                data_out = jsonobj.toString();
                System.out.println(data_out);

                OutputStream os = httpCon.getOutputStream();
                os.write(data_out.getBytes("utf-8"));
                os.flush();

            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            // data in
            is = httpCon.getInputStream();
            if(is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
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
                Log.e("Login_data_in >> ", data_in);

            } else {
                data_in = "Did not Work";
                Log.e("Login_data_in >> ", data_in);
            }

            // 종료
            httpCon.disconnect();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    class endThread extends Thread {
        public void run() {
            Connecthttp();
        }
    }


    public void Connecthttp2() {
        //String urlstr = "http://210.119.12.75:7878/phone/del";
        String urlstr = "http://192.168.0.150:8080/phone/del";
        String data_out = "";

        try {
            URL urlCon = new URL(urlstr);
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();

            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            httpCon.setDoOutput(true);  // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoInput(true); // InputStream으로 서버로 부터 응답을 받겠다는 옵션.

            try {
                // data out
                // json -> string -> byte
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("BookingNo", bookingno);

                data_out = jsonobj.toString();
                System.out.println(data_out);

                OutputStream os = httpCon.getOutputStream();
                os.write(data_out.getBytes("utf-8"));
                os.flush();

            } catch (JSONException e1) {
                e1.printStackTrace();
            }

            // data in
            is = httpCon.getInputStream();
            if(is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
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
                Log.e("Login_data_in >> ", data_in);

            } else {
                data_in = "Did not Work";
                Log.e("Login_data_in >> ", data_in);
            }

            // 종료
            httpCon.disconnect();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    class delThread extends Thread {
        public void run() {
            Connecthttp2();
        }
    }

    // 세탁기 열기 버튼 클릭시 대리 수령 신청 진행중이면 작동 X
    public void delcheck() {
        try {
            JSONObject obj = new JSONObject(data_in);

            if(obj.get("Status").toString().equals("Already")) {
                Toast.makeText(getApplicationContext(), "대리 수령 진행중입니다.", Toast.LENGTH_SHORT).show();
            } else if(obj.get("Status").toString().equals("End")) {
                Toast.makeText(getApplicationContext(), "대리 수령 되어 세탁물 보관중입니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "세탁기가 열립니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }
}

