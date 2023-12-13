package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
 * Created by PKNU on 2017-07-21.
 */

public class LoginActivity extends AppCompatActivity {
    BasicInformation basicurl;

    EditText idinput, pwdinput;
    Button logBtn;
    InputStream is = null;
    String data_in = "";
    JSONObject jsonlogin;
    String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        idinput = (EditText) findViewById(R.id.id_txt);
        pwdinput = (EditText) findViewById(R.id.pwd_txt);
        logBtn = (Button) findViewById(R.id.logBtn);


        logBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectThread thread = new ConnectThread();
                thread.start();
            }
        });


    }

    public void Connecthttp() {
        //String urlstr = "http://210.119.12.75:7878/phone/login";
        String urlstr = "http://192.168.0.150:8080/phone/login";
        //String urlstr = basicurl.makeurl() + "login";
        String id = idinput.getText().toString();
        String pwd = pwdinput.getText().toString();

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
                jsonlogin.put("UserId", id);
                jsonlogin.put("Pwd", pwd);

                data_out = jsonlogin.toString();
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

    class ConnectThread extends Thread {
        public void run() {
            Connecthttp();
            goMain();
        }
    }

    public void goMain() {
        try {
            JSONObject obj = new JSONObject(data_in);

            if(obj.get("Pass").equals("ok")) {
                //로그인 성공
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                // 인텐트에 데이터 넘겨주기
                intent.putExtra("UserId",idinput.getText().toString());
                intent.putExtra("Name", obj.get("Name").toString());
                intent.putExtra("MyLocation", obj.get("MyLocation").toString());
                intent.putExtra("MyBook1", obj.get("MyBook1").toString());
                intent.putExtra("MyBook2", obj.get("MyBook2").toString());
                intent.putExtra("MyBook3", obj.get("MyBook3").toString());
                intent.putExtra("UserGPS", obj.get("UserGPS").toString());
                intent.putExtra("UserNo", obj.get("UserNo").toString());



                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else if (obj.get("Pass").equals("fail")){
                // 로그인 실패
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
                Toast.makeText(LoginActivity.this, "아이디, 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goJoin(View view) {
        Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
        startActivity(intent);
    }

    /*
    public void sendRegistrationToServer(String token, String uid) {
        OkHttpClient client = new OkHttpClient();

        JSONObject obj = new JSONObject();
        try {
            obj.put("Token", token);
            obj.put("UserId", uid); // Login시 받아 줘야함~

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, obj.toString());
        //request
        Request request = new Request.Builder()
                .url("http://192.168.0.150/8080/phone/token")
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
}
