package com.gdmap.newscctv;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.animation.Animation;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.maps.model.animation.AlphaAnimation;
import com.gdmap.newscctv.model.Cluster;
import com.gdmap.newscctv.model.ClusterClickListener;
import com.gdmap.newscctv.model.ClusterItem;
import com.gdmap.newscctv.model.ClusterRender;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinjiarui on 2017/10/9.
 */

public class ClusterOverlay implements AMap.OnCameraChangeListener, AMap.OnMarkerClickListener {

    private AMap aMap;
    private Context mContext;
    private List<ClusterItem> mClusterItems;
    private List<Cluster> mClusters;
    private int mClusterSize;
    private ClusterClickListener mClusterClickListener;
    private ClusterRender mClusterRender;
    private LruCache<Integer, BitmapDescriptor> mLruCache;
    private float mPXInMeters;
    private double mClusterDistance;
    private HandlerThread mMarkerHandlerThread = new HandlerThread("addMarker");
    private HandlerThread mSignClusterThread = new HandlerThread("calculateCluster");
    private Handler mMarkerhandler;
    private Handler mSignClusterHandler;
    private boolean mIsCanceled = false;
    private List<Marker> mAddMarkers = new ArrayList<Marker>();

    private static final int CALCULATE_CLUSTER = 0;
    private static final int CALCULATE_SINGLE_CLUSTER = 1;
    private static final int ADD_CLUSTER_LIST = 0;

    private static final int ADD_SINGLE_CLUSTER = 1;

    private static final int UPDATE_SINGLE_CLUSTER = 2;


    public ClusterOverlay(AMap map, int clusterSize, Context context) {
        this(map, context, null, clusterSize);
    }

    public ClusterOverlay(AMap aMap, Context mContext, List<ClusterItem> clusterItems, int mClusterSize) {
        this.aMap = aMap;
        this.mContext = mContext;
        if (clusterItems != null) {
            this.mClusterItems = clusterItems;
        } else {
            this.mClusterItems = new ArrayList<ClusterItem>();
        }

        this.mClusters = new ArrayList<Cluster>();

        this.mClusterSize = mClusterSize;
        mLruCache = new LruCache<Integer, BitmapDescriptor>(80) {
            @Override
            public void resize(int maxSize) {
                super.resize(maxSize);
            }

            @Override
            protected void entryRemoved(boolean evicted, Integer key, BitmapDescriptor oldValue, BitmapDescriptor newValue) {
                oldValue.recycle();
            }
        };
        this.mPXInMeters = aMap.getScalePerPixel();//获取当前缩放级别下，地图上1像素点对应的长度，单位米
        this.mClusterDistance = this.mPXInMeters * this.mClusterSize;
        aMap.setOnMarkerClickListener(this);//onMarker的点击时间
        aMap.setOnCameraChangeListener(this);
        initThreadHandler();
        assignClusters();
    }

    //初始化Handler
    private void initThreadHandler() {
        mMarkerHandlerThread.start();
        mSignClusterThread.start();
        mMarkerhandler = new Handler(mMarkerHandlerThread.getLooper(), MarkerCallback);
        mSignClusterHandler = new Handler(mSignClusterThread.getLooper(), SignClusterCallback);
    }

    /**
     * 对点进行聚合
     */
    private void assignClusters() {
        mIsCanceled = true;
        mSignClusterHandler.removeMessages(CALCULATE_CLUSTER);
        mSignClusterHandler.sendEmptyMessage(CALCULATE_CLUSTER);
    }

    private void calculateClusters() {
        mIsCanceled = false;
        mClusters.clear();
        LatLngBounds visibleBounds = aMap.getProjection().getVisibleRegion().latLngBounds;
        for (ClusterItem item : mClusterItems) {
            if (mIsCanceled) {
                return;
            }
            LatLng latlng = item.getPosition();
            if (visibleBounds.contains(latlng)) {
                Cluster cluster = getCluster(latlng, mClusters);
                if (cluster != null) {
                    cluster.addClusterItem(item);
                } else {
                    cluster = new Cluster(latlng);
                    mClusters.add(cluster);
                    cluster.addClusterItem(item);
                }
            }
        }

        //复制一份数据，规避同步
        List<Cluster> clusters = new ArrayList<Cluster>();
        clusters.addAll(mClusters);
        Message message = mMarkerhandler.obtainMessage();
        message.obj = clusters;
        message.what = ADD_CLUSTER_LIST;
        if (mIsCanceled) {
            return;
        }
        mMarkerhandler.sendMessage(message);
    }

    /**
     * 在已有的聚合基础上，对添加的单个元素进行聚合
     *
     * @param clusterItem
     */
    private void calculateSingleCluster(ClusterItem clusterItem) {
        LatLngBounds bounds = aMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng latlng = clusterItem.getPosition();
        if (!bounds.contains(latlng)) {
            return;
        }
        Cluster cluster = getCluster(latlng, mClusters);
        if (cluster != null) {
            cluster.addClusterItem(clusterItem);
            Message message = mMarkerhandler.obtainMessage();
            message.what = UPDATE_SINGLE_CLUSTER;
            message.obj = cluster;

            mMarkerhandler.removeMessages(UPDATE_SINGLE_CLUSTER);
            mMarkerhandler.sendMessageDelayed(message, 5);

        } else {
            cluster = new Cluster(latlng);
            mClusters.add(cluster);
            cluster.addClusterItem(clusterItem);

            Message message = mMarkerhandler.obtainMessage();
            message.what = ADD_SINGLE_CLUSTER;
            message.obj = cluster;
            mMarkerhandler.sendMessage(message);
        }
    }

    /**
     * 添加一个聚合点
     *
     * @param item
     */
    public void addClusterItem(ClusterItem item) {
        Message msg = mSignClusterHandler.obtainMessage();
        msg.what = CALCULATE_SINGLE_CLUSTER;
        msg.obj = item;
        mSignClusterHandler.sendMessage(msg);
    }

    /**
     * 将聚合元素添加至地图上
     */
    private void addClusterToMap(List<Cluster> clusters) {

        ArrayList<Marker> removeMarkers = new ArrayList<>();
        removeMarkers.addAll(mAddMarkers);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        MyAnimationListener myAnimationListener = new MyAnimationListener(removeMarkers);
        for (Marker marker : removeMarkers) {
            marker.setAnimation(alphaAnimation);
            marker.setAnimationListener(myAnimationListener);
            marker.startAnimation();
        }

        for (Cluster cluster : clusters) {
            addSingleClusterToMap(cluster);
        }
    }

    /**
     * 更新已加入地图聚合点的样式
     */
    private void updateCluster(Cluster cluster) {
        Marker marker = cluster.getMarker();
        marker.setIcon(getBitmapDes(cluster.getClusterCount()));
    }

    public class MyAnimationListener implements com.amap.api.maps.model.animation.Animation.AnimationListener {

        private List<Marker> removeMarkers;

        public MyAnimationListener(List<Marker> removeMarkers) {
            this.removeMarkers = removeMarkers;
        }

        @Override
        public void onAnimationStart() {

        }

        @Override
        public void onAnimationEnd() {
            for (Marker marker : removeMarkers) {
                marker.remove();
            }
            removeMarkers.clear();
        }
    }


    private AlphaAnimation mADDAnimation = new AlphaAnimation(0, 1);

    /**
     * 将单个聚合元素添加至地图显示
     *
     * @param cluster
     */
    int i = 0;
    private void addSingleClusterToMap(Cluster cluster) {
        MarkerOptions options = new MarkerOptions();
        options.position(cluster.getCenterLatLng());
        options.anchor(0.5f, 0.5f);
        options.icon(getBitmapDes(cluster.getClusterCount()));
        Marker marker = aMap.addMarker(options);
        marker.setAnimation(mADDAnimation);
        marker.setObject(cluster);

        marker.startAnimation();
        cluster.setMarker(marker);
        mAddMarkers.add(marker);
        if( i == 0){
            marker.showInfoWindow();
        }
        i++;
    }

    /**
     * 获取每个聚合点的绘制样式
     */
    private BitmapDescriptor getBitmapDes(int num) {
        BitmapDescriptor bitmapDescriptor = mLruCache.get(num);
        if (bitmapDescriptor == null) {
            TextView textView = new TextView(mContext);
            if (num > 1) {
                String tile = String.valueOf(num);
                textView.setText(tile);
            }
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            if (mClusterRender != null && mClusterRender.getDrawAble(num) != null) {
                textView.setBackgroundDrawable(mClusterRender.getDrawAble(num));
            } else {
                textView.setBackgroundResource(R.mipmap.defaultcluster);
            }
            bitmapDescriptor = BitmapDescriptorFactory.fromView(textView);
            mLruCache.put(num, bitmapDescriptor);

        }
        return bitmapDescriptor;
    }

    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     *
     * @param latLng
     * @return
     */
    private Cluster getCluster(LatLng latLng, List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            LatLng latLng1 = cluster.getCenterLatLng();
            double distance = AMapUtils.calculateLineDistance(latLng, latLng1);
            if (distance < mClusterDistance) {
                return cluster;
            }
        }
        return null;
    };

    private Handler.Callback MarkerCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case ADD_CLUSTER_LIST:
                    Log.i("MarkerCallback", Thread.currentThread().getName() + "线程名字");
                    List<Cluster> clusters = (List<Cluster>) message.obj;
                    addClusterToMap(clusters);
                    break;
                case ADD_SINGLE_CLUSTER:
                    Cluster cluster = (Cluster) message.obj;
                    addSingleClusterToMap(cluster);
                    break;
                case UPDATE_SINGLE_CLUSTER:
                    Cluster updateCluster = (Cluster) message.obj;
                    updateCluster(updateCluster);
                    break;
            }
            return false;
        }

    };

    private Handler.Callback SignClusterCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case CALCULATE_CLUSTER:
                    calculateClusters();
                    break;
                case CALCULATE_SINGLE_CLUSTER:
                    ClusterItem item = (ClusterItem) message.obj;
                    mClusterItems.add(item);
                    calculateSingleCluster(item);
                    break;
            }
            return false;
        }
    };

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        mPXInMeters = aMap.getScalePerPixel();
        mClusterDistance = mPXInMeters * mClusterSize;
        assignClusters();
        VisibleRegion visibleRegion = aMap.getProjection().getVisibleRegion();//获取可视区域
        LatLngBounds latLngBounds = visibleRegion.latLngBounds;
        boolean isContain = latLngBounds.contains(Const.BEIJING);// 判断上海经纬度是否包括在当前地图可见区域
        if (isContain) {
            ToastUtil.show(mContext, "北京市在地图当前可见区域内");
        } else {
            ToastUtil.show(mContext, "北京市市超出地图当前可见区域");
        }

    }


    public void setmClusterRender(ClusterRender mClusterRender) {
        this.mClusterRender = mClusterRender;
    }

    public void setmClusterClickListener(ClusterClickListener mClusterClickListener) {
        this.mClusterClickListener = mClusterClickListener;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(mClusterClickListener == null){
            return true;
        }
        Cluster cluster = (Cluster) marker.getObject();
        if(cluster != null){
            mClusterClickListener.onClick(marker, cluster.getClusterItems());
            return true;
        }
        return false;
    }
}
