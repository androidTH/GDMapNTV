package com.gdmap.newscctv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LastLocation_Activity extends CheckPermissionsActivity implements View.OnClickListener {

    @BindView(R.id.bt_lastLoc)
    public Button mBtnLastLoc;
    @BindView(R.id.tv_result)
    public TextView mTvLastLoc;
    private AMapLocationClient mapLocationClient;
    private Unbinder unBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_location);
        unBind = ButterKnife.bind(this);
        mBtnLastLoc.setOnClickListener(this);
        init();
    }

    private void init(){
        mapLocationClient = new AMapLocationClient(getApplicationContext());
    }

    @Override
    public void onClick(View view) {
           AMapLocation locationInfo = mapLocationClient.getLastKnownLocation();
           mTvLastLoc.setText(Utils.getLocationStr(locationInfo));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unBind.unbind();
        if(null != mapLocationClient){
            mapLocationClient.onDestroy();
            mapLocationClient = null;
        }
    }
}
