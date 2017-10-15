package com.gdmap.newscctv;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gdmap.newscctv.overlay.PoiOverlay;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PoiAroundSearchActivity extends AppCompatActivity implements
        View.OnClickListener, PoiSearch.OnPoiSearchListener, AMap.OnMarkerClickListener{


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
    private PoiResult poiResult; // poi返回的结果
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private LatLonPoint lp = new LatLonPoint(39.993743, 116.472995);// 116.472995,39.993743
    private Marker locationMarker; // 选择的点
    private Marker detailMarker;
    private Marker mlastMarker;
    private PoiSearch poiSearch;
    private List<PoiItem> poiItems;// poi数据
    private String keyWord = "";
    private PoiOverlay poiOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_around_search);
        ButterKnife.bind(this);
        mapView.onCreate(savedInstanceState);
        setMapView();
    }

    public void setMapView() {
        if (aMap == null) {
            aMap = mapView.getMap();
            MarkerOptions locOptions = new MarkerOptions();
            locOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.gps_point)));
            locOptions.anchor(0.5f, 0.5f);
            locOptions.position(new LatLng(lp.getLatitude(), lp.getLongitude()));
            locationMarker = aMap.addMarker(locOptions);
            locationMarker.showInfoWindow();
        }
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lp.getLatitude(), lp.getLongitude()), 14));
        setUp();
    }

    public void setUp(){
        aMap.setOnMarkerClickListener(this);
        poiDetail.setOnClickListener(this);
        btnSearch.setOnClickListener(this);

    }


    private void whetherToShowDetailInfo(boolean isToShow) {
        if (isToShow) {
           poiDetail.setVisibility(View.VISIBLE);

        } else {
            poiDetail.setVisibility(View.GONE);
        }
    }


    public void doSearchQuery(){
        keyWord = inputEdittext.getText().toString().trim();
        currentPage = 0;
        query = new PoiSearch.Query(keyWord, "", "北京市");
        query.setPageSize(20);
        query.setPageNum(currentPage);

        if(lp != null){
            poiSearch = new PoiSearch(getApplicationContext(), query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.setBound(new PoiSearch.SearchBound(lp, 5000, true));//默认由近到远排序
            poiSearch.searchPOIAsyn();
        }
    }

    private void showSuggestCity(List<SuggestionCity> suggestionCities){
        String infomation = "推荐城市\n";
        for (int i = 0; i < suggestionCities.size(); i++) {
            infomation += "城市名称:" + suggestionCities.get(i).getCityName() + "城市区号:"
                    + suggestionCities.get(i).getCityCode() + "城市编码:"
                    + suggestionCities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(PoiAroundSearchActivity.this, infomation);
    }


    // 将之前被点击的marker置为原来的状态
    private void resetlastmarker() {
        int index = poiOverlay.getPoiIndex(mlastMarker);
        if (index < 10) {
            mlastMarker.setIcon(BitmapDescriptorFactory
                    .fromBitmap(BitmapFactory.decodeResource(
                            getResources(),
                            Utils.markers[index])));
        }else {
            mlastMarker.setIcon(BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(getResources(), R.mipmap.marker_highlight)));
        }
        mlastMarker = null;

    }

    private void setPoiItemDisplayContent(final PoiItem mCurrentPoi) {
        poiName.setText(mCurrentPoi.getTitle());
        poiAddress.setText(mCurrentPoi.getSnippet()+mCurrentPoi.getDistance());
    }

    @Override
    public void onPoiSearched(PoiResult Result, int rcode) {
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (Result != null && Result.getQuery() != null) {// 搜索poi的结果
                if (Result.getQuery().equals(query)) {// 是否是同一条
                    this.poiResult = Result;
                    poiItems = poiResult.getPois();
                    List<SuggestionCity> suggestionCities = poiResult
                            .getSearchSuggestionCitys();
                    if (poiItems != null && poiItems.size() > 0) {
                        //清除POI信息显示
                        whetherToShowDetailInfo(false);
                        //并还原点击marker样式
                        if (mlastMarker != null) {
                            resetlastmarker();
                        }
                        //清理之前搜索结果的marker
                        if (poiOverlay !=null) {
                            poiOverlay.removeFromMap();
                        }
                        aMap.clear();
                        poiOverlay = new PoiOverlay(aMap, getApplicationContext(), poiItems);
                        poiOverlay.setPoiType(1);
                        poiOverlay.addToMap();
                        poiOverlay.zoomToSpan();

                        aMap.addMarker(new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .icon(BitmapDescriptorFactory
                                        .fromBitmap(BitmapFactory.decodeResource(
                                                getResources(), R.mipmap.gps_point)))
                                .position(new LatLng(lp.getLatitude(), lp.getLongitude())));

                        aMap.addCircle(new CircleOptions().center(new
                                LatLng(lp.getLatitude(), lp.getLongitude()
                        )).radius(5000).strokeColor(Color.BLUE).fillColor(Color.argb(50, 1, 1, 1))
                                .strokeWidth(2));

                    }else if (suggestionCities != null
                            && suggestionCities.size() > 0) {
                        showSuggestCity(suggestionCities);
                    } else {
                        ToastUtil.show(PoiAroundSearchActivity.this,
                                R.string.no_result);
                    }
                }
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.getObject() != null) {
            whetherToShowDetailInfo(true);
            try {
                PoiItem mCurrentPoi = (PoiItem) marker.getObject();
                if (mlastMarker == null) {
                    mlastMarker = marker;
                } else {
                    // 将之前被点击的marker置为原来的状态
                    resetlastmarker();
                    mlastMarker = marker;
                }
                detailMarker = marker;
                detailMarker.setIcon(BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory.decodeResource(
                                getResources(),
                                R.mipmap.poi_marker_pressed)));

                setPoiItemDisplayContent(mCurrentPoi);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }else {
            whetherToShowDetailInfo(false);
            resetlastmarker();
        }


        return true;
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
}
