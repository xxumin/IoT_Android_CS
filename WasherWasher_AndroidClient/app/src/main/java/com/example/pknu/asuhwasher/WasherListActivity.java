package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by PKNU on 2017-07-21.
 */

public class WasherListActivity extends AppCompatActivity {
    BasicInformation basicurl;

    ListView washer_list; // 세탁소별 세탁기 리스트
    Button reserve_btn;
    TextView cname_txt, opentime_txt, closetime_txt;
    ImageButton bookmark_btn;
    washerListAdapter washerListAdapter; // 세탁기 리스트 어댑터

    InputStream is = null;
    String data_in = "";
    JSONObject jsonShop;
    cleanerView cleanerView;

//    String name, open, close;
    String washernum, workstatus;
    String shopname, shopaddr, opentime, closetime;
    String managerno;
    String id, Bookmark = null;
    double shoplat, shoplng;
    String shopgps;

    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_washer_list);

        reserve_btn = (Button) findViewById(R.id.reserve_btn);
        bookmark_btn = (ImageButton) findViewById(R.id.bookmark_btn);

        cname_txt = (TextView) findViewById(R.id.cname_txt);
        opentime_txt = (TextView) findViewById(R.id.open_txt);
        closetime_txt = (TextView) findViewById(R.id.close_txt);



    } // onCreate();


    @Override
    protected void onResume() {
        super.onResume();

        // 데이터 값 불러올 Parameter
        Intent shopintent = getIntent();
        id = shopintent.getStringExtra("id");
        Bookmark = shopintent.getStringExtra("Bookmark");
        token = FirebaseInstanceId.getInstance().getToken();
        Log.i("북마크 확인하기!!!!!>>>>", Bookmark);

        shoplat = shopintent.getDoubleExtra("shoplat", 0);
        shoplng = shopintent.getDoubleExtra("shoplng", 0);
        shopgps = shoplat + "," + shoplng;

        // Cleaner's Info Request
        data_AsyncTask2 async2 = new data_AsyncTask2();
        async2.execute();

        // 예약 신청 버튼
        reserve_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReserveThread thread = new ReserveThread();
                thread.start();

                try {
                    JSONObject obj = new JSONObject(data_in);
                    //String status = obj.getString("Status");

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "예약 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });



        // 즐겨찾기 추가 버튼
        bookmark_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookThread thread = new BookThread();
                thread.start();

                try {
                    JSONObject obj = new JSONObject(data_in);
                    String status = obj.getString("Status");

                    if(status.contains("Removed")) {
                        Toast.makeText(getApplicationContext(), "북마크가 제거 되었습니다", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "북마크가 추가 되었습니다", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void Connecthttp() {
        //String urlstr = "http://210.119.12.75:7878/phone/registerBook";
        String urlstr = "http://192.168.0.150:8080/phone/registerBook";
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
                JSONObject jsonbook = new JSONObject();
                jsonbook.put("UserId", id);
                //jsonbook.put("Check", bk); // 즐겨찾기 추가, 제거 확인
                jsonbook.put("ManagerNo", managerno);

                if(Bookmark.equals("null")) { // 지도를 통해서 들어왔을때
                    jsonbook.put("Params", shopgps);
                } else { // 내 정보에서 들어왔을때
                    jsonbook.put("Params", Bookmark);
                }

                data_out = jsonbook.toString();
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

    class BookThread extends Thread {
        public void run() {
            Connecthttp();
        }
    }

    public void ReserveConnect() {
        //String urlstr = "http://210.119.12.75:7878/phone/reserve";
        String urlstr = "http://192.168.0.150:8080/phone/reserve";
        //String urlstr = basicurl.makeurl() + "reserve";
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
                JSONObject jsonreserve = new JSONObject();
                jsonreserve.put("UserId", id);
                jsonreserve.put("ManagerNo", managerno);

                data_out = jsonreserve.toString();
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

    class ReserveThread extends Thread {
        public void run() {
            ReserveConnect();
        }
    }

    // 세탁소 정보 찾기
    public class data_AsyncTask2 extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            //String urlstr = "http://210.119.12.75:7878/phone/shopInfo";
            String urlstr = "http://192.168.0.150:8080/phone/shopInfo";
            //String urlstr = basicurl.makeurl() + "shopInfo";

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
                    jsonShop = new JSONObject();
                    jsonShop.put("UserId", id);
                    if(!(Bookmark.equals("null"))) { // 내 정보에서 클릭 // 어떤 Activity를 통해서 들어왔는지 판단.
                        jsonShop.put("Params", Bookmark);
                        jsonShop.put("CheckCode", "1");
                    } else if(Bookmark.equals("null")) { // 맵에서 클릭
                        jsonShop.put("Params", shopgps);
                        jsonShop.put("CheckCode", "2");
                    }

                    data_out = jsonShop.toString();
                    Log.e("data_out >>>> ", data_out);

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
                    Log.e("Washer_data_in >> ", data_in);
                } else {
                    data_in = "Did not Work";
                    Log.e("Washer_data_in >> ", data_in);
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
            addWasherListView();

            cname_txt.setText(shopname);
            // shopaddr 추가 안해도 될듯
            opentime_txt.setText(opentime);
            closetime_txt.setText(closetime);
            // 예약된 인원 : setText 나중에 추가
        }
    }

    public void addWasherListView() {
        // tab2 > listView = avail_wlist
        washer_list = (ListView) findViewById(R.id.washer_list);
        washerListAdapter = new washerListAdapter();

        try {
            JSONObject preobj = new JSONObject(data_in);
            JSONArray jsonarr = new JSONArray(preobj.get("ShopInfo").toString());
            Log.e(">>>>>>>>>> ", jsonarr.toString());
            int i= 0;
            if(i == 0) {
                JSONObject obj = jsonarr.getJSONObject(0);
                shopname = obj.get("ShopName").toString();
                shopaddr = obj.get("ShopAddr").toString();
                opentime = obj.get("OpenTime").toString();
                closetime = obj.get("CloseTime").toString();
                managerno = obj.get("ManagerNo").toString();
            }

            for (i = 1; i < jsonarr.length(); i++){
                JSONObject obj = jsonarr.getJSONObject(i);
                washernum = obj.get("WasherNum").toString();
                workstatus = obj.get("WorkStatus").toString();

                String Work = null;
                int img;
                if (workstatus.equals("true")) {
                    Work = "사용중";
                    img = R.drawable.washer;
                } else {
                    Work = "사용가능";
                    img = R.drawable.emptywasher;
                }

                // 세탁기 대수 만큼 List Show
                if (Integer.parseInt(washernum) > 0) {
                    washerListAdapter.add_wlist_Item(new washerInfo(
                            washernum,
                            Work,
                            img));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 임시 등록
        // 여기서 등록된 세탁기 대수만큼 리스트 띄우는 처리
        /*
        washerListAdapter.add_wlist_Item(new washerInfo("#2", "00:41:23", R.drawable.emptywasher));
        washerListAdapter.add_wlist_Item(new washerInfo("#3", "00:41:23", R.drawable.emptywasher));
        washerListAdapter.add_wlist_Item(new washerInfo("#4", "00:41:23", R.drawable.washer));
        washerListAdapter.add_wlist_Item(new washerInfo("#5", "00:41:23", R.drawable.emptywasher));
        washerListAdapter.add_wlist_Item(new washerInfo("#6", "00:41:23", R.drawable.washer));
        washerListAdapter.add_wlist_Item(new washerInfo("#7", "00:41:23", R.drawable.emptywasher));
        */

        // ** SingerAdapter2 객체 생성
        washer_list.setAdapter(washerListAdapter);
    }

    // ---------------------------------------------------------------------------------------
    // tab2_adapter
    // ---------------------------------------------------------------------------------------

    class washerListAdapter extends BaseAdapter {
        ArrayList<washerInfo> tab2_items = new ArrayList<>();

        public void add_wlist_Item(washerInfo wlist_item) {
            tab2_items.add(wlist_item);
        }

        @Override
        public int getCount() {
            return tab2_items.size();
        }

        @Override
        public Object getItem(int position2) {
            return tab2_items.get(position2);
        }

        @Override
        public long getItemId(int position2) {
            return position2;
        }

        @Override
        public View getView(int position2, View convertView, ViewGroup parent) {
            washerView tab2_listview = new washerView(getApplicationContext());
            washerInfo tab2_item = tab2_items.get(position2);

            // 각 Item 객체에 해당하는 내용 설정.
            tab2_listview.setWasher_id_txt(tab2_item.getWasher_id());
            tab2_listview.setR_time_txt(tab2_item.getR_time());
            tab2_listview.setImg(tab2_item.getImg_id());
            tab2_listview.setPbar(60);

            return tab2_listview;
        }
    }
}
