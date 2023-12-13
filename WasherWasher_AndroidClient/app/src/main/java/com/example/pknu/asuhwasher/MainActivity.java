package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.example.pknu.asuhwasher.MyFirebaseInstanceIDService.JSON;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // DaumMap 사용시
    // implements MapView.MapViewEventListener, MapView.POIItemEventListener
    // MapView mapView;
    //MapPoint myPoint;

    BasicInformation basicurl;

    TabHost tabHost; // 탭 호스트 / 탭 추가시 필요
    // tab1
    ListView cleaner_list; // 세탁소 리스트
    cleanerListAdapter adapter; // 세탁소 리스트 어댑터
    ImageButton myinfo_btn;
    // tab2
    private GoogleMap googleMap;
    LatLng MyLoc;
    LatLng latLng;

    TextView user_name, user_location;

    InputStream is = null;
    String data_in = "";
    JSONObject jsonlogin;

    boolean bookmark = true; // 즐겨찾기 등록 여부
    double mylati, mylongi;
    String UserGPS;
    String m1, m2, m3;
    String b1, b2, b3; // bookmark
    String id, Bookmark, userno;
    String[][] ShopData;
    TextView ShopName_txt;

    data_AsyncTask asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShopName_txt = (TextView) findViewById(R.id.nick_text);
        user_name = (TextView) findViewById(R.id.nick_text);
        user_location = (TextView) findViewById(R.id.mylocation_text);
        myinfo_btn = (ImageButton) findViewById(R.id.myinfo_btn);

        // 탭 만들기
        addTabs();



        // User Information Setting
        Intent intent  = getIntent();
        user_name.setText(intent.getStringExtra("Name"));
        user_location.setText(intent.getStringExtra("MyLocation"));
        id = intent.getStringExtra("UserId");
        b1 = intent.getStringExtra("MyBook1"); // MyBook1의 value 값 저장
        b2 = intent.getStringExtra("MyBook2");
        b3 = intent.getStringExtra("MyBook3");
        UserGPS = intent.getStringExtra("UserGPS");
        userno = intent.getStringExtra("UserNo");

        String temp[] = UserGPS.split(",");
        mylati = Double.parseDouble(temp[0]);
        mylongi = Double.parseDouble(temp[1]);
        MyLoc = new LatLng(mylati, mylongi);

//                // Cleaner's List Request in Background
//        asyncTask = new data_AsyncTask();
//        asyncTask.execute();


        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.d("탭 체인지", tabId);
                Log.e("ayncTask시작됨", "데이터 들어옵니다");
                if(tabId.equals("Tab1")) {
                    asyncTask = new data_AsyncTask();
                    asyncTask.execute();


                } else {
                    // Add Google Map
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
                    mapFragment.getMapAsync(MainActivity.this); // 이부분 꼭 MainThread에 구현되어 있어야함
                }
            }
        });


        // Add Google Map
        //MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.googleMap);
        //mapFragment.getMapAsync(this); // 이부분 꼭 MainThread에 구현되어 있어야함


        // ------------------------------------------------------------------------------------------
        // Add Daum MapView : 메소드로 따로 빼면 오류남->이유 모름
        //-------------------------------------------------------------------------------------------
        /*
        mapView = new MapView(this);
        mapView.setDaumMapApiKey("e76650b01e866c28d9607374419e7ac2");

        mapView.setPOIItemEventListener(this); // EventListener 건들지 말기
        mapView.setMapViewEventListener(this); // EventListener 건들지 말기

        // Add MapView
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        //-------------------------------------------------------------------------------------------
        */


        // Firebase
        //FirebaseMessaging.getInstance().subscribeToTopic("Washer"); //그룹명 설정 가능
        // 토큰 받아옴, 나중에 LoginActivity로?
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyFirebaseInstanceIDService msg = new MyFirebaseInstanceIDService();
                msg.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken(), id);
                Log.e("토큰 보기", FirebaseInstanceId.getInstance().getToken());
            }
        }); */
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("리줌", "리줌임");


        if(b1.equals("null")) {
            b1 = "99999";
        }

        if(b2.equals("null")) {
            b2 = "99999";
        }

        if(b3.equals("null")) {
            b3 = "99999";
        }

        // Cleaner's List Request in Background
        asyncTask = new data_AsyncTask();
        asyncTask.execute();

        // My Washer's Info
        myinfo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myinfointent = new Intent(getApplicationContext(), MyinfoActivity.class);
                myinfointent.putExtra("UserNo", userno);
                startActivity(myinfointent);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken(), id);
                Log.e("토큰 보기", FirebaseInstanceId.getInstance().getToken());
            }
        }).start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e("포즈", "멈춤");
    }

    public void sendRegistrationToServer(String token, String uid) {
        OkHttpClient client = new OkHttpClient();

        JSONObject obj = new JSONObject();
        try {
            Log.e("token 알아보기 ", token);
            Log.e("id알아보기", uid);
            obj.put("Token", token);
            obj.put("UserId", uid); // Login시 받아 줘야함~

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(JSON, obj.toString());
        //request
        Request request = new Request.Builder()
                .url("http://192.168.0.150:8080/phone/token")
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("지도 세팅중", "구글");
        this.googleMap = googleMap;
        this.googleMap.addMarker(new MarkerOptions().position(MyLoc).title("내 위치"));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyLoc, 17.0f));
        this.googleMap.addCircle(new CircleOptions().center(MyLoc) // 내 위치중심
                .radius(250).strokeColor(Color.RED)
                .strokeWidth(1f).fillColor(Color.parseColor("#33FF0000")));

        Log.e("++", "getShopData() 호출");

        ShopData = getShopData();
        Log.e("++", "getShopData() 실행완료");
        MarkerOptions opt = new MarkerOptions();
        System.out.println("ShoData length :: " + ShopData.length);
        for(int i=1; i< ShopData.length; i++) {
            String[] gps = ShopData[i][2].split(",");
            latLng = new LatLng(Double.parseDouble(gps[0]), Double.parseDouble(gps[1]));

            addMarkers(latLng, ShopData[i][0], ShopData[i][3], ShopData[i][4]); // gps, title, snippet
        }
    }

    public void addMarkers(LatLng loc, String title, String open, String close)
    {
        MarkerOptions opt = new MarkerOptions();

        //세탁소위치 마커 설정
        opt.position(loc); // 위치
        opt.title(title); // 타이틀
        opt.snippet(open + "/" + close); // 세부 설명
        opt.icon(BitmapDescriptorFactory.defaultMarker(200f)); // 마커 색상

        this.googleMap.addMarker(opt).showInfoWindow(); // 마커 추가

        //정보창 클릭 리스너
        googleMap.setOnInfoWindowClickListener(infoWindowClickListener);
    }


    // 등록되어 있는 세탁소 정보를 모두 불러옴
    public String[][] getShopData()
    {
        int size=1;
        try
        {
            Log.e("확인합니다", data_in.toString());
            if (data_in.equals(null) || data_in.equals("") || data_in == null) {
                Log.e("데이터 없음", "null 입니다");
//                asyncTask.execute();
            }
            Log.e("!!", "data_in 전");
                      JSONArray jsonArray = new JSONArray(data_in);
            Log.e("!!", "data_in 후");
            size = jsonArray.length();
            Log.e("몇개", Integer.valueOf(size).toString());
            String [][] strings = new String[size][5];

            for(int i=1; i< jsonArray.length(); i++) // jsonArray[0]은 북마크 정보
            {
                JSONObject obj = jsonArray.getJSONObject(i);
                System.out.println(i + "번째 resJson :: " + obj.toString());

                String name = obj.get("name").toString();
                String address = obj.get("address").toString();
                String gps = obj.get("gps").toString();
                String opentime = obj.get("opentime").toString();
                String closetime = obj.get("closetime").toString();

                System.out.println("gps is :: " + gps);
                strings[i][0]=name;
                strings[i][1]=address;
                strings[i][2]=gps;
                strings[i][3]=opentime;
                strings[i][4]=closetime;
            }

            return strings;
        } catch(Exception e) { e.printStackTrace(); }

        return null;
    }


    // Marker's Info Window Click Event
    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            //String markerId = marker.getId();
            LatLng shop = marker.getPosition();
            double shoplat = shop.latitude;
            double shoplng = shop.longitude;


            Toast.makeText(getApplicationContext(), shop.toString(), Toast.LENGTH_SHORT).show();
            Intent moveShop = new Intent(getApplicationContext(), WasherListActivity.class);
            moveShop.putExtra("Bookmark", "null");
            moveShop.putExtra("id", id);
            moveShop.putExtra("shoplat", shoplat);
            moveShop.putExtra("shoplng", shoplng);
            startActivity(moveShop);
        }
    };


    // Bookmark Cleaner's List Request
    public class data_AsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            System.out.println("data_Task 실행");
            //String urlstr = "http://210.119.12.75:7878/phone/myBookmark";
            String urlstr = "http://192.168.0.150:8080/phone/myBookmark";
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
                System.out.println("is :: " + is);
                if(is != null) {
                    System.out.println("is는 낫널");
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
                    Log.e("!!", "data_in 전송 완료");

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
            addCleanerListView();
        }
    }

    public void addTabs() { // Adding Tabs in MainActivity
        // TabHost > TabWidget > TabButton
        // TabHost 실체화
        tabHost = (TabHost) findViewById(R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec spec1 = tabHost.newTabSpec("Tab1").setContent(R.id.tab1).setIndicator(getString(R.string.tab1_name));
        tabHost.addTab(spec1);

        TabHost.TabSpec spec2 = tabHost.newTabSpec("Tab2").setContent(R.id.tab2).setIndicator(getString(R.string.tab2_mapView));
        tabHost.addTab(spec2);

    }

    public void addCleanerListView() { // Add List in MainActivity
        // tab1 > listView = cleanerList

        cleaner_list = (ListView) findViewById(R.id.cleaner_list);
        adapter = new cleanerListAdapter();

        try {
            //JSONObject obj = new JSONObject(data_in);

            JSONArray jsonArray = new JSONArray(data_in);
            Log.e("세탁소 정보보기  data_in >>", data_in.toString());
            JSONObject obj = new JSONObject(jsonArray.get(0).toString());

            m1 = obj.get("B1_Mng").toString();
            m2 = obj.get("B2_Mng").toString();
            m3 = obj.get("B3_Mng").toString();

            if(!(obj.get("B1_name").toString().equals("null"))) {
                adapter.addItem(new cleanerInfo(
                        obj.get("B1_name").toString(),
                        obj.get("B1_open").toString(),
                        obj.get("B1_close").toString()
                ));
            }

            if (!(obj.get("B2_name").toString().equals("null"))) {
                adapter.addItem(new cleanerInfo(
                        obj.get("B2_name").toString(),
                        obj.get("B2_open").toString(),
                        obj.get("B2_close").toString()
                ));
            }

            if (!(obj.get("B3_name").toString().equals("null"))) {
                adapter.addItem(new cleanerInfo(
                        obj.get("B3_name").toString(),
                        obj.get("B3_open").toString(),
                        obj.get("B3_close").toString()
                ));
            }

         } catch (Exception exex) {
            exex.printStackTrace();
        }

        // ** cleanerAdapter 객체 생성
        cleaner_list.setAdapter(adapter);

        // 클릭 > 선택한 세탁소 정보보기
        cleaner_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id_k) {
                // ListView Position = 0부터 시작
                if(position == 0) {
                    Bookmark = m1;
                } else if (position == 1) {
                    Bookmark = m2;
                } else if (position == 2) {
                    Bookmark = m3;
                }

                Intent moveShop = new Intent(getApplicationContext(), WasherListActivity.class);
                moveShop.putExtra("Bookmark", Bookmark);
                moveShop.putExtra("id", id);
                startActivity(moveShop);
            }
        });
    }

    // ---------------------------------------------------------------------------------------
    // List 관련 Class
    // cleanerList_adapter
    // ---------------------------------------------------------------------------------------
    class cleanerListAdapter extends BaseAdapter {
        ArrayList<cleanerInfo> main_items = new ArrayList<>();

        public void addItem(cleanerInfo item) {
            main_items.add(item);
        }

        @Override
        public int getCount() {
            return main_items.size();
        }

        @Override
        public Object getItem(int position) {
            return main_items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            cleanerView tab1_listview = new cleanerView(getApplicationContext());
            cleanerInfo item = main_items.get(position);

            // 각 Item 객체에 해당하는 내용 설정.
            tab1_listview.setName(item.getW_Name());
            tab1_listview.setOpentime(item.get_oTime());
            tab1_listview.setClosetime(item.get_cTime());
            tab1_listview.setImg(R.drawable.emptywasher);

            return tab1_listview;
        }
    }


    // Add Daum Map
    /*
    private class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            mylati = location.getLatitude();
            mylongi = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private void startLocationService() {
        // 위치 관리자 객체 참조
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 위치 정보를 받을 리스너 생성
        GPSListener gpsListener = new GPSListener();
        long minTime = 1000;
        float minDistance = 0;

        try {
            // GPS를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 네트워크를 이용한 위치 요청
            manager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    minTime,
                    minDistance,
                    gpsListener);

            // 위치 확인이 안되는 경우에도 최근에 확인된 위치 정보 먼저 확인
            Location lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                mylati = lastLocation.getLatitude();
                mylongi = lastLocation.getLongitude();

            }  else {
                manager.removeUpdates(gpsListener); // 리스너 해제
            }
        } catch(SecurityException ex) {
            ex.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "위치 확인이 시작되었습니다. 로그를 확인하세요.", Toast.LENGTH_SHORT).show();

    }

    public void addMyLocation(MapView mapView) {
        // My Location
        //MapPoint myPoint = MapPoint.mapPointWithGeoCoord(mylati, mylongi);
        myPoint = MapPoint.mapPointWithGeoCoord(mylati, mylongi);


        // make My Location Marker
        MapPOIItem mymarker = new MapPOIItem();
        mymarker.setItemName("My Location");
        mymarker.setTag(0);
        mymarker.setMapPoint(myPoint);
        mymarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        mymarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        // add Marker
        mapView.addPOIItem(mymarker);

        MapCircle myCircle = new MapCircle(
                myPoint, // center
                250, // radius
                Color.argb(128, 255, 0, 0), // strokeColor green
                Color.argb(128, 0, 255, 0) // fillColor

                //Color.argb(128,255,0,0), // strokeColor yellow
                //Color.argb(128,255,255,0) // fillColor
        );

        myCircle.setTag(1234);
        mapView.addCircle(myCircle);

        // 지도뷰의 중심좌표와 줌레벨을 Circle이 모두 나오도록 조정.
        MapPointBounds[] mapPointBoundsArray = { myCircle.getBound() };
        MapPointBounds mapPointBounds = new MapPointBounds(mapPointBoundsArray);
        int padding = 50; // px
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
    }

    // ------------------------------------------------------------------------------------------
    // mapView Marker Method(세탁소 마커 표시/ 위도,경도 기반)
    //-------------------------------------------------------------------------------------------
    public void addMarker(MapView mapView) {
        // ------------------------------------------------------------------------------------------
        // make Cleaner Marker
        // ------------------------------------------------------------------------------------------
        // make Marker1
        MapPoint mapPoint1 = MapPoint.mapPointWithGeoCoord(35.11634,129.087726);

        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Pukyung Univ");
        marker.setTag(1);
        marker.setMapPoint(mapPoint1);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        // add Marker
        mapView.addPOIItem(marker);

        // make Marker2
        MapPoint mapPoint2 = MapPoint.mapPointWithGeoCoord(35.1223422,129.0995152);

        MapPOIItem marker2 = new MapPOIItem();
        marker2.setItemName("DongMyung Univ");
        marker2.setTag(2);
        marker2.setMapPoint(mapPoint2);
        marker2.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker2.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        // add Marker2
        mapView.addPOIItem(marker2);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {
        // 위치정보 읽어오기
        startLocationService();
        // 내 위치 표시
        addMyLocation(mapView);
        // 세탁소 마커 표시
        addMarker(mapView);
        // 지도 중심 설정
        mapView.setMapCenterPoint(myPoint, true); // Center Camera
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        // 지도의 이동이 완료된 경우 호출된다.
    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        // 사용자가 MapView 에 등록된 POI Item 아이콘(마커)를 터치한 경우 호출된다.
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
        // 권장하지 않음
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
        Intent intent = new Intent(getApplicationContext(), WasherListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {
            ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.setting);
            ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("Custom CalloutBalloon");
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }
    }

    */

}
