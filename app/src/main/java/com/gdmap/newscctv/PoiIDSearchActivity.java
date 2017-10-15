package com.gdmap.newscctv;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PoiIDSearchActivity extends AppCompatActivity implements View.OnClickListener,
        PoiSearch.OnPoiSearchListener, AMap.OnMarkerClickListener, AMap.OnMarkerDragListener{


    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.btn_search)
    TextView btnSearch;
    @BindView(R.id.input_edittext)
    EditText inputEdittext;
    @BindView(R.id.poi_name)
    TextView poiName;
    @BindView(R.id.poi_address)
    TextView poiAddress;
    @BindView(R.id.poi_info)
    TextView poiInfo;
    @BindView(R.id.poi_detail)
    RelativeLayout poiDetail;

    private AMap aMap;
    private PoiSearch poiSearch;
    private PoiItem poiResult;
    private  Marker detailMarker;
    private LatLng latlngB = new LatLng(39.924870, 116.403270);



    private String ID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_idsearch);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        setMapView();
    }

    public void setMapView() {
        inputEdittext.setText("B0FFFZ7A7D");
        btnSearch.setHint("请输入搜索ID");
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.addMarker(new MarkerOptions().position(latlngB).icon(
                    BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_BLUE)).draggable(true));
            aMap.setOnMarkerDragListener(this);
        }
        btnSearch.setOnClickListener(this);
    }

    private void doSearchQuery(){
        ID = inputEdittext.getText().toString();
        poiSearch = new PoiSearch(this, null);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIIdAsyn(ID);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_search:
                doSearchQuery();
                break;

            default:
                break;
        }
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

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
        whetherToShowDetailInfo(true);
        poiName.setText(AMapUtils.calculateArea(marker.getPosition(), latlngB) +"m");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int rCode) {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {
          if(i == 1000){
              if(poiItem != null){
                  aMap.clear();
                  poiResult = poiItem;
                  MarkerOptions options = new MarkerOptions();
                  options.draggable(true);
                  options.anchor(0.5f, 0.5f);
                  options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.gps_point)));
                  options.position(new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude()));
                  aMap.addMarker(options);
                  whetherToShowDetailInfo(true);
                  setPoiItemDisplayContent(poiResult);
              }
          }else{
              ToastUtil.showerror(this, i);
          }
        //AMapUtils.calculateLineDistance();//二点之间的距离
    }

    private void setPoiItemDisplayContent(final PoiItem mCurrentPoi) {
        poiName.setText(mCurrentPoi.getTitle());
        poiAddress.setText(mCurrentPoi.getSnippet());
        poiInfo.setText("营业时间："+ mCurrentPoi.getPoiExtension().getOpentime()
                +"     评分："+ mCurrentPoi.getPoiExtension().getmRating());

    }

    private void whetherToShowDetailInfo(boolean isToShow) {
        if (isToShow) {
            poiDetail.setVisibility(View.VISIBLE);

        } else {
            poiDetail.setVisibility(View.GONE);

        }
    }

}
