package com.wcb.contactlist;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wcb.contactlist.adapter.ContactAdapter;
import com.wcb.contactlist.model.SortModel;
import com.wcb.contactlist.util.DataUtil;
import com.wcb.contactlist.view.ClearEditText;
import com.wcb.contactlist.view.SideBar;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    @Bind(R.id.et_clear)
    ClearEditText et_clear;
    @Bind(R.id.sidebar)
    SideBar sideBar;
    @Bind(R.id.dialog)
    TextView dialog;
    @Bind(R.id.lv_contact)
    ListView sortListView;
    private static final int REQUEST_CODE = 254;
    private String uri_data_phones = "content://com.android.contacts/data/phones";

    private ContactAdapter adapter;
    private List<SortModel> sourceDateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        if (checkPermission()) {//检查并获取权限
            readContactNum();
        }
    }

    private void initView() {
        sideBar.setTextView(dialog);
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (adapter != null) {
                    int position = adapter.getPositionForSection(s.charAt(0));//该字母首次出现的位置
                    if (position != -1) {
                        sortListView.setSelection(position);//滚该位置
                    }
                }
            }
        });
        et_clear.setOnTextChangeListener(new ClearEditText.OnTextChangeListener() {
            @Override
            public void textChanged(String text) {
                if (adapter != null) {
                    DataUtil.filterData(adapter, sourceDateList, text);//过滤数据列表
                    adapter.notifyDataSetChanged();
                }
            }
        });

        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, sourceDateList.get(i).getName() + "-" + sourceDateList.get(i).getPhoneNum(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void readContactNum() {//读取通讯录
        getSupportLoaderManager().initLoader(1, null, this);
    }


    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//权限申请成功
                readContactNum();
            } else {
                Toast.makeText(this, "请允许权限!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {//读取通讯录
        return new CursorLoader(this, Uri.parse(uri_data_phones), new String[]{"_id", "display_name", "data1"}, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        sourceDateList = DataUtil.getModels(data);
        if (sourceDateList != null && sourceDateList.size() > 0) {
            adapter = new ContactAdapter(this, sourceDateList);
            sortListView.setAdapter(adapter);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        ButterKnife.unbind(this);
    }
}
