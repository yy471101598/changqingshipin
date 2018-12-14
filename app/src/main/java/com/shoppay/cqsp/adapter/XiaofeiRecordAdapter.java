package com.shoppay.cqsp.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.shoppay.cqsp.MyApplication;
import com.shoppay.cqsp.R;
import com.shoppay.cqsp.bean.XiaofeiRecord;
import com.shoppay.cqsp.tools.BluetoothUtil;
import com.shoppay.cqsp.tools.DayinUtils;
import com.shoppay.cqsp.tools.LogUtils;
import com.shoppay.cqsp.tools.NoDoubleClickListener;
import com.shoppay.cqsp.tools.NullUtils;
import com.shoppay.cqsp.tools.UrlTools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class XiaofeiRecordAdapter extends BaseAdapter {
    private Activity context;
    private List<XiaofeiRecord> list;
    private LayoutInflater inflater;
    private Dialog dialog;

    public XiaofeiRecordAdapter(Activity context, List<XiaofeiRecord> list, Dialog dialog) {
        this.context = context;
        if (list == null) {
            this.list = new ArrayList<XiaofeiRecord>();
        } else {
            this.list = list;
        }
        this.dialog = dialog;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder vh = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_xiaofeijilu, null);
            vh = new ViewHolder(convertView);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final XiaofeiRecord home = list.get(position);
        vh.mTvCode.setText(home.getOrderAccount());
        vh.mTvVipcard.setText(home.getMemCard());
        vh.mTvVipname.setText(NullUtils.noNullHandle(home.MemCardNumber).toString());
        vh.mTvZhmoney.setText(home.getOrderDiscountMoney());
//        OrderStatus=订单状态 1:完成 2：撤销 3：挂单
//        String orderstate="";
//        switch (home.getor)
//        vh.mTvOrderstate.setText(orderstate);
//        OrderType=订单类型 （0:会员登记1:会员充值2:充值撤销3:积分变动4:积分抵现5:积分提现
// 6:积分提成7:商品消费8:商品退货9:快速消费10:快消撤单11:会员充次12:充次撤销13:会员充时
// 14:充时撤销15:积分兑换16:会员签到17:积分转盘）

        vh.rl_dayin.setOnClickListener(new NoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View view) {
                obtainXiaofeiRecord(home);
            }
        });

        vh.mTvOrderstyle.setText(home.getOrderTypeTxt());
        vh.mTvDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return convertView;
    }


    class ViewHolder {
        @Bind(R.id.tv_code)
        TextView mTvCode;
        @Bind(R.id.tv_vipcard)
        TextView mTvVipcard;
        @Bind(R.id.tv_vipname)
        TextView mTvVipname;
        @Bind(R.id.tv_orderstate)
        TextView mTvOrderstate;
        @Bind(R.id.tv_orderstyle)
        TextView mTvOrderstyle;
        @Bind(R.id.tv_zzzz)
        TextView mTvZzzz;
        @Bind(R.id.vvvvvv)
        View mVvvvvv;
        @Bind(R.id.tv_zhmoney)
        TextView mTvZhmoney;
        @Bind(R.id.tv_detail)
        TextView mTvDetail;
        @Bind(R.id.rl_dayin)
        RelativeLayout rl_dayin;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


    private void obtainXiaofeiRecord(XiaofeiRecord xiaofeiRecord) {
        dialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
        client.setCookieStore(myCookieStore);
        RequestParams params = new RequestParams();
        params.put("OrderID", xiaofeiRecord.getOrderID());
        LogUtils.d("xxparams", params.toString());
        String url = UrlTools.obtainUrl(context, "?Source=3", "PrintOrderLogList");
        LogUtils.d("xxurl", url);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dialog.dismiss();
                try {
                    LogUtils.d("xxxiaofeiS", new String(responseBody, "UTF-8"));
                    JSONObject jso = new JSONObject(new String(responseBody, "UTF-8"));
                    if (jso.getInt("flag") == 1) {
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

                        Toast.makeText(context, jso.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    dialog.dismiss();
                    Toast.makeText(context, "获取小票信息失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dialog.dismiss();
                Toast.makeText(context, "获取小票信息失败", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
