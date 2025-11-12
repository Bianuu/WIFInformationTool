package com.example.wifiinformationtool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Clasa SpeedTestActivity masoara viteza de download prin descarcarea unui fisier test.
 */
public class SpeedTestActivity extends AppCompatActivity {
    private static final String TAG = "SpeedTestActivity"; // Tag pentru log-uri
    private TextView tvDownloadSpeed; // TextView pentru afisarea vitezei
    private Button btnTestSpeed;      // Buton pentru initierea testului

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_test);

        // Legare view-uri
        tvDownloadSpeed = findViewById(R.id.tvDownloadSpeed);
        btnTestSpeed = findViewById(R.id.btnTestSpeed);

        // Setare listener pentru butonul de testare viteza
        btnTestSpeed.setOnClickListener(view -> testDownloadSpeed());
    }

    /**
     * Metoda care ruleaza testul de download pe un fir separat.
     */
    private void testDownloadSpeed() {
        // Afisam mesaj ca testul este in desfasurare
        tvDownloadSpeed.setText("Testing...");

        // Pornim un thread pentru descarcarea test
        new Thread(() -> {
            try {
                // URL-ul fisierului de 1GB pentru test
                String testUrl = "https://nbg1-speed.hetzner.com/1GB.bin";
                URL url = new URL(testUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                // Inregistram timpul de start
                long startTime = System.currentTimeMillis();

                // Deschidem input stream pentru citirea datelor
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytes = 0;

                // Citim datele pana la sfarsit
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    totalBytes += bytesRead;
                }

                // Inregistram timpul de sfarsit
                long endTime = System.currentTimeMillis();
                double timeTakenSeconds = (endTime - startTime) / 1000.0;

                // Calculam viteza in Mbps daca timpul este valid
                if (timeTakenSeconds > 0) {
                    double speedMbps = (totalBytes * 8) / (timeTakenSeconds * 1_000_000.0);
                    Log.d(TAG, "Downloaded " + totalBytes + " bytes in " + timeTakenSeconds +
                            " seconds. Speed: " + speedMbps + " Mbps");

                    // Actualizam UI cu viteza masurata
                    new Handler(Looper.getMainLooper()).post(() ->
                            tvDownloadSpeed.setText(String.format("Viteza download: %.2f Mbps", speedMbps))
                    );
                } else {
                    // Daca timpul este prea mic, afisam un mesaj corespunzator
                    Log.e(TAG, "Download time is too small, retrying.");
                    new Handler(Looper.getMainLooper()).post(() ->
                            tvDownloadSpeed.setText("Download Speed: Too fast to measure")
                    );
                }

                // Inchidem stream-ul si conexiunea
                inputStream.close();
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                // In caz de eroare, actualizam UI cu mesaj de esec
                new Handler(Looper.getMainLooper()).post(() ->
                        tvDownloadSpeed.setText("Speed Test Failed")
                );
            }
        }).start();
    }
}
