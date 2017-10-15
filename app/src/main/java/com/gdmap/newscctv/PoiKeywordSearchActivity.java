package com.gdmap.newscctv;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapException;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.gdmap.newscctv.overlay.PoiOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PoiKeywordSearchActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener,
        Inputtips.InputtipsListener, PoiSearch.OnPoiSearchListener,
        AMap.OnMarkerClickListener{


    @BindView(R.id.keyWord)
    AutoCompleteTextView keyWord;
    @BindView(R.id.city)
    EditText city;
    @BindView(R.id.searchButton)
    Button searchButton;
    @BindView(R.id.nextButton)
    Button nextButton;
//    @BindView(R.id.map)
    SupportMapFragment map;

    private AMap aMap;
    private int currentPage = 0;// 当前页面，从0开始计数
    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;// POI搜索
    private PoiResult poiResult; // poi返回的结果
    private String keyWords;

    private ProgressDialog progDialog = null;// 搜索时进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_keyword_search);
        ButterKnife.bind(this);
        map = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        initView();
        setUpMap();
    }

    private void initView(){
        keyWord.addTextChangedListener(this);
        searchButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
    }

    private void setUpMap(){
        if(aMap == null){
            aMap = map.getMap();
        }
        aMap.setOnMarkerClickListener(this);
    }

    /**
     * 显示进度框
     */
    private void showProgressDialog() {
        if (progDialog == null)
            progDialog = new ProgressDialog(this);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setIndeterminate(false);
        progDialog.setCancelable(false);
        progDialog.setMessage("正在搜索:\n" + keyWord);
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.searchButton:
                searchButton();
                break;
            case R.id.nextButton:
                nextPage();
                break;
        }
    }

    /**
     * 点击搜索按钮
     */
    public void searchButton() {
        keyWords = Utils.checkEditText(keyWord);
        if ("".equals(keyWords)) {
            ToastUtil.show(PoiKeywordSearchActivity.this, "请输入搜索关键字");
            return;
        } else {
            doSearchQuery();
        }
    }

    public void doSearchQuery(){
        showProgressDialog();
        currentPage = 0;
        query = new PoiSearch.Query(keyWords, "" , city.getText().toString());
        query.setPageNum(10);
        query.setPageSize(currentPage);

        poiSearch = new PoiSearch(getApplicationContext(), query);
        poiSearch.setOnPoiSearchListener(this);
        poiSearch.searchPOIAsyn();
    }

    public void nextPage(){
        if (query != null && poiSearch != null && poiResult != null) {
            if(poiResult.getPageCount() - 1 > currentPage){
                currentPage ++;
                query.setPageSize(currentPage);
                poiSearch.searchPOIAsyn();
            }else{
                ToastUtil.show(PoiKeywordSearchActivity.this,
                        R.string.no_result);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String newText = charSequence.toString().trim();
        if(!Utils.IsEmptyOrNullString(newText)){
            InputtipsQuery inputtipsQuery = new InputtipsQuery(newText, city.getText().toString());
            Inputtips inputtips = new Inputtips(PoiKeywordSearchActivity.this, inputtipsQuery);
            inputtips.setInputtipsListener(this);
            inputtips.requestInputtipsAsyn();
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        if(i == 1000){
              List<String> listTips = new ArrayList<>();
              for(Tip tip:list){
                  listTips.add(tip.getName());
              }
              ArrayAdapter<String> adapter = new ArrayAdapter<String>(PoiKeywordSearchActivity.this, R.layout.route_inputs,listTips);
              keyWord.setAdapter(adapter);
              adapter.notifyDataSetChanged();
        }else{
           ToastUtil.showerror(PoiKeywordSearchActivity.this, i);
        }

    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
          dissmissProgressDialog();
          if(i == 1000){
              if(poiResult != null && poiResult.getQuery() != null){
                  if(poiResult.getQuery().equals(query)){
                      List<PoiItem> poiItems = poiResult.getPois();
                      List<SuggestionCity> suggestionCities = poiResult.getSearchSuggestionCitys();
                      if(poiItems != null && poiItems.size() > 0){
                          aMap.clear();
                          PoiOverlay overlay = new PoiOverlay(aMap, getApplicationContext(), poiItems);
                          overlay.removeFromMap();
                          overlay.addToMap();
                          overlay.zoomToSpan();
                      }else if(suggestionCities != null && suggestionCities.size() > 0){
                          showSuggestCity(suggestionCities);
                      }else{
                          ToastUtil.show(PoiKeywordSearchActivity.this,
                                  R.string.no_result);
                      }
                  }
              }
          }else{
              ToastUtil.showerror(this, i);
          }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    /**
     * poi没有搜索到数据，返回一些推荐城市的信息
     */
    private void showSuggestCity(List<SuggestionCity> cities) {
        String infomation = "推荐城市\n";
        for (int i = 0; i < cities.size(); i++) {
            infomation += "城市名称:" + cities.get(i).getCityName() + "城市区号:"
                    + cities.get(i).getCityCode() + "城市编码:"
                    + cities.get(i).getAdCode() + "\n";
        }
        ToastUtil.show(PoiKeywordSearchActivity.this, infomation);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return false;
    }
}
