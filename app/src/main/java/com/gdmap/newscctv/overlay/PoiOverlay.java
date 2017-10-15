package com.gdmap.newscctv.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.NaviPara;
import com.amap.api.services.core.PoiItem;
import com.gdmap.newscctv.R;
import com.gdmap.newscctv.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Poi图层类。在高德地图API里，如果要显示Poi，可以用此类来创建Poi图层。如不满足需求，也可以自己创建自定义的Poi图层。
 * @since V2.1.0
 */
public class PoiOverlay implements AMap.InfoWindowAdapter{
	private List<PoiItem> mPois;
	private AMap mAMap;
	private ArrayList<Marker> mPoiMarks = new ArrayList<Marker>();
	private Map<Integer, Bitmap> mBackDrawAbles = new HashMap<Integer, Bitmap>();
    private Context mContext;
    private int PoiType = 0;

	/**
	 * 通过此构造函数创建Poi图层。
	 * @param amap 地图对象。
	 * @param pois 要在地图上添加的poi。列表中的poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类<strong> <a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
	 * @since V2.1.0
	 */
	public PoiOverlay(AMap amap, Context context, List<PoiItem> pois) {
		mAMap = amap;
		this.mContext = context;
		mPois = pois;
		mAMap.setInfoWindowAdapter(this);
	}
	/**
	 * 添加Marker到地图中。
	 * @since V2.1.0
	 */
	public void addToMap() {
		try{
			for (int i = 0; i < mPois.size(); i++) {
				Marker marker = mAMap.addMarker(getMarkerOptions(i));
				PoiItem poiItem = mPois.get(i);
				marker.setObject(poiItem);
				mPoiMarks.add(marker);
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
	/**
	 * 去掉PoiOverlay上所有的Marker。
	 * @since V2.1.0
	 */
	public void removeFromMap() {
		for (Marker mark : mPoiMarks) {
			mark.remove();
		}
	}
	/**
	 * 移动镜头到当前的视角。
	 * @since V2.1.0
	 */
	public void zoomToSpan() {
		try{
			if (mPois != null && mPois.size() > 0) {
				if (mAMap == null)
					return;
				if(mPois.size()==1){
					mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mPois.get(0).getLatLonPoint().getLatitude(),
							mPois.get(0).getLatLonPoint().getLongitude()), 18f));
				}else{
					LatLngBounds bounds = getLatLngBounds();
					mAMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
				}
			}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	private LatLngBounds getLatLngBounds() {
		LatLngBounds.Builder b = LatLngBounds.builder();
		for (int i = 0; i < mPois.size(); i++) {
			b.include(new LatLng(mPois.get(i).getLatLonPoint().getLatitude(),
					mPois.get(i).getLatLonPoint().getLongitude()));
		}
		return b.build();
	}

	private MarkerOptions getMarkerOptions(int index) {
		return new MarkerOptions()
				.position(
						new LatLng(mPois.get(index).getLatLonPoint()
								.getLatitude(), mPois.get(index)
								.getLatLonPoint().getLongitude()))
				.title(getTitle(index)).snippet(getSnippet(index))
				.icon(getBitmapDescriptor(index));
	}

	/**
	 * 给第几个Marker设置图标，并返回更换图标的图片。如不用默认图片，需要重写此方法。
	 * @param index 第几个Marker。
	 * @return 更换的Marker图片。
	 * @since V2.1.0
	 */
	protected BitmapDescriptor getBitmapDescriptor(int index) {
		if(getPoiType() == 0){
			return null;
		}else if(getPoiType() == 1){
			if (index< 10) {
				BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
						BitmapFactory.decodeResource(mContext.getResources(), Utils.markers[index]));
				return icon;
			}else {
				BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(
						BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.marker_highlight));
				return icon;
			}
		}
		return null;
	}

	public Bitmap getDrawAble(int clusterNum) {
		int radius = Utils.dp2px(mContext, 80);
		if (clusterNum == 1) {
			Bitmap bitmapDrawable = mBackDrawAbles.get(1);
			if (bitmapDrawable == null) {
				bitmapDrawable = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.icon_openmap_mark);
				mBackDrawAbles.put(1, bitmapDrawable);
			}

			return bitmapDrawable;
		} else if (clusterNum < 5) {

			Bitmap bitmapDrawable = mBackDrawAbles.get(2);
			if (bitmapDrawable == null) {
				bitmapDrawable = Utils.drawCircle(radius,
						Color.argb(159, 210, 154, 6));
				mBackDrawAbles.put(2, bitmapDrawable);
			}

			return bitmapDrawable;
		} else if (clusterNum < 10) {
			Bitmap bitmapDrawable = mBackDrawAbles.get(3);
			if (bitmapDrawable == null) {
				bitmapDrawable = Utils.drawCircle(radius,
						Color.argb(199, 217, 114, 0));
				mBackDrawAbles.put(3, bitmapDrawable);
			}

			return bitmapDrawable;
		} else {
			Bitmap bitmapDrawable = mBackDrawAbles.get(4);
			if (bitmapDrawable == null) {
				bitmapDrawable = Utils.drawCircle(radius,
						Color.argb(235, 215, 66, 2));

				mBackDrawAbles.put(4, bitmapDrawable);
			}
			return bitmapDrawable;
		}
	}

	/**
	 * 返回第index的Marker的标题。
	 * @param index 第几个Marker。
	 * @return marker的标题。
	 * @since V2.1.0
	 */
	protected String getTitle(int index) {
		return mPois.get(index).getTitle();
	}
	/**
	 * 返回第index的Marker的详情。
	 * @param index 第几个Marker。
	 * @return marker的详情。
	 * @since V2.1.0
	 */
	protected String getSnippet(int index) {
		return mPois.get(index).getSnippet();
	}
	/**
	 * 从marker中得到poi在list的位置。
	 * @param marker 一个标记的对象。
	 * @return 返回该marker对应的poi在list的位置。
	 * @since V2.1.0
	 */
	public int getPoiIndex(Marker marker) {
		for (int i = 0; i < mPoiMarks.size(); i++) {
			if (mPoiMarks.get(i).equals(marker)) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * 返回第index的poi的信息。
	 * @param index 第几个poi。
	 * @return poi的信息。poi对象详见搜索服务模块的基础核心包（com.amap.api.services.core）中的类 <strong><a href="../../../../../../Search/com/amap/api/services/core/PoiItem.html" title="com.amap.api.services.core中的类">PoiItem</a></strong>。
	 * @since V2.1.0
	 */
	public PoiItem getPoiItem(int index) {
		if (index < 0 || index >= mPois.size()) {
			return null;
		}
		return mPois.get(index);
	}

	@Override
	public View getInfoWindow(Marker marker) {
		return null;
	}

	@Override
	public View getInfoContents(Marker marker) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.poikeywordsearch_uri, null);
		TextView title = view.findViewById(R.id.title);
		title.setText(marker.getTitle());

		TextView snippet = view.findViewById(R.id.snippet);
		final PoiItem poiItem = (PoiItem) marker.getObject();
		snippet.setText(poiItem.getSnippet());
		ImageButton button = view
				.findViewById(R.id.start_amap_app);
		//调起高德地图app
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startAMapNavi(poiItem);
			}
		});
		return view;
	}

	/**
	 * 调起高德地图导航功能，如果没安装高德地图，会进入异常，可以在异常中处理，调起高德地图app的下载页面
	 */
	public void startAMapNavi(PoiItem poiItem) {
		// 构造导航参数
		NaviPara naviPara = new NaviPara();
		// 设置终点位置
		naviPara.setTargetPoint(new LatLng(poiItem.getLatLonPoint().getLatitude(), poiItem.getLatLonPoint().getLongitude()));
		// 设置导航策略，这里是避免拥堵
		naviPara.setNaviStyle(NaviPara.DRIVING_AVOID_CONGESTION);

		// 调起高德地图导航
		try {
			AMapUtils.openAMapNavi(naviPara, mContext);
		} catch (com.amap.api.maps.AMapException e) {
			// 如果没安装会进入异常，调起下载页面
			AMapUtils.getLatestAMapApp(mContext);
		}
	}

	public int getPoiType() {
		return PoiType;
	}

	public void setPoiType(int poiType) {
		PoiType = poiType;
	}
}
