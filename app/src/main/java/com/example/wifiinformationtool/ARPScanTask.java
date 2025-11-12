package com.example.wifiinformationtool;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Clasa ARPScanTask executa un scan ARP pentru a obtine lista de dispozitive conectate
 * si afiseaza rezultatul in log.
 */
public class ARPScanTask extends AsyncTask<Void, Void, String> {

    /**
     * Ruleaza comanda ARP in background pentru a nu bloca thread-ul UI.
     */
    @Override
    protected String doInBackground(Void... params) {
        try {
            // Executa comanda ARP -a pentru a obtine tabela ARP
            Process process = Runtime.getRuntime().exec("arp -a");
            // Pregateste reader pentru citirea output-ului procesului
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );
            StringBuilder output = new StringBuilder();
            String line;
            // Citeste fiecare linie din output si adauga-o la StringBuilder
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            // Inchide reader-ul dupa citire
            reader.close();
            // Asteapta ca procesul sa se termine
            process.waitFor();
            // Returneaza textul rezultat din scanarea ARP
            return output.toString();
        } catch (Exception e) {
            // Afiseaza detalii despre exceptie si returneaza null
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Dupa terminarea scanarii, afiseaza rezultatul in log sau un mesaj de eroare.
     */
    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            // Logheaza rezultatul scanarii ARP la nivel DEBUG
            Log.d("ARP Scan", result);
        } else {
            // Logheaza un mesaj de eroare la nivel ERROR
            Log.e("ARP Scan", "Scanarea ARP a esuat");
        }
    }
}
