package com.example.pknu.asuhwasher;

/**
 * Created by PKNU on 2017-07-21.
 */

public class cleanerInfo {
    // tab1의 ListView에 들어갈 Item 객체의 데이터 정보

    String w_Name;
    String openTime, closeTime;
    int resId;

    public cleanerInfo(String name, String otime, String ctime) {
        w_Name = name;
        openTime = otime;
        closeTime = ctime;
    }

    public cleanerInfo(String name, String otime, String ctime, int resId) {
        w_Name = name;
        openTime = otime;
        closeTime = ctime;
        this.resId = resId;
    }

    public String getW_Name() {
        return w_Name;
    }

    public void setW_Name(String w_Name) {
        this.w_Name = w_Name;
    }

    public String get_oTime() {
        return openTime;
    }

    public void set_oTime(String otime) {
        openTime = otime;
    }

    public String get_cTime() {
        return closeTime;
    }

    public void set_cTime(String ctime) {
        closeTime = ctime;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
}
