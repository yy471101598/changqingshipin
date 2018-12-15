package com.shoppay.cqsp;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.cqsp.bean.YinxiaoCenterNew;
import com.shoppay.cqsp.http.InterfaceBack;
import com.shoppay.cqsp.tools.BluetoothUtil;
import com.shoppay.cqsp.tools.DateHmChoseDialog;
import com.shoppay.cqsp.tools.DateUtils;
import com.shoppay.cqsp.tools.DayinUtils;
import com.shoppay.cqsp.tools.DialogUtil;
import com.shoppay.cqsp.tools.LogUtils;
import com.shoppay.cqsp.tools.NoDoubleClickListener;
import com.shoppay.cqsp.tools.StringUtil;
import com.shoppay.cqsp.tools.UrlTools;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

/**
 * Created by songxiaotao on 2018/1/23.
 */

public class YingxiaoCenterNewActivity extends Activity {
    @Bind(R.id.img_left)
    ImageView mImgLeft;
    @Bind(R.id.rl_left)
    RelativeLayout mRlLeft;
    @Bind(R.id.tv_title)
    TextView mTvTitle;
    @Bind(R.id.rl_right)
    RelativeLayout mRlRight;
    @Bind(R.id.boss_tv_starttime)
    TextView mBossTvStarttime;
    @Bind(R.id.boss_tv_endtime)
    TextView mBossTvEndtime;
    @Bind(R.id.tv_yuexf)
    TextView mTvYuexf;
    @Bind(R.id.tv_account)
    TextView mTvAccount;
    @Bind(R.id.tv_shop)
    TextView mTvShop;
    @Bind(R.id.rl_dayin)
    RelativeLayout mRlDayin;
    private Activity ac;
    private Dialog dialog;
    private boolean isDayin = false;
    private String dayinmsg = "";
    private int dayinnum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yingxiaocenternew);
        ButterKnife.bind(this);
        ac = this;
        dialog = DialogUtil.loadingDialog(ac, 1);
        mTvTitle.setText("营销中心");
        mBossTvStarttime.setText(DateUtils.getCurrentTime("yyyy-MM-dd") + " 00:00");
        mBossTvEndtime.setText(DateUtils.getCurrentTime("yyyy-MM-dd HH:mm"));
        isDayin = false;
        obtainBoss();
        mRlDayin.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                if(isDayin) {
                    if(dayinnum==0){
                        Toast.makeText(ac,"打印份数为0",Toast.LENGTH_SHORT).show();
                    }else {
                        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (bluetoothAdapter.isEnabled()) {
                            BluetoothUtil.connectBlueTooth(MyApplication.context);
                            BluetoothUtil.sendData(DayinUtils.dayin(dayinmsg), dayinnum);
                        } else {
                            Toast.makeText(ac,"请打开蓝牙",Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    Toast.makeText(ac, "营销中心数据获取失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void obtainBoss() {
        dialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        RequestParams map = new RequestParams();

        map.put("StartDate", mBossTvStarttime.getText().toString() + ":00");
        map.put("EndDate", mBossTvEndtime.getText().toString() + ":00");
        String url = UrlTools.obtainUrl(ac, "?Source=3", "PrintRptOverallData");
        LogUtils.d("xxurl", url);
        client.post(url, map, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    dialog.dismiss();
                    LogUtils.d("xxyingxiaoS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
//                        Toast.makeText(ac, jso.getString("msg"), Toast.LENGTH_LONG).show();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<YinxiaoCenterNew>>() {
                        }.getType();
                        List<YinxiaoCenterNew> yinxiaoCenters = gson.fromJson(jso.getString("vdata"), listType);
                        handlerYinxiaoMsg(yinxiaoCenters.get(0));
                        isDayin = true;
                        JSONObject jsonObject = (JSONObject) jso.getJSONArray("print").get(0);
                        if (jsonObject.getInt("printNumber") == 0) {
                            dayinnum = 0;
                        } else {
                            dayinnum = jsonObject.getInt("printNumber");
                            dayinmsg = jsonObject.getString("printContent");
                        }
                    } else {
                        isDayin = false;
                        mTvAccount.setText("");
                        mTvShop.setText("");
                        mTvYuexf.setText("");
                        Toast.makeText(ac, jso.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    dialog.dismiss();
                    isDayin = false;
                    mTvAccount.setText("");
                    mTvShop.setText("");
                    mTvYuexf.setText("");
                    Toast.makeText(ac, "营销中心数据获取失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                isDayin = false;
                mTvAccount.setText("");
                mTvShop.setText("");
                mTvYuexf.setText("");
                Toast.makeText(ac, "营销中心数据获取失败", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void handlerYinxiaoMsg(YinxiaoCenterNew yx) {
        mTvAccount.setText(yx.UserAccount);
        mTvShop.setText(yx.ShopName);
        mTvYuexf.setText(StringUtil.twoNum(yx.OrderBalance));
    }


    @OnClick({R.id.rl_left, R.id.boss_tv_starttime, R.id.boss_tv_endtime})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_left:
                finish();
                break;
            case R.id.boss_tv_starttime:
                DateHmChoseDialog.datehmChoseDialog(ac, 2, new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {
                        String data = DateUtils.timeTodata((String) response + ":00", "yyyy-MM-dd HH:mm:ss");
                        String cru = DateUtils.timeTodata(DateUtils.getCurrentTime_Today("yyyy-MM-dd HH:mm:ss"),"yyyy-MM-dd HH:mm:ss");
                        Log.d("xxTime", data + ";" + cru + ";" + DateUtils.getCurrentTime_Today() + ";" + (String) response);
                        if (Double.parseDouble(data) <= Double.parseDouble(cru)) {
                            mBossTvStarttime.setText((String) response);
                            obtainBoss();
                        } else {
                            Toast.makeText(ac, "开始时间应小于当前时间", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(Object msg) {

                    }
                });
                break;
            case R.id.boss_tv_endtime:
                DateHmChoseDialog.datehmChoseDialog(ac, 2, new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {
                        String data = DateUtils.timeTodata((String) response + ":00", "yyyy-MM-dd HH:mm:ss");
                        String cru = DateUtils.timeTodata(mBossTvStarttime.getText().toString() + ":00", "yyyy-MM-dd HH:mm:ss");
                        Log.d("xxTime", data + ";" + cru + ";" + DateUtils.getCurrentTime_Today() + ";" + (String) response);
                        if (Double.parseDouble(data) >= Double.parseDouble(cru)) {
                            mBossTvEndtime.setText((String) response);
                            obtainBoss();
                        } else {
                            Toast.makeText(ac, "结束时间要大于起始时间", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onErrorResponse(Object msg) {

                    }
                });
                break;
        }
    }

}
