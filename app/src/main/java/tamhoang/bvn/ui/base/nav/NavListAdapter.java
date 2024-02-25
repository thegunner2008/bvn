package tamhoang.bvn.ui.base.nav;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import tamhoang.bvn.R;

public class NavListAdapter extends ArrayAdapter<NavItem> {
    Context context;
    List<NavItem> listNavItems;
    int resLayout;

    public NavListAdapter(Context context2, int i, List<NavItem> list) {
        super(context2, i, list);
        this.context = context2;
        this.resLayout = i;
        this.listNavItems = list;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        View inflate = View.inflate(this.context, this.resLayout, (ViewGroup) null);
        NavItem navItem = this.listNavItems.get(i);
        ((TextView) inflate.findViewById(R.id.tittle)).setText(navItem.getTitle());
        ((TextView) inflate.findViewById(R.id.sub_tittle)).setText(navItem.getSubtitle());
        ((ImageView) inflate.findViewById(R.id.icon)).setImageResource(navItem.getResIcons());
        return inflate;
    }
}
