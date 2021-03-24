package com.gdmap.newscctv.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import com.gdmap.newscctv.R;

import java.util.ArrayList;
import java.util.List;

/**
 * author : jinjiarui
 * time   : 2021/03/24
 * desc   :
 * version:
 */
public class AggregationUtils implements AMap.OnCameraChangeListener, AMap.InfoWindowAdapter, AMap.OnMarkerClickListener,AMap.OnMapClickListener {

        private ArrayList<Cluster> mClusters = new ArrayList<>();   //聚合点

        private List<Marker> allMarker = new ArrayList<>(); //所有的marker

        private Marker clickMarker; //当前点击的marker

        private float zoom;   //当前放大缩小程度

        private int aggregationRadius=100;//聚合半径

        private double mClusterDistance;    //聚合范围

        private List<ClusterItem> allPoints = new ArrayList<ClusterItem>(); //所有的点

        private Context context;

        private AMap aMap;

        public void setContext(Context context) {
            this.context = context;
        }

        /**
         * 设置所有的点
         * @param allPoints
         */
        public void setAllPoints(List<ClusterItem> allPoints) {
            this.allPoints = allPoints;
        }

        /**
         * 设置map
         * @param aMap
         */
        public void setaMap(AMap aMap) {
            this.aMap = aMap;
            zoom = aMap.getCameraPosition().zoom;
            aMap.setInfoWindowAdapter(this);
            aMap.setOnCameraChangeListener(this);
            aMap.setOnMarkerClickListener(this);
            aMap.setOnMapClickListener(this);
        }

        /**
         * 显示所有的点
         */
        private void showPoint(){
            assignClusters();

            //画圆
            for (int i=0;i<mClusters.size();i++){
                Cluster cluster = mClusters.get(i);

                if(clickMarker!=null){
                    if(clickMarker.getPosition().latitude==cluster.getCenterLatLng().latitude&&clickMarker.getPosition().longitude==cluster.getCenterLatLng().longitude){
                        allMarker.add(clickMarker);
                        continue;
                    }
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.anchor(0.5f, 0.5f).icon(getBitmapDes(cluster.mClusterItems.size())).position(cluster.mLatLng).title("...");

                Marker marker = aMap.addMarker(markerOptions);
                marker.setInfoWindowEnable(true);
                marker.setObject(cluster);
                allMarker.add(marker);
            }
        }

        /**
         * 对点进行聚合
         */
        private void assignClusters() {
            //算出聚合点
            mClusterDistance = aMap.getScalePerPixel()*aggregationRadius;

            //屏幕范围
            LatLngBounds visibleBounds = aMap.getProjection().getVisibleRegion().latLngBounds;

            //循环所有点
            for (int i=0;i<allPoints.size();i++) {
                LatLng latlng = allPoints.get(i).latLng;

                //判断当前点是否在可视范围内
                if (visibleBounds.contains(latlng)) {
                    //获取聚合点
                    Cluster cluster = getCluster(latlng,mClusters);

                    //判断聚合点是否为空
                    if (cluster != null) {
                        //不为空则直接加入到聚合点内
                        cluster.addClusterItem(latlng,allPoints.get(i).address,allPoints.get(i).id);
                    } else {
                        //为空则创建聚合点
                        cluster = new Cluster(latlng);
                        mClusters.add(cluster);
                        cluster.addClusterItem(latlng,allPoints.get(i).address,allPoints.get(i).id);
                    }

                }
            }
        }

        /**
         * 判断当前点附近是否有聚合点
         *
         * @param latLng
         * @return
         */
        private Cluster getCluster(LatLng latLng, List<Cluster> clusters) {
            //循环所有的聚合点
            for (Cluster cluster : clusters) {
                LatLng clusterCenterPoint = cluster.getCenterLatLng();
                //计算当前点和聚合点之间的距离
                double distance = AMapUtils.calculateLineDistance(latLng, clusterCenterPoint);

                //如果距离在规定点范围内，则说明有聚合点
                if (distance < mClusterDistance) {
                    return cluster;
                }
            }
            return null;
        }

        /**
         * 获取每个聚合点的绘制样式
         */
        private BitmapDescriptor getBitmapDes(int num) {
            TextView textView = new TextView(context);
            textView.setText(String.valueOf(num));
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            textView.setBackgroundDrawable(getDrawAble());

            return BitmapDescriptorFactory.fromView(textView);
        }


        private Drawable getDrawAble() {
            int radius = DensityUtils.dp2px(context, 50);
            Drawable bitmapDrawable = new BitmapDrawable(null, drawCircle(radius));
            return bitmapDrawable;
        }


        private Bitmap drawCircle(int radius) {
            Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2,
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint1 = new Paint();

            paint1.setColor(Color.parseColor("#2dbdff"));
            paint1.setAlpha(160);
            canvas.drawCircle(radius,radius,radius-10,paint1);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.parseColor("#0386ba"));                          //设置画笔颜色
            paint.setStyle(Paint.Style.STROKE);                       //设置画笔为空心
            paint.setStrokeWidth(DensityUtils.dp2px(context, 4));             //设置线宽
            canvas.drawCircle(radius,radius,radius-10,paint);

            return bitmap;
        }

        /**
         * 点击地图
         * @param latLng
         */
        @Override
        public void onMapClick(LatLng latLng) {
            clickMarker.hideInfoWindow();
        }

        /**
         * 地图移动
         * @param cameraPosition
         */
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if(zoom!=cameraPosition.zoom){
                clickMarker = null;
                zoom = cameraPosition.zoom;
            }
        }
        /**
         * 地图移动完成
         * @param cameraPosition
         */
        @Override
        public void onCameraChangeFinish(CameraPosition cameraPosition) {
            for (Marker marker:allMarker){
                if(clickMarker!=null&&clickMarker.getPosition().longitude==marker.getPosition().longitude&&clickMarker.getPosition().latitude==marker.getPosition().latitude){
                    continue;
                }

                marker.hideInfoWindow();
                marker.remove();
            }

            allMarker = new ArrayList<Marker>();
            mClusters = new ArrayList<Cluster>();
            mClusterDistance = aMap.getScalePerPixel()*aggregationRadius;//聚合的范围半径
            showPoint();

            //如果点击的marker 不再可是范围内
            LatLngBounds visibleBounds = aMap.getProjection().getVisibleRegion().latLngBounds;
            if(!visibleBounds.contains(clickMarker.getPosition())){
                clickMarker.hideInfoWindow();
                clickMarker.remove();
                clickMarker=null;
            }
        }
        /**
         * marker 点击
         * @param
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            clickMarker = marker;
            marker.showInfoWindow();

            //返回:true 表示点击marker 后marker 不会移动到地图中心；返回false 表示点击marker 后marker 会自动移动到地图中心
            return true;
        }

        /**
         * 自定义弹框
         * @param marker
         * @return
         */
        @Override
        public View getInfoWindow(Marker marker) {
            View infoContent = LayoutInflater.from(context).inflate(0, null);
            render(infoContent,marker);
            return infoContent;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }


        /**
         * 自定义infowinfow窗口
         * @param
         * @param view
         */
        public void render(View view,Marker marker) {
            final Cluster cluster = (Cluster)marker.getObject();

            ListView recyclerView = (ListView) view.findViewById(0);
            recyclerView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return cluster.mClusterItems.size();
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = LayoutInflater.from(context).inflate(0, parent,false);
                    TextView tv = (TextView) view.findViewById(0);
                    tv.setText(cluster.mClusterItems.get(position).address);
                    return view;
                }
            });
            setListViewHeightBasedOnChildren(recyclerView);

        }

        /**
         * 切换页面的时候调用
         */
        public void resetData(){
            if(clickMarker!=null){
                clickMarker.hideInfoWindow();
            }
            allPoints.clear();
        }

        /**
         * 动态设置listview 的高度
         * @param listView
         */
        public void setListViewHeightBasedOnChildren(ListView listView) {

            //获取listview的适配器
            ListAdapter listAdapter = listView.getAdapter(); //item的高度

            if (listAdapter == null) {
                return;
            }
            int totalHeight = 0;

            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);

                listItem.measure(0, 0); //计算子项View 的宽高 //统计所有子项的总高度
                totalHeight += listItem.getMeasuredHeight()+listView.getDividerHeight();
            }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            int maxHeight=DensityUtils.dp2px(context,100);
            if(totalHeight>maxHeight){
                params.height=maxHeight;
            }else{
                params.height = totalHeight;
            }

            listView.setLayoutParams(params);
        }

        /**
         *
         * 聚合点
         */
        public class Cluster {
            //聚合点位置
            private LatLng mLatLng;

            //聚合点中列表
            private List<ClusterItem> mClusterItems = new ArrayList<ClusterItem>();

            Cluster( LatLng latLng) {
                mLatLng = latLng;
            }

            LatLng getCenterLatLng() {
                return mLatLng;
            }

            void addClusterItem(LatLng latLng ,String address,String id) {
                ClusterItem clusterItem=new ClusterItem();
                clusterItem.latLng=latLng;
                clusterItem.address=address;
                clusterItem.id=id;
                mClusterItems.add(clusterItem);
            }
        }

        static class ClusterItem{
            public LatLng latLng;
            public String address;
            public String id;
        }
}