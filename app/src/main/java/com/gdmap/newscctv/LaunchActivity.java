package com.gdmap.newscctv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.amap.api.fence.GeoFenceListener;
import com.meituan.android.walle.WalleChannelReader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LaunchActivity extends AppCompatActivity {

    @BindView(R.id.location)
    public Button mLocation;
    @BindView(R.id.maplocation)
    public Button mMapLocation;
    @BindView(R.id.mapgeofence)
    public Button mBtnGeoFence;
    @BindView(R.id.maplastloc)
    public Button mapLastLoc;
    @BindView(R.id.mapkeyword)
    public Button mapKeyWord;
    @BindView(R.id.mapmode)
    public Button mapMode;
    @BindView(R.id.mapmaker)
    public Button mapMaker;
    @BindView(R.id.mapcustommaker)
    Button mapcustommaker;
    @BindView(R.id.maplimit)
    Button maplimit;
    @BindView(R.id.mapgeodesic)
    Button mapmapgeodesic;
    @BindView(R.id.mapgecluster)
    Button mapgecluster;
    @BindView(R.id.mapgepoikeyword)
    Button mapgepoikeyword;
    @BindView(R.id.mapgepoiaround)
    Button mapgepoiaround;
    @BindView(R.id.mapgepoiId)
    Button mapgepoiId;
    @BindView(R.id.mapgeregeocode)
    Button mapgeregeocode;
    @BindView(R.id.mapwalkroute)
    Button mapwalkroute;
    @BindView(R.id.mapcheckin)
    Button mapcheckin;
    @BindView(R.id.maplocationcheckin)
    Button maplocationcheckin;
    @BindView(R.id.mapplacechoose)
    Button mapplacechoose;
    @BindView(R.id.searchplace)
    Button mapsearchplace;
    @BindView(R.id.province)
    Button mProvince;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
//        String channel = WalleChannelReader.getChannel(this.getApplicationContext());
//        ToastUtil.show(this,"渠道" + channel);
        mLocation.setOnClickListener(view -> {
            Intent intent = new Intent(LaunchActivity.this, LocationActivity.class);
            startActivity(intent);
        });
        mMapLocation.setOnClickListener(view -> {
            Intent intent = new Intent(LaunchActivity.this, MapLocationActivity.class);
            startActivity(intent);
        });

        mBtnGeoFence.setOnClickListener(view -> {
            Intent intent = new Intent(LaunchActivity.this, GeoFence_Polygon_Activity.class);
            startActivity(intent);
        });

        mapLastLoc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, LastLocation_Activity.class);
                startActivity(intent);
            }
        });

        mapKeyWord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeoFence_District_Activity.class);
                startActivity(intent);
            }
        });

        mapMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, BasicMapActivity.class);
                startActivity(intent);
            }
        });

        mapMaker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, MarkerClickActivity.class);
                startActivity(intent);
            }
        });

        mapcustommaker.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, CustomMarkerActivity.class);
                startActivity(intent);
            }
        });

        maplimit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, LimitBoundsActivity.class);
                startActivity(intent);
            }
        });

        mapmapgeodesic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeodesicActivity.class);
                startActivity(intent);
            }
        });

        mapgecluster.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, ClusterActivity.class);
                startActivity(intent);
            }
        });

        mapgepoikeyword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, PoiKeywordSearchActivity.class);
                startActivity(intent);
            }
        });

        mapgepoiaround.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, PoiAroundSearchActivity.class);
                startActivity(intent);
            }
        });

        mapgepoiId.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, PoiIDSearchActivity.class);
                startActivity(intent);
            }
        });

        mapgeregeocode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, ReGeocoderActivity.class);
                startActivity(intent);
            }
        });

        mapwalkroute.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, WalkRouteActivity.class);
                startActivity(intent);
            }
        });

        mapcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeoFencesActivity.class);
                startActivity(intent);
            }
        });

        maplocationcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchActivity.this, LocationCheckIn.class);
                startActivity(intent);
            }
        });

        mapplacechoose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchActivity.this, PlaceChooseActivity.class);
                startActivity(intent);
            }
        });

        mapsearchplace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LaunchActivity.this, SearchPlaceActivity.class);
                startActivity(intent);
            }
        });

        mProvince.setOnClickListener(v -> {
            Intent intent = new Intent(LaunchActivity.this, ProvinceHoleActivity.class);
//            Intent intent = new Intent(LaunchActivity.this, DistrictWithBoundaryActivity.class);
            startActivity(intent);
        });
    }
}
