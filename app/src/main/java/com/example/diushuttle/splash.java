package com.example.diushuttle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class splash extends AppCompatActivity {
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        image = (ImageView) findViewById(R.id.logo );
        Animation myanimation = AnimationUtils.loadAnimation(this, R.anim.mytranstion );
        image.startAnimation( myanimation );
        final Intent intent = new Intent( this , MainActivity.class );
        Thread timer = new Thread() {
            public  void  run()
            {
                try {
                    sleep(3300);
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }
                finally {
                    startActivity( intent );
                    finish();
                }
            }
        };
        timer.start();
    }
}
