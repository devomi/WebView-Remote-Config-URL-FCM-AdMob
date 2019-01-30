package xposed73.com.simplifywebview.Activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.net.URL;

import xposed73.com.simplifywebview.Config.Config;
import xposed73.com.simplifywebview.R;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mRegisterBroadcastReceiver;
    private FirebaseRemoteConfig fbRemoteConfig;
    private AdView mAdView;
    String ERROR_PAGE = "file:///android_asset/error.html";

    private static String TAG = MainActivity.class.getSimpleName();
    WebView myWebView;
    ProgressBar progressBar;
    boolean loadingFinished = true;
    boolean redirect = false;
    private ProgressDialog dialog;
    Button backButton, fwdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fbRemoteConfig = FirebaseRemoteConfig.getInstance();
        fbRemoteConfig.setDefaults(R.xml.config);
        fbRemoteConfig.activateFetched();
        fbRemoteConfig.fetch(0);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        backButton = (Button) findViewById(R.id.action_back);
        fwdButton = (Button) findViewById(R.id.action_forward);

        mRegisterBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(Config.STR_PUSH))
                {
                    String message = intent.getStringExtra(Config.STR_MESSAGE);
                    showNotification("Magical Methods",message);
                }
            }
        };



        setupWebView();
        loadHomePage();
        mAdView.loadAd(adRequest);
        onNewIntent(getIntent());


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void setupWebView() {

        myWebView = (WebView) findViewById(R.id.webview);
        final WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.getSettings().setAppCacheEnabled(false);
        myWebView.clearCache(true);
        myWebView.getSettings().setLoadWithOverviewMode(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.getSettings().setBuiltInZoomControls(true);
        myWebView.getSettings().setDisplayZoomControls(false);


        myWebView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                myWebView.loadUrl(urlNewString);
                return true;

            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadingFinished = false;
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!redirect) {
                    loadingFinished = true;
                }

                if(dialog.isShowing())
                    dialog.dismiss();

                if (loadingFinished && !redirect) {
                    progressBar.setVisibility(View.INVISIBLE);

                } else {
                    redirect = false;
                }

            }
        });
    }

    private void setButtonStatus() {
        if (myWebView.canGoForward()) {
            fwdButton.setEnabled(true);
        } else {
            fwdButton.setEnabled(false);
        }

        if (myWebView.canGoBack()) {
            backButton.setEnabled(true);
        } else {
            backButton.setEnabled(false);
        }
    }

    private void loadHomePage() {
        if (isConnected()) {
            Log.i(TAG, "Connected");
            navigateToURL(fbRemoteConfig.getString("webview_url"));



        } else {

            Toast toast = Toast.makeText(getApplicationContext(), "No Internet Access", Toast.LENGTH_SHORT);
            toast.show();
            navigateToURL(ERROR_PAGE);

        }
    }

    private void navigateToURL(String url) {

        myWebView.loadUrl(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {

            case R.id.action_refresh:
                myWebView.reload();
                break;

            case R.id.action_back:
                if (myWebView.canGoBack()) {
                    myWebView.goBack();
                }
                break;

            case R.id.action_forward:
                if (myWebView.canGoForward()) {
                    myWebView.goForward();
                }
                break;

            case R.id.action_home:
                loadHomePage();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean isConnected() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }


    public void updateConfig(View view) {
        fbRemoteConfig.fetch(0).addOnCompleteListener(this,
                new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            fbRemoteConfig.activateFetched();
                            loadHomePage();
                        } else {

                            Toast toast = Toast.makeText(getApplicationContext(), "Configuration Error !", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
    }


    //for notifications

    @Override
    protected void onNewIntent(Intent intent) {
        dialog = new ProgressDialog(this);
        if(intent.getStringExtra(Config.STR_KEY)!= null){
            dialog.show();
            dialog.setMessage("Loading,Please wait...");
            myWebView.loadUrl(intent.getStringExtra(Config.STR_KEY));
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(getBaseContext(),MainActivity.class);
        intent.putExtra(Config.STR_KEY,message);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent);
        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,builder.build());

    }


    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegisterBroadcastReceiver);
        super.onPause();
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegisterBroadcastReceiver,new IntentFilter("registration complete"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegisterBroadcastReceiver,new IntentFilter(Config.STR_PUSH));

    }



}
