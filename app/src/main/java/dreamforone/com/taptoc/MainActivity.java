package dreamforone.com.taptoc;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import android.widget.LinearLayout;

import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import util.BackPressCloseHandler;
import util.Common;
import util.retrofit.RetrofitService;
import util.retrofit.ServerPost;
import util.retrofit.StaticRetrofit;


public class MainActivity extends AppCompatActivity {
    LinearLayout webLayout;
    static public WebView webView;

    //ProgressBar loadingProgress;
    public static boolean execBoolean = true;
    private BackPressCloseHandler backPressCloseHandler;
    boolean isIndex = true;
    private static final int APP_PERMISSION_STORAGE = 9787;
    private final int APPS_PERMISSION_REQUEST = 1000;
    String firstUrl = "";
    private CustomDialog customDialog;
    Context mContext;
    Activity mActivity;
    final int REQUEST_IMAGE_CODE = 1010;
    private Uri cameraImageUri;
    Vibrator vibrator;
    final int FILECHOOSER_NORMAL_REQ_CODE = 1200,FILECHOOSER_LOLLIPOP_REQ_CODE=1300;
    private final int REQUEST_VIEWER=1000;
    ValueCallback<Uri> filePathCallbackNormal;
    ValueCallback<Uri[]> filePathCallbackLollipop;
    Uri mCapturedImageURI;
    boolean nextForwardBoolean=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //상태바 변경하기
        if(Build.VERSION.SDK_INT>=21){
            getWindow().setStatusBarColor(Color.parseColor("#3a2051"));
        }

        mContext=this;
        mActivity=this;
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("tactok");
        FirebaseInstanceId.getInstance().getToken();
        Intent intent = getIntent();
        try {
            if (Common.TOKEN.equals("") || Common.TOKEN.equals(null)) {
                refreshToken();
            } else {

            }
        }catch (Exception e){
            refreshToken();
        }
        firstUrl = getString(R.string.url);
        try{
            if(!intent.getExtras().getString("goUrl").equals("")){
                firstUrl =intent.getExtras().getString("goUrl");
            }
        }catch(Exception e){

        }
        setLayout();
    }
    private void refreshToken(){
        FirebaseMessaging.getInstance().subscribeToTopic("tactok");
        Common.TOKEN= FirebaseInstanceId.getInstance().getToken();
    }

    //레이아웃 설정
    public void setLayout() {
        vibrator=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        webLayout = (LinearLayout) findViewById(R.id.webLayout);//웹뷰 레이아웃 가져오기
        //loadingProgress = (ProgressBar)findViewById(R.id.loadingProgress2);

        webView = (WebView) findViewById(R.id.webView);//웹뷰 가져오기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.loadUrl(firstUrl);
        webViewSetting();

        customDialog=new CustomDialog(this,galleryOnClickListener,photoOnClickListener);

    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR_MR1)
    public void webViewSetting() {


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
        webView.getSettings().setUserAgentString(userAgent+" TapToc");
        webView.addJavascriptInterface(new WebJavascriptEvent(), "Android");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // 혼합된 컨텐츠 허용//
        }

        //현재 안드로이드 버전이 허니콤(3.0) 보다 높으면 줌 컨트롤 사용여부 체킹
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setting.setBuiltInZoomControls(true);
            setting.setDisplayZoomControls(false);
        }

        webView.addJavascriptInterface(new WebJavascriptEvent(),"android");


        //뒤로가기 버튼을 눌렀을 때 클래스로 제어함
        backPressCloseHandler = new BackPressCloseHandler(this);
        //네트워크 체킹을 할 때 쓰임

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
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
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
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("\n" + message + "\n")
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
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

            // For Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
                Log.d("iii","111");
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                filePathCallbackNormal = uploadMsg;
                Log.d("iii","111");
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
            }

            // For Android 4.1+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                Log.d("iii","222");
                openFileChooser(uploadMsg, acceptType);
            }


            // For Android 5.0+
            public boolean onShowFileChooser(
                    WebView webView, ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {
                Log.d("iii","333");
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
                //loadingProgress.setVisibility(View.VISIBLE);
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



                }else if(0<url.lastIndexOf(".php")){
                    Log.d("lastIndexOf",url.lastIndexOf("login.php")+"");
                    if(url.lastIndexOf("login.php")<0&&url.lastIndexOf("logout.php")<0&&url.lastIndexOf("register_form.php")<0&&url.lastIndexOf("register_result.php")<0) {
                        Log.d("url", url);
                        Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        //loadingProgress.setVisibility(View.GONE);
                        return true;
                    }else{
                        return false;
                    }
                }else if(url.startsWith("https://smartstore")){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }
            //페이지 로딩이 다 끝났을 때
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //webLayout.setRefreshing(false);
                //loadingProgress.setVisibility(View.GONE);
                Log.d("url",url);
                Log.d("ss_mb_id", Common.getPref(getApplicationContext(),"ss_mb_id",""));
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                //로그인할 때
                if(url.startsWith(getString(R.string.domain)+"bbs/login.php")){
                    Log.d("token",Common.TOKEN);
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

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    //쿠키 값 삭제
    public void deleteCookie(){
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(webView.getContext());
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.removeAllCookie();
        cookieSyncManager.sync();
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
        webView.loadUrl("javascript:roomDisconnect()");
        execBoolean=false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
    //뒤로가기를 눌렀을 때
    public void onBackPressed() {
        //super.onBackPressed();
        //웹뷰에서 히스토리가 남아있으면 뒤로가기 함
        if(!isIndex) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else if (webView.canGoBack() == false) {
                backPressCloseHandler.onBackPressed();
            }
        }else{
            backPressCloseHandler.onBackPressed();
        }
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
        public void screenShot(){
            View rootView=getWindow().getDecorView();
            File screenShot=Common.ScreenShot(rootView);
            Log.d("screenshot","screenshot");
            if(screenShot!=null){
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.fromFile(screenShot)));
                Common.sharedPhoto(MainActivity.this);
            }
        }
        @JavascriptInterface
        public void back(){
            if(!webView.canGoBack()) {
                finish();
            }
        }
        @JavascriptInterface
        public void customDialog(){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
                    params.width = WindowManager.LayoutParams.MATCH_PARENT;
                    customDialog.getWindow().setAttributes((WindowManager.LayoutParams)params);
                    customDialog.show();


                }
            });

        }
        @JavascriptInterface
        public void answerVibrate(){
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    long[] pattern={100,0,100,0};
                    vibrator.vibrate(pattern,0);
                }
            });
        }
    }
    //갤러리 온클릭리스너 만들기
    View.OnClickListener galleryOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(MainActivity.this, AlbumSelectActivity.class);
//set limit on number of images that can be selected, default is 10
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 1);
            startActivityForResult(intent, Constants.REQUEST_CODE);





            customDialog.dismiss();
        }
    };
    //사진찍기 온클릭 리스너 만들기
    View.OnClickListener photoOnClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "mb_photo.jpg"));
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,cameraImageUri);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CODE);
                customDialog.dismiss();
            }
        }
    };
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("resultCode",resultCode+"");
        if(resultCode==RESULT_OK) {
            if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
                if (filePathCallbackNormal == null) return;
                Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                Log.d("filePath", mCapturedImageURI.toString());
                CropImage.activity(mCapturedImageURI)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setCropShape(CropImageView.CropShape.OVAL)
                        .setFixAspectRatio(true)
                        .start(this);

                filePathCallbackNormal = null;

            } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
                Uri[] result = new Uri[0];
                Log.d("filePath1", mCapturedImageURI.toString());
                Toast.makeText(mContext, mCapturedImageURI.toString(), Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


                    if (resultCode == RESULT_OK) {
                        result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    }
                    filePathCallbackLollipop.onReceiveValue(result);
                    CropImage.activity(result[0])
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setCropShape(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? CropImageView.CropShape.RECTANGLE : CropImageView.CropShape.OVAL)
                            .setFixAspectRatio(true)
                            .start(this);


                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();
                    Log.d("image-uri", resultUri.toString());
                    serverPost(resultUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
            } else if (requestCode == 1200) {
                Toast.makeText(mContext, "새로고침", Toast.LENGTH_SHORT).show();
                webView.reload();

            } else {

                Log.d("aaa", "111");

                return;
            }
        }else{
            try {
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                }
            }catch (Exception e){

            }
        }
    }

    public void serverPost(Uri imageUri) {
        //httpok 로그 보기
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        //클라이언트 설정
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .build();
        //레트로핏 설정
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.domain))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //파라미터 넘길 값 설정
        Map<String, RequestBody> map = new HashMap<>();
        map.put("division", StaticRetrofit.toRequestBody("photo_save"));
        map.put("mb_id", StaticRetrofit.toRequestBody(Common.getPref(this, "ss_mb_id", "")));


        File file = new File(imageUri.getPath()); // 이미지파일주소는 확인됨
        RequestBody fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        map.put("mb_photo\"; filename=\"mb_photo.png", fileBody);


        //레트로핏 서비스 실행하기
        RetrofitService retrofitService = retrofit.create(RetrofitService.class);
        //데이터 불러오기
        Call<ServerPost> call = retrofitService.FileUpload(map);
        call.enqueue(new Callback<ServerPost>() {
            @Override
            public void onResponse(Call<ServerPost> call, Response<ServerPost> response) {
                if (response.isSuccessful()) {
                    ServerPost repo = response.body();
                    Log.d("response", response + "");
                    if (Boolean.parseBoolean(repo.getSuccess()) == false) {
                    } else {

                        String mb_photo_url = repo.getMb_photo_url().replace("\\", "");
                        webView.loadUrl("javascript:photoChange('" + mb_photo_url + "')");
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<ServerPost> call, Throwable t) {

            }
        });
    }


}