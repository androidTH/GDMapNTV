package com.gdmap.newscctv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DistanceItem;
import com.amap.api.services.route.DistanceResult;
import com.amap.api.services.route.DistanceSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeoFence_Polygon_Activity extends CheckPermissionsActivity implements
        View.OnClickListener, CompoundButton.OnCheckedChangeListener, LocationSource, AMapLocationListener, GeoFenceListener, AMap.OnMapClickListener {


    @BindView(R.id.rb_roundFence)
    RadioButton rbRoundFence;
    @BindView(R.id.rb_polygonFence)
    RadioButton rbPolygonFence;
    @BindView(R.id.rb_keywordFence)
    RadioButton rbKeywordFence;
    @BindView(R.id.rb_nearbyFence)
    RadioButton rbNearbyFence;
    @BindView(R.id.rb_districeFence)
    RadioButton rbDistriceFence;
    @BindView(R.id.rg_fenceType)
    RadioGroup rgFenceType;
    @BindView(R.id.et_customId)
    EditText etCustomId;
    @BindView(R.id.et_keyword)
    EditText etKeyword;
    @BindView(R.id.et_city)
    EditText etCity;
    @BindView(R.id.et_poitype)
    EditText etPoitype;
    @BindView(R.id.et_radius)
    EditText etRadius;
    @BindView(R.id.et_fenceSize)
    EditText etFenceSize;
    @BindView(R.id.ly_option)
    LinearLayout lyOption;
    @BindView(R.id.cb_alertIn)
    CheckBox cbAlertIn;
    @BindView(R.id.cb_alertOut)
    CheckBox cbAlertOut;
    @BindView(R.id.cb_alertStated)
    CheckBox cbAlertStated;
    @BindView(R.id.tv_guide)
    TextView tvGuide;
    @BindView(R.id.bt_addFence)
    Button btAddFence;
    @BindView(R.id.bt_option)
    Button btOption;
    @BindView(R.id.mapview)
    public MapView mapView;

    public AMapLocationClient mapLocationClient;
    public AMapLocationClientOption mapLocationClientOption;
    public AMap mAMapControl;
    public GeoFenceClient mGeoFenceClient;
    public Marker marker;
    private MarkerOptions markerOption = null;
    public OnLocationChangedListener listener;

    // 地理围栏的广播action
    private static final String GEOFENCE_BROADCAST_ACTION = "com.gdmap.newscctv.geofence.round";
    private List<GeoFence> mListGeoFences;
    private HashMap<String, GeoFence> fenceMap = new HashMap<String, GeoFence>();
    private List<Marker> markerList = new ArrayList<Marker>();
    private BitmapDescriptor ICON_YELLOW = BitmapDescriptorFactory
            .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
    private List<LatLng> polygonPoints;

    //测量距离
    private DistanceSearch mDistanceSearch;
    private DistanceSearch.DistanceQuery mDistanceQuery;

    private float distance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_fence__round);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        mGeoFenceClient = new GeoFenceClient(getApplicationContext());
        markerOption = new MarkerOptions().icon(ICON_YELLOW).draggable(true);
        initMap();

        btOption.setVisibility(View.VISIBLE);
        btOption.setText(getString(R.string.hideOption));
        resetView_polygon();

        btAddFence.setOnClickListener(this);
        btOption.setOnClickListener(this);
        cbAlertIn.setOnCheckedChangeListener(this);
        cbAlertOut.setOnCheckedChangeListener(this);
        cbAlertStated.setOnCheckedChangeListener(this);
    }

    public void initMap() {
        if (mAMapControl == null) {
            mAMapControl = mapView.getMap();
            mAMapControl.setLocationSource(this);
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
            // 自定义精度范围的圆形边框颜色
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            // 自定义精度范围的圆形边框宽度
            myLocationStyle.strokeWidth(0);
            // 设置圆形的填充颜色
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
            // 将自定义的 myLocationStyle 对象添加到地图上
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
            mAMapControl.setMyLocationStyle(myLocationStyle);
            mAMapControl.setMyLocationEnabled(true);
            mAMapControl.getUiSettings().setRotateGesturesEnabled(false);
            mAMapControl.moveCamera(CameraUpdateFactory.zoomBy(16));
        }

        IntentFilter filter = new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver, filter);

        mGeoFenceClient.createPendingIntent(GEOFENCE_BROADCAST_ACTION);
        mGeoFenceClient.setGeoFenceListener(this);
        mGeoFenceClient.setActivateAction(GeoFenceClient.GEOFENCE_IN);
        mAMapControl.setOnMapClickListener(this);

//        mDistanceSearch = new DistanceSearch(this);
//        mDistanceQuery = new DistanceSearch.DistanceQuery();
//        mDistanceQuery.setType(DistanceSearch.TYPE_DRIVING_DISTANCE);
//        mDistanceSearch.setDistanceSearchListener((distanceResult, i) -> {
//            List<DistanceItem> list = distanceResult.getDistanceResults();
//            for (int j=0;j<list.size();j++){
//                distance = distance + list.get(j).getDistance();
//            }
//            tvGuide.setText(list.size()+"选中的坐标:"+distance+"米");
//        });
    }

    private void resetView_polygon() {
        tvGuide.setBackgroundColor(getResources().getColor(R.color.red));
        tvGuide.setText("请点击地图选择围栏的边界点,至少3个点");
        tvGuide.setVisibility(View.VISIBLE);
        tvGuide.setVisibility(View.VISIBLE);
        polygonPoints = new ArrayList<LatLng>();
        btAddFence.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_addFence:
                addFence();
                break;
            case R.id.bt_option:
                if (btOption.getText().toString()
                        .equals(getString(R.string.showOption))) {
                    lyOption.setVisibility(View.VISIBLE);
                    btOption.setText(getString(R.string.hideOption));
                } else {
                    lyOption.setVisibility(View.GONE);
                    btOption.setText(getString(R.string.showOption));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (this.listener != null) {
            if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                Toast.makeText(GeoFence_Polygon_Activity.this, "定位正确", Toast.LENGTH_LONG).show();
                this.listener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                Toast.makeText(GeoFence_Polygon_Activity.this, "定位错误", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.listener = onLocationChangedListener;
        if (onLocationChangedListener != null) {
            mapLocationClient = new AMapLocationClient(this);
            mapLocationClientOption = new AMapLocationClientOption();
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
        if (mapLocationClient != null) {
            mapLocationClient.stopLocation();
            mapLocationClient.onDestroy();
        }
        mapLocationClient = null;
    }

    @Override
    public void onGeoFenceCreateFinished(List<GeoFence> list, int i, String s) {
        Message msg = mHandler.obtainMessage();
        if (i == GeoFence.ADDGEOFENCE_SUCCESS) {
            mListGeoFences = list;
            msg.what = 0;
            msg.obj = s;
        } else {
            msg.arg1 = i;
            msg.what = 1;
        }
        mHandler.sendMessage(msg);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        polygonPoints.add(latLng);
        addCenterMarker(latLng);
        tvGuide.setBackgroundColor(getResources().getColor(R.color.gary));
//        tvGuide.setText("选中的坐标：度:" + latLng.longitude + ",经度:"
//                + latLng.latitude);
        if(polygonPoints.size() >= 3 ){
            btAddFence.setEnabled(true);
        }

        if(polygonPoints.size()>=2){
          Polyline mPolyline =  mAMapControl.addPolyline(new PolylineOptions().
                    addAll(polygonPoints).width(30).color(ContextCompat.getColor(this,R.color.f850_default_blue)));

//            LatLonPoint origins =new  LatLonPoint(polygonPoints.get(polygonPoints.size()-2).latitude,polygonPoints.get(polygonPoints.size()-2).longitude);
//            LatLonPoint dest= new LatLonPoint(polygonPoints.get(polygonPoints.size()-1).latitude,polygonPoints.get(polygonPoints.size()-1).longitude);
//            mDistanceQuery.addOrigins(origins);
//            mDistanceQuery.setDestination(dest);
//            mDistanceSearch.calculateRouteDistanceAsyn(mDistanceQuery);
//设置测量方式，支持直线和驾车
            distance = distance+AMapUtils.calculateLineDistance(polygonPoints.get(polygonPoints.size()-2), polygonPoints.get(polygonPoints.size()-1));
            etCustomId.setText("选中的坐标:"+distance+"米");
        }
    }

    private void addCenterMarker(LatLng latlng) {
        marker = mAMapControl.addMarker(markerOption);
        marker.setPosition(latlng);
//        markerList.add(marker);
    }

    private void removeMarkers() {
        if (null != marker) {
            marker.remove();
            marker = null;
        }
//        if (null != markerList && markerList.size() > 0) {
//            for (Marker marker : markerList) {
//                marker.remove();
//            }
//            markerList.clear();
//        }
    }

    public void addFence() {
        addPolygonFence();
    }

    public void addPolygonFence() {
        String custonId = etCustomId.getText().toString();
        String radioStr = etRadius.getText().toString();
        if (null == polygonPoints || polygonPoints.size() < 3) {
            Toast.makeText(getApplicationContext(), "参数不全", Toast.LENGTH_SHORT)
                    .show();
            btAddFence.setEnabled(true);
            return;
        }

        List<DPoint> Dpoints = new ArrayList<>();
        for(LatLng latLng:polygonPoints){
            DPoint point = new DPoint();
            point.setLatitude(latLng.latitude);
            point.setLongitude(latLng.longitude);
            Dpoints.add(point);
        }

        mGeoFenceClient.addGeoFence(Dpoints, custonId);
    }

    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(GEOFENCE_BROADCAST_ACTION)) {
                Bundle bundle = intent.getExtras();
//获取围栏行为：
                int status = bundle.getInt(GeoFence.BUNDLE_KEY_FENCESTATUS);
//获取自定义的围栏标识：
                String customId = bundle.getString(GeoFence.BUNDLE_KEY_CUSTOMID);
//获取围栏ID:
                String fenceId = bundle.getString(GeoFence.BUNDLE_KEY_FENCEID);
//获取当前有触发的围栏对象：
                GeoFence fence = bundle.getParcelable(GeoFence.BUNDLE_KEY_FENCE);
                StringBuffer sb = new StringBuffer();
                switch (status){
                    case GeoFence.STATUS_LOCFAIL:
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
                    default :
                        break;
                }
                if(status != GeoFence.STATUS_LOCFAIL){
                    if(!TextUtils.isEmpty(customId)){
                        sb.append(" customId: " + customId + "坐标系" + fence.getPointList().size());
                    }
                    sb.append(" fenceId: " + fenceId);
                }
                String str = sb.toString();
                Message msg = mHandler.obtainMessage();
                msg.obj = str;
                msg.what = 2;
                mHandler.sendMessage(msg);
            }
        }
    };


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
        mapView.onDestroy();
        unregisterReceiver(mGeoFenceReceiver);
        if (null != mGeoFenceClient) {
            mGeoFenceClient.removeGeoFence();
        }
        if (null != mapLocationClient) {
            mapLocationClient.onDestroy();
        }
    }

    private Handler mHandler = new Handler() {
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
                    Toast.makeText(GeoFence_Polygon_Activity.this, "信息：" + statusStr, Toast.LENGTH_LONG).show();
                    break;
                default :
                    break;
            }
        }
    };

    Object lock = new Object();
    void drawFence2Map() {
        new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (lock) {
                        if (null == mListGeoFences || mListGeoFences.isEmpty()) {
                            return;
                        }
                        for (GeoFence fence : mListGeoFences) {
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
                drawCircle(fence);
                break;
            case GeoFence.TYPE_POLYGON :
            case GeoFence.TYPE_DISTRICT :
                drawPolygon(fence);
                break;
            default :
                break;
        }

        // 设置所有maker显示在当前可视区域地图中
        LatLngBounds bounds = boundsBuilder.build();
        mAMapControl.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
        polygonPoints.clear();
        removeMarkers();
    }

    private void drawCircle(GeoFence fence) {
        LatLng center = new LatLng(fence.getCenter().getLatitude(),
                fence.getCenter().getLongitude());
        // 绘制一个圆形
        mAMapControl.addCircle(new CircleOptions().center(center)
                .radius(fence.getRadius()).strokeColor(Const.STROKE_COLOR)
                .fillColor(Const.FILL_COLOR).strokeWidth(Const.STROKE_WIDTH));
        boundsBuilder.include(center);
    }

    private void drawPolygon(GeoFence fence) {
        final List<List<DPoint>> pointList = fence.getPointList();
        if (null == pointList || pointList.isEmpty()) {
            return;
        }
        for (List<DPoint> subList : pointList) {
            List<LatLng> lst = new ArrayList<LatLng>();

            PolygonOptions polygonOption = new PolygonOptions();
            for (DPoint point : subList) {
                lst.add(new LatLng(point.getLatitude(), point.getLongitude()));
                boundsBuilder.include(
                        new LatLng(point.getLatitude(), point.getLongitude()));
            }
            polygonOption.addAll(lst);

            polygonOption.strokeColor(Const.STROKE_COLOR)
                    .fillColor(Const.FILL_COLOR).strokeWidth(Const.STROKE_WIDTH);
            mAMapControl.addPolygon(polygonOption);
        }
    }

    // 当前的坐标点集合，主要用于进行地图的可视区域的缩放
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
}
