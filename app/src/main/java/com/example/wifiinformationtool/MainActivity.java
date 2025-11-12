package com.example.wifiinformationtool;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Clasa MainActivity gestioneaza ecranul principal al aplicatiei,
 * incluzand animatii pentru header si navigarea intre activitati.
 */
public class MainActivity extends AppCompatActivity {

    // TextView pentru header-ul animat
    private TextView headerText;
    // Text original al header-ului, folosit pentru efectul typewriter
    private String originalText;
    // Handler folosit pentru programarea task-urilor pe firul principal
    private Handler mainHandler = new Handler();
    // Runnable pentru repornirea animatiei initiale periodic
    private Runnable startAnimationRunnable;
    // Runnable pentru schimbarea culorii textului la intervale regulate
    private Runnable colorChangerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializare butoane pentru navigare
        Button wifiInfoButton = findViewById(R.id.wifiInfoButton);
        Button communicationsButton = findViewById(R.id.communicationsButton);
        Button viewCurrentWifiDetailsButton = findViewById(R.id.viewCurrentWifiDetailsButton);
        Button internetSpeedButton = findViewById(R.id.btnSpeedTest);

        // Configurare iconita Instagram cu click listener
        ImageView instagramIcon = findViewById(R.id.instagramIcon);
        instagramIcon.setOnClickListener(v -> {
            // Deschide pagina de Instagram in browser
            String instagramUrl = "https://instagram.com/fabi.cioban";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl));
            startActivity(intent);
        });

        // Initializare TextView pentru header si salvare text original
        headerText = findViewById(R.id.headerText);
        originalText = headerText.getText().toString();

        // Definire Runnable pentru animatia de start repetata
        startAnimationRunnable = new Runnable() {
            @Override
            public void run() {
                startInitialAnimations();
                // Repeta animatia la fiecare 30 de secunde
                mainHandler.postDelayed(this, 30000);
            }
        };

        // Definire Runnable pentru schimbarea de culoare a textului
        colorChangerRunnable = new Runnable() {
            // Lista de culori prin care va parcurge textul
            int[] colors = {
                    Color.WHITE,
                    Color.parseColor("#7C4DFF"),
                    Color.parseColor("#FF5722"),
                    Color.parseColor("#03A9F4"),
                    Color.parseColor("#4CAF50")
            };
            int index = 0;

            @Override
            public void run() {
                // Seteaza culoarea curenta si creste indexul circular
                headerText.setTextColor(colors[index]);
                index = (index + 1) % colors.length;
                // Reapeleaza la fiecare 800ms
                mainHandler.postDelayed(this, 800);
            }
        };

        // Porneste animatia initiala imediat
        startInitialAnimations();
        // Porneste schimbarea de culoare continua
        mainHandler.post(colorChangerRunnable);
        // Programeaza prima repetare a animatiei dupa 30 de secunde
        mainHandler.postDelayed(startAnimationRunnable, 30000);

        // Seteaza navigarea catre alte activitati la click pe butoane
        wifiInfoButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, NetworkDevicesActivity.class))
        );
        communicationsButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CommunicationsActivity.class))
        );
        viewCurrentWifiDetailsButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, WifiDetailsActivity.class))
        );
        internetSpeedButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SpeedTestActivity.class))
        );
    }

    /**
     * Metoda care porneste animatiile initiale pentru header,
     * inclusiv gradient animat, fade-in, bounce, scale si efect typewriter.
     */
    private void startInitialAnimations() {
        // Animatie de fundal: gradient animat
        AnimationDrawable animationDrawable = (AnimationDrawable) headerText.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        // Animatie fade-in pentru text
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(2000);
        fadeIn.setFillAfter(true);
        headerText.startAnimation(fadeIn);

        // Animatie bounce (sarituri) pentru text dupa 1s
        Animation bounce = new TranslateAnimation(0, 0, -30, 0);
        bounce.setDuration(1000);
        bounce.setInterpolator(this, android.R.interpolator.bounce);
        new Handler().postDelayed(() -> headerText.startAnimation(bounce), 1000);

        // Animatie de scalare pentru text dupa 2s
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                0.8f, 1.0f, // scale X de la 80% la 100%
                0.8f, 1.0f, // scale Y de la 80% la 100%
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnimation.setDuration(1000);
        scaleAnimation.setFillAfter(true);
        new Handler().postDelayed(() -> headerText.startAnimation(scaleAnimation), 2000);

        // Efect typewriter (scriere litera cu litera) dupa 3s
        new Handler().postDelayed(() -> {
            headerText.setText("");
            Handler typewriterHandler = new Handler();
            typewriterHandler.post(new Runnable() {
                int charIndex = 0;
                @Override
                public void run() {
                    // Adauga cate o litera din textul original
                    if (charIndex <= originalText.length()) {
                        headerText.setText(originalText.substring(0, charIndex));
                        charIndex++;
                        // Urmatoarea litera dupa 100ms
                        typewriterHandler.postDelayed(this, 100);
                    }
                }
            });
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Indeparteaza callback-urile pentru a preveni memory leaks
        mainHandler.removeCallbacks(startAnimationRunnable);
        mainHandler.removeCallbacks(colorChangerRunnable);
    }
}
