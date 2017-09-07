package com.jzg.crash;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LogsActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView rvLogs;
    private TextView tvEmpty;
    private ImageView ivBack;
    private TextView tvTitle;
    private ProgressDialog dialog;
    private LogAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        rvLogs = (RecyclerView) findViewById(R.id.rvLog);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.log_list_title);
        ivBack.setOnClickListener(this);
        dialog = ProgressDialog.show(this,"","loading...",false,false);
        Observable.create(new Observable.OnSubscribe<List<File>>() {
            @Override
            public void call(Subscriber<? super List<File>> subscriber) {
                subscriber.onNext(readLogs());
                subscriber.onCompleted();

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<File>>() {
                    @Override
                    public void onCompleted() {
                        if(dialog!=null && dialog.isShowing())
                            dialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dialog.dismiss();
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<File> files) {
                        if(files!=null && files.size()>0){
                            rvLogs.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(LogsActivity.this);
                            rvLogs.setLayoutManager(layoutManager);
                            adapter = new LogAdapter(LogsActivity.this,R.layout.item_logs,files);
                            rvLogs.setAdapter(adapter);
                            adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                                    File file = adapter.getDatas().get(position);
                                    Intent intent = new Intent(LogsActivity.this,LogDetailActivity.class);
                                    intent.putExtra("logPath",file.getAbsolutePath());
                                    startActivity(intent);
                                }

                                @Override
                                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                                    return false;
                                }
                            });
                        }
                    }
                });

    }

    private List<File> readLogs(){
        List<File> logs = new ArrayList<>();
        String logDir = getFilesDir().getAbsolutePath() + File.separator + "JzgCrash";
        File dir = new File(logDir);
        if(!dir.exists()){
            dir.mkdirs();
        }
        File[] logArr = dir.listFiles();
        if(logArr!=null && logArr.length>0){
            for(File log:logArr){
                logs.add(log);
            }
        }
        Collections.reverse(logs);
        return logs;
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
