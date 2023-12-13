package com.example.pknu.asuhwasher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by PKNU on 2017-07-21.
 */

public class washerView extends LinearLayout {
    // tab2의 ListView 세팅 정보
    TextView washer_id_txt, r_time_txt;
    ProgressBar pbar;
    ImageView img;

    public washerView(Context context) {
        super(context);
        init(context);
    }

    public washerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_washer, this, true);

        washer_id_txt = (TextView) findViewById(R.id.wNum);
        r_time_txt = (TextView) findViewById(R.id.waiting_time_txt);
        pbar = (ProgressBar) findViewById(R.id.progressBar);
        img = (ImageView) findViewById(R.id.imageView);
    }

    public void setWasher_id_txt(String id) {
        washer_id_txt.setText(id);
    }

    public void setR_time_txt(String time) {
        r_time_txt.setText(time);
    }

    public void setPbar(int r_time) {
        pbar.setProgress(r_time);
    }

    public void setImg(int imgId) {
        img.setImageResource(imgId);
    }
}
