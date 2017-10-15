package com.gdmap.newscctv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LimitBoundsActivity extends AppCompatActivity implements AMap.OnMarkerClickListener{


    @BindView(R.id.button_set)
    Button buttonSet;
    @BindView(R.id.map)
    MapView map;

    // 西南坐标
    private LatLng southwestLatLng = new LatLng(39.674949, 115.932873);
    // 东北坐标
    private LatLng northeastLatLng = new LatLng(40.159453, 116.767834);

    private AMap mapControl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_bounds);
        ButterKnife.bind(this);
        map.onCreate(savedInstanceState);

        if(mapControl == null){
            mapControl = map.getMap();
        }
        mapControl.addMarker(new MarkerOptions().position(southwestLatLng));
        mapControl.addMarker(new MarkerOptions().position(northeastLatLng));
        mapControl.moveCamera(CameraUpdateFactory.zoomTo(8f));

        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLngBounds bounds = new LatLngBounds(southwestLatLng, northeastLatLng);
                mapControl.setMapStatusLimits(bounds);
            }
        });

        mapControl.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        map.onSaveInstanceState(outState);
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
}
