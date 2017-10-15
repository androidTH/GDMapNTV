package com.gdmap.newscctv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.fence.GeoFence;
import com.amap.api.fence.GeoFenceClient;
import com.amap.api.fence.GeoFenceListener;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.DPoint;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeoFence_District_Activity extends CheckPermissionsActivity implements
        GeoFenceListener ,LocationSource, AMapLocationListener, AMap.OnMapLoadedListener{

    @BindView(R.id.tv_customid)
    public EditText mTvCustomId;
    @BindView(R.id.tv_keyword)
    public EditText mTvKeyWord;
    @BindView(R.id.mapview)
    public MapView mapView;
    @BindView(R.id.btn_geofence)
    public Button mBtnGeoFence;

    private GeoFenceClient geoFenceClient;
    private AMap mAMapControl;
    private MyLocationStyle myLocationStyle;
    private AMapLocationClient mapLocationClient;
    private OnLocationChangedListener listener;
    List<GeoFence> fenceList = new ArrayList<GeoFence>();

    private static final String GEOFENCE_BROADCAST_ACTION = "com.example.geofence.keyword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence__district);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        initMapView();
        mBtnGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFence();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mapView != null){
            mapView.onDestroy();
        }
        unregisterReceiver(geofenceReceiver);

        if(null != geoFenceClient){
            geoFenceClient.removeGeoFence();
        }

        if(null != null){
            mapLocationClient.onDestroy();
        }
    }

    private void initMapView(){
        if(mAMapControl == null){
            mAMapControl = mapView.getMap();
            mAMapControl.getUiSettings().setRotateGesturesEnabled(true);
            mAMapControl.getUiSettings().setZoomControlsEnabled(false);
            mAMapControl.moveCamera(CameraUpdateFactory.zoomBy(6));
            mAMapControl.setOnMapLoadedListener(this);
        }
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(0);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        mAMapControl.setMyLocationStyle(myLocationStyle);
        mAMapControl.getUiSettings().setMyLocationButtonEnabled(true);
        mAMapControl.setMyLocationEnabled(true);
//        mAMapControl.setLocationSource(this);

        geoFenceClient = new GeoFenceClient(getApplicationContext());
        geoFenceClient.setActivateAction(GeoFence.STATUS_IN);
        geoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        geoFenceClient.setGeoFenceListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(geofenceReceiver, filter);

    }

    @Override
    public void onGeoFenceCreateFinished(List<GeoFence> list, int i, String s) {
        Message msg = geoFenceHandler.obtainMessage();
        if(i == GeoFence.ADDGEOFENCE_SUCCESS){
             this.fenceList = list;
             msg.what = 0;
             msg.obj = s;
          }else{
            msg.what = 1;
            msg.obj = i;
          }
          geoFenceHandler.sendMessage(msg);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.listener = onLocationChangedListener;
        if(mapLocationClient == null){
            mapLocationClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption mapLocationClientOption = new AMapLocationClientOption();
            mapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mapLocationClientOption.setOnceLocation(true);
            mapLocationClientOption.setOnceLocationLatest(true);
            mapLocationClient.setLocationOption(mapLocationClientOption);
            mapLocationClient.setLocationListener(this);
            mapLocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
       this.listener = null;
       if(mapLocationClient != null){
           mapLocationClient.stopLocation();
           mapLocationClient.onDestroy();
           mapLocationClient = null;
       }
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
         if(this.listener != null && aMapLocation != null){
             if(aMapLocation.getErrorCode() == 0){
                 this.listener.onLocationChanged(aMapLocation);
             }else{
                 Toast.makeText(GeoFence_District_Activity.this, "定位错误", Toast.LENGTH_LONG).show();
             }
         }
    }

    //地图加载成功时回掉
    @Override
    public void onMapLoaded() {

    }

    private BroadcastReceiver geofenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)){
                Bundle bundle = intent.getExtras();
                String customId = bundle
                        .getString(GeoFence.BUNDLE_KEY_CUSTOMID);
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
                //status标识的是当前的围栏状态，不是围栏行为
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
                StringBuffer sb = new StringBuffer();
                switch (status) {
                    case GeoFence.STATUS_LOCFAIL :
                        sb.append("定位失败");
                        break;
                    case GeoFence.STATUS_IN :
                        sb.append("进入围栏 ");
                        break;
                    case GeoFence.STATUS_OUT :
                        sb.append("离开围栏 ");
                        break;
                    case GeoFence.STATUS_STAYED :
                        sb.append("停留在围栏内 ");
                        break;
                    default :
                        break;
                }
                if(status != GeoFence.STATUS_LOCFAIL){
                    if(!TextUtils.isEmpty(customId)){
                        sb.append(" customId: " + customId);
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = geoFenceHandler.obtainMessage();
                msg.obj = str;
                msg.what = 2;
                geoFenceHandler.sendMessage(msg);
            }
        }
    };

    /**
     * 添加围栏
     *
     * @since 3.2.0
     * @author hongming.wang
     *
     */
    private void addFence() {
        addDistrictFence();
    }

    public void addDistrictFence(){
        String customId = mTvCustomId.getText().toString();
        String keyWord = mTvKeyWord.getText().toString();
        if (TextUtils.isEmpty(keyWord)) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        geoFenceClient.addGeoFence(keyWord, customId);
    }

    private Handler geoFenceHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0 :
                    StringBuffer sb = new StringBuffer();
                    sb.append("添加围栏成功");
                    String customId = (String)msg.obj;
                    if(!TextUtils.isEmpty(customId)){
                        sb.append("customId: ").append(customId);
                    }
                    Toast.makeText(getApplicationContext(), sb.toString(),
                            Toast.LENGTH_SHORT).show();
                    drawFence2Map();
                    break;
                case 1 :
                    int errorCode = msg.arg1;
                    Toast.makeText(getApplicationContext(),
                            "添加围栏失败 " + errorCode, Toast.LENGTH_SHORT).show();
                    break;
                case 2 :
                    String statusStr = (String) msg.obj;
                    Toast.makeText(GeoFence_District_Activity.this, statusStr, Toast.LENGTH_LONG).show();
                    break;
                default :
                    break;
            }
        }
    };

    // 记录已经添加成功的围栏
    private HashMap<String, GeoFence> fenceMap = new HashMap<String, GeoFence>();
    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

    Object lock = new Object();
    void drawFence2Map() {
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        if (null == fenceList || fenceList.isEmpty()) {
                            return;
                        }
                        for (GeoFence fence : fenceList) {
                            if (fenceMap.containsKey(fence.getFenceId())) {
                                continue;
                            }
                            drawFence(fence);
                            fenceMap.put(fence.getFenceId(), fence);
                        }
                    }
                } catch (Throwable e) {

                }
            }
        }.start();
    }

    private void drawFence(GeoFence fence) {
        switch (fence.getType()) {
            case GeoFence.TYPE_ROUND :
            case GeoFence.TYPE_AMAPPOI :
                Utils.drawCircle(fence, mAMapControl, boundsBuilder);
                break;
            case GeoFence.TYPE_POLYGON :
            case GeoFence.TYPE_DISTRICT :
                Utils.drawPolygon(fence, mAMapControl, boundsBuilder);
                break;
            default :
                break;
        }
        LatLngBounds bounds = boundsBuilder.build();
        mAMapControl.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,150));
    }
}
