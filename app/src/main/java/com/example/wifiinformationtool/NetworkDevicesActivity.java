package com.example.wifiinformationtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Clasa NetworkDevicesActivity se ocupa cu scanarea retelelor Wi‑Fi disponibile
 * si afisarea lor intr‑un ListView, cu actualizare periodica si salvare in baza de date.
 */
public class NetworkDevicesActivity extends AppCompatActivity {
    // Cod cerere pentru permisiuni
    private static final int PERMISSION_REQUEST_CODE = 100;
    // Interval de scanare periodica (10 secunde)
    private static final int SCAN_INTERVAL_MS = 10000;
    // Marimea batch‑ului folosit la scanarea IP‑urilor (daca este decommentat)
    private static final int BATCH_SIZE = 100000;

    // Componente UI
    private ListView deviceListView;
    private ProgressBar loadingSpinner;
    private TextView noDataMessage;
    // Handler pentru scheduling pe thread‑ul principal
    private Handler handler = new Handler();
    // Flag care indica daca scanarea e in desfasurare
    private boolean isScanning = false;
    // Runnable care executa periodic scanNetwork()
    private Runnable scanRunnable = new Runnable() {
        @Override
        public void run() {
            if (isScanning) {
                scanNetwork();
                // Reapeleaza dupa intervalul definit
                handler.postDelayed(this, SCAN_INTERVAL_MS);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_devices);

        // Legare view‑uri
        deviceListView = findViewById(R.id.deviceListView);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        noDataMessage = findViewById(R.id.noDataMessage);

        // Buton pentru intoarcere la ecranul anterior
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Buton pentru start/stop scanare
        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> {
            if (!isScanning) {
                // Daca nu scanam deja, verificam permisiunile
                if (checkPermissions()) {
                    isScanning = true;
                    scanButton.setText("Stop Scanning");
                    startPeriodicScan();
                } else {
                    requestPermissions();
                }
            } else {
                // Daca scanam, oprim scanarea
                isScanning = false;
                scanButton.setText("Start Scanning");
                stopPeriodicScan();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Oprire scanare la inchiderea activitatii pentru a evita leak‑uri
        stopPeriodicScan();
    }

    /** Porneste scanarea periodica prin handler.post */
    private void startPeriodicScan() {
        handler.post(scanRunnable);
    }

    /** Opreste scanarea periodica prin handler.removeCallbacks */
    private void stopPeriodicScan() {
        handler.removeCallbacks(scanRunnable);
    }

    /** Verifica daca avem permisiunile necesare pentru locatie si wifi */
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    /** Cere permisiunile necesare prin ActivityCompat */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE
                },
                PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Daca permisiunea de locatie a fost acordata, pornim scanarea
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanNetwork();
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Scanare pentru toate adresele IPv4 (metoda comentata by default) */
    public void scanAllIPv4Addresses() {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    processBatch(i, j, k);
                }
            }
        }
    }

    /** Proceseaza un batch de adrese IP generate din parametri */
    private void processBatch(int i, int j, int k) {
        for (int l = 0; l < 256; l++) {
            String ip = String.format("%d.%d.%d.%d", i, j, k, l);
            processIp(ip);
            // Daca ajungem la dimensiunea batch-ului, afisam un log si continuam
            if (l % BATCH_SIZE == 0) {
                System.out.println("Processed IPs for batch: " + (i * 256 * 256 + j * 256 + k));
            }
        }
    }

    /** Exemplu de procesare a unei adrese IP (router detection) */
    private void processIp(String ip) {
        RouterInfo ri = new RouterInfo();
        System.out.println("Checking IP: " + ri.getRouterInfo(ip, this));
    }

    /** Metoda principala de scanare a retelei Wi-Fi */
    private void scanNetwork() {
        // Verificam daca serviciile de locatie sunt activate
        if (!isLocationEnabled()) {
            Toast.makeText(this,
                    "Please enable location services for Wi-Fi scanning",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        // DACA VREAU SA SCANEZ IPV4 FULL, DECOMENTEZ urmatoarea linie
        // scanAllIPv4Addresses();

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            loadingSpinner.setVisibility(View.VISIBLE);
            noDataMessage.setVisibility(View.GONE);

            // Verificam din nou permisiunea locatiei
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            List<ScanResult> wifiList = wifiManager.getScanResults();
            if (!wifiList.isEmpty()) {
                displayWifiList(wifiList);
                saveWifiSignalStrength(wifiList);
            } else {
                Toast.makeText(this, "No Wi-Fi networks found!", Toast.LENGTH_SHORT).show();
            }

            // Pornim un nou scan (wifiManager poate throttla task-urile)
            boolean scanSuccess = wifiManager.startScan();
            if (!scanSuccess) {
                Toast.makeText(this,
                        "Wi-Fi scan throttled! Using last known results.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Salveaza nivelul semnalului in baza de date, cu timestamp curent */
    private void saveWifiSignalStrength(List<ScanResult> wifiList) {
        WifiSignalDatabase db = WifiSignalDatabase.getDatabase(this);
        new Thread(() -> {
            WifiSignalDao dao = db.wifiSignalDao();
            long timestamp = System.currentTimeMillis();
            for (ScanResult scanResult : wifiList) {
                WifiSignalEntity signal = new WifiSignalEntity(
                        scanResult.SSID,
                        scanResult.level,
                        timestamp
                );
                dao.insert(signal);
            }
        }).start();
    }

    /** Afiseaza lista de retele Wi‑Fi in ListView cu ajutorul unui adapter custom */
    private void displayWifiList(List<ScanResult> wifiList) {
        // Listelor de date care vor fi transmise catre adapter
        ArrayList<String> wifiNames = new ArrayList<>();
        ArrayList<Integer> wifiSignalImages = new ArrayList<>();
        ArrayList<String> wifiSignalStrengths = new ArrayList<>();
        ArrayList<String> wifiSecurityTypes = new ArrayList<>();
        ArrayList<String> wifiIpAddresses = new ArrayList<>();
        ArrayList<String> wifiMacAddresses = new ArrayList<>();
        ArrayList<String> wifiIpBand = new ArrayList<>();

        // Sortam retelele descrescator dupa puterea semnalului (level)
        Collections.sort(wifiList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult o1, ScanResult o2) {
                return Integer.compare(o2.level, o1.level);
            }
        });

        // Parcurgem fiecare rezultat scan
        for (ScanResult scanResult : wifiList) {
            String wifiName = (scanResult.SSID == null || scanResult.SSID.isEmpty())
                    ? "<Hidden Network>"
                    : scanResult.SSID;
            // Eliminam caractere speciale din SSID
            wifiName = wifiName.replaceAll("[^\\x20-\\x7E]", "");
            wifiNames.add(wifiName);

            int signalStrength = scanResult.level;
            wifiSignalStrengths.add(String.valueOf(signalStrength));
            wifiSignalImages.add(getWifiStrengthImage(signalStrength));

            // Determinam tipul de securitate
            String securityType = scanResult.capabilities;
            if (securityType.contains("EAP")) {
                securityType += " (Enterprise)";
            }
            wifiSecurityTypes.add(securityType);

            // Determinam banda si frecventa
            String frequency = (scanResult.frequency == 2412) ? "2.4 GHz"
                    : (scanResult.frequency == 5180) ? "5 GHz"
                    : "Other Frequency";
            String band = (scanResult.frequency >= 2412 && scanResult.frequency <= 2472)
                    ? "2.4 GHz"
                    : (scanResult.frequency >= 5180 && scanResult.frequency <= 5825)
                    ? "5 GHz"
                    : "Other Frequency";
            wifiIpBand.add(band);
            wifiIpAddresses.add(frequency);

            // Adaugam BSSID (MAC)
            wifiMacAddresses.add(scanResult.BSSID);
        }

        // Daca ListView e valid, setam adapterul custom
        if (deviceListView != null) {
            WifiListAdapter adapter = new WifiListAdapter(
                    this,
                    wifiNames,
                    wifiSignalImages,
                    wifiSignalStrengths,
                    wifiSecurityTypes,
                    wifiIpAddresses,
                    wifiMacAddresses,
                    wifiIpBand
            );
            deviceListView.setAdapter(adapter);
        }

        // Ascundem spinnerul dupa incarcare date
        loadingSpinner.setVisibility(View.GONE);
    }

    /** Returneaza resource‑ul imaginii pe baza nivelului semnalului */
    private int getWifiStrengthImage(int signalStrength) {
        if (signalStrength > -50) {
            return R.drawable.wifi_high_strength;
        } else if (signalStrength > -70) {
            return R.drawable.wifi_medium_strength;
        } else if (signalStrength > -90) {
            return R.drawable.wifi_low_strength;
        } else {
            return R.drawable.wifi_no_strength;
        }
    }

    /** Verifica daca serviciile de locatie (GPS) sunt activate */
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null
                && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
