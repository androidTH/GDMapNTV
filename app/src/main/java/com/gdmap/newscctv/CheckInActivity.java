package com.gdmap.newscctv;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CheckInActivity extends AppCompatActivity implements AMapLocationListener,
        AMap.OnMapLoadedListener, PoiSearch.OnPoiSearchListener, AMap.OnCameraChangeListener{


    @BindView(R.id.checkinbtn)
    Button checkinbtn;
    @BindView(R.id.map)
    MapView mapView;
    @BindView(R.id.locbtn)
    Button locbtn;
    @BindView(R.id.listview)
    ListView listview;

    private AMap aMap;
    private PoiSearch mPoiSearch;
    private PoiSearch.Query query;
    private AMapLocationClient mapLocationClient;
    private Marker locationMarker, checkinMarker;
    private Circle mcircle;
    private LatLng checkinpoint,mlocation;
    private boolean isItemClickAction, isLocationAction;
    private LatLonPoint  searchLatlonPoint;
    private List<PoiItem> resultData = new ArrayList<>();
    ;
    private SearchResultAdapter searchResultAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        setMapView();
    }

    private void setMapView(){
        if(aMap == null){
            aMap = mapView.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.setOnCameraChangeListener(this);
            aMap.setOnMapLoadedListener(this);
        }

        searchResultAdapter = new SearchResultAdapter(CheckInActivity.this);
        searchResultAdapter.setData(resultData);
        listview.setAdapter(searchResultAdapter);
        listview.setOnItemClickListener(onItemClickListener);
        initLocation();
        startLoaction();
    }

    private void initLocation(){
        mapLocationClient = new AMapLocationClient(this);
        mapLocationClient.setLocationListener(this);
    }

    private void startLoaction(){
        mapLocationClient.setLocationOption(getLocationOptions());
        mapLocationClient.startLocation();
    }

    private AMapLocationClientOption getLocationOptions(){
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setHttpTimeOut(3000);
        option.setNeedAddress(true);
        option.setLocationCacheEnable(false);
        option.setOnceLocation(true);
        return option;
    }

    public void addMarkerInScreenCenter(){
        LatLng latLng = aMap.getCameraPosition().target;
        Point point = aMap.getProjection().toScreenLocation(latLng);
        locationMarker = aMap.addMarker(new MarkerOptions().position(latLng).
                icon(BitmapDescriptorFactory.fromResource(R.drawable.purple_pin)).anchor(0.5f, 0.5f));
        locationMarker.setPositionByPixels(point.x, point.y);
    }

    @Override
    public void onMapLoaded() {
        addMarkerInScreenCenter();
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(aMapLocation != null && aMapLocation.getErrorCode() == 0){
            mlocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            searchLatlonPoint = new LatLonPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
            checkinpoint = mlocation;
            isLocationAction = true;
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mlocation, 16f));
            if(mcircle != null){
                mcircle.setCenter(mlocation);
            }else{
                mcircle = aMap.addCircle(new CircleOptions().center(mlocation).radius(500).strokeWidth(5));
            }
            if (searchLatlonPoint != null) {
                resultData.clear();
                resultData.add(new PoiItem("ID", searchLatlonPoint,"我的位置", searchLatlonPoint.toString()));
                doSearchQuery(searchLatlonPoint);
            }
        }else{
            String errText = "定位失败," + aMapLocation.getErrorCode()+ ": " + aMapLocation.getErrorInfo();
            Log.e("AmapErr",errText);
        }
    }

    private void doSearchQuery(LatLonPoint latLonPoint){
        query = new PoiSearch.Query("", "", "");
        query.setPageNum(0);
        query.setPageSize(20);
        mPoiSearch = new PoiSearch(this, query);
        mPoiSearch.setOnPoiSearchListener(this);
        mPoiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 500, true));
        mPoiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
          if(i == 1000){
              if(poiResult != null && poiResult.getPois() != null){
                 List<PoiItem>  data = poiResult.getPois();
                  resultData.addAll(data);
                  searchResultAdapter.notifyDataSetChanged();
              }
          }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        if (!isItemClickAction && !isLocationAction){
            searchResultAdapter.setSelectedPosition(-1);
            searchResultAdapter.notifyDataSetChanged();
        }
        if (isItemClickAction)
            isItemClickAction = false;
        if (isLocationAction)
            isLocationAction = false;

        if (mcircle != null) {
            if (mcircle.contains(cameraPosition.target)){
                checkinpoint = cameraPosition.target;
            } else{
                ToastUtil.show(this, "微调距离不可超过500米");
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mlocation ,16f ));
            }
        }else {
            startLoaction();
            ToastUtil.show(this, "重新定位中。。。");
        }
    }

    /**
     * 列表点击监听
     */
    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position != searchResultAdapter.getSelectedPosition()) {
                PoiItem poiItem = (PoiItem) searchResultAdapter.getItem(position);
                LatLng curLatlng = new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude());
                isItemClickAction = true;
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(curLatlng));
                searchResultAdapter.setSelectedPosition(position);
                searchResultAdapter.notifyDataSetChanged();

            }
        }
    };


    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        destroyLocation();
    }

    /**
     * 销毁定位
     *
     */
    private void destroyLocation(){
        if (null != mapLocationClient) {
            mapLocationClient.onDestroy();
            mapLocationClient = null;
        }
    }

}
