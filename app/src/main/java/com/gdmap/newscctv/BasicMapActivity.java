package com.gdmap.newscctv;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyTrafficStyle;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BasicMapActivity extends CheckPermissionsActivity implements View.OnClickListener ,
        AMap.OnMarkerClickListener, AMap.OnMapClickListener{


    @BindView(R.id.map)
    MapView map;
    @BindView(R.id.check_style)
    CheckBox checkStyle;
    @BindView(R.id.basicmap)
    Button basicmap;
    @BindView(R.id.rsmap)
    Button rsmap;
    @BindView(R.id.nightmap)
    Button nightmap;
    @BindView(R.id.navimap)
    Button navimap;
    @BindView(R.id.traffic)
    CheckBox traffic;
    @BindView(R.id.building)
    CheckBox building;
    @BindView(R.id.maptext)
    CheckBox maptext;
    @BindView(R.id.Lujiazui)
    Button Lujiazui;
    @BindView(R.id.Zhongguancun)
    Button Zhongguancun;

    private AMap mAmapControl;
    private MarkerOptions markerOptions;
//    private LatLng latlng = new LatLng(39.91746, 116.396481);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map);
        ButterKnife.bind(this);
        basicmap.setOnClickListener(this);
        rsmap.setOnClickListener(this);
        nightmap.setOnClickListener(this);
        navimap.setOnClickListener(this);

        traffic.setOnClickListener(this);

        building.setOnClickListener(this);

        maptext.setOnClickListener(this);

        Zhongguancun.setOnClickListener(this);
        Lujiazui.setOnClickListener(this);
        checkStyle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                mAmapControl.setMapCustomEnable(b);
            }
        });
        map.onCreate(savedInstanceState);
        setMap();
    }

    public void setMap() {
        if (mAmapControl == null) {
            mAmapControl = map.getMap();
        }
        mAmapControl.setOnMapClickListener(this);
        //自定义实时交通信息的颜色样式
        MyTrafficStyle myTrafficStyle = new MyTrafficStyle();
        myTrafficStyle.setSeriousCongestedColor(0xff92000a);
        myTrafficStyle.setCongestedColor(0xffea0312);
        myTrafficStyle.setSlowColor(0xffff7508);
        myTrafficStyle.setSmoothColor(0xff00a209);
        mAmapControl.setMyTrafficStyle(myTrafficStyle);
        mAmapControl.showMapText(true);
        mAmapControl.setMapType(AMap.MAP_TYPE_NORMAL);
//        addMarkersToMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        map.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.basicmap:
                mAmapControl.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
                float scale = mAmapControl.getScalePerPixel();
                Toast.makeText(BasicMapActivity.this, "每像素代表" + scale + "米", Toast.LENGTH_LONG).show();
                break;
            case R.id.rsmap:
                mAmapControl.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
                break;
            case R.id.nightmap:
                mAmapControl.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图模式
                break;
            case R.id.navimap:
                mAmapControl.setMapType(AMap.MAP_TYPE_NAVI);//导航地图模式
                break;
            case R.id.traffic:
                mAmapControl.setTrafficEnabled(((CheckBox) v).isChecked());
                break;
            case R.id.building:
                mAmapControl.showBuildings(((CheckBox) v).isChecked());
                break;
            case R.id.maptext:
                mAmapControl.showMapText(((CheckBox) v).isChecked());
                break;
            case R.id.Lujiazui:
                mAmapControl.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(Const.SHANGHAI, 18, 30, 0)));
                mAmapControl.clear();
                mAmapControl.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.mipmap.gps_point)).position(Const.SHANGHAI));
                break;
            case R.id.Zhongguancun:
                mAmapControl.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(Const.ZHONGGUANCUN, 18, 30, 30)), 1000, null);
                mAmapControl.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).position(Const.ZHONGGUANCUN));
                break;
        }

        checkStyle.setChecked(false);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mAmapControl != null) {
            jumpPoint(marker);
        }
        Toast.makeText(BasicMapActivity.this, "您点击了Marker", Toast.LENGTH_LONG).show();

        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarkersToMap(latLng);
    }

    /**
     * marker点击时跳动一下
     */
    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mAmapControl.getProjection();
        final LatLng markerLatlng = marker.getPosition();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        markerPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng latLng) {
        mAmapControl.clear();
        mAmapControl.setOnMarkerClickListener(this);

        markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(latLng)
                .draggable(true);
        mAmapControl.addMarker(markerOptions);

    }
}
