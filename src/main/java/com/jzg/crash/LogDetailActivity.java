package com.jzg.crash;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class LogDetailActivity extends AppCompatActivity{
    private TextView tvContent;
    private ImageView ivBack;
    private TextView tvTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_detail);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(R.string.log_detail_title);
        tvContent = (TextView) findViewById(R.id.tvContent);
        tvContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(tvContent.getText());
                Toast.makeText(LogDetailActivity.this, getString(R.string.copy_ok), Toast.LENGTH_LONG).show();
            }
        });
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        String logPath = getIntent().getStringExtra("logPath");
        if(!TextUtils.isEmpty(logPath)){
            File logFile = new File(logPath);
            String content = readTxtFile(logFile);
            if(!TextUtils.isEmpty(content)){
                tvContent.setText(content);
            }
        }
    }

    public String readTxtFile(File file){
        StringBuffer sb = new StringBuffer();
        try {
            if(file.isFile() && file.exists()){
                InputStreamReader read = new InputStreamReader(new FileInputStream(file),"GBK");
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null){
                    sb.append(lineTxt).append("\n");
                }
                read.close();
                bufferedReader.close();
            }else{
                Toast.makeText(this,"file not exist",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this,"read file error",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        return sb.toString();

    }
}
