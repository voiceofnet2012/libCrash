package com.jzg.crash;

import android.content.Context;

import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by voiceofnet on 2017/9/7.
 */

public class LogAdapter extends CommonAdapter<File> {
    public LogAdapter(Context context, int layoutId, List<File> datas) {
        super(context, layoutId, datas);
    }

    @Override
    protected void convert(ViewHolder holder, File file, int position) {
        holder.setText(R.id.tvFileName,file.getName());
        holder.setText(R.id.tvTime,new Date(file.lastModified()).toLocaleString());
        holder.setVisible(R.id.ivNew,position==0?true:false);
    }
}
