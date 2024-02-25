package tamhoang.bvn.ui.base.nav;

public class NavItem {
    private int resIcons;
    private String subtitle;
    private String title;

    public NavItem(String str, String str2, int i) {
        this.title = str;
        this.subtitle = str2;
        this.resIcons = i;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public void setSubtitle(String str) {
        this.subtitle = str;
    }

    public int getResIcons() {
        return this.resIcons;
    }

    public void setResIcons(int i) {
        this.resIcons = i;
    }
}
