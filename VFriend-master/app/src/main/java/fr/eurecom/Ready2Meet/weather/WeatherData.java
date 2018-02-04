package fr.eurecom.Ready2Meet.weather;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherData {

    public WeatherData(JSONObject jsonObject) {
        try {
            dt = jsonObject.getLong("dt");
            temp = jsonObject.getJSONObject("main").getDouble("temp");
            tempMin = jsonObject.getJSONObject("main").getDouble("temp_min");
            tempMax = jsonObject.getJSONObject("main").getDouble("temp_max");
            pressure = jsonObject.getJSONObject("main").getDouble("pressure");
            seaLevel = jsonObject.getJSONObject("main").getDouble("sea_level");
            grndLevel = jsonObject.getJSONObject("main").getDouble("grnd_level");
            humidity = jsonObject.getJSONObject("main").getDouble("humidity");
            tempKf = jsonObject.getJSONObject("main").getDouble("temp_kf");
            windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");
            clouds = jsonObject.getJSONObject("clouds").getInt("all");
            id = jsonObject.getJSONArray("weather").getJSONObject(0).getInt("id");
            main = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            description = jsonObject.getJSONArray("weather").getJSONObject(0).getString
                    ("description");
            icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public long dt;
    public double temp, tempMin, tempMax, pressure, seaLevel, grndLevel, humidity, tempKf,
            windSpeed;
    public int id, clouds;
    public String main, description, icon;
}
