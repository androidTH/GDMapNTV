package com.gdmap.newscctv;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.gdmap.newscctv.overlay.WalkRouteOverlay;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WalkRouteActivity extends AppCompatActivity implements RouteSearch.OnRouteSearchListener{

    @BindView(R.id.route_map)
    public MapView mapView;
    @BindView(R.id.route_drive)
    ImageView routeDrive;
    @BindView(R.id.route_bus)
    ImageView routeBus;
    @BindView(R.id.route_walk)
    ImageView routeWalk;
    @BindView(R.id.route_CrosstownBus)
    TextView routeCrosstownBus;
    @BindView(R.id.routemap_choose)
    LinearLayout routemapChoose;
    @BindView(R.id.routemap_header)
    RelativeLayout mHeadLayout;
    @BindView(R.id.firstline)
    TextView mRotueTimeDes;
    @BindView(R.id.secondline)
    TextView mRouteDetailDes;
    @BindView(R.id.detail)
    LinearLayout detail;
    @BindView(R.id.bottom_layout)
    RelativeLayout mBottomLayout;
    @BindView(R.id.bus_result_list)
    ListView busResultList;
    @BindView(R.id.bus_result)
    LinearLayout busResult;

    private AMap aMap;
    private Context mContext;
    private RouteSearch mRouteSearch;
    private WalkRouteResult mWalkRouteResult;
    private LatLonPoint mStartPoint = new LatLonPoint(39.942295, 116.335891);//起点，116.335891,39.942295
    private LatLonPoint mEndPoint = new LatLonPoint(39.995576, 116.481288);//终点，116.481288,39.995576
    private final int ROUTE_TYPE_WALK = 3;

    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_route);
        ButterKnife.bind(this);
        mContext = getApplicationContext();
        mapView.onCreate(savedInstanceState);
        setMap();
        initView();
        searchRouteResult(ROUTE_TYPE_WALK, 1);
    }

    public void setMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.getUiSettings().setMyLocationButtonEnabled(false);
            aMap.getUiSettings().setRotateGesturesEnabled(false);
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.addMarker(new MarkerOptions().icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.amap_start)).position(Utils.convertToLatLng(mStartPoint)));
            aMap.addMarker(new MarkerOptions().icon(
                    BitmapDescriptorFactory.fromResource(R.drawable.amap_end)).position(Utils.convertToLatLng(mEndPoint)));

        }
        mRouteSearch = new RouteSearch(this);
        mRouteSearch.setRouteSearchListener(this);
    }

    public void initView() {
        mHeadLayout.setVisibility(View.GONE);
        mRotueTimeDes = (TextView) findViewById(R.id.firstline);
        mRouteDetailDes = (TextView) findViewById(R.id.secondline);
    }

    public void searchRouteResult(int routeType, int mode) {

        if (mStartPoint == null) {
            ToastUtil.show(mContext, "定位中，稍后再试...");
            return;
        }
        if (mEndPoint == null) {
            ToastUtil.show(mContext, "终点未设置");
        }
        showProgressDialog();
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(mStartPoint, mEndPoint);
        RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(fromAndTo);

        if(routeType == ROUTE_TYPE_WALK){
            mRouteSearch.calculateWalkRouteAsyn(walkRouteQuery);
        }
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(true);
        progDialog.setMessage("正在搜索");
        progDialog.show();
    }

    /**
     * 隐藏进度框
     */
    private void dissmissProgressDialog() {
        if (progDialog != null) {
            progDialog.dismiss();
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

    }

    @Override
    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
        dissmissProgressDialog();
        aMap.clear();// 清理地图上的所有覆盖物
           if(i == 1000){
               if(walkRouteResult != null && walkRouteResult.getPaths() != null){
                   if(walkRouteResult.getPaths().size() > 0){
                       this.mWalkRouteResult = walkRouteResult;
                       WalkPath walkPath = walkRouteResult.getPaths().get(0);
                       WalkRouteOverlay walkRouteOverlay = new WalkRouteOverlay(
                               mContext, aMap, walkPath,
                               mWalkRouteResult.getStartPos(),
                               mWalkRouteResult.getTargetPos());
                       walkRouteOverlay.removeFromMap();
                       walkRouteOverlay.addToMap();
                       walkRouteOverlay.zoomToSpan();
                       mBottomLayout.setVisibility(View.VISIBLE);
                       float dur = walkPath.getDuration();
                       float dis = walkPath.getDuration();
                       String des = Utils.getFriendlyTime((int)dur)+"("+Utils.getFriendlyLength((int)dis)+")";
                       mRotueTimeDes.setText(des);
                       mRouteDetailDes.setVisibility(View.GONE);
                   } else {
                       ToastUtil.showerror(this.getApplicationContext(), R.string.no_result);
                   }
               } else {
                   ToastUtil.showerror(this.getApplicationContext(), R.string.no_result);
               }
           } else {
               ToastUtil.showerror(this.getApplicationContext(), i);
           }
    }

    @Override
    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

    }

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
    }

}
