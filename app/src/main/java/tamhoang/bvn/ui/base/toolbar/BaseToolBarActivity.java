package tamhoang.bvn.ui.base.toolbar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import tamhoang.bvn.R;

public abstract class BaseToolBarActivity extends AppCompatActivity {
    protected final String TAG_TITLE = getClass().getSimpleName();
    protected final String TAG = "ContentValues";
    protected Toolbar toolbar;

    /* access modifiers changed from: protected */
    public abstract int getLayoutId();

    /* access modifiers changed from: protected */
    @Override // android.support.v7.app.AppCompatActivity, android.support.v4.app.SupportActivity, android.support.v4.app.FragmentActivity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setTitle(TAG_TITLE);
        Toolbar toolbar2 = findViewById(R.id.toolbar);
        this.toolbar = toolbar2;
        setSupportActionBar(toolbar2);
    }
}