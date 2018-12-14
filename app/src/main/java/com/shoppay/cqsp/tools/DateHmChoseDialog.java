package com.shoppay.cqsp.tools;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.shoppay.cqsp.R;
import com.shoppay.cqsp.http.InterfaceBack;
import com.shoppay.cqsp.view.wheelview.OnWheelChangedListener;
import com.shoppay.cqsp.view.wheelview.OnWheelScrollListener;
import com.shoppay.cqsp.view.wheelview.WheelView;
import com.shoppay.cqsp.view.wheelview.adapter.NumericWheelAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Administrator on 2018/5/12 0012.
 */

public class DateHmChoseDialog {
    public static int curYear;
    public static int curMonth;
    public static int curDate;
    public static int curHour;
    public static int curMin;
    public static WheelView wl_start_year;
    public static WheelView wl_start_month;
    public static WheelView wl_start_day;
    public static WheelView wl_start_h;
    public static WheelView wl_start_m;
    public static String time;

    /**
     * 日期选择器
     *
     * @param context
     * @param showingLocation 0：顶部 1：中间 2：底部 3：距离底部100dp 对话框的位置
     */
    public static void datehmChoseDialog(final Context context,
                                       int showingLocation, final InterfaceBack handler) {
        final Dialog dialog;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_datechosehm, null);
        wl_start_year= (WheelView) view.findViewById(R.id.wl_year);
        wl_start_month= (WheelView) view.findViewById(R.id.wl_month);
        wl_start_day= (WheelView) view.findViewById(R.id.wl_day);
        wl_start_h= (WheelView) view.findViewById(R.id.wl_hour);
        wl_start_m= (WheelView) view.findViewById(R.id.wl_moth);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        final String date = sDateFormat.format(new java.util.Date());
        time=date;
        RelativeLayout rl_confirm= (RelativeLayout) view.findViewById(R.id.rl_confirm);
        RelativeLayout rl_cancel= (RelativeLayout) view.findViewById(R.id.rl_cancel);
        initWheelView(context);
        dialog = new Dialog(context, R.style.DialogNotitle1);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        int screenWidth = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                .getWidth();
        dialog.setContentView(view, new LinearLayout.LayoutParams(
                screenWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        dialog.show();

        rl_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        rl_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                handler.onResponse(time);
            }
        });
        Window window = dialog.getWindow();
        switch (showingLocation) {
            case 0:
                window.setGravity(Gravity.TOP); // 此处可以设置dialog显示的位置
                break;
            case 1:
                window.setGravity(Gravity.CENTER);
                break;
            case 2:
                window.setGravity(Gravity.BOTTOM);
                break;
            case 3:
                WindowManager.LayoutParams params = window.getAttributes();
                dialog.onWindowAttributesChanged(params);
                params.x = screenWidth-dip2px(context,100);// 设置x坐标
                params.gravity = Gravity.TOP;
                params.y = dip2px(context, 45);// 设置y坐标
                Log.d("xx", params.y + "");
                window.setGravity(Gravity.TOP);
                window.setAttributes(params);
                break;
            default:
                window.setGravity(Gravity.CENTER);
                break;
        }
    }
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
    private static void initWheelView(final Context context) {
        Calendar c = Calendar.getInstance();
        curYear = c.get(Calendar.YEAR);
        curMonth = c.get(Calendar.MONTH) + 1;//通过Calendar算出的月数要+1
        curDate = c.get(Calendar.DATE);
        curHour=c.get(Calendar.HOUR);
        curMin=c.get(Calendar.MINUTE);
        LogUtils.d("xxhm",curHour+";;"+curMin);

        NumericWheelAdapter numericWheelAdapterStart1=new NumericWheelAdapter(context,1900, 2100);
        numericWheelAdapterStart1.setLabel(" ");
        wl_start_year.setViewAdapter(numericWheelAdapterStart1);
        numericWheelAdapterStart1.setTextColor(R.color.text_30);
        numericWheelAdapterStart1.setTextSize(20);
        wl_start_year.setCyclic(true);//是否可循环滑动
        wl_start_year.addScrollingListener(startScrollListener);

        wl_start_year.setCurrentItem(curYear - 1900);
        wl_start_year.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                curYear = newValue+2000;
                initStartDayAdapter(context);
            }
        });

        NumericWheelAdapter numericWheelAdapterStart2=new NumericWheelAdapter(context,1, 12, "%02d");
        numericWheelAdapterStart2.setLabel(" ");
        wl_start_month.setViewAdapter(numericWheelAdapterStart2);
        numericWheelAdapterStart2.setTextColor(R.color.text_30);
        numericWheelAdapterStart2.setTextSize(20);
        wl_start_month.setCyclic(true);
        wl_start_month.addScrollingListener(startScrollListener);
        wl_start_month.setCurrentItem(curMonth-1);
        wl_start_month.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                curMonth = newValue+1;
                initStartDayAdapter(context);
            }
        });
        initStartDayAdapter(context);

        NumericWheelAdapter numericWheelAdapterStart4=new NumericWheelAdapter(context,0, 23, "%02d");
        numericWheelAdapterStart4.setLabel(" ");
        wl_start_h.setViewAdapter(numericWheelAdapterStart4);
        numericWheelAdapterStart4.setTextColor(R.color.text_30);
        numericWheelAdapterStart4.setTextSize(20);
        wl_start_h.setCyclic(true);
        wl_start_h.addScrollingListener(startScrollListener);
        wl_start_h.setCurrentItem(curHour);
        wl_start_h.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                LogUtils.d("xxh",newValue+";"+oldValue);
                curHour = newValue;
            }
        });
        NumericWheelAdapter numericWheelAdapterStart5=new NumericWheelAdapter(context,0, 59, "%02d");
        numericWheelAdapterStart5.setLabel(" ");
        wl_start_m.setViewAdapter(numericWheelAdapterStart5);
        numericWheelAdapterStart5.setTextColor(R.color.text_30);
        numericWheelAdapterStart5.setTextSize(20);
        wl_start_m.setCyclic(true);
        wl_start_m.addScrollingListener(startScrollListener);
        wl_start_m.setCurrentItem(curMin);
        wl_start_m.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                LogUtils.d("xxm",newValue+"");
                curMin = newValue;
            }
        });
    }
    private static void initStartDayAdapter(Context context){
        NumericWheelAdapter numericWheelAdapterStart3=new NumericWheelAdapter(context,1,getDay(curYear,curMonth), "%02d");
        numericWheelAdapterStart3.setLabel(" ");
        wl_start_day.setViewAdapter(numericWheelAdapterStart3);
        numericWheelAdapterStart3.setTextColor(R.color.text_30);
        numericWheelAdapterStart3.setTextSize(20);
        wl_start_day.setCyclic(true);
        wl_start_day.addScrollingListener(startScrollListener);
        wl_start_day.setCurrentItem(curDate-1);
    }
    static OnWheelScrollListener startScrollListener = new OnWheelScrollListener() {
        @Override
        public void onScrollingStarted(WheelView wheel) {
        }
        @Override
        public void onScrollingFinished(WheelView wheel) {
            int n_year = wl_start_year.getCurrentItem() + 1900;//年
            int n_month = wl_start_month.getCurrentItem() + 1;//月
            int n_day = wl_start_day.getCurrentItem() + 1;//日
            String month=String.valueOf(n_month);
            if(n_month<10){
                month="0"+month;
            }
            String day=String.valueOf(n_day);
            if(n_day<10){
                day="0"+day;
            }
            String hour=String.valueOf(curHour);
            if(curHour<10){
                hour="0"+curHour;
            }
            String min=String.valueOf(curMin);
            if(curMin<10){
                min="0"+curMin;
            }
            time=n_year+"-"+month+"-"+day+" "+hour+":"+min;
        }
    };

    /**
     * 根据年月获得 这个月总共有几天
     * @param year
     * @param month
     * @return
     */
    public static int getDay(int year, int month) {
        int day = 30;
        boolean flag = false;
        switch (year % 4) {
            case 0:
                flag = true;
                break;
            default:
                flag = false;
                break;
        }
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                day = 31;
                break;
            case 2:
                day = flag ? 29 : 28;
                break;
            default:
                day = 30;
                break;
        }
        return day;
    }

}
