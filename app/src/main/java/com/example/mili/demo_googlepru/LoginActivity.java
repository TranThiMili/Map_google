package com.example.mili.demo_googlepru;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.VideoView;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private VideoView videoView;;
    private Switch iSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        videoView = findViewById(R.id.videoView);
        iSwitch = findViewById(R.id.iSwitch);
        setBackGroundVideo("https://storage.googleapis.com/coverr-main/mp4/Mt_Baker.mp4");
        loadLocale();
        changeLanguage();

    }

    private void setBackGroundVideo(String url) {
        try {
            videoView.setVideoURI(Uri.parse(url));
            videoView.start();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            videoView.setBackgroundDrawable(null);
                        }
                    },100);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void changeLanguage(){
        if(iSwitch.isChecked()){
            setLocale("en");
            recreate();
        }else {
            setLocale("vi");
            recreate();
        }

    }

    public void setLocale(String lang){
        try {
            if(lang!=null && !lang.equals("")){
                Locale locale = new Locale(lang);
                Locale.setDefault(locale);
                Configuration configuration = new Configuration();
                configuration.locale = locale;
                getResources().updateConfiguration(configuration,getResources().getDisplayMetrics());
                SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
                editor.putString("My_lang",lang);
                editor.apply();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadLocale(){
        try{
            SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
            String lang = preferences.getString("My_lang", "");
            setLocale(lang);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
