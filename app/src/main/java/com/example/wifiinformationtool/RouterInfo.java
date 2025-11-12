package com.example.wifiinformationtool;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

/**
 * Clasa RouterInfo se ocupa cu obtinerea informatiilor despre un router dat prin IP.
 */
public class RouterInfo {
    public String ip;                 // IP-ul routerului
    public boolean isReachable;       // Indica daca IP-ul este accesibil
    public boolean isCurrentGateway;  // Indica daca IP-ul este gateway-ul curent
    public String ssid;               // SSID-ul retelei Wi-Fi conectate
    public String bssid;              // BSSID-ul routerului
    public int signalStrength;        // Nivelul semnalului Wi-Fi (RSSI)
    public int linkSpeed;             // Viteza de legatura Wi-Fi (Mbps)
    public String serverHeader;       // Headerul "Server" din raspuns HTTP

    /**
     * Construieste un string cu toate informatiile despre router.
     */
    @Override
    public String toString() {
        return "IP: " + ip +
                "\nReachable: " + isReachable +
                "\nIs Gateway: " + isCurrentGateway +
                "\nSSID: " + ssid +
                "\nBSSID: " + bssid +
                "\nSignal: " + signalStrength + " dBm" +
                "\nLink Speed: " + linkSpeed + " Mbps" +
                "\nHTTP Server Header: " + serverHeader;
    }

    /**
     * Metoda principala care returneaza informatii despre routerul cu IP-ul dat.
     * @param ip Adresa IP care se verifica
     * @param context Contextul aplicatiei pentru acces la servicii
     * @return String cu detalii despre router sau mesaj de eroare
     */
    public String getRouterInfo(String ip, Context context) {
        RouterInfo info = new RouterInfo();
        info.ip = ip;

        // Verifica daca IP-ul este accesibil prin ping (timeout 300ms)
        try {
            InetAddress address = InetAddress.getByName(ip);
            info.isReachable = address.isReachable(300);
        } catch (Exception e) {
            info.isReachable = false;
        }

        // Verifica daca IP-ul este gateway-ul curent configurat
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifiManager.getDhcpInfo();
            String gatewayIp = String.format("%d.%d.%d.%d",
                    (dhcp.gateway & 0xff),
                    (dhcp.gateway >> 8 & 0xff),
                    (dhcp.gateway >> 16 & 0xff),
                    (dhcp.gateway >> 24 & 0xff));
            info.isCurrentGateway = ip.equals(gatewayIp);
        } catch (Exception e) {
            info.isCurrentGateway = false;
        }

        // Daca este gateway-ul curent, preia informatii Wi-Fi
        if (info.isCurrentGateway) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            info.ssid = wifiInfo.getSSID();
            info.bssid = wifiInfo.getBSSID();
            info.signalStrength = wifiInfo.getRssi();
            info.linkSpeed = wifiInfo.getLinkSpeed();
        }

        // Incearca sa preia headerul HTTP "Server" de pe router
        try {
            URL url = new URL("http://" + ip);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(300);
            conn.setReadTimeout(300);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            info.serverHeader = conn.getHeaderField("Server");

            // Daca se primeste raspuns valid sau header contine "router", returneaza informatiile
            if (code == 200 || code == 401 ||
                    (info.serverHeader != null && info.serverHeader.toLowerCase().contains("router"))) {
                return info.toString();
            }
        } catch (Exception e) {
            // Nu s-a putut conecta la IP
            Log.e("RouterScan", "Cannot connect to IP: " + ip);
        }

        // Daca nu s-a gasit router sau nu s-a putut conecta
        return "Unable to connect to IP: " + ip;
    }
}
