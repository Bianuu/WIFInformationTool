package com.example.wifiinformationtool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clasa CommunicationsActivity se ocupa cu preluarea listei de tari europene
 * si afisarea conditiilor meteo curente pentru tara selectata.
 */
public class CommunicationsActivity extends AppCompatActivity {

    // ExecutorService pentru task-uri in background (network calls)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler pentru a publica rezultate pe thread-ul principal (UI)
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    // Harta intre numele tarii si coordonatele (latitudine,longitudine)
    private final Map<String, String> countryCoordinates = new HashMap<>();

    // Componente UI
    private Spinner countrySpinner;       // Dropdown cu tari
    private TextView weatherTextView;     // TextView pentru afisarea vremii
    private ProgressBar loadingSpinner;   // Indicator de incarcare
    private String selectedCountry = "";  // Tara aleasa de utilizator

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communications);

        // Initializare componente UI
        countrySpinner = findViewById(R.id.countrySpinner);
        weatherTextView = findViewById(R.id.weatherTextView);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        Button fetchWeatherButton = findViewById(R.id.fetchWeatherButton);

        // Porneste procesul de preluare a listelor de tari europene
        fetchEuropeanCountries();

        // Seteaza actiunea pentru butonul de fetch weather
        fetchWeatherButton.setOnClickListener(v -> {
            if (!selectedCountry.isEmpty() && countryCoordinates.containsKey(selectedCountry)) {
                // Daca a fost selectata o tara valida, se preia vremea
                fetchWeatherData();
            } else {
                // Altfel, afiseaza un mesaj de atentionare
                Toast.makeText(this, "Select a country first!", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener pentru spinner - actualizeaza tara selectata
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nu e nevoie sa facem nimic aici
            }
        });
    }

    /**
     * Metoda pentru preluarea listei de tari europene printr-un apel REST.
     */
    private void fetchEuropeanCountries() {
        executorService.execute(() -> {
            try {
                String apiUrl = "https://restcountries.com/v3.1/region/europe";
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");

                // Citeste raspunsul de la server
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parseaza JSON si completeaza harta de coordonate
                parseCountryData(response.toString());

            } catch (Exception e) {
                // Afiseaza un Toast cu eroare, direct pe UI thread
                uiHandler.post(() ->
                        Toast.makeText(CommunicationsActivity.this, "Error fetching countries", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * Parseaza JSON-ul primit cu date despre tari si construieste o harta
     * intre numele tarii si coordonatele sale.
     */
    private void parseCountryData(String json) {
        try {
            JSONArray countriesArray = new JSONArray(json);
            countryCoordinates.clear();

            // Parcurge fiecare obiect tara din array
            for (int i = 0; i < countriesArray.length(); i++) {
                JSONObject countryObject = countriesArray.getJSONObject(i);
                String countryName = countryObject.getJSONObject("name").getString("common");
                JSONArray latlngArray = countryObject.getJSONArray("latlng");

                // Daca exista doua valori lat si lon, adauga in harta
                if (latlngArray.length() == 2) {
                    String lat = latlngArray.getString(0);
                    String lon = latlngArray.getString(1);
                    countryCoordinates.put(countryName, lat + "," + lon);
                }
            }

            // Populeaza dropdown-ul pe UI thread
            uiHandler.post(this::populateCountryDropdown);

        } catch (JSONException e) {
            uiHandler.post(() ->
                    Toast.makeText(CommunicationsActivity.this, "Error parsing country data", Toast.LENGTH_SHORT).show()
            );
        }
    }

    /**
     * Populeaza Spinner-ul cu lista de tari disponibile.
     */
    private void populateCountryDropdown() {
        if (!countryCoordinates.isEmpty()) {
            String[] countries = countryCoordinates.keySet().toArray(new String[0]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    countries
            );
            countrySpinner.setAdapter(adapter);
        }
    }

    /**
     * Fetch weather data pentru tara selectata, folosind API-ul open-meteo.
     */
    private void fetchWeatherData() {
        String coordinates = countryCoordinates.get(selectedCountry);
        String[] parts = coordinates.split(",");
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + parts[0] +
                "&longitude=" + parts[1] + "&current_weather=true";

        // Afiseaza spinner-ul de incarcare si ascunde TextView-ul
        loadingSpinner.setVisibility(View.VISIBLE);
        weatherTextView.setVisibility(View.GONE);
        weatherTextView.setBackground(null);

        executorService.execute(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                // Citeste raspunsul meteo
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parseaza JSON si formateaza textul de afisat
                String weatherInfo = parseWeatherData(response.toString());

                // Actualizeaza UI cu rezultatul
                uiHandler.post(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    weatherTextView.setText(weatherInfo);
                    weatherTextView.setVisibility(View.VISIBLE);
                    weatherTextView.setBackgroundResource(R.drawable.weather_text_background);
                });

            } catch (Exception e) {
                // In caz de eroare, ascunde spinner si afiseaza un Toast
                uiHandler.post(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    weatherTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(CommunicationsActivity.this, "Error fetching weather", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Parseaza JSON-ul cu date despre vreme si returneaza un string formatat.
     */
    private String parseWeatherData(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONObject weather = jsonObject.getJSONObject("current_weather");
            double temp = weather.getDouble("temperature");
            double windspeed = weather.getDouble("windspeed");

            return "🌍 " + selectedCountry +
                    "\n🌡 Temperature: " + temp + "°C" +
                    "\n💨 Wind Speed: " + windspeed + " km/h";
        } catch (JSONException e) {
            return "Error parsing weather data";
        }
    }
}
