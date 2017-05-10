package com.jinkun.map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.fengmap.android.map.FMGroupInfo;
import com.fengmap.android.map.FMMap;
import com.fengmap.android.map.FMMapCoordZType;
import com.fengmap.android.map.FMMapInfo;
import com.fengmap.android.map.FMMapUpgradeInfo;
import com.fengmap.android.map.FMMapView;
import com.fengmap.android.map.FMPickMapCoordResult;
import com.fengmap.android.map.animator.FMLinearInterpolator;
import com.fengmap.android.map.event.OnFMMapClickListener;
import com.fengmap.android.map.event.OnFMMapInitListener;
import com.fengmap.android.map.event.OnFMSwitchGroupListener;
import com.fengmap.android.map.geometry.FMMapCoord;
import com.fengmap.android.map.geometry.FMScreenCoord;
import com.fengmap.android.map.layer.FMImageLayer;
import com.fengmap.android.map.marker.FMImageMarker;

import java.util.ArrayList;

import static android.R.attr.x;
import static android.R.attr.y;

public class MainActivity extends AppCompatActivity implements OnFMMapInitListener,OnFMMapClickListener,CompoundButton.OnCheckedChangeListener {
    private FMMapView mMapView;
    private FMMap mFMMap;
    private CheckBox mGroupControl;
    private RadioButton[] mRadioButtons;
    private Handler mHandler = new Handler();
    private FMImageLayer mImageLayer;
    private FMImageMarker mImageMarker;
    public static final FMMapCoord CENTER_COORD = new FMMapCoord(427, 876);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = (FMMapView) findViewById(R.id.map_view);
        mFMMap = mMapView.getFMMap();
        mFMMap.setOnFMMapInitListener(this);
        mFMMap.setOnFMMapClickListener(this);



        String bid = "tc10001";             //地图id
        mFMMap.openMapById(bid, true);          //打开地图

    }

    /**
     * 地图加载成功回调事件
     *
     * @param path 地图所在sdcard路径
     */
    @Override
    public void onMapInitSuccess(String path) {
        Log.e("MainActivity", "onMapInitSuccess: " + "成功");
        //加载离线主题
        mFMMap.loadThemeByPath(FileUtils.getDefaultThemePath(this));
        FMMapInfo mapInfo = mFMMap.getFMMapInfo();
        ArrayList<FMGroupInfo> groups = mapInfo.getGroups();
        displayGroupView(groups);

        addImageMarker();

    }

    /**
     * 添加图片标注
     */
    private void addImageMarker() {
//        if (mImageMarker != null) {
//            return;
//        }
        int groupId = mFMMap.getFocusGroupId();
        //获取图片图层
        mImageLayer = mFMMap.getFMLayerProxy().createFMImageLayer(groupId);
        mFMMap.addLayer(mImageLayer);
        FMMapCoord centerCoord = new FMMapCoord(1.2946768E7, 4863037.0);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_marker_blue);
        mImageMarker = new FMImageMarker(centerCoord, bitmap);
//设置图片宽高
        mImageMarker.setMarkerWidth(30);
        mImageMarker.setMarkerHeight(30);
//设置图片垂直偏离距离
        mImageMarker.setFMImageMarkerOffsetMode(FMImageMarker.FMImageMarkerOffsetMode.FMNODE_CUSTOM_HEIGHT);
        mImageMarker.setCustomOffsetHeight(5);

        mImageLayer.addMarker(mImageMarker);            //添加图片标志物
    }

    @Override
    public void onMapInitFailure(String s, int i) {

    }

    @Override
    public boolean onUpgrade(FMMapUpgradeInfo fmMapUpgradeInfo) {
        return false;
    }

    /**
     * 展示地图楼层
     *
     * @param groups 地图楼层信息
     */
    private void displayGroupView(ArrayList<FMGroupInfo> groups) {
        RadioGroup radioGroup = ViewHelper.getView(MainActivity.this, R.id.rg_groups);
        int count = groups.size();
        mRadioButtons = new RadioButton[count];

        for (int i = 0; i < count; i++) {
            int position = radioGroup.getChildCount() - i - 1;
            mRadioButtons[i] = (RadioButton) radioGroup.getChildAt(position);

            FMGroupInfo groupInfo = groups.get(i);
            mRadioButtons[i].setTag(groupInfo.getGroupId());
            mRadioButtons[i].setText(groupInfo.getGroupName().toUpperCase());
            mRadioButtons[i].setOnCheckedChangeListener(this);
        }

        //单、多层控制
        mRadioButtons[count - 1].setChecked(true);

        mGroupControl = ViewHelper.getView(MainActivity.this, R.id.cb_groups);
        mGroupControl.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.e("MainActivity", "onCheckedChanged: " + "单层显示");
                    mGroupControl.setText("单层显示");
                    multiDisplayFloor();
                } else {
                    Log.e("MainActivity", "onCheckedChanged: " + "多层显示");
                    mGroupControl.setText("多层显示");
                    singleDisplayFloor();
                }
            }
        });
    }

    /**
     * 单层显示
     */
    private void singleDisplayFloor() {
        int groupId = mFMMap.getFocusGroupId();
        singleDisplayFloor(groupId);
    }

    /**
     * 单层显示
     *
     * @param groupId 楼层id
     */
    private void singleDisplayFloor(int groupId) {
        int[] showFloors = new int[1]; // 需要显示的楼层
        showFloors[0] = groupId;
        // 设置单层显示,及焦点层
        mFMMap.setMultiDisplay(showFloors, 0, null);
    }

    /**
     * 多层显示
     *
     * @param groupId 焦点层id
     */
    private void multiDisplayFloor(int groupId) {
        int[] showFloors = mFMMap.getMapGroupIds();
        // 设置多层显示,及焦点层
        int focus = convertToFocus(groupId);
        mFMMap.setMultiDisplay(showFloors, focus, null);
    }

    /**
     * 多层显示
     */
    private void multiDisplayFloor() {
        int focusGroupId = mFMMap.getFocusGroupId();
        multiDisplayFloor(focusGroupId);
    }

    /**
     * 焦点层id转换成焦点层索引
     *
     * @param focusGroupId 焦点层id
     * @return
     */
    private int convertToFocus(int focusGroupId) {
        FMMapInfo mapInfo = mFMMap.getFMMapInfo();
        int size = mapInfo.getGroups().size();
        int focus = 0;
        for (int i = 0; i < size; i++) {
            int groupId = mapInfo.getGroups().get(i).getGroupId();
            if (focusGroupId == groupId) {
                focus = i;
                break;
            }
        }
        return focus;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            int groupId = (int) buttonView.getTag();
            mFMMap.setFocusByGroupIdAnimated(groupId, new FMLinearInterpolator(), new OnFMSwitchGroupListener() {
                @Override
                public void beforeGroupChanged() {
                    setRadioButtonEnable(false);
                    Log.e("MainActivity", "beforeGroupChanged: " + "1");
                }

                @Override
                public void afterGroupChanged() {
                    setRadioButtonEnable(true);
                    Log.e("MainActivity", "beforeGroupChanged: " + "2");
                }
            });
        }
    }

    /**
     * 设置楼层是否可用
     *
     * @param enable true 可以被点击
     *               false 不可被点击
     */
    private void setRadioButtonEnable(final boolean enable) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mRadioButtons.length; i++) {
                    mRadioButtons[i].setEnabled(enable);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mFMMap != null) {
            mFMMap.onDestroy();
        }
        super.onBackPressed();
    }

    @Override
    public void onMapClick(float v, float v1) {
        Log.e("MainActivity", "onMapClick: " + v+"      "+v1);
        mImageLayer.removeAll();
        //添加图片标注
        FMPickMapCoordResult mapCoordResult = mFMMap.pickMapCoord(v,v1 );
        if (mapCoordResult != null) {
            FMMapCoord mapCoord = mapCoordResult.getMapCoord();
            Log.e("TAG", "onMapClick: "+mapCoord.x+"    " +mapCoord.y);

            FMImageMarker imageMarker = ViewHelper.buildImageMarker(getResources(), mapCoord);
            mImageLayer.addMarker(imageMarker);
        }

        int groupId = mFMMap.getFocusGroupId();
        //屏幕坐标转换为地图坐标
        FMScreenCoord screenCoord = new FMScreenCoord(x, y);
        Log.e("TAG", "onMapClick: "+x+"   "+y );
        FMMapCoord convertMapCoord = mFMMap.toFMMapCoord(groupId, screenCoord);

        //地图坐标转换为屏幕坐标
        FMScreenCoord convertScreenCoord = mFMMap.toFMScreenCoord(groupId,FMMapCoordZType.MAPCOORDZ_MODEL, convertMapCoord);
    }
}
