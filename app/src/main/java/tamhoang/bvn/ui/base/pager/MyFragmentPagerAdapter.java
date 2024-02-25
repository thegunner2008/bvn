package tamhoang.bvn.ui.base.pager;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    List<Fragment> fragments;

    public MyFragmentPagerAdapter(FragmentManager fragmentManager, List<Fragment> list) {
        super(fragmentManager);
        this.fragments = list;
    }

    public Fragment getItem(int i) {
        return this.fragments.get(i);
    }

    public int getCount() {
        return this.fragments.size();
    }
}
