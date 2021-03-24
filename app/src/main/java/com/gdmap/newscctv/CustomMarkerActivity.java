package com.gdmap.newscctv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomMarkerActivity extends AppCompatActivity implements View.OnClickListener,
        AMap.OnInfoWindowClickListener ,AMap.OnMarkerDragListener, AMap.OnMapLoadedListener ,
        AMap.InfoWindowAdapter, AMap.OnMarkerClickListener{


    @BindView(R.id.mark_listenter_text)
    TextView markListenterText;
    @BindView(R.id.map)
    MapView map;
    @BindView(R.id.clearMap)
    Button clearMap;
    @BindView(R.id.resetMap)
    Button resetMap;
    @BindView(R.id.default_info_window)
    RadioButton defaultInfoWindow;
    @BindView(R.id.custom_info_contents)
    RadioButton customInfoContents;
    @BindView(R.id.custom_info_window)
    RadioButton customInfoWindow;
    @BindView(R.id.custom_info_window_options)
    RadioGroup customInfoWindowOptions;

    private AMap mAmapControl;
    private MarkerOptions markerOptions;
    private Marker marker;
    private Marker growMarker;
    private LatLng latlng = new LatLng(36.061, 103.834);
    private LatLng newlatlng = new LatLng(39.761, 116.434);
    private Marker mMoveMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_marker);
        ButterKnife.bind(this);
        map.onCreate(savedInstanceState);
        clearMap.setOnClickListener(this);
        resetMap.setOnClickListener(this);
        customInfoWindowOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(radioGroup.getId() == R.id.default_info_window){
                   mAmapControl.setInfoWindowAdapter(null);
                }else if(radioGroup.getId() == R.id.custom_info_window){
                    mAmapControl.setInfoWindowAdapter(CustomMarkerActivity.this);
                }else if(radioGroup.getId() == R.id.custom_info_window_options){
                    mAmapControl.setInfoWindowAdapter(CustomMarkerActivity.this);
                }
            }
        });
        setMap();
    }

    private void setMap(){
        if(mAmapControl == null){
            mAmapControl = map.getMap();
        }
        addMarkersToMap();
        mAmapControl.setOnInfoWindowClickListener(this);
    }

    private void addMarkersToMap() {
        TextOptions textOptions = new TextOptions();
        textOptions.backgroundColor(R.color.black);
        textOptions.position(Const.BEIJING);
        textOptions.text("haha");
        textOptions.fontSize(15);
        textOptions.fontColor(Color.GREEN);
        textOptions.rotate(20);
        textOptions.align(Text.ALIGN_CENTER_HORIZONTAL, Text.ALIGN_CENTER_VERTICAL);
        textOptions.zIndex(1.f).typeface(Typeface.DEFAULT_BOLD);
        mAmapControl.addText(textOptions);

        MarkerOptions centermarkOptions = new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        centermarkOptions.title("好好学习");
        centermarkOptions.draggable(true);
        Marker marker = mAmapControl.addMarker(centermarkOptions);
        marker.setPositionByPixels(400, 400);
        marker.setRotateAngle(90);
        marker.showInfoWindow();

        MarkerOptions optionsXiAn = new MarkerOptions();
        optionsXiAn.draggable(true);
        optionsXiAn.title("西安");
        optionsXiAn.position(Const.XIAN);
        optionsXiAn.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.location_marker)));
        optionsXiAn.setFlat(true);

        ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
        giflist.add(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        giflist.add(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED));
        giflist.add(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

        MarkerOptions markerOptionChengDu = new MarkerOptions().anchor(0.5f, 0.5f)
                .position(Const.CHENGDU).title("成都市")
                .snippet("成都市:30.679879, 104.064855").icons(giflist)
                .draggable(true).period(10);

        ArrayList<MarkerOptions> markerOptionsList = new ArrayList<>();
        markerOptionsList.add(optionsXiAn);
        markerOptionsList.add(markerOptionChengDu);

        List<Marker> markerList = mAmapControl.addMarkers(markerOptionsList, true);
        marker = markerList.get(0);

        growMarker = mAmapControl.addMarker(new MarkerOptions().position(Const.ZHENGZHOU).icon(
                BitmapDescriptorFactory.fromResource(
                        R.mipmap.ic_launcher_round)));

        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.location_marker))
                .position(newlatlng)
                .draggable(false);
        mMoveMarker = mAmapControl.addMarker(markerOption);

        initView(marker);
    }

    public void initView(Marker marker){
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.marker_view, null);
        ImageView iv_head = view.findViewById(R.id.iv_head);
        Glide.with( getApplicationContext())
                .load("https://imagepphcloud.thepaper.cn/pph/image/121/973/683.jpg")
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        iv_head.setImageDrawable(resource);
                        Bitmap bitmap = getViewBitmap(view);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

    }

    public Bitmap getViewBitmap(View view) {
        view.setDrawingCacheEnabled(true);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
        view.buildDrawingCache();
        return Bitmap.createBitmap(view.getDrawingCache());
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            /**
             * 清空地图上所有已经标注的marker
             */
            case R.id.clearMap:
                if (mAmapControl != null) {
                    mAmapControl.clear();
                }
                break;
            /**
             * 重新标注所有的marker
             */
            case R.id.resetMap:
                if (mAmapControl != null) {
                    mAmapControl.clear();
                    addMarkersToMap();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        String curString  = marker.getTitle() + "拖动时当前位置:(lat,lng)\n(" +
                  marker.getPosition().latitude + marker.getPosition().longitude +")";
        markListenterText.setText(curString);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
       markListenterText.setText(marker.getTitle() + "开始拖动");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
      markListenterText.setText(marker.getTitle() + "停止拖动");
    }

    @Override
    public void onMapLoaded() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds = new LatLngBounds.Builder().include(Const.XIAN).include(Const.CHENGDU)
                .include(Const.BEIJING).include(latlng).build();
        mAmapControl.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
    }


    @Override
    public View getInfoWindow(Marker marker) {
        if (customInfoWindowOptions.getCheckedRadioButtonId() != R.id.custom_info_contents) {
            return null;
        }
        View view = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
        render(marker, view);
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (customInfoWindowOptions.getCheckedRadioButtonId() != R.id.custom_info_window) {
            return null;
        }
        View view = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        render(marker, view);
        return view;
    }

    /**
     * 自定义infowinfow窗口
     */
    public void render(Marker marker, View view) {
        if (customInfoWindowOptions.getCheckedRadioButtonId() == R.id.custom_info_contents) {
            ((ImageView) view.findViewById(R.id.badge))
                    .setImageResource(R.mipmap.badge_sa);
        } else if (customInfoWindowOptions.getCheckedRadioButtonId() == R.id.custom_info_window) {
            ImageView imageView = (ImageView) view.findViewById(R.id.badge);
            imageView.setImageResource(R.mipmap.badge_wa);
        }
        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if (title != null) {
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED), 0,
                    titleText.length(), 0);
            titleUi.setTextSize(15);
            titleUi.setText(titleText);

        } else {
            titleUi.setText("");
        }
        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if (snippet != null) {
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.GREEN), 0,
                    snippetText.length(), 0);
            snippetUi.setTextSize(20);
            snippetUi.setText(snippetText);
        } else {
            snippetUi.setText("");
        }
    }

    //infowindow点击事件
    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "你点击了infoWindow窗口" + marker.getTitle(), Toast.LENGTH_LONG).show();
        Toast.makeText(this, "当前地图可视区域内Marker数量:"
                + mAmapControl.getMapScreenMarkers().size(), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mAmapControl != null) {
            if (marker.equals(marker)) {
                jumpPoint(marker);
            } else if (marker.equals(growMarker)) {
                growInto(marker);
            }

        }
        markListenterText.setText("你点击的是" + marker.getTitle());

        return false;
    }

    /**
     * marker点击时跳动一下
     */
    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mAmapControl.getProjection();
        Point startPoint = proj.toScreenLocation(Const.XIAN);
        startPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * Const.XIAN.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * Const.XIAN.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private int count = 1;
    Bitmap lastMarkerBitMap = null;

    /**
     * 从地上生长效果，实现思路
     * 在较短的时间内，修改marker的图标大小，从而实现动画<br>
     * 1.保存原始的图片；
     * 2.在原始图片上缩放得到新的图片，并设置给marker；
     * 3.回收上一张缩放后的图片资源；
     * 4.重复2，3步骤到时间结束；
     * 5.回收上一张缩放后的图片资源，设置marker的图标为最原始的图片；
     *
     * 其中时间变化由AccelerateInterpolator控制
     * @param marker
     */
    private void growInto(final Marker marker) {
        marker.setVisible(false);
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 250;// 动画总时长
        final Bitmap bitMap = marker.getIcons().get(0).getBitmap();// BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher);
        final int width = bitMap.getWidth();
        final int height = bitMap.getHeight();

        final Interpolator interpolator = new AccelerateInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);

                if (t > 1) {
                    t = 1;
                }

                // 计算缩放比例
                int scaleWidth = (int) (t * width);
                int scaleHeight = (int) (t * height);
                if (scaleWidth > 0 && scaleHeight > 0) {

                    // 使用最原始的图片进行大小计算
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(Bitmap
                            .createScaledBitmap(bitMap, scaleWidth,
                                    scaleHeight, true)));
                    marker.setVisible(true);

                    // 因为替换了新的图片，所以把旧的图片销毁掉，注意在设置新的图片之后再销毁
                    if (lastMarkerBitMap != null
                            && !lastMarkerBitMap.isRecycled()) {
                        lastMarkerBitMap.recycle();
                    }

                    //第一次得到的缩放图片，在第二次回收，最后一次的缩放图片，在动画结束时回收
                    ArrayList<BitmapDescriptor> list = marker.getIcons();
                    if (list != null && list.size() > 0) {
                        // 保存旧的图片
                        lastMarkerBitMap = marker.getIcons().get(0).getBitmap();
                    }

                }

                if (t < 1.0 && count < 10) {
                    handler.postDelayed(this, 16);
                } else {
                    // 动画结束回收缩放图片，并还原最原始的图片
                    if (lastMarkerBitMap != null
                            && !lastMarkerBitMap.isRecycled()) {
                        lastMarkerBitMap.recycle();
                    }
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(bitMap));
                    marker.setVisible(true);
                }
            }
        });
    }
}
