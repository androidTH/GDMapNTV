package com.gdmap.newscctv;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GeodesicActivity extends AppCompatActivity {

    @BindView(R.id.mapview)
    public MapView mapView;

    private AMap aMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geodesic);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        aMap.moveCamera(CameraUpdateFactory.zoomTo(4));
        aMap.setMapTextZIndex(2);

        aMap.addPolyline((new PolylineOptions())
                .add(new LatLng(43.828, 87.621), new LatLng(45.808, 126.55))
                .geodesic(true).color(Color.RED));
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
