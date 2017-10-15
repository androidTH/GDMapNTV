package com.gdmap.newscctv;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;

/**
 * 自定义的缩放控件
 * Created by my94493 on 2017/1/6.
 */

public class ZoomView extends LinearLayout {
    private ImageView zoomIn, zoomOut;
    private OnChangeCamera mOnChangeCamera;

    public ZoomView(Context context) {
        this(context, null);
    }
    public ZoomView(Context context,AttributeSet attrs){
        super(context, attrs);
        initView(context);
    }
    public void initView(Context context) {

        LayoutParams mParams = new LayoutParams(130, 130);
        zoomIn = new ImageView(context);
        zoomIn.setId(R.id.zoomin_view);
        zoomIn.setLayoutParams(mParams);
        zoomIn.setScaleType(ImageView.ScaleType.FIT_XY);
        zoomIn.setImageResource(R.drawable.zoomin_v);
        zoomIn.setClickable(true);
        zoomIn.setOnClickListener(onClickListener);

        zoomOut = new ImageView(context);
        zoomOut.setId(R.id.zoomout_view);
        zoomOut.setLayoutParams(mParams);
        zoomOut.setScaleType(ImageView.ScaleType.FIT_XY);
        zoomOut.setImageResource(R.drawable.zoomout_v);
        zoomOut.setClickable(true);
        zoomOut.setOnClickListener(onClickListener);

        this.setOrientation(VERTICAL);//HORIZONTAL水平排列，VERTICAL垂直排列
        this.addView(zoomIn);
        this.addView(zoomOut);
    }

    /**
     * 根据地图缩放级别，调整缩放按键的图片
     * @param zoomlevel
     */
    public void setZoomBitmap(float zoomlevel, float maxZoomLevel, float minZoomLevel) {
        try {
            if (zoomlevel < maxZoomLevel
                    && zoomlevel > minZoomLevel) {
                zoomIn.setImageResource(R.drawable.zoomin_v);
                zoomOut.setImageResource(R.drawable.zoomout_v);
            } else if (zoomlevel == minZoomLevel) {
                zoomOut.setImageResource(R.mipmap.zoomout_v_disabled);
                zoomIn.setImageResource(R.drawable.zoomin_v);
            } else if (zoomlevel == maxZoomLevel) {
                zoomIn.setImageResource(R.mipmap.zoomin_v_disabled);
                zoomOut.setImageResource(R.drawable.zoomout_v);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.equals(zoomIn)){
                if(mOnChangeCamera != null){
                    mOnChangeCamera.changeCamera(CameraUpdateFactory.zoomIn(), Const.ZOOMIN);
                }
            } else if(view.equals(zoomOut)){
                if(mOnChangeCamera != null){
                    mOnChangeCamera.changeCamera(CameraUpdateFactory.zoomOut(), Const.ZOOMOUT);
                }
            }
        }
    };


    public OnChangeCamera getmOnChangeCamera() {
        return mOnChangeCamera;
    }

    public void setmOnChangeCamera(OnChangeCamera mOnChangeCamera) {
        this.mOnChangeCamera = mOnChangeCamera;
    }

    public interface OnChangeCamera{
        public void changeCamera(CameraUpdate update, int zoommode);
    }
}
