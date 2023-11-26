package com.lanhin.account.ui.fragments;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.widget.LinearLayout;

import com.lanhin.account.R;
import com.lanhin.account.bean.AccountModel;
import com.lanhin.account.constants.Extra;
import com.lanhin.account.db.database.DbHelper;
import com.lanhin.account.ui.fragments.adapter.BaseFragmentPagerAdapter;
import com.lanhin.account.ui.fragments.chart.ChartDetailFragment;
import com.lanhin.account.util.AccListUtil;
import com.lanhin.account.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * 图表
 */
public class ChartTypeFragment extends BaseFragment {
    private static final String TIME_TYPE = "timeType";
    public static final int TYPE_WEEK = 1;
    public static final int TYPE_MONTH = 2;
    public static final int TYPE_YEAR = 3;
    @BindView(R.id.tab_dettail)
    TabLayout mTabDettail;
    @BindView(R.id.vp_chart)
    ViewPager mVpChart;
    @BindView(R.id.lin_empty)
    LinearLayout mLinEmpty;

    private int mType;
    private int mAccountType = Extra.ACCOUNT_TYPE_EXPEND;
    private List<AccountModel> mDetailTypeList = new ArrayList<>();

    private ArrayList<String> mTitleList = new ArrayList<>();
    private ArrayList<ChartDetailFragment> mDetailFragmentList = new ArrayList<>();

    public ChartTypeFragment() {
        // Required empty public constructor
    }


    public static ChartTypeFragment newInstance(int param1) {
        ChartTypeFragment fragment = new ChartTypeFragment();
        Bundle args = new Bundle();
        args.putInt(TIME_TYPE, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getInt(TIME_TYPE);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            initChartData();
//            //消息发送到fragmentChart，获取分类列表
//            EventBus.getDefault().post(mDetailTypeList);
//        }
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chart_type;
    }

    @Override
    protected void initData() {
        initChartData();
    }


    @Override
    protected void initView(View view) {
        initViewPager(mDetailFragmentList, mTitleList);
        initListener();

    }

    private void initListener() {
        //mTabDettail.getSelectedTabPosition()
        mVpChart.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                mDetailTypeList = AccListUtil.removeRepeat(mDetailFragmentList.get(position).getTypeListData());
//                //消息发送到fragmentChart，获取分类列表
//                EventBus.getDefault().post(mDetailTypeList);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initChartData() {
        mTitleList.clear();
        mDetailFragmentList.clear();

        Date maxDate = DbHelper.getInstance().getMaxDate();
        Date minDate = DbHelper.getInstance().getMinDate();
        if (minDate == null || maxDate == null) {
            mVpChart.setVisibility(View.GONE);
            mLinEmpty.setVisibility(View.VISIBLE);
            return;
        }

        List<AccountModel> accountList = DbHelper.getInstance().getAccountList(mAccountType, minDate, maxDate);
        mDetailTypeList = AccListUtil.removeRepeat(accountList);
        float maxValue = getMaxValue(accountList);

        switch (mType) {
            case TYPE_WEEK:
                if (maxDate != null && minDate != null) {
                    int minWeek = TimeUtil.getWeekOfYear(minDate);
                    int maxWeek = TimeUtil.getWeekOfYear(maxDate);
                    for (int i = minWeek; i <= maxWeek; i++) {
                        mTitleList.add(i + "周");
                        mDetailFragmentList.add(ChartDetailFragment.newInstance(TYPE_WEEK, i, maxValue));
                    }
                    //setPagerData(mDetailFragmentList, mTitleList);
                }

                break;
            case TYPE_MONTH:
                if (maxDate != null && minDate != null) {
                    int minMonth = TimeUtil.getMonthOfYear(minDate);
                    int maxMonth = TimeUtil.getMonthOfYear(maxDate);
                    for (int i = minMonth; i <= maxMonth; i++) {
                        mTitleList.add((i + 1) + "月");
                        mDetailFragmentList.add(ChartDetailFragment.newInstance(TYPE_MONTH, i, maxValue));
                    }
                    //setPagerData(mDetailFragmentList, mTitleList);
                }

                break;
            case TYPE_YEAR:
                if (maxDate != null && minDate != null) {
                    int minYear = TimeUtil.getYear(minDate);
                    int maxYear = TimeUtil.getYear(maxDate);
                    for (int i = minYear; i <= maxYear; i++) {
                        mTitleList.add((i) + "年");
                        mDetailFragmentList.add(ChartDetailFragment.newInstance(TYPE_YEAR, i, maxValue));
                    }
                    //setPagerData(mDetailFragmentList, mTitleList);
                }
                break;
            default:
                break;
        }

    }

    public List<AccountModel> getTypeListData() {
        return mDetailTypeList;
    }


    /**
     * 获取时间段类记账金额最大值 （以天为单位）
     *
     * @return
     */
    private float getMaxValue(List<AccountModel> accountList) {

        ArrayList<Float> listFloat = new ArrayList<>();
        if (accountList == null || accountList.size() == 0) { //天数为0
            return 0f;
        } else {
            float sumDayCount = 0f;
            int day = TimeUtil.getDayOfYear(accountList.get(0).getTime());
            for (AccountModel accountModel : accountList) {
                int dayTemp = TimeUtil.getDayOfYear(accountModel.getTime());
                if (dayTemp != day) { //如果不是同一天
                    listFloat.add(sumDayCount);
                    day = dayTemp;
                    sumDayCount = 0f;
                }
                //如果是同一天则相加
                sumDayCount += accountModel.getCount();
            }
            //循环最后一轮数据还没加入list
            listFloat.add(sumDayCount);
            Float maxValue = Collections.max(listFloat);
            //Logger.e(maxValue + ":" + maxValue);
            return maxValue;
        }
    }


    /**
     * @param fragmentList
     * @param titleList
     */
    private void initViewPager(ArrayList<ChartDetailFragment> fragmentList, ArrayList<String> titleList) {
        BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(getChildFragmentManager(), fragmentList, titleList);
        mVpChart.setAdapter(adapter);
        // 设置ViewPager最大缓存的页面个数(cpu消耗少)
        mVpChart.setOffscreenPageLimit(1);
        mVpChart.setCurrentItem(titleList.size() - 1);
        //vpContent.addOnPageChangeListener(this);
        //vpContent.setCurrentItem(0);
        //adapter.notifyDataSetChanged();
        if (titleList.size() < 6)
            mTabDettail.setTabMode(TabLayout.MODE_FIXED);
        else
            mTabDettail.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabDettail.setupWithViewPager(mVpChart);
        //TabLayoutIndicator.setIndicatorWithTextWidth(mTabDettail);
        //了解源码得知 线的宽度是根据 tabView的宽度来设置的

    }

}
