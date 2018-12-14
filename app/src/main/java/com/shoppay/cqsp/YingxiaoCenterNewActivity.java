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
import com.shoppay.cqsp.bean.YinxiaoCenter;
import com.shoppay.cqsp.http.InterfaceBack;
import com.shoppay.cqsp.tools.BluetoothUtil;
import com.shoppay.cqsp.tools.DateHmChoseDialog;
import com.shoppay.cqsp.tools.DateUtils;
import com.shoppay.cqsp.tools.DayinUtils;
import com.shoppay.cqsp.tools.DialogUtil;
import com.shoppay.cqsp.tools.LogUtils;
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
        obtainBoss();
    }


    private void obtainBoss() {
        dialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(this);
        client.setCookieStore(myCookieStore);
        RequestParams map = new RequestParams();

        map.put("StartDate", mBossTvStarttime.getText().toString() + ":00");
        map.put("EndDate", mBossTvEndtime.getText().toString() + ":00");
        String url = UrlTools.obtainUrl(ac, "?Source=3", "RptOverallDataGet");
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
                        Type listType = new TypeToken<List<YinxiaoCenter>>() {
                        }.getType();
                        List<YinxiaoCenter> yinxiaoCenters = gson.fromJson(jso.getString("vdata"), listType);
                        handlerYinxiaoMsg(yinxiaoCenters.get(0));

                        JSONObject jsonObject = (JSONObject) jso.getJSONArray("print").get(0);
                        if (jsonObject.getInt("printNumber") == 0) {
                        } else {
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            if (bluetoothAdapter.isEnabled()) {
                                BluetoothUtil.connectBlueTooth(MyApplication.context);
                                BluetoothUtil.sendData(DayinUtils.dayin(jsonObject.getString("printContent")), jsonObject.getInt("printNumber"));
                            } else {
                            }
                        }
                    } else {

                        Toast.makeText(ac, jso.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(ac, "会员充值失败，请重新登录", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                Toast.makeText(ac, "会员充值失败，请重新登录", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void handlerYinxiaoMsg(YinxiaoCenter yx) {

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
                        mBossTvStarttime.setText(response.toString());
                        obtainBoss();
                    }

                    @Override
                    public void onErrorResponse(Object msg) {

                    }
                });
                DialogUtil.dateChoseDialog(YingxiaoCenterNewActivity.this, 1, new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {
                        String data = DateUtils.timeTodata((String) response);
                        String cru = DateUtils.timeTodata(DateUtils.getCurrentTime_Today());
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
                        mBossTvStarttime.setText((String) msg);
                        obtainBoss();
                    }
                });
                break;
            case R.id.boss_tv_endtime:
                DateHmChoseDialog.datehmChoseDialog(ac, 2, new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {
                        mBossTvEndtime.setText(response.toString());
                        obtainBoss();
                    }

                    @Override
                    public void onErrorResponse(Object msg) {

                    }
                });

                DialogUtil.dateChoseDialog(YingxiaoCenterNewActivity.this, 1, new InterfaceBack() {
                    @Override
                    public void onResponse(Object response) {

                        String data = DateUtils.timeTodata((String) response);
                        String cru = DateUtils.timeTodata(mBossTvStarttime.getText().toString());
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
                        mBossTvEndtime.setText((String) msg);
                        obtainBoss();
                    }
                });
                break;
        }
    }

}
