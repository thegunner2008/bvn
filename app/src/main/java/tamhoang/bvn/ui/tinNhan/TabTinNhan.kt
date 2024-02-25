package tamhoang.bvn.ui.tinNhan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.TabHost
import android.widget.TabHost.OnTabChangeListener
import android.widget.TabHost.TabContentFactory
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import tamhoang.bvn.R
import tamhoang.bvn.ui.base.pager.MyFragmentPagerAdapter
import tamhoang.bvn.ui.tinNhan.suaTin.FragSuaTin

class TabTinNhan : Fragment(), OnTabChangeListener, OnPageChangeListener {
    var i = 0
    private var tabHost: TabHost? = null
    var v: View? = null
    private var viewPager: ViewPager? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.frag_mo_report, container, false)
        initializeTabHost(savedInstanceState)
        initializeViewPager()
        tabHost!!.currentTab = 0
        return v
    }

    private fun initializeViewPager() {
        val fragments = ArrayList<Fragment>()
        fragments.add(FragSuaTin())
        fragments.add(FragTinChiTiet())
        val myViewpagerAdapter = MyFragmentPagerAdapter(childFragmentManager, fragments)
        viewPager = v!!.findViewById<View>(R.id.viewPager) as ViewPager
        viewPager!!.adapter = myViewpagerAdapter
        viewPager!!.setOnPageChangeListener(this)
    }

    private fun initializeTabHost(args: Bundle?) {
        if (activity == null) return
        val tabHost2 = v!!.findViewById<View>(R.id.tabhost) as TabHost
        tabHost = tabHost2
        tabHost2.setup()
        val tabSpec1 = tabHost!!.newTabSpec("Sửa tin")
        tabSpec1.setIndicator("Sửa tin")
        tabSpec1.setContent(FakeContent(activity!!))
        val tabSpec2 = tabHost!!.newTabSpec("Tin nhắn")
        tabSpec2.setIndicator("Tin chi tiết")
        tabSpec2.setContent(FakeContent(activity!!))
        tabHost!!.addTab(tabSpec1)
        tabHost!!.addTab(tabSpec2)
        tabHost!!.setOnTabChangedListener(this)
    }

    override fun onTabChanged(tabId: String) {
        viewPager!!.currentItem = tabHost!!.currentTab
        val hScrollView = v!!.findViewById<View>(R.id.hScrollView) as HorizontalScrollView
        val tabView = tabHost!!.currentTabView
        hScrollView.smoothScrollTo(tabView!!.left - (hScrollView.width - tabView.width) / 2, 0)
    }

    override fun onPageScrollStateChanged(arg0: Int) {}

    override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
    override fun onPageSelected(position: Int) {
        tabHost!!.currentTab = position
    }

    inner class FakeContent(private val mContext: Context) : TabContentFactory {
        override fun createTabContent(tag: String): View {
            val v = View(mContext)
            v.minimumHeight = 0
            v.minimumWidth = 0
            return v
        }
    }
}