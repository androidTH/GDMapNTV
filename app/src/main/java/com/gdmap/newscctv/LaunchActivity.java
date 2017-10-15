package com.gdmap.newscctv;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.amap.api.fence.GeoFenceListener;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);

        mLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, LocationActivity.class);
                startActivity(intent);
            }
        });
        mMapLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, MapLocationActivity.class);
                startActivity(intent);
            }
        });
        mBtnGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeoFence_Polygon_Activity.class);
                startActivity(intent);
            }
        });

        mapLastLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, LastLocation_Activity.class);
                startActivity(intent);
            }
        });

        mapKeyWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeoFence_District_Activity.class);
                startActivity(intent);
            }
        });

        mapMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, BasicMapActivity.class);
                startActivity(intent);
            }
        });

        mapMaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, MarkerClickActivity.class);
                startActivity(intent);
            }
        });

        mapcustommaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, CustomMarkerActivity.class);
                startActivity(intent);
            }
        });

        maplimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, LimitBoundsActivity.class);
                startActivity(intent);
            }
        });

        mapmapgeodesic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeodesicActivity.class);
                startActivity(intent);
            }
        });

        mapgecluster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, ClusterActivity.class);
                startActivity(intent);
            }
        });

        mapgepoikeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, PoiKeywordSearchActivity.class);
                startActivity(intent);
            }
        });

        mapgepoiaround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, PoiAroundSearchActivity.class);
                startActivity(intent);
            }
        });

        mapgepoiId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, PoiIDSearchActivity.class);
                startActivity(intent);
            }
        });

        mapgeregeocode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, ReGeocoderActivity.class);
                startActivity(intent);
            }
        });

        mapwalkroute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, WalkRouteActivity.class);
                startActivity(intent);
            }
        });

        mapcheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LaunchActivity.this, GeoFencesActivity.class);
                startActivity(intent);
            }
        });
    }
}
