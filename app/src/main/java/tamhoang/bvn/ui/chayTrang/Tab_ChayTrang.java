package tamhoang.bvn.ui.chayTrang;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import java.util.Vector;

import tamhoang.bvn.ui.base.pager.MyFragmentPagerAdapter;
import tamhoang.bvn.R;

public class Tab_ChayTrang extends Fragment implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
    int i = 0;
    private MyFragmentPagerAdapter myViewpagerAdapter;
    private TabHost tabHost;
    View v;
    private ViewPager viewPager;

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.v = inflater.inflate(R.layout.frag_mo_report, container, false);
        initializeTabHost(savedInstanceState);
        initializeViewPager();
        this.tabHost.setCurrentTab(0);
        return this.v;
    }

    private void initializeViewPager() {
        Vector fragments = new Vector();
        fragments.add(new Frag_Chaytrang());
        fragments.add(new Frag_ChayTrang_Acc());
        this.myViewpagerAdapter = new MyFragmentPagerAdapter(getChildFragmentManager(), fragments);
        ViewPager viewPager2 = (ViewPager) this.v.findViewById(R.id.viewPager);
        this.viewPager = viewPager2;
        viewPager2.setAdapter(this.myViewpagerAdapter);
        this.viewPager.setOnPageChangeListener(this);
    }

    private void initializeTabHost(Bundle savedInstanceState) {
        TabHost tabHost2 = (TabHost) this.v.findViewById(R.id.tabhost);
        this.tabHost = tabHost2;
        tabHost2.setup();
        TabHost.TabSpec tabSpec1 = this.tabHost.newTabSpec("Chạy Trang");
        tabSpec1.setIndicator("Vào trang");
        tabSpec1.setContent(new FakeContent(getActivity()));
        this.tabHost.addTab(tabSpec1);
        TabHost.TabSpec tabSpec2 = this.tabHost.newTabSpec("Mã chạy");
        tabSpec2.setIndicator("Tài khoản");
        tabSpec2.setContent(new FakeContent(getActivity()));
        this.tabHost.addTab(tabSpec2);
        this.tabHost.setOnTabChangedListener(this);
    }

    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
    public void onPageScrolled(int i2, float v2, int i1) {
    }

    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
    public void onPageSelected(int position) {
//        this.tabHost.setCurrentTab(position);
    }

    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
    public void onPageScrollStateChanged(int i2) {
    }

    public void onTabChanged(String s) {
        this.viewPager.setCurrentItem(this.tabHost.getCurrentTab());
        HorizontalScrollView hScrollView = (HorizontalScrollView) this.v.findViewById(R.id.hScrollView);
        View tabView = this.tabHost.getCurrentTabView();
        hScrollView.smoothScrollTo(tabView.getLeft() - ((hScrollView.getWidth() - tabView.getWidth()) / 2), 0);
    }

    /* access modifiers changed from: package-private */
    public class FakeContent implements TabHost.TabContentFactory {
        private final Context mContext;

        public FakeContent(Context context) {
            this.mContext = context;
        }

        public View createTabContent(String tag) {
            View v = new View(this.mContext);
            v.setMinimumHeight(0);
            v.setMinimumWidth(0);
            return v;
        }
    }
}