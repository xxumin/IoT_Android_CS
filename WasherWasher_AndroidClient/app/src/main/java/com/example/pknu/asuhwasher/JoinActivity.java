package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
 * Created by PKNU on 2017-07-24.
 */

public class JoinActivity extends AppCompatActivity {
    BasicInformation basicurl;
    EditText join_id, join_pwd, join_check, join_name;
    TextView join_addr;
    Button joinbtn, searchbtn, confirmbtn;
    ImageView setImg;

    InputStream is = null;
    String data_in = "";
    JSONObject jsonjoin, jsoncheck;

    String mygps;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        join_id = (EditText) findViewById(R.id.join_id_input);
        join_pwd = (EditText) findViewById(R.id.join_pwd_input);
        join_check = (EditText) findViewById(R.id.join_pwd_check);
        join_name = (EditText) findViewById(R.id.join_name_input);
        join_addr = (TextView) findViewById(R.id.join_addr_input);

        joinbtn = (Button) findViewById(R.id.join_btn);
        searchbtn = (Button) findViewById(R.id.search_btn);
        confirmbtn = (Button) findViewById(R.id.confirm_btn);

        joinbtn.setEnabled(false); // 비활성

        setImg = (ImageView) findViewById(R.id.check_img);


        pwdCheck();

        searchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addrintent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(addrintent);
            }
        });
        // Join Http Connection Request
        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectThread jointhread = new ConnectThread();
                jointhread.start();
            }
        });

        // id check
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectThread2 thread2 = new ConnectThread2();
                thread2.start();

                idchk();
            }
        });
    }

    public void idchk() {
        try {
            JSONObject obj = new JSONObject(data_in);

            if(obj.get("Pass").toString().equals("ok")) {
                joinbtn.setEnabled(true);
                Toast.makeText(getApplicationContext(), "가입가능합니다", Toast.LENGTH_SHORT).show();
            } else {
                joinbtn.setEnabled(false);
                Toast.makeText(getApplicationContext(), "다른 아이디를 사용해주세요", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void Connecthttp() {
        //String urlstr = "http://210.119.12.75:7878/phone/join";
        String urlstr = "http://192.168.0.150:8080/phone/join";
        //String urlstr = basicurl.makeurl() + "join";
        String id = join_id.getText().toString();
        String pwd = join_pwd.getText().toString();
        String name = join_name.getText().toString();
        String addr = join_addr.getText().toString();


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
                jsonjoin = new JSONObject();
                jsonjoin.put("UserId", id);
                jsonjoin.put("Pwd", pwd);
                jsonjoin.put("Name", name);
                jsonjoin.put("UserAddr", addr);
                jsonjoin.put("lat", Double.toString(lat));
                jsonjoin.put("lng", Double.toString(lng));

                data_out = jsonjoin.toString();
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

            // 종료
            httpCon.disconnect();


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent addrintent = getIntent();
        mygps = addrintent.getStringExtra("mylocation");
        lat = addrintent.getDoubleExtra("lat", 0);
        lng = addrintent.getDoubleExtra("lng", 0);

        join_addr.setText(mygps);
    }

    public void Connecthttp2() {
        //String urlstr = "http://210.119.12.75:7878/phone/idcheck";
        String urlstr = "http://192.168.0.150:8080/phone/idcheck";
        //String urlstr = basicurl.makeurl() + "idcheck";

        String id = join_id.getText().toString();

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
                jsoncheck = new JSONObject();
                jsoncheck.put("UserId", id);

                data_out = jsoncheck.toString();
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
                Log.e("Join_data_in >> ", data_in);

            } else {
                data_in = "Did not Work";
                Log.e("Join_data_in >> ", data_in);
            }

            // 종료
            httpCon.disconnect();


        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    class ConnectThread2 extends Thread {
        public void run() {
            Connecthttp2();
        }
    }

    public void pwdCheck() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //... UI 업데이트 작업
                join_check.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(join_pwd.getText().toString().equals(join_check.getText().toString())) {
                            setImg.setImageResource(R.drawable.check);
                            joinbtn.setEnabled(true);
                        } else {
                            setImg.setImageResource(R.drawable.x);
                        }
                    }
                });
            }
        });
    }

    class ConnectThread extends Thread {
        public void run() {
            Connecthttp();
            goLogin();
        }
    }


    public void goLogin() {
        try {
            JSONObject obj = new JSONObject(data_in);

            if(obj.get("Pass").equals("ok")) {
                //가입 성공
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // 수정필요

                startActivity(intent);

            } else if (obj.get("Pass").equals("fail")){
                // 회원가입 실패
                showToast();
            }
        } catch (Exception exex) {
            exex.printStackTrace();
        }
    }

    public void showToast() // Show Toast Message on Thread
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(JoinActivity.this, "가입 정보를 확인해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
