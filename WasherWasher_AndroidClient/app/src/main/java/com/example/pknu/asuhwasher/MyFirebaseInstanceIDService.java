package com.example.pknu.asuhwasher;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    public static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");
    BasicInformation basicurl;
    String urlstr;
    String id;
    // [START refresh_token]
    @Override
    public void onTokenRefresh() { // Token Reset 될 때 서버에 전송
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + token);

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        //sendRegistrationToServer(token); // UserNum 확인해서 없으면 안보내고, 있으면 전송. 회원가입 후 로그인하면 전송.
    }

    public void sendRegistrationToServer(String token, String uid) {
        // Add custom implementation, as needed.

        OkHttpClient client = new OkHttpClient();
        urlstr = "http://192.168.0.150:8080/phone/token";
        //urlstr = "http://210.119.12.75:7878/phone/token";
        JSONObject obj = new JSONObject();
        try {
            obj.put("Token", token);
            obj.put("UserId", uid); // Login시 받아 줘야함~
            //obj.put("UserId", userid);
            //obj.put("ManagerNo", managerno);

        }
        catch (JSONException e) {
            e.printStackTrace();        }

        RequestBody body = RequestBody.create(JSON, obj.toString());
        //request
       /*
        Request request = new Request.Builder()
                .url("http://210.119.12.75:7878/phone/token")
                .post(body)
                .build();
       */
        Request request = new Request.Builder()
                .url(urlstr)
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}