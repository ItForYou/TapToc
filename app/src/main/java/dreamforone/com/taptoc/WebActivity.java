package dreamforone.com.taptoc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Parcelable;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import util.BackPressCloseHandler;
import util.Common;


public class WebActivity extends AppCompatActivity {
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    LinearLayout webLayout;

    WebView webView;

    //ProgressBar loadingProgress2;
    ImageView loadingProgress;
    public static boolean execBoolean = true;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    private static final int APP_PERMISSION_STORAGE = 9787;
    private final int APPS_PERMISSION_REQUEST = 1000;
    String firstUrl = "";
    Vibrator vibrator;
    final public String CAPTURE_PATH="/capture";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //상태바 변경하기
        if(Build.VERSION.SDK_INT>=21){
            getWindow().setStatusBarColor(Color.parseColor("#3a2051"));
        }
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        setContentView(R.layout.activity_web);
        //화면 꺼짐 방지
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        firstUrl = intent.getExtras().getString("url");
        setLayout();
    }

    //레이아웃 설정
    public void setLayout() {
        loadingProgress=(ImageView)findViewById(R.id.loadingProgress);

        Glide.with(this)
                .load(R.drawable.loader)
                .into(loadingProgress);



        webLayout = (LinearLayout) findViewById(R.id.webLayout);//웹뷰 레이아웃 가져오기
        //loadingProgress2 = (ProgressBar)findViewById(R.id.loadingProgress2);
        loadingProgress.setVisibility(View.VISIBLE);

        webView = (WebView) findViewById(R.id.webView);//웹뷰 가져오기
        webView.loadUrl(firstUrl);
        webViewSetting();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {
        /*webView.addJavascriptInterface(new AppShare(), "appshare");
        webView.addJavascriptInterface(new AppShares(), "appshares");
        webView.addJavascriptInterface(new PostEmail(), "postemail");*/
        WebSettings setting = webView.getSettings();//웹뷰 세팅용

        setting.setAllowFileAccess(true);//웹에서 파일 접근 여부
        setting.setAppCacheEnabled(true);//캐쉬 사용여부
        setting.setGeolocationEnabled(true);//위치 정보 사용여부
        setting.setDatabaseEnabled(true);//HTML5에서 db 사용여부
        setting.setDomStorageEnabled(true);//HTML5에서 DOM 사용여부
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);//캐시 사용모드 LOAD_NO_CACHE는 캐시를 사용않는다는 뜻
        setting.setJavaScriptEnabled(true);//자바스크립트 사용여부
        setting.setSupportMultipleWindows(false);//윈도우 창 여러개를 사용할 것인지의 여부 무조건 false로 하는 게 좋음
        setting.setUseWideViewPort(true);//웹에서 view port 사용여부
        webView.setWebChromeClient(chrome);//웹에서 경고창이나 또는 컴펌창을 띄우기 위한 메서드
        webView.setWebViewClient(client);//웹페이지 관련된 메서드 페이지 이동할 때 또는 페이지가 로딩이 끝날 때 주로 쓰임
        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent+" TabToc");
        webView.addJavascriptInterface(new WebActivity.WebJavascriptEvent(), "Android");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 혼합된 컨텐츠 허용//
        }

        //현재 안드로이드 버전이 허니콤(3.0) 보다 높으면 줌 컨트롤 사용여부 체킹
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setting.setBuiltInZoomControls(true);
            setting.setDisplayZoomControls(false);
        }

        webView.addJavascriptInterface(new WebActivity.WebJavascriptEvent(),"android");
        webView.addJavascriptInterface(new WebActivity.WebJavascriptEvent(),"history");
        //웹뷰 하드웨어 가속
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        //네트워크 체킹을 할 때 쓰임
        /*netCheck = new NetworkCheck(this, this);
        netCheck.setNetworkLayout(networkLayout);
        netCheck.setWebLayout(webLayout);
        netCheck.networkCheck();
        //뒤로가기 버튼을 눌렀을 때 클래스로 제어함
        backPressCloseHandler = new BackPressCloseHandler(this);

        replayBtn=(Button)findViewById(R.id.replayBtn);
        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                netCheck.networkCheck();
            }
        });*/
    }

    WebChromeClient chrome;

    {
        chrome = new WebChromeClient() {
            //새창 띄우기 여부
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                return false;
            }

            //경고창 띄우기
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(WebActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                }).create().show();
                return true;
            }

            //컴펌 띄우기
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                                       final JsResult result) {
                new AlertDialog.Builder(WebActivity.this)
                        .setMessage("\n" + message + "\n")
                        .setCancelable(false)
                        .setPositiveButton("확인",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        result.cancel();
                                    }
                                }).create().show();
                return true;
            }

            //현재 위치 정보 사용여부 묻기
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // Should implement this function.
                final String myOrigin = origin;
                final GeolocationPermissions.Callback myCallback = callback;
                AlertDialog.Builder builder = new AlertDialog.Builder(WebActivity.this);
                builder.setTitle("Request message");
                builder.setMessage("Allow current location?");
                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, true, false);
                    }

                });
                builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        myCallback.invoke(myOrigin, false, false);
                    }

                });
                AlertDialog alert = builder.create();
                alert.show();
            }
            //현재 위치 정보 사용여부 묻기

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                if (filePathCallbackLollipop != null) {
//                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
                filePathCallbackLollipop = filePathCallback;


                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AndroidExampleFolder");
                if (!imageStorageDir.exists()) {
                    // Create AndroidExampleFolder at sdcard
                    imageStorageDir.mkdirs();
                }

                // Create camera captured image file path and name
                File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);

                Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                // Create file chooser intent
                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                // Set camera intent to file chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});

                // On select image call onActivityResult method of activity
                startActivityForResult(chooserIntent, FILECHOOSER_LOLLIPOP_REQ_CODE);
                return true;

            }
        };
    }

    WebViewClient client;

    {
        client = new WebViewClient() {
            //페이지 로딩중일 때 (마시멜로) 6.0 이후에는 쓰지 않음
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                loadingProgress.setVisibility(View.VISIBLE);
                if (url.startsWith("tel")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.


                        }
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }



                }else{
                    if(url.equals(getString(R.string.domain)+"student/")||url.equals(getString(R.string.domain)+"teacher/")){

                        finish();
                        return true;
                    }
                }
                return false;
            }
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //webLayout.setRefreshing(false);
                loadingProgress.setVisibility(View.GONE);
                Log.d("url",url);
                Log.d("ss_mb_id", Common.getPref(getApplicationContext(),"ss_mb_id",""));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                //로그인할 때
                if(url.startsWith(getString(R.string.domain)+"bbs/login.php")){
                    view.loadUrl("javascript:fcmKey('"+Common.TOKEN+"')");
                }

                if (url.equals(getString(R.string.teacher_url)) || url.equals(getString(R.string.student_url))) {
                    isIndex=true;
                } else {
                    isIndex=false;
                }
            }
            //페이지 오류가 났을 때 6.0 이후에는 쓰이지 않음
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //super.onReceivedError(view, request, error);
                //view.loadUrl("");
                //페이지 오류가 났을 때 오류메세지 띄우기
                /*AlertDialog.Builder builder = new AlertDialog.Builder(WebActivity.this);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                builder.show();*/
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    //다시 들어왔을 때
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        execBoolean=true;
        Log.d("newtork","onResume");
        webView.loadUrl("javascript:roomConnect();roomIn()");
        //netCheck.networkCheck();
    }
    //홈버튼 눌러서 바탕화면 나갔을 때
    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
        firstUrl=webView.getUrl();
        webView.loadUrl("javascript:roomDisconnect();roomIn()");
        execBoolean=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //netCheck.stopReciver();
    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        /*if (webView.canGoBack()) {
            webView.goBack();
        } else if (webView.canGoBack() == false) {

        }*/

        finish();
    }
    //로그인 로그아웃
    class WebJavascriptEvent{
        @JavascriptInterface
        public void setLogin(String mb_id){
            Log.d("login","로그인");
            Common.savePref(getApplicationContext(),"ss_mb_id",mb_id);
        }
        @JavascriptInterface
        public void setLogout(){
            Log.d("logout","로그아웃");
            Common.savePref(getApplicationContext(),"ss_mb_id","");
        }
        @JavascriptInterface
        public  void getHtml(String html){
            Log.d("pdf-html",html);
        }
        @JavascriptInterface
        public void screenShot(){


            View rootView=getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            rootView.buildDrawingCache();

            Bitmap screenshot=rootView.getDrawingCache();
            int[] location=new int[2];
            rootView.getLocationInWindow(location);
            Bitmap bmp = Bitmap.createBitmap(screenshot, location[0], location[1], rootView.getWidth(), rootView.getHeight(), null, false);
            String strFolderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + CAPTURE_PATH;
            String fileName=System.currentTimeMillis() + ".png";
            File folder = new File(strFolderPath);
            if(!folder.exists()) {
                folder.mkdirs();
            }

            String strFilePath = strFolderPath + "/" + fileName;
            File fileCacheItem = new File(strFilePath);
            OutputStream out = null;
            try {
                fileCacheItem.createNewFile();
                out = new FileOutputStream(fileCacheItem);
                bmp.compress(Bitmap.CompressFormat.PNG, 100, out);

                Common.sharedPhoto(WebActivity.this,fileName);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    out.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }




            /*if(screenShot!=null){
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(screenShot)));
                Common.sharedPhoto(WebActivity.this);
            }*/
        }
        @JavascriptInterface
        public void back(){
            Log.d("back","back");
            try{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (webView.canGoBack()) {
                            webView.goBack();
                        } else if (webView.canGoBack() == false) {
                            Intent intent = new Intent();
                            intent.putExtra("result","OK");
                            setResult(1000,intent);
                            finish();
                        }
                    }
                });
            }catch (Exception e){

            }

        }
        @JavascriptInterface
        public void finish(){
            Log.d("finish","finish");
            WebActivity.this.finish();
        }
        @JavascriptInterface
        public void answerVibrate(){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(WebActivity.this, "진동", Toast.LENGTH_SHORT).show();
                    vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(1000);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
            if (filePathCallbackNormal == null) return;
            Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
            filePathCallbackNormal.onReceiveValue(result);
            filePathCallbackNormal = null;

        } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
            Uri[] result = new Uri[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(resultCode == RESULT_OK){
                    result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                }

                filePathCallbackLollipop.onReceiveValue(result);

            }
        }
    }
}
