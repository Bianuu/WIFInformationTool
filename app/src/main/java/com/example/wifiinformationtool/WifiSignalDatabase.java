package com.example.wifiinformationtool;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Baza de date Room pentru stocarea istoricului semnalului Wi‑Fi.
 * Entitatile gestionate: WifiSignalEntity.
 */
@Database(entities = {WifiSignalEntity.class}, version = 1, exportSchema = false)
public abstract class WifiSignalDatabase extends RoomDatabase {
    // Instanta singleton a bazei de date
    private static volatile WifiSignalDatabase INSTANCE;

    /**
     * Returneaza instanta singleton a bazei de date.
     * Daca nu exista, o construieste folosind Room.databaseBuilder.
     *
     * @param context contextul aplicatiei
     * @return instanta WifiSignalDatabase
     */
    public static WifiSignalDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WifiSignalDatabase.class) {
                if (INSTANCE == null) {
                    // Creaza baza de date cu numele "wifi_signal_db"
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    WifiSignalDatabase.class,
                                    "wifi_signal_db"
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Metoda abstracta care furnizeaza DAO-ul pentru operatiuni pe tabelul wifi_signal_table.
     *
     * @return WifiSignalDao pentru insert si interogari istoricului
     */
    public abstract WifiSignalDao wifiSignalDao();
}
