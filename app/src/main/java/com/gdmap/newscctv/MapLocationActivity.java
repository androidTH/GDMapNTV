package com.gdmap.newscctv;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RidePath;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkRouteResult;
import com.gdmap.newscctv.overlay.RideRouteOverlay;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


//23:10:8F:E6:99:1B:67:E3:2A:49:E9:34:FF:09:46:46:B7:A4:85:B5
//A8:64:C9:80:3E:22:76:8C:71:C9:49:8A:BE:58:14:CD:8C:01:7E:5E
//Android开发——高德地图波纹扩散效果动效及自定义缩放、定位控件 https://github.com/wenzhihao123/AMapCircleWave
//快速实现自定义地图聚合操作 http://blog.csdn.net/qq_23547831/article/details/52063010
public class MapLocationActivity extends CheckPermissionsActivity implements View.OnClickListener,
        GeocodeSearch.OnGeocodeSearchListener, AMap.OnMyLocationChangeListener, AMap.OnCameraChangeListener,
        ZoomView.OnChangeCamera, CompoundButton.OnCheckedChangeListener, AMap.OnMapClickListener,
        AMap.OnPOIClickListener, AMap.OnMarkerClickListener, RouteSearch.OnRouteSearchListener {


    @BindView(R.id.tturemapview)
    public TextureMapView mMapView;
    @BindView(R.id.btn_backlocation)
    public Button mBtnBackLocation;
    @BindView(R.id.zoomview)
    public ZoomView mZoomView;
    @BindView(R.id.scale_toggle)
    public CheckBox mScaleToggle;
    @BindView(R.id.tvriderouteinfo)
    TextView tvriderouteinfo;
    private Unbinder unbinder;

    private AMap mAMapControl; //地图控制器

    private GeocodeSearch mGeocodeSearch;
    private MyLocationStyle mLocationStyle;
    private RouteSearch mRouteSearch;
    private RouteSearch.RideRouteQuery mRideRouteQuery;
    private RideRouteOverlay rideRouteOverlay;
    private Marker mEndMarker;
    private MarkerOptions mEndmarkerOptions;
    private LatLng mLoclatlng;


    private LocationHandler mLocationHandler = new LocationHandler(this);

    public static int LOCATION_NUM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maplocation);
        unbinder = ButterKnife.bind(this);
        mBtnBackLocation.setOnClickListener(this);
        mZoomView.setmOnChangeCamera(this);
        mScaleToggle.setOnCheckedChangeListener(this);
        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);

        }
        initMap();
    }

    public void initMap() {
        if (mAMapControl == null && mMapView != null) {
            mAMapControl = mMapView.getMap();
            mAMapControl.getUiSettings().setRotateGesturesEnabled(false);
            mAMapControl.getUiSettings().setGestureScaleByMapCenter(true);
            mAMapControl.getUiSettings().setLogoBottomMargin(40);
            mAMapControl.getUiSettings().setLogoLeftMargin(30);
            mAMapControl.getUiSettings().setZoomPosition(30);
            mAMapControl.getUiSettings().setZoomControlsEnabled(false);//自带的缩放按钮
            mAMapControl.getUiSettings().setMyLocationButtonEnabled(false);
            mAMapControl.getUiSettings().setScaleControlsEnabled(false);
            //设置+-符号的位置
            mAMapControl.getUiSettings().setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_CENTER);
            mAMapControl.moveCamera(CameraUpdateFactory.zoomTo(16));
        }

        mGeocodeSearch = new GeocodeSearch(this.getApplicationContext());
        mGeocodeSearch.setOnGeocodeSearchListener(this);

        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);

        mLocationStyle = new MyLocationStyle();
        mLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);//LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER LOCATION_TYPE_LOCATE
        mLocationStyle.showMyLocation(true);
        mLocationStyle.interval(3000);
        mLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point));
        mLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
        // 自定义精度范围的圆形边框宽度
        mLocationStyle.strokeWidth(0);
        // 设置圆形的填充颜色
        mLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));
        mAMapControl.setMyLocationStyle(mLocationStyle);
        mAMapControl.setMyLocationEnabled(true);
        mAMapControl.showMapText(true);
        mAMapControl.setOnMyLocationChangeListener(this);//用户定位信息监听接口
        mAMapControl.setOnCameraChangeListener(this);
        mAMapControl.setOnMapClickListener(this);
        mAMapControl.setOnPOIClickListener(this);
        mAMapControl.setOnMarkerClickListener(this);
        mAMapControl.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
            }
        });
    }


    public static class LocationHandler extends Handler {

        private WeakReference<AppCompatActivity> mWeakActivity;

        public LocationHandler(MapLocationActivity activity) {
            mWeakActivity = new WeakReference<AppCompatActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(mWeakActivity.get(), msg.obj.toString() + "数量", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_backlocation) {
            if (null != mLoclatlng) {
                mAMapControl.moveCamera(CameraUpdateFactory.newLatLng(mLoclatlng));
//                mAMapControl.moveCamera(CameraUpdateFactory.zoomTo(17));
            }
        }
//        else if(view.getId() == R.id.btn_plus){
//            //mAMapControl.moveCamera(update); //无动画的时候调用
//            if(mAMapControl.getCameraPosition().zoom > mAMapControl.getMaxZoomLevel()){
//                return;
//            }
//            changeCamera(CameraUpdateFactory.zoomIn(),null);
//        }
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
//    private void changeCamera(CameraUpdate update, AMap.CancelableCallback callback) {
//        mAMapControl.animateCamera(update, 300, new AMap.CancelableCallback() {
//            @Override
//            public void onFinish() {
//                Toast.makeText(MapLocationActivity.this, "onFinish", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//        });
//        aMap.moveCamera(update); //无动画的时候调用
//    }
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        if (i == 1000) {
            Toast.makeText(MapLocationActivity.this, regeocodeResult.getRegeocodeAddress().getFormatAddress() + "", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    @Override
    public void onMyLocationChange(Location location) {
        //逆地理编码
//        LatLonPoint latLonPoint = new LatLonPoint(location.getLatitude(),location.getLongitude());
//        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,GeocodeSearch.GPS);
//        mGeocodeSearch.getFromLocationAsyn(query);
        mLoclatlng = new LatLng(location.getLatitude(), location.getLongitude());
        //实现第一次的时候定位移到屏幕中间,以后就不再移动屏幕中间
        mMapView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);
                mAMapControl.setMyLocationStyle(mLocationStyle);
            }
        }, 500);
//        changeCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latlng,16, 30, 30)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.i("OnCameraChangeListener", "坐标" + cameraPosition.target.latitude + "," + cameraPosition.target.longitude);
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        Log.i("OnCameraChangeListener", "坐标" + cameraPosition.target.latitude + "," + cameraPosition.target.longitude);
        mZoomView.setZoomBitmap(cameraPosition.zoom, mAMapControl.getMaxZoomLevel(), mAMapControl.getMinZoomLevel());
//        LatLonPoint point = new LatLonPoint(cameraPosition.target.latitude, cameraPosition.target.longitude);
//        RegeocodeQuery regeocodeQuery = new RegeocodeQuery(point, 200, GeocodeSearch.GPS);
//        mGeocodeSearch.getFromLocationAsyn(regeocodeQuery);
//        VisibleRegion visibleRegion = mAMapControl.getProjection().getVisibleRegion();//获取可是区域
//        LatLngBounds latLngBounds = visibleRegion.latLngBounds;// 获取可视区域的Bounds
//        boolean isContain = latLngBounds.contains(Const.SHANGHAI);// 判断上海经纬度是否包括在当前地图可见区域
//        if (isContain) {
//            Toast.makeText(MapLocationActivity.this, "上海市在地图当前可见区域内", Toast.LENGTH_LONG).show();
//        } else {
//            Toast.makeText(MapLocationActivity.this, "上海市超出地图当前可见区域", Toast.LENGTH_LONG).show();
//        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.i("onMapClick", "坐标" + latLng.latitude + "," + latLng.longitude);
//        if (mEndMarker != null) {
//            mEndMarker.remove();
//            mEndMarker = null;
//        }
//
//        if (mEndmarkerOptions == null) {
//            mEndmarkerOptions = new MarkerOptions();
//            mEndmarkerOptions.draggable(false);
//            mEndmarkerOptions.anchor(0.5f, 0.5f);
//            mEndmarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//        }
//
//        mEndmarkerOptions.position(latLng);
//        mEndMarker = mAMapControl.addMarker(mEndmarkerOptions);

        if (rideRouteOverlay != null) {
            rideRouteOverlay.removeFromMap();
        }

        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(Utils.convertToLatLonPoint(mLoclatlng), Utils.convertToLatLonPoint(latLng));
        if (mRouteSearch != null) {
            mRideRouteQuery = new RouteSearch.RideRouteQuery(fromAndTo);
            mRouteSearch.calculateRideRouteAsyn(mRideRouteQuery);
        }

    }


    @Override
    public void onPOIClick(Poi poi) {
        mAMapControl.clear();
        MarkerOptions markerOptions = new MarkerOptions().draggable(true);
        markerOptions.position(poi.getCoordinate());
        TextView mTv = new TextView(getApplicationContext());
        mTv.setText("到" + poi.getName() + "去");
        mTv.setGravity(Gravity.CENTER);
        mTv.setTextColor(Color.BLACK);
        mTv.setBackgroundResource(R.drawable.custom_info_bubble);
        markerOptions.icon(BitmapDescriptorFactory.fromView(mTv));
        mAMapControl.addMarker(markerOptions);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
//        NaviPara naviPara = new NaviPara();
//        naviPara.setTargetPoint(marker.getPosition());
//        naviPara.setNaviStyle(AMapUtils.DRIVING_AVOID_CONGESTION);
//        try {
//            // 调起高德地图导航
//            AMapUtils.openAMapNavi(naviPara, getApplicationContext());
//        } catch (com.amap.api.maps.AMapException e) {
//            // 如果没安装会进入异常，调起下载页面
//            AMapUtils.getLatestAMapApp(getApplicationContext());
//        }
//        mAMapControl.clear();

        return true;
    }

    /**
     * 底图poi点击回调
     */
    @Override
    public void changeCamera(CameraUpdate update, int zoommode) {
        if (zoommode == Const.ZOOMIN) {
            if (mAMapControl.getCameraPosition().zoom >= mAMapControl.getMaxZoomLevel()) {
                return;
            }
            mAMapControl.animateCamera(update);
        } else if (zoommode == Const.ZOOMOUT) {
            if (mAMapControl.getCameraPosition().zoom <= mAMapControl.getMinZoomLevel()) {
                return;
            }
            mAMapControl.animateCamera(update);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        mAMapControl.getUiSettings().setScaleControlsEnabled(b);
    }

    public void changeCamera(CameraUpdate update) {
        mAMapControl.animateCamera(update, 500, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {

    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {
        if (i == 1000) {
            if (rideRouteResult != null && rideRouteResult.getPaths() != null) {
                if (rideRouteResult.getPaths().size() > 0) {
                    RidePath ridePath = rideRouteResult.getPaths().get(0);
                    rideRouteOverlay = new RideRouteOverlay(MapLocationActivity.this, mAMapControl,
                            ridePath, rideRouteResult.getStartPos(), rideRouteResult.getTargetPos());
                    rideRouteOverlay.removeFromMap();
                    rideRouteOverlay.addToMap();
                    rideRouteOverlay.zoomToSpan();
                    tvriderouteinfo.setVisibility(View.GONE);
//                    float dur = ridePath.getDuration();
//                    float dis = ridePath.getDistance();
//                    String des = getResources().getString(R.string.ride_time) + Utils.getFriendlyTime((int)dur)+"("+Utils.getFriendlyLength((int)dis)+")";
//                    tvriderouteinfo.setText(des);
                } else if (rideRouteResult != null && rideRouteResult.getPaths() == null) {
                    ToastUtil.show(MapLocationActivity.this, R.string.no_result);
                }
            } else if (rideRouteResult != null && rideRouteResult.getPaths() == null) {
                ToastUtil.show(MapLocationActivity.this, R.string.no_result);
            }
        } else if (rideRouteResult != null && rideRouteResult.getPaths() == null) {
            ToastUtil.show(MapLocationActivity.this, R.string.no_result);
        }
    }
}
