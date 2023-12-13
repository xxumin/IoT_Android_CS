package com.example.pknu.asuhwasher;

/**
 * Created by PKNU on 2017-07-21.
 */

public class washerInfo {
    // tab2의 ListView Item의 관련된 정보
    String washer_id;
    String r_time; // 추후 Date 형으로 바꿔야할것..
    int img_id;

    public washerInfo(String id, String time, int img_id) {
        washer_id = id;
        r_time = time;
        this.img_id = img_id;
    }

    public String getWasher_id() {
        return washer_id;
    }

    public void setWasher_id(String washer_id) {
        this.washer_id = washer_id;
    }

    public String getR_time() {
        return r_time;
    }

    public void setR_time(String r_time) {
        this.r_time = r_time;
    }

    public int getImg_id() {
        return img_id;
    }

    public void setImg_id(int img_id) {
        this.img_id = img_id;
    }
}
