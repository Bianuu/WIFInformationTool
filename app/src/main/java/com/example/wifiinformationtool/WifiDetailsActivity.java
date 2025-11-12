package com.example.wifiinformationtool;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Clasa WifiDetailsActivity afiseaza detalii despre conexiunea Wi‑Fi curenta
 * si o lista (golita) de dispozitive conectate.
 */
public class WifiDetailsActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 101; // cod cerere permisiuni locatie

    // Componente UI pentru afisare detalii Wi‑Fi
    private TextView ssidTextView;
    private TextView ipTextView;
    private TextView macTextView;
    private TextView signalStrengthTextView;
    private TextView securityTextView;
    private TextView frequencyTextView;
    private TextView channelTextView;
    private ListView connectedDevicesListView;

    private WifiManager wifiManager; // manager Wi‑Fi pentru obtinere informatii si scan

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_details);

        // Legare view‑uri din layout
        ssidTextView = findViewById(R.id.ssidTextView2);
        ipTextView = findViewById(R.id.ipAddressTextView2);
        macTextView = findViewById(R.id.macAddressTextView2);
        signalStrengthTextView = findViewById(R.id.signalStrengthTextView2);
        securityTextView = findViewById(R.id.securityTypeTextView2);
        frequencyTextView = findViewById(R.id.frequencyTextView2);
        channelTextView = findViewById(R.id.channelTextView2);
        connectedDevicesListView = findViewById(R.id.connectedDevicesListView2);

        // Initializare WifiManager pentru acces la informatii si scanari
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Verificare permisiuni si, daca sunt OK, afisare detalii
        if (checkPermissions()) {
            displayWifiDetails();
            displayConnectedDevices();
        } else {
            requestPermissions();
        }
    }

    /**
     * Verifica daca permisiunea ACCESS_FINE_LOCATION este acordata.
     */
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Cere permisiunea de locatie necesara pentru scanare Wi‑Fi.
     */
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Daca permisiunea a fost acordata, afisam detaliile
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayWifiDetails();
                displayConnectedDevices();
            } else {
                // Daca permisiunea e refuzata, afisam mesaj si inchidem activitatea
                Toast.makeText(this, "Permisiune refuzata!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /**
     * Afiseaza detaliile conexiunii Wi‑Fi curente.
     */
    private void displayWifiDetails() {
        // Daca serviciile de locatie nu sunt activate, cerem utilizatorului sa le activeze
        if (!isLocationEnabled()) {
            Toast.makeText(this,
                    "Va rugam sa activati serviciile de localizare!",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        // Preluam informatiile conexiunii curente
        WifiInfo currentConnection = wifiManager.getConnectionInfo();
        if (currentConnection != null) {
            String ssid = currentConnection.getSSID();
            String ipAddress = Formatter.formatIpAddress(currentConnection.getIpAddress());
            String macAddress = currentConnection.getBSSID();
            int signalStrength = currentConnection.getRssi();
            int frequency = currentConnection.getFrequency();
            int channel = frequencyToChannel(frequency);
            String securityType = getWifiSecurity(ssid);

            // Setam text in TextView‑uri
            ssidTextView.setText("SSID: " + ssid);
            ipTextView.setText("IP Address: " + ipAddress);
            macTextView.setText("MAC Address: " + macAddress);
            signalStrengthTextView.setText("Signal Strength: " + signalStrength + " dBm");
            securityTextView.setText("Security Type: " + securityType);
            frequencyTextView.setText("Frequency: " + frequency + " MHz");
            channelTextView.setText("Channel: " + channel);
        }
    }

    /**
     * Conversie frecventa Wi‑Fi (MHz) in canal.
     */
    private int frequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2484) {
            return (frequency - 2412) / 5 + 1;
        } else if (frequency >= 5170 && frequency <= 5825) {
            return (frequency - 5170) / 5 + 34;
        }
        return -1; // canal necunoscut
    }

    /**
     * Determina tipul de securitate al retelei Wi‑Fi dupa SSID.
     */
    private String getWifiSecurity(String ssid) {
        // Necesita permisiune ACCESS_FINE_LOCATION pentru scan result
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return "No permission";
        }

        // Pornim un scan pentru a obtine capabilitatile retelei
        wifiManager.startScan();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        String normalizedSSID = ssid.replace("\"", "");

        // Cautam reteaua cu SSID-ul curent si extragem capabilitatile
        for (ScanResult scanResult : scanResults) {
            if (scanResult.SSID.equals(normalizedSSID)) {
                return getSecurityType(scanResult.capabilities);
            }
        }

        return "No Security (SSID not found)";
    }

    /**
     * Returneaza tipul de securitate din stringul capabilities.
     */
    private String getSecurityType(String capabilities) {
        if (capabilities.contains("WPA3")) {
            return "WPA3";
        } else if (capabilities.contains("WPA2")) {
            return "WPA2";
        } else if (capabilities.contains("WEP")) {
            return "WEP";
        } else if (capabilities.contains("EAP")) {
            return "EAP (Enterprise)";
        }
        return "No Security";
    }

    /**
     * Afiseaza lista de dispozitive conectate (in acest moment goala).
     */
    private void displayConnectedDevices() {
        List<String> connectedDevices = getConnectedDevices();
        ArrayAdapter<String> devicesAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                connectedDevices
        );
        connectedDevicesListView.setAdapter(devicesAdapter);
    }

    /**
     * Returneaza o lista de adrese/mac-uri ale dispozitivelor conectate.
     * In prezent nu implementata.
     */
    private List<String> getConnectedDevices() {
        List<String> connectedDevices = new ArrayList<>();
        // TODO: adauga logica pentru obtinerea dispozitivelor conectate
        return connectedDevices;
    }

    /**
     * Verifica daca serviciile de locatie sunt activate (sau permisiunea acordata).
     */
    private boolean isLocationEnabled() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
