package com.example.pknu.asuhwasher;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class SearchActivity extends AppCompatActivity {

    // 우체국 오픈API KEY
    private String key = "af15cf8118c1b7f9b1501736522724";
    private TextView currentTxt, totalTxt;
    private EditText addressEdit ;
    private Button searchBtn, preBtn, nextBtn;
    private ListView addressListView;
    private ArrayAdapter<String> addressListAdapter;
    final String apiURL = "https://biz.epost.go.kr/KpostPortal/openapi";
    public static final String TARGET = "postNew";
    public static final int COUNT_PER_PAGE = 10;

    public int totalCount, totalPage, countPerPage, currentPage;

    // 사용자가 입력한 주소
    private String putAddress;

    // 우체국으로부터 반환받은 우편주소 리스트
    private ArrayList<String> addressSearchResultArr = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        currentTxt = (TextView) findViewById(R.id.currentTxt);
        totalTxt = (TextView) findViewById(R.id.totalTxt);
        preBtn = (Button) findViewById(R.id.preBtn);
        nextBtn = (Button)findViewById(R.id.nextBtn);
        addressEdit = (EditText) findViewById(R.id.addressEdit);
        searchBtn = (Button) findViewById(R.id.searchBtn);
        addressListView = (ListView) findViewById(R.id.addressList);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAddress(addressEdit.getText().toString());
           //     totalTxt.setText(String.valueOf(totalPage));
            }
        });
    }

    // 주소 검색
    private void getAddress(String address)
    {
        putAddress = address;
        new GetAddressDataTask().execute();
    }

    // 주소 xml 쿼리문 작성
    public String setQuery(int currentPage) throws Exception
    {
        StringBuffer sb = new StringBuffer(3);
        sb.append(apiURL);
        sb.append("?regkey="+key);
        sb.append("&target=" + TARGET);
        sb.append("&query=" + URLEncoder.encode(putAddress, "EUC-KR"));
        sb.append("&countPerPage=" + COUNT_PER_PAGE);
        sb.append("&currentPage=" + currentPage);

        String query = sb.toString();

        return query;
    }

    // 주소 root 얻기
    public Element getAddrDataRoot(HttpURLConnection conn, int currentPage)
    {
        try
        {

            String query = setQuery(currentPage);
            conn = requestAddrData(conn, query);
            String xmlData = readXML(conn);

            // read Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // make builder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // parsing xml
            Document doc = builder.parse(new InputSource(new StringReader(xmlData)));

            Element root = doc.getDocumentElement();

            return root;

        } catch (Exception e) { e.printStackTrace();}

        return null;

    }

    // 우체국 openapi에 주소데이터 요청
    public HttpURLConnection requestAddrData(HttpURLConnection conn, String query)
    {
        try
        {
//            StringBuffer sb = new StringBuffer(3);
//            sb.append(apiURL);
//            sb.append("?regkey="+key);
//            sb.append("&target=" + TARGET);
//            sb.append("&query=" + URLEncoder.encode(addr, "EUC-KR"));
//            sb.append("&countPerPage=" + countPerPage);
//            sb.append("&currentPage=" + currentPage);
//
//            String query = sb.toString();

            // +++++++++++  download URL  +++++++++++++++
            URL url = new URL(query);

            Log.e("Test", query);


            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("accept-language", "ko");   // 언어설정

            return conn;

        }catch (Exception e) { e.printStackTrace();}

        return null;
    }

    // 주소 데이터 xml 읽어오기
    public String readXML(HttpURLConnection conn)
    {
        try
        {
            // ---- XML read ---
            byte[] bytes = new byte[4096];
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while(true)
            {
                int red = is.read(bytes);
                if(red < 0) break;
                baos.write(bytes, 0, red);
            }
            String xmlData = baos.toString("utf-8");
//                Log.e("xmlData", xmlData);

            baos.close();
            is.close();
            conn.disconnect();

            return xmlData;
        } catch(Exception e) { e.printStackTrace(); }

        return "";

    }

    // 주소 통해 위도, 경도 검색
    public Location findGeoPoint(String address)
    {

        Location loc = new Location("");
        Geocoder coder = new Geocoder(getApplicationContext(), Locale.KOREA);

        List<Address> addr = null;
        try
        {
            addr = coder.getFromLocationName(address, 5);

        } catch(IOException e){ e.printStackTrace();}

        if(addr != null)
        {
            for(int i=0; i<addr.size(); i++)
            {
                Address lating = addr.get(i);
                double lat = lating.getLatitude();
                double lon = lating.getLongitude();
                loc.setLatitude(lat);
                loc.setLongitude(lon);
            }
        }
        return loc;
    }

    // 주소 데이터
    private class GetAddressDataTask extends AsyncTask<String, Void, HttpResponse>
    {
        @Override
        protected HttpResponse doInBackground(String... urls) {
            HttpResponse response = null;

            ArrayList<String> addressInfo = new ArrayList<String>();

            HttpURLConnection conn = null;
//            try
//            {
//                Element root = getAddrDataRoot(conn, 1);
//
///*
//                StringBuffer sb = new StringBuffer(3);
//                sb.append(apiURL);
//                sb.append("?regkey=" + key + "&target=" + TARGET + "&query=");
//                 //   + URLEncoder.encode(putAddress));
//                sb.append(URLEncoder.encode(putAddress, "EUC-KR"));
////                sb.append(URLEncoder.encode("우정사업본부", "EUC-KR"));
//
//
//                String query = sb.toString();
//
//                // +++++++++++  download URL  +++++++++++++++
//                URL url = new URL(query);
//
//                Log.e("Test", query);
//
//
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestProperty("accept-language", "ko");   // 언어설정
//
//                // +++++++
//
//                // ---- XML read ---
//                byte[] bytes = new byte[4096];
//                InputStream is = conn.getInputStream();
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                while(true)
//                {
//                    int red = is.read(bytes);
//                    if(red < 0) break;
//                    baos.write(bytes, 0, red);
//                }
//                String xmlData = baos.toString("utf-8");
////                Log.e("xmlData", xmlData);
//
//                baos.close();
//                is.close();
//                conn.disconnect();
//
//                // read Document
//                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//                // make builder
//                DocumentBuilder builder = factory.newDocumentBuilder();
//
//                // parsing xml
//                Document doc = builder.parse(new InputSource(new StringReader(xmlData)));
//
//                Element root = doc.getDocumentElement();
//*/
//
//                // get ChildNode
//                NodeList all = root.getChildNodes();
//
//                NodeList pageInfoList = root.getElementsByTagName("pageinfo");
//                Element e_pageInfoList = (Element) pageInfoList.item(0);
//
//                getPageInfo(e_pageInfoList);
//
////
////                // find totalCount
//////                NodeList totalCntList = e_pageInfoList.getElementsByTagName("totalCount");
//////
//////                Element e_totalCnt = (Element) totalCntList.item(0);
//////                Node totalCntNode = e_totalCnt.getFirstChild();
//////
//////                int totalCount = Integer.parseInt(totalCntNode.getNodeValue());
////                int totalCount = Integer.parseInt(getItem(e_pageInfoList, "totalCount", 0));
////                System.out.println("totalCount is " + totalCount);
////
////                // find totalPage
//////                NodeList totalPageList = e_pageInfoList.getElementsByTagName("totalPage");
//////
//////                Element e_totalPage = (Element) totalPageList.item(0);
//////                Node totalPageNode = e_totalPage.getFirstChild();
//////
//////                int totalPage = Integer.parseInt(totalPageNode.getNodeValue());
////                int totalPage = Integer.parseInt(getItem(e_pageInfoList, "totalPage", 0));
////                System.out.println("totalPage is " + totalPage);
////
////                // find countPerPage
//////                NodeList countPerPageList = e_pageInfoList.getElementsByTagName("countPerPage");
//////
//////                Element e_countPerPage = (Element) countPerPageList.item(0);
//////                Node countPerPageNode = e_countPerPage.getFirstChild();
//////
//////                int countPerPage = Integer.parseInt(countPerPageNode.getNodeValue());
////                int countPerPage = Integer.parseInt(getItem(e_pageInfoList, "countPerPage", 0));
////                System.out.println("countPerPage is " + countPerPage);
////
////                // find currentPage
//////                NodeList currentPageList = e_pageInfoList.getElementsByTagName("currentPage");
//////
//////                Element e_currentPage = (Element) currentPageList.item(0);
//////                Node currentPageNode = e_currentPage.getFirstChild();
//////
//////                int currentPage = Integer.parseInt(currentPageNode.getNodeValue());
////                int currentPage = Integer.parseInt(getItem(e_pageInfoList, "currentPage", 0));
////                System.out.println("currentPage is " + currentPage);
////
//
//                // find itemlist
//                NodeList itemList = root.getElementsByTagName("itemlist");
//                Element e_itemList = (Element) itemList.item(0);
//
//                int rest = 0, quot=0;
//
//                rest = totalCount % countPerPage;
//                quot = totalCount / countPerPage;
//                int count = 10;
//
//
//                // ---
////                System.out.println("!!! : " + e_itemList.getChildNodes().getLength());
//              for(int i=0; i<10; i++)
//              {
//
//                  String postcd = getItem(e_itemList, "postcd", i);
//                  String address = getItem(e_itemList, "address", i);
//                  String addrjibun = getItem(e_itemList, "addrjibun", i);
////                  Element e_postcd = (Element)postcds.item(i);
////                  Element e_address = (Element)addresses.item(i);
////                  Element e_addrjibuns = (Element)addrjibuns.item(i);
////
////                  Node postcdNode = e_postcd.getFirstChild();
////                  Node addressNode = e_address.getFirstChild();
////                  Node addrjibunNode = e_addrjibuns.getFirstChild();
//
////                  System.out.println(i + "번째 우편주소 : " + postcdNode.getNodeValue());
//               //   System.out.println(i + "번째 우편주소 : " + postcd);
//                  System.out.println(i + "번째 신주소 : " + address);
//                  System.out.println(i + "번째 구주소 : " + addrjibun);
////                  addressInfo.add(address + "\n우편번호 : " + postcd);
//                  addressInfo.add(address);
//              }
//
//// +++++++++++++++++
//                for(int i=2; i<=totalPage; i++)
//                {
//                    Element root2 = getAddrDataRoot(conn, i);
//                    NodeList all2 = root2.getChildNodes();
//
//                    NodeList pageInfoList2 = root2.getElementsByTagName("pageinfo");
//                    Element e_pageInfoList2 = (Element) pageInfoList2.item(0);
//
//
//                    // find itemlist
//                    NodeList itemList2 = root2.getElementsByTagName("itemlist");
//                    Element e_itemList2 = (Element) itemList2.item(0);
//
//                    if(i==totalPage) count = rest;
//                    for(int j=0; j<count; j++)
//                    {
//                        String postcd = getItem(e_itemList2, "postcd", j);
//                        String address = getItem(e_itemList2, "address", j);
//                        String addrjibun = getItem(e_itemList2, "addrjibun", j);
////                  Element e_postcd = (Element)postcds.item(i);
////                  Element e_address = (Element)addresses.item(i);
////                  Element e_addrjibuns = (Element)addrjibuns.item(i);
////
////                  Node postcdNode = e_postcd.getFirstChild();
////                  Node addressNode = e_address.getFirstChild();
////                  Node addrjibunNode = e_addrjibuns.getFirstChild();
//
////                  System.out.println(i + "번째 우편주소 : " + postcdNode.getNodeValue());
////                        System.out.println(i + "번째 우편주소 : " + postcd);
////                        System.out.println(i + "번째 신주소 : " + address);
////                        System.out.println(i + "번째 구주소 : " + addrjibun);
//                        addressInfo.add(address);
//                    }
//                }
//                // +++++++++++++++++
//                // http://parkys.tistory.com/entry/XML%EB%AC%B8%EC%84%9C%EB%A5%BC-Document-%EB%A1%9C-%ED%8C%8C%EC%8B%B1%ED%95%B4%EC%84%9C-%EC%9A%94%EC%86%8C%EB%A5%BC-%EA%B5%AC%ED%95%98%EA%B8%B0-DocumentBuilderFactory-DocumentBuilder-getDocumentElement
//                // +++++++
//
//                addressSearchResultArr = addressInfo;
//
//
//                publishProgress();
//
//
//
//
//            } catch (Exception e) { e.printStackTrace();}
//
            try
            {
                int page = 1;
                int rest=10, quot = 1;
                int count = rest;


                while(true)
                {
                    Element root = getAddrDataRoot(conn, page);

                    // getChildNode
                    NodeList all = root.getChildNodes();
                    NodeList pageInfoList = root.getElementsByTagName("pageinfo");
                    Element e_pageInfoList = (Element) pageInfoList.item(0);

                    if(page == 1)
                    {
                        getPageInfo(e_pageInfoList);	// 페이지정보들 저장
                        rest = totalCount % countPerPage;
                    }

                    NodeList itemList = root.getElementsByTagName("itemlist");
                    Element e_itemList = (Element) itemList.item(0);

                    if(totalPage == page) count = rest;

                    for(int i=0; i<count; i++) // ListView에 Item 추가
                    {
//                        String postcd = getItem(e_itemList, "postcd", i);
                        String address = getItem(e_itemList, "address", i);
                        String addrjibun = getItem(e_itemList, "addrjibun", i);
//                        System.out.println(i + "번째 신주소 : " + address);
                        addressInfo.add(address);

                        addressSearchResultArr = addressInfo;
                        publishProgress();

                    }

                    if(totalPage == page) break;
                    page++;
                }

            } catch(Exception e) { e.printStackTrace();}
            finally {
                try
                {
                    if(conn != null)
                        conn.disconnect();
                } catch (Exception e) { }
            }
            return response;
        }

        // 데이터 얻기
        public String getItem(Element e_itemList, String str, int index)
        {
            NodeList nodeList = e_itemList.getElementsByTagName(str);
            Element elementItem = (Element)nodeList.item(index);
            Node node = elementItem.getFirstChild();

            return node.getNodeValue();
        }

        // 페이지정보 얻기
        private void getPageInfo(Element e_pageInfoList)
        {
            // find totalCount
//                NodeList totalCntList = e_pageInfoList.getElementsByTagName("totalCount");
//
//                Element e_totalCnt = (Element) totalCntList.item(0);
//                Node totalCntNode = e_totalCnt.getFirstChild();
//
//                int totalCount = Integer.parseInt(totalCntNode.getNodeValue());
            totalCount = Integer.parseInt(getItem(e_pageInfoList, "totalCount", 0));
            System.out.println("totalCount is " + totalCount);

            // find totalPage
//                NodeList totalPageList = e_pageInfoList.getElementsByTagName("totalPage");
//
//                Element e_totalPage = (Element) totalPageList.item(0);
//                Node totalPageNode = e_totalPage.getFirstChild();
//
//                int totalPage = Integer.parseInt(totalPageNode.getNodeValue());
            totalPage = Integer.parseInt(getItem(e_pageInfoList, "totalPage", 0));
            System.out.println("totalPage is " + totalPage);

            // find countPerPage
//                NodeList countPerPageList = e_pageInfoList.getElementsByTagName("countPerPage");
//
//                Element e_countPerPage = (Element) countPerPageList.item(0);
//                Node countPerPageNode = e_countPerPage.getFirstChild();
//
//                int countPerPage = Integer.parseInt(countPerPageNode.getNodeValue());
            countPerPage = Integer.parseInt(getItem(e_pageInfoList, "countPerPage", 0));
            System.out.println("countPerPage is " + countPerPage);

            // find currentPage
//                NodeList currentPageList = e_pageInfoList.getElementsByTagName("currentPage");
//
//                Element e_currentPage = (Element) currentPageList.item(0);
//                Node currentPageNode = e_currentPage.getFirstChild();
//
//                int currentPage = Integer.parseInt(currentPageNode.getNodeValue());
            currentPage = Integer.parseInt(getItem(e_pageInfoList, "currentPage", 0));
            System.out.println("currentPage is " + currentPage);
        }


        // listview
        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            String[] addressStrArray = new String[addressSearchResultArr.size()];
            addressStrArray = addressSearchResultArr.toArray(addressStrArray);
            addressListAdapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, addressStrArray);
            addressListView.setAdapter(addressListAdapter);
            addressListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            addressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // get Lat, Lon From Address
                    String item = (String) parent.getItemAtPosition(position);
                    String substr = item.substring(0, item.indexOf("("));

                    Location loc = findGeoPoint(substr);

                    double lat = loc.getLatitude();
                    double lng = loc.getLongitude();

                    // 회원가입화면으로 전환
                    Intent intent = new Intent(getApplicationContext(), JoinActivity.class);
                    intent.putExtra("mylocation", item);
                    intent.putExtra("lat", lat);
                    intent.putExtra("lng", lng);
                    Log.e("GPS 확인하기 >>> ", lat + "/" + lng);
                    startActivity(intent);
                }
            });
        }

    }
}
