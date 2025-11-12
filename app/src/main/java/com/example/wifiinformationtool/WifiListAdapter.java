package com.example.wifiinformationtool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter custom pentru afisarea listei de retele Wi‑Fi.
 * Include posibilitatea de a extinde detalii si un grafic cu istoricul semnalului.
 */
public class WifiListAdapter extends ArrayAdapter<String> {

    // Executor pentru operatiuni in background (ex. incarcare istoric semnal)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private Context context;                     // Contextul activity-ului
    private List<String> wifiNames;              // Lista de SSID-uri
    private List<Integer> wifiSignalImages;      // Lista de resurse imagine pentru semnal
    private List<String> wifiSignalStrengths;    // Lista de valori semnal in dBm
    private List<String> wifiSecurityTypes;      // Lista de tipuri de securitate CAPABILITIES
    private List<String> wifiIpAddresses;        // Lista de frecvente (MHz)
    private List<String> wifiMacAddresses;       // Lista de adrese MAC (BSSID)
    private List<String> wifiIpBand;             // Lista de benzi (2.4/5 GHz)
    private List<Boolean> expandedStates;        // Stare extins/colapsat pentru fiecare element

    /**
     * Constructor care primeste toate listele necesare pentru afisare.
     */
    public WifiListAdapter(Context context,
                           List<String> wifiNames,
                           List<Integer> wifiSignalImages,
                           List<String> wifiSignalStrengths,
                           List<String> wifiSecurityTypes,
                           List<String> wifiIpAddresses,
                           List<String> wifiMacAddresses,
                           List<String> wifiIpBand) {
        super(context, 0, wifiNames);
        this.context = context;
        this.wifiNames = wifiNames;
        this.wifiSignalImages = wifiSignalImages;
        this.wifiSignalStrengths = wifiSignalStrengths;
        this.wifiSecurityTypes = wifiSecurityTypes;
        this.wifiIpAddresses = wifiIpAddresses;
        this.wifiMacAddresses = wifiMacAddresses;
        this.wifiIpBand = wifiIpBand;
        // Initializam starea de extindere cu false pentru toate elementele
        this.expandedStates = new ArrayList<>(Collections.nCopies(wifiNames.size(), false));
    }

    /**
     * Interpreteaza stringul de capabilities intr-un tip de securitate prietenos.
     */
    public static String interpretWifiSecurity(String securityCapabilities) {
        Map<String, String> securityMap = new HashMap<>();
        securityMap.put("WPA2-PSK", "WPA2");
        securityMap.put("WPA3-PSK", "WPA3");
        securityMap.put("WEP", "WEP");
        securityMap.put("WPA2-Enterprise", "WPA2 Enterprise");
        securityMap.put("WPA3-Enterprise", "WPA3 Enterprise");
        securityMap.put("WPA-PSK", "WPA Personal (PSK)");
        securityMap.put("EAP", "EAP Authentication (Enterprise)");

        // Cautam prima potrivire in capabilities
        for (Map.Entry<String, String> entry : securityMap.entrySet()) {
            if (securityCapabilities.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "No security or unknown type";
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Inflam view-ul daca nu exista deja
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.wifi_item, parent, false);
        }

        // Legare componente UI
        TextView wifiNameTextView         = convertView.findViewById(R.id.wifiNameTextView);
        TextView wifiSignalStrengthValue  = convertView.findViewById(R.id.wifiSignalStrengthValue);
        ImageView wifiSignalImageView     = convertView.findViewById(R.id.wifiSignalImageView);
        LinearLayout wifiDetailsLayout    = convertView.findViewById(R.id.wifiDetailsLayout);
        TextView wifiSsidDetail           = convertView.findViewById(R.id.wifiSsidDetail);
        TextView wifiSecurityTypeDetail   = convertView.findViewById(R.id.wifiSecurityTypeDetail);
        TextView wifiMacAddressDetail     = convertView.findViewById(R.id.wifiMacAddressDetail);
        TextView wifiBand                 = convertView.findViewById(R.id.wifiBandDetail);
        LineChart signalStrengthChart     = convertView.findViewById(R.id.signalStrengthChart);

        // Prenume SSID si validare
        String wifiName = wifiNames.get(position);
        if (wifiName == null || wifiName.isEmpty()) {
            wifiName = "<Hidden Network>";
        }
        wifiNameTextView.setText(wifiName);

        // Afisam semnalul in dBm
        String signalStrength = wifiSignalStrengths.get(position);
        wifiSignalStrengthValue.setText(signalStrength + " dBm");
        int strengthValue = Integer.parseInt(signalStrength);
        wifiSignalImageView.setImageResource(getWifiStrengthImage(strengthValue));

        // Afisam detalii in layout-ul extins
        wifiSsidDetail.setText("SSID: " + wifiNames.get(position));
        wifiSecurityTypeDetail.setText("Security: " +
                interpretWifiSecurity(wifiSecurityTypes.get(position)));
        wifiMacAddressDetail.setText("MAC Address: " + wifiMacAddresses.get(position));
        wifiBand.setText("Band: " + wifiIpBand.get(position));

        // Setam click listener pentru toggling detalii si chart
        convertView.setOnClickListener(v -> {
            if ("<Hidden Network>".equals(wifiNames.get(position))) {
                Toast.makeText(context,
                        "Retea ascunsa. Nu se pot vedea detalii.",
                        Toast.LENGTH_SHORT).show();
            } else {
                boolean isExpanded = expandedStates.get(position);
                expandedStates.set(position, !isExpanded);
                // Aratam sau ascundem detaliile si graficul
                wifiDetailsLayout.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                signalStrengthChart.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
                // Daca s-a extins, incarcam datele in grafic
                if (!isExpanded) {
                    loadSignalHistoryChart(signalStrengthChart, wifiNames.get(position));
                }
            }
        });

        // Setam initial vizibilitatea in functie de stare
        wifiDetailsLayout.setVisibility(expandedStates.get(position) ? View.VISIBLE : View.GONE);
        signalStrengthChart.setVisibility(expandedStates.get(position) ? View.VISIBLE : View.GONE);

        return convertView;
    }

    /**
     * Incarca istoricul semnalului din baza de date si actualizeaza LineChart.
     */
    private void loadSignalHistoryChart(LineChart chart, String ssid) {
        executorService.execute(() -> {
            // Preluam lista de entitati din DB
            List<WifiSignalEntity> history = WifiSignalDatabase.getDatabase(context)
                    .wifiSignalDao()
                    .getSignalHistory(ssid);

            // Pregatim punctele pentru grafic
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < history.size(); i++) {
                entries.add(new Entry(i, history.get(i).signalStrength));
            }

            // Construim dataset-ul si linia de date
            LineDataSet dataSet = new LineDataSet(entries, "Signal Strength (dBm)");
            dataSet.setColor(ContextCompat.getColor(context, R.color.purple_500));
            dataSet.setValueTextColor(ContextCompat.getColor(context, R.color.black));
            LineData lineData = new LineData(dataSet);

            // Actualizam graficul pe UI thread
            chart.post(() -> {
                chart.setData(lineData);
                Description description = new Description();
                description.setText("Signal Strength Over Time");
                chart.setDescription(description);
                chart.invalidate();
            });
        });
    }

    /**
     * Returneaza resursa imaginii potrivita pentru valoarea semnalului.
     */
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
}
