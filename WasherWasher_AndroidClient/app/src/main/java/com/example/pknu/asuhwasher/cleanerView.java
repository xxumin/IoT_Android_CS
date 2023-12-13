package com.example.pknu.asuhwasher;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by PKNU on 2017-07-21.
 */

public class cleanerView extends LinearLayout {
    // tab1의 ListView 세팅
    TextView t1, opentime, closetime;
    ImageView img;

    public cleanerView(Context context) {
        super(context);
        init(context);
    }
    public cleanerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.list_cleaner, this, true);

        t1 = (TextView) findViewById(R.id.w_name); // cleaner's name
        opentime = (TextView) findViewById(R.id.open_txt); // cleaner's open Time
        closetime = (TextView) findViewById(R.id.close_txt); // cleaner's close Time

        img =(ImageView) findViewById(R.id.img); // cleaner's shop img
    }

    public void setName(String name) {
        t1.setText(name);
    }

    public void setOpentime(String otime) {
        opentime.setText(otime);
    }

    public void setClosetime(String ctime) {
        closetime.setText(ctime);
    }

    public void setImg(int resId) {
        img.setImageResource(resId);
    }
}
