package com.liuchao.h5;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by liuchao on 2017/4/19.
 */
public class WebViewUtils {

    private static Context mContext;
    private static String mPath = "";
    private static WebViewUtils mWebViewUtils;
    private static H5Listener mH5Listener;

    private WebViewUtils(Context context){
        super();
        mContext=context;
    }

    public static WebViewUtils getInstance(Context context){
        if (mWebViewUtils==null){
            mWebViewUtils=new WebViewUtils(context);
        }
        mPath = Environment.getExternalStorageDirectory() + "/" + mContext.getPackageName() + "/";
        return mWebViewUtils;
    }


    /**
     * 拷贝到SD卡中
     * @param assetFileName
     */
    public void copyBigDataToSD(String assetFileName,final String internetUrl,H5Listener h5Listener) {
        mH5Listener=h5Listener;
        InputStream myInput = null;
        OutputStream myOutput = null;

        try {
            myOutput = new FileOutputStream(mPath+"/"+assetFileName);
            myInput = mContext.getAssets().open(assetFileName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {

        } finally {
            ZipExtractorTask task = new ZipExtractorTask(mPath + "/" + assetFileName, mPath, mContext, true,
                    new ZipExtractorTask.ZipOverListener() {
                        @Override
                        public void zipOver() {
                            String url;
                            File file = new File(mPath + "/dist/index.html");

                            if (!file.exists()) {
                                url=internetUrl;
                            } else {
                                url="file:///" + mPath + "/dist/index.html";
                            }
                            mH5Listener.copyDigDataSDSuccess(url);
                        }
                    });
            task.execute();
        }
    }

    /**
     * 下载文件
     */
    public void downloadH5Zip(String downloadUrl,final String internetUrl,H5Listener h5Listener) {
        mH5Listener=h5Listener;
        DownLoaderTask task = new DownLoaderTask(downloadUrl,
                mPath, mContext, new ZipExtractorTask.ZipOverListener() {
            @Override
            public void zipOver() {
                String url;

                File file = new File(mPath + "/dist/index.html");

                if (!file.exists()) {
                    url=internetUrl;
                } else {
                    url="file:///" + mPath + "/dist/index.html";
                }

                mH5Listener.shouldShowUrl(url);
            }
        });
        task.execute();
    }


    //解压并将文件进行加载
    private ZipExtractorTask.ZipOverListener mZipOverListener = new ZipExtractorTask.ZipOverListener() {
        @Override
        public void zipOver() {

        }
    };






}
