package com.liuchao.zip;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.liuchao.h5.DownLoaderTask;
import com.liuchao.h5.H5Listener;
import com.liuchao.h5.WebViewUtils;
import com.liuchao.h5.ZipExtractorTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    WebView mWebView;
    private static final String APP_CACAHE_DIRNAME = "/webcache";
    private static final String SHAREDPREFERENCES_NAME = "first_pref";
    private SharedPreferences preferences;
    //第一次加载
    private boolean isFirstIn;
    private String PATH = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        //1、初始化地址及其他参数
        initParams();
        //2、初始化各种组件
        initComponent();
        //3、初始化浏览器
        initWebView();
        //4、初始化加载内容
        initContent();
    }

    /**
     * 初始化各种组件
     */
    private void initComponent(){
        mWebView = (WebView) findViewById(R.id.webView);
    }

    /**
     * 初始化地址及其他参数
     */
    private void initParams(){
        preferences = getSharedPreferences(
                SHAREDPREFERENCES_NAME, MODE_PRIVATE);

        // 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
        isFirstIn = preferences.getBoolean("isFirstIn", true);
        //存放assets文件下压缩文件的地方
        PATH = Environment.getExternalStorageDirectory() + "/" + getApplication().getPackageName() + "/";

        Log.d(TAG, "Environment.getExternalStorageDirectory()=" + Environment.getExternalStorageDirectory());
        Log.d(TAG, "getCacheDir().getAbsolutePath()=" + getCacheDir().getAbsolutePath());
    }

    /**
     * 初始化加载内容
     */
    private void initContent(){
        //第一进入复制assets文件下的压缩包并解压运行。
        if (isFirstIn) {
            Toast.makeText(this, "拷贝资源文件，并解压到SD", Toast.LENGTH_LONG).show();

            //copyBigDataToSD(PATH + "/mifan-app-frontend.zip");

            SharedPreferences.Editor editor = preferences.edit();
            // 存入数据
            editor.putBoolean("isFirstIn", false);
            // 提交修改
            editor.apply();

            showDownLoadDialog();
        }else {
            //非第一次进入直接加载内容就行，有可能复制失败找不到文件
            //copyBigDataToSD(PATH + "/mifan-app-frontend.zip");
            loadView();
        }
    }

    /**
     * 检查H5是否更新 有更新
     */
    private void checkH5Version(){

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();// 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 加载sd卡文件，没有从网络获取
     */
    private void loadView() {
        String url = "file:///" + PATH + "/dist/index.html"; // url
        Log.i(TAG, url);
        File file = new File(PATH + "/dist/index.html");

        if (!file.exists()) {
            mWebView.loadUrl("https://mi.4zlink.com/mifan-app-frontend/index.html");
        } else {
            Toast.makeText(this,"加载中...",Toast.LENGTH_SHORT).show();
            mWebView.loadUrl("file:///" + PATH + "/dist/index.html");
        }
    }




    //本地asset的文件过期了进行重新下载，版本更新
    private void showDownLoadDialog() {
        new AlertDialog.Builder(this).setTitle("确认")
                .setMessage("资源文件存在更新，是否下载？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onClick 1 = " + which);
                        doDownLoadWork("http://www.4zlink.com/sdk/mifan-app-frontend.zip");
                    }
                }).setCancelable(false)
                .setNegativeButton("否", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.d(TAG, "onClick 2 = " + which);
                        loadView();
                    }
                })
                .show();
    }

    /**
     * 拷贝到SD卡中
     * @param strOutFileName
     */
    /*private void copyBigDataToSD(String strOutFileName) {
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            myOutput = new FileOutputStream(strOutFileName);
            myInput = this.getAssets().open("mifan-app-frontend.zip");
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
            ZipExtractorTask task = new ZipExtractorTask(PATH + "/" + "mifan-app-frontend.zip", PATH, MainActivity.this, true,
                    mZipOverListener);
            task.execute();
        }
    }*/

    /**
     * 下载之后直接解压
     */
    private void doDownLoadWork(String downUrl) {
        DownLoaderTask task = new DownLoaderTask(downUrl,
                PATH, this, mZipOverListener);
        task.execute();
    }



    //解压并将文件进行加载
    public ZipExtractorTask.ZipOverListener mZipOverListener = new ZipExtractorTask.ZipOverListener() {
        @Override
        public void zipOver() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("Tag","");
                    loadView();
                }
            });
        }
    };



































    /**
     * 初始化加载内容
     */
    private void initWebView() {

        initSetting();

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onLoadResource(WebView view, String url) {

                Log.i(TAG, "onLoadResource url=" + url);

                super.onLoadResource(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url) {

                Log.i(TAG, "intercept url=" + url);

                webview.loadUrl(url);

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                Log.e(TAG, "onPageStarted");
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                String title = view.getTitle();

                Log.e(TAG, "onPageFinished WebView title=" + title);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {

            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {

                Log.e(TAG, "onJsAlert " + message);

                Toast.makeText(getApplicationContext(), message,
                        Toast.LENGTH_SHORT).show();

                result.confirm();

                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url,
                                       String message, JsResult result) {

                Log.e(TAG, "onJsConfirm " + message);

                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                                      String defaultValue, JsPromptResult result) {

                Log.e(TAG, "onJsPrompt " + url);

                return super.onJsPrompt(view, url, message, defaultValue,
                        result);
            }
        });
    }

    @SuppressWarnings({"deprecation", "deprecation"})
    private void initSetting() {

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT); // 设置
        // 缓存模式
        // 开启 DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        // 开启 database storage API 功能
        mWebView.getSettings().setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath()
                + APP_CACAHE_DIRNAME;
        // String cacheDirPath =
        // getCacheDir().getAbsolutePath()+Constant.APP_DB_DIRNAME;
        Log.i(TAG, "cacheDirPath=" + cacheDirPath);
        // 设置数据库缓存路径
        mWebView.getSettings().setDatabasePath(cacheDirPath);
        // 设置 Application Caches 缓存目录
        mWebView.getSettings().setAppCachePath(cacheDirPath);
        // 开启 Application Caches 功能
        mWebView.getSettings().setAppCacheEnabled(true);
    }


    /**
     * 清除WebView缓存
     */
    public void clearWebViewCache() {
        // 清理Webview缓存数据库
        try {
            deleteDatabase("webview.db");
            deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // WebView 缓存文件
        File appCacheDir = new File(getFilesDir().getAbsolutePath()
                + APP_CACAHE_DIRNAME);
        Log.e(TAG, "appCacheDir path=" + appCacheDir.getAbsolutePath());

        File webviewCacheDir = new File(getCacheDir().getAbsolutePath()
                + "/webviewCache");
        Log.e(TAG, "webviewCacheDir path=" + webviewCacheDir.getAbsolutePath());

        // 删除webview 缓存目录
        if (webviewCacheDir.exists()) {
            deleteFile(webviewCacheDir);
        }
        // 删除webview 缓存 缓存目录
        if (appCacheDir.exists()) {
            deleteFile(appCacheDir);
        }
    }

    /**
     * 递归删除 文件/文件夹
     *
     * @param file
     */
    public void deleteFile(File file) {

        Log.i(TAG, "delete file path=" + file.getAbsolutePath());

        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFile(files[i]);
                }
            }
            file.delete();
        } else {
            Log.e(TAG, "delete file no exists " + file.getAbsolutePath());
        }
    }
}
