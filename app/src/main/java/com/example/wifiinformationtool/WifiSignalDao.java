package com.example.wifiinformationtool;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Interfata DAO pentru operatii pe entitatea WifiSignalEntity in baza de date Room.
 */
@Dao
public interface WifiSignalDao {

    /**
     * Insereaza un obiect WifiSignalEntity in tabelul wifi_signal_table.
     * @param signal obiectul cu datele semnalului Wi‑Fi
     */
    @Insert
    void insert(WifiSignalEntity signal);

    /**
     * Returneaza istoricul ultimelor 50 inregistrari de semnal pentru SSID‑ul specificat,
     * sortate descrescator dupa timestamp.
     * @param ssid SSID‑ul retelei pentru care se interogheaza istoricul semnalului
     * @return lista de obiecte WifiSignalEntity
     */
    @Query("SELECT * FROM wifi_signal_table WHERE ssid = :ssid ORDER BY timestamp DESC LIMIT 50")
    List<WifiSignalEntity> getSignalHistory(String ssid);
}
