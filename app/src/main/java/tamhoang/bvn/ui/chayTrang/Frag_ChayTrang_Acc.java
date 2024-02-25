package tamhoang.bvn.ui.chayTrang;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import tamhoang.bvn.R;
import tamhoang.bvn.data.DbOpenHelper;

public class Frag_ChayTrang_Acc extends Fragment {
    public List<String> Account = new ArrayList();
    Button btn_them_trang;
    DbOpenHelper db;
    ListView lv_account;
    View v;

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.frag_chaytrang_acc, container, false);
        this.v = inflate;
        this.btn_them_trang = (Button) inflate.findViewById(R.id.btn_them_trang);
        this.lv_account = (ListView) this.v.findViewById(R.id.lv_account);
        this.db = new DbOpenHelper(getActivity());
        this.btn_them_trang.setOnClickListener(view -> {
            Intent intent = new Intent(Frag_ChayTrang_Acc.this.getActivity(), ActivityAccWeb.class);
            intent.putExtra("new_web", "");
            Frag_ChayTrang_Acc.this.startActivity(intent);
        });
        xem_lv();
        return this.v;
    }

    @Override // android.support.v4.app.Fragment
    public void onResume() {
        super.onResume();
        xem_lv();
    }

    public void xem_lv() {
        this.Account.clear();
        Cursor cursor = this.db.getData("select * from tbl_chaytrang_acc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                this.Account.add(cursor.getString(0));
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        if (getActivity() != null) {
            this.lv_account.setAdapter((ListAdapter) new KHAdapter(getActivity(), R.layout.frag_khach_hang_lv, this.Account));
        }
    }

    /* access modifiers changed from: package-private */
    public class KHAdapter extends ArrayAdapter {
        public KHAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.frag_chaytrang_acc_lv, (ViewGroup) null);
            ((TextView) v.findViewById(R.id.tv_acc_trang)).setText(Frag_ChayTrang_Acc.this.Account.get(position));
            ((TextView) v.findViewById(R.id.tv_edit)).setOnClickListener(v12 -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(Frag_ChayTrang_Acc.this.getActivity());
                builder.setTitle("Sửa thông tin");
                builder.setMessage("Sửa thông tin trang " + Frag_ChayTrang_Acc.this.Account.get(position) + "?");
                builder.setNegativeButton("Có", (dialog, which) -> {
                    Intent intent = new Intent(Frag_ChayTrang_Acc.this.getActivity(), ActivityAccWeb.class);
                    intent.putExtra("new_web", Frag_ChayTrang_Acc.this.Account.get(position));
                    intent.putExtra("kh_new", "");
                    Frag_ChayTrang_Acc.this.startActivity(intent);
                });
                builder.setPositiveButton("Không", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
            ((TextView) v.findViewById(R.id.tv_delete)).setOnClickListener(v1 -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(Frag_ChayTrang_Acc.this.getActivity());
                builder.setTitle("Xoá tài khoản");
                builder.setMessage("Xoá " + Frag_ChayTrang_Acc.this.Account.get(position) + " ra khỏi danh sách?");
                builder.setNegativeButton("Có", (dialog, which) -> {
                    DbOpenHelper database = Frag_ChayTrang_Acc.this.db;
                    database.queryData("Delete FROM tbl_chaytrang_acc where Username = '" + Frag_ChayTrang_Acc.this.Account.get(position) + "'");
                    Frag_ChayTrang_Acc.this.xem_lv();
                    dialog.dismiss();
                    Toast.makeText(Frag_ChayTrang_Acc.this.getActivity(), "Xoá thành công!", Toast.LENGTH_LONG).show();
                });
                builder.setPositiveButton("Không", (dialog, which) -> dialog.dismiss());
                builder.show();
            });
            return v;
        }
    }
}