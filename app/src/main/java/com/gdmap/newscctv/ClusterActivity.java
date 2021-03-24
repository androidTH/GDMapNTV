package com.gdmap.newscctv;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.gdmap.newscctv.model.ClusterClickListener;
import com.gdmap.newscctv.model.ClusterItem;
import com.gdmap.newscctv.model.ClusterRender;
import com.gdmap.newscctv.model.RegionItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ClusterActivity extends AppCompatActivity implements AMap.OnMapLoadedListener, ClusterRender, ClusterClickListener, AMap.OnMapClickListener {


    @BindView(R.id.map)
    public MapView mapView;
    @BindView(R.id.btn_empty)
    public Button mBtnEmpty;

    private AMap aMap;
    private ClusterOverlay mClusterOverlay;
    private int clusterRadius = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        setMapView();
        mBtnEmpty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClusterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setMapView(){
        if(aMap == null){
            aMap = mapView.getMap();
        }
        aMap.setOnMapLoadedListener(this);
        aMap.setOnMapClickListener(this);
    }


    @Override
    public void onMapLoaded() {
          new Thread(new Runnable() {
              @Override
              public void run() {
                  List<ClusterItem> items = new ArrayList<ClusterItem>();

                  //随机10000个点
                  for (int i = 0; i < 10000; i++) {

                      double lat = Math.random() + 39.474923;
                      double lon = Math.random() + 116.027116;

                      LatLng latLng = new LatLng(lat, lon, false);
                      RegionItem regionItem = new RegionItem(latLng,
                              "test" + i);
                      items.add(regionItem);

                  }
                  mClusterOverlay = new ClusterOverlay(aMap,
                          getApplicationContext(), items,
                          Utils.dp2px(getApplicationContext(), clusterRadius));
                  mClusterOverlay.setmClusterRender(ClusterActivity.this);
                  mClusterOverlay.setmClusterClickListener(ClusterActivity.this);

              }
          }).start();
    }

    private Map<Integer, Drawable> mBackDrawAbles = new HashMap<Integer, Drawable>();


    @Override
    public Drawable getDrawAble(int clusterNum) {
        int radius = Utils.dp2px(getApplicationContext(), 80);
        if (clusterNum == 1) {
            Drawable bitmapDrawable = mBackDrawAbles.get(1);
            if (bitmapDrawable == null) {
                bitmapDrawable =
                        getApplication().getResources().getDrawable(
                                R.mipmap.icon_openmap_mark);
                mBackDrawAbles.put(1, bitmapDrawable);
            }

            return bitmapDrawable;
        } else if (clusterNum < 5) {

            Drawable bitmapDrawable = mBackDrawAbles.get(2);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, Utils.drawCircle(radius,
                        Color.argb(159, 210, 154, 6)));
                mBackDrawAbles.put(2, bitmapDrawable);
            }

            return bitmapDrawable;
        } else if (clusterNum < 10) {
            Drawable bitmapDrawable = mBackDrawAbles.get(3);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, Utils.drawCircle(radius,
                        Color.argb(199, 217, 114, 0)));
                mBackDrawAbles.put(3, bitmapDrawable);
            }

            return bitmapDrawable;
        } else {
            Drawable bitmapDrawable = mBackDrawAbles.get(4);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, Utils.drawCircle(radius,
                        Color.argb(235, 215, 66, 2)));
                mBackDrawAbles.put(4, bitmapDrawable);
            }

            return bitmapDrawable;
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        double lat = Math.random() + 39.474923;
//        double lon = Math.random() + 116.027116;
//
//        LatLng latLng1 = new LatLng(lat, lon);
        RegionItem regionItem = new RegionItem(latLng, "test");
        mClusterOverlay.addClusterItem(regionItem);
    }

    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for(ClusterItem item : clusterItems){
            bounds.include(item.getPosition());
        }
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),0));//设置地理显示范围 LatLngBounds与地图边缘10像素的填充区域
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
