package fr.eurecom.Ready2Meet.weather;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.eurecom.Ready2Meet.R;

public class JSONParser extends AsyncTaskLoader<List<WeatherData>> {
    private LatLng latLng;

    public JSONParser(Context context, LatLng latLng) {
        super(context);
        this.latLng = latLng;
    }

    @Override
    public List<WeatherData> loadInBackground() {
        Log.i("main", "loader in Background");
        HttpURLConnection conn;
        try {
            String url = "http://api.openweathermap.org/data/2.5/forecast?APPID=" + getContext()
                    .getString(R.string.weather_key) + "&lat=" + latLng.latitude + "&lon=" +
                    latLng.longitude + "&units=metric";
            URL page = new URL(url);
            conn = (HttpURLConnection) page.openConnection();
            conn.connect();
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Reader in = new InputStreamReader((InputStream) conn.getContent(), "UTF8");
            String jsonString = readAll(in);
            JSONObject listOfData = new JSONObject(jsonString);
            JSONArray array = listOfData.getJSONArray("list");
            int len = array.length();
            List<WeatherData> result = new ArrayList<>(len);
            for(int i = 0; i < len; i++) {
                JSONObject obj = array.getJSONObject(i);
                result.add(new WeatherData(obj));
            }
            return result;

        } catch(IOException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readAll(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder(4096);
        for(CharBuffer buf = CharBuffer.allocate(512); (reader.read(buf)) > - 1; buf.clear()) {
            builder.append(buf.flip());
        }
        return builder.toString();
    }
}
