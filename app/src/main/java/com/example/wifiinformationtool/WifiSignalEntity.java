package com.example.wifiinformationtool;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entitate Room pentru stocarea nivelului de semnal Wi‑Fi cu timestamp.
 */
@Entity(tableName = "wifi_signal_table")
public class WifiSignalEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;               // Cheia primara generata automat

    public String ssid;          // SSID-ul retelei Wi‑Fi
    public int signalStrength;   // Nivelul semnalului in dBm
    public long timestamp;       // Timpul inregistrarii semnalului (millis)

    /**
     * Constructor pentru entitatea WifiSignalEntity.
     *
     * @param ssid            SSID-ul retelei
     * @param signalStrength  Nivelul semnalului (dBm)
     * @param timestamp       Timpul inregistrarii (millis)
     */
    public WifiSignalEntity(String ssid, int signalStrength, long timestamp) {
        this.ssid = ssid;
        this.signalStrength = signalStrength;
        this.timestamp = timestamp;
    }
}
