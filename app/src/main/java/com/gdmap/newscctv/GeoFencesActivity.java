package com.gdmap.newscctv;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MyLocationStyle;

public class GeoFencesActivity extends AppCompatActivity implements LocationSource, AMapLocationListener{

    private AMap aMap;
    private SupportMapFragment mapFragment;
    private OnLocationChangedListener mOnLoctionChangeListener;
    private AMapLocationClient mapLocationClient;
    private AMapLocationClientOption mapLocationClientOption;
    private AMapGeoFence mAMapGeoFence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fences);
        mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        setMap();
        mAMapGeoFence = new AMapGeoFence(this, aMap, handler);
    }

    public void setMap(){
        if(aMap == null){
            aMap = mapFragment.getMap();
            UiSettings settings = aMap.getUiSettings();
            if(settings != null){
                settings.setZoomControlsEnabled(false);
                settings.setRotateGesturesEnabled(false);
                settings.setMyLocationButtonEnabled(true);
            }
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.navi_map_gps_locked));
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0))
                    .strokeColor(Color.argb(0, 0, 0, 0)).strokeWidth(1);
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);//LOCATION_TYPE_LOCATE
            aMap.setLocationSource(this);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);//设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
            aMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        }
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
//                    Toast.makeText(getApplicationContext(), "添加围栏成功",
//                            Toast.LENGTH_SHORT).show();
                    mAMapGeoFence.drawFenceToMap();
                    break;
                case 1:
                    int errorCode = msg.arg1;
                    Toast.makeText(getApplicationContext(),
                            "添加围栏失败 " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    String statusStr = (String) msg.obj;
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
         if(aMapLocation != null && mOnLoctionChangeListener != null){
               if(aMapLocation != null && aMapLocation.getErrorCode() == 0){
                    this.mOnLoctionChangeListener.onLocationChanged(aMapLocation);
               }else{
                   String errText = "定位失败," + aMapLocation.getErrorCode() + ": "
                           + aMapLocation.getErrorInfo();
                   Log.e("AmapErr", errText);
                   ToastUtil.show(GeoFencesActivity.this, errText);
               }
         }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.mOnLoctionChangeListener = onLocationChangedListener;
        if(mapLocationClient == null){
            mapLocationClient = new AMapLocationClient(GeoFencesActivity.this);
            mapLocationClientOption = new AMapLocationClientOption();
            mapLocationClientOption.setOnceLocation(true);
            mapLocationClientOption.setOnceLocationLatest(true);
            mapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mapLocationClient.setLocationOption(mapLocationClientOption);
            mapLocationClient.setLocationListener(this);
            mapLocationClient.startLocation();
        }

    }

    @Override
    public void deactivate() {
          mOnLoctionChangeListener = null;
          if(mapLocationClient != null){
              mapLocationClient.stopLocation();
              mapLocationClient.onDestroy();
          }
          mapLocationClient = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapGeoFence.removeAll();
    }

}
