package vandy.mooc.jsonweather;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;
import android.util.JsonToken;

/**
 * Parses the Json weather data returned from the Weather Services API
 * and returns a List of JsonWeather objects that contain this data.
 */
public class WeatherJSONParser {
    /**
     * Used for logging purposes.
     */
    private final String TAG =
        this.getClass().getCanonicalName();

    /**
     * Parse the @a inputStream and convert it into a List of JsonWeather
     * objects.
     */
    public List<JsonWeather> parseJsonStream(InputStream inputStream)
        throws IOException {
        // Create a JsonReader for the inputStream.
        try (JsonReader reader =
                     new JsonReader(new InputStreamReader(inputStream,
                             "UTF-8"))) {
            // Log.d(TAG, "Parsing the results returned as an array");

            // Handle the array returned from the Acronym Service.
            return parseJsonWeatherArray(reader);
        }
    }

    /**
     * Parse a Json stream and convert it into a List of JsonWeather
     * objects.
     */
    public List<JsonWeather> parseJsonWeatherArray(JsonReader reader)
        throws IOException {
        List<JsonWeather> jsonWeather = new ArrayList<JsonWeather>();
        jsonWeather.add(parseJsonWeather(reader));
        return jsonWeather;
    }

    /**
     * Parse a Json stream and return a JsonWeather object.
     */
    public JsonWeather parseJsonWeather(JsonReader reader) 
        throws IOException {

        Sys mSys = null;
        Main mMain = null;
        Wind mWind = null;
        String mName = null;

        reader.beginObject();

        try {
            outerloop:
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case JsonWeather.name_JSON:
                        mName = parseName(reader);
                        break;
                    case JsonWeather.wind_JSON:
                        mWind = parseWind(reader);
                        break;
                    case JsonWeather.main_JSON:
                        mMain = parseMain(reader);
                        break;
                    case JsonWeather.sys_JSON:
                        mSys = parseSys(reader);
                        break;
                    case JsonWeather.cod_JSON:
                        reader.nextLong();
                        break outerloop;
                    default:
                        // Other fields will not be used in the assignment, so
                        // skip them
                        reader.skipValue();
                        break;
                }
            }
        } finally {
            reader.endObject();
        }
        // Other fields in JsonWeather will not be used in the assignment, so
        // leave them blank
        return new JsonWeather(mSys, null, mMain, null, mWind, 0, 0, mName, 0);
    }
    
    /**
     * Parse a Json stream and return a List of Weather objects.
     */
    public String parseName(JsonReader reader) throws IOException {
        // weather object is not stored in WeatherData, so no need to parse it
        return reader.nextString();
    }
    
    /**
     * Parse a Json stream and return a Main Object.
     */
    public Main parseMain(JsonReader reader) 
        throws IOException {

        Main mMain = new Main();

        reader.beginObject();

        try {
            outerloop:
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case Main.temp_JSON:
                        mMain.setTemp(reader.nextDouble());
                        break;
                    case Main.humidity_JSON:
                        mMain.setHumidity(reader.nextLong());
                        break;
                    case Main.tempMax_JSON:
                        reader.nextDouble();
                        break outerloop;
                    default:
                        // Other fields will not be used in the assignment, so
                        // skip them
                        reader.skipValue();
                        break;
                }
            }
        } finally {
            reader.endObject();
        }

        return mMain;
    }

    /**
     * Parse a Json stream and return a Wind Object.
     */
    public Wind parseWind(JsonReader reader) throws IOException {

        Wind mWind = new Wind();

        reader.beginObject();

        try {
            outerloop:
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case Wind.speed_JSON:
                        mWind.setSpeed(reader.nextDouble());
                        break;
                    case Wind.deg_JSON:
                        mWind.setDeg(reader.nextDouble());
                        break outerloop;
                    default:
                        reader.skipValue();
                        break;
                }
            }
        } finally {
            reader.endObject();
        }

        return mWind;
    }

    /**
     * Parse a Json stream and return a Sys Object.
     */
    public Sys parseSys(JsonReader reader) throws IOException {
        Sys mSys = new Sys();

        reader.beginObject();

        try {
            outerloop:
            while (reader.hasNext()) {
                String name = reader.nextName();
                switch (name) {
                    case Sys.sunrise_JSON:
                        mSys.setSunrise(reader.nextLong());
                        break;
                    case Sys.sunset_JSON:
                        mSys.setSunset(reader.nextLong());
                        break outerloop;
                    default:
                        // Other fields will not be used in the assignment, so
                        // skip them
                        reader.skipValue();
                        break;
                }
            }
        } finally {
            reader.endObject();
        }

        return mSys;
    }
}
