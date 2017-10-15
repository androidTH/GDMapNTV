package com.gdmap.newscctv;

import android.graphics.Color;

import com.amap.api.maps.model.LatLng;

/**
 * @since 3.3.0
 * Created by hongming.wang on 2016/12/19.
 */

public class Const {
	/**
	 * 地图中绘制多边形、圆形的边界颜色
	 * @since 3.3.0
	 */
	public static final int STROKE_COLOR = Color.argb(180, 63, 145, 252);
	/**
	 * 地图中绘制多边形、圆形的填充颜色
	 * @since 3.3.0
	 */
	public static final int FILL_COLOR = Color.argb(163, 118, 212, 243);

	/**
	 * 地图中绘制多边形、圆形的边框宽度
	 * @since 3.3.0
	 */
	public static final float STROKE_WIDTH = 5F;

	public static final int ZOOMIN = 0;
	public static final int ZOOMOUT = 1;

	public static final LatLng BEIJING = new LatLng(39.90403, 116.407525);// 北京市经纬度
	public static final LatLng ZHONGGUANCUN = new LatLng(39.983456, 116.3154950);// 北京市中关村经纬度
	public static final LatLng SHANGHAI = new LatLng(31.238068, 121.501654);// 上海市经纬度
	public static final LatLng FANGHENG = new LatLng(39.989614, 116.481763);// 方恒国际中心经纬度
	public static final LatLng CHENGDU = new LatLng(30.679879, 104.064855);// 成都市经纬度
	public static final LatLng XIAN = new LatLng(34.341568, 108.940174);// 西安市经纬度
	public static final LatLng ZHENGZHOU = new LatLng(34.7466, 113.625367);// 郑州市经纬度
}
