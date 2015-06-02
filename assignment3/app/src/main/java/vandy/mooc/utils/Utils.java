package vandy.mooc.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vandy.mooc.aidl.WeatherData;
import vandy.mooc.jsonweather.WeatherJSONParser;
import vandy.mooc.jsonweather.JsonWeather;
import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * @class WeatherDownloadUtils
 *
 * @brief Handles the actual downloading of Weather information from
 *        the Weather web service.
 */
public class Utils {
    /**
     * Logging tag used by the debugger. 
     */
    private final static String TAG = Utils.class.getCanonicalName();
    private final static long CACHE_EXPIRATION_TIME = 10000;

    /** 
     * URL to the Weather web service.
     */
    private final static String sWeather_Web_Service_URL =
        "http://api.openweathermap.org/data/2.5/weather?q=";

    private static final Map<String, Long> timeOfCacheForWeather
            = new HashMap<String, Long>();
    private static final Map<String, List<WeatherData>> weatherDataCachedForLocation
            = new HashMap<String, List<WeatherData>>();


    /**
     * Obtain the Weather information.
     *
     * @return The information that responds to your current weather search.
     */
    public static List<WeatherData> getResults(final String location) {
        List<WeatherData> weatherData = null;
        long currentTime = System.currentTimeMillis();
        long timeOfCache = timeOfCacheForWeather.containsKey(location) ?
                timeOfCacheForWeather.get(location) : -1 * CACHE_EXPIRATION_TIME;

        long timeElapsed = currentTime - timeOfCache;

        if (timeElapsed >= CACHE_EXPIRATION_TIME || timeElapsed < 0) {
            Log.i(TAG, "Query weather info from web service");
            weatherData = queryDataFromWeatherWebService(location);
            if (weatherData != null) {
                weatherDataCachedForLocation.put(location, weatherData);
                timeOfCacheForWeather.put(location, currentTime);
            }
        } else {
            Log.i(TAG, "Getting weather info from cache");
            weatherData = weatherDataCachedForLocation.get(location);
        }

        return weatherData;

    }

    private static List<WeatherData> queryDataFromWeatherWebService(String location) {
        // Create a List that will return the WeatherData obtained
        // from the Weather Service web service.
        final List<WeatherData> returnList =
            new ArrayList<>();

        // A List of JsonWeather objects.
        List<JsonWeather> jsonWeathers = null;

        try {
            // Append the location to create the full URL.
            final URL url =
                new URL(sWeather_Web_Service_URL
                        + URLEncoder.encode(location));

            // Opens a connection to the Weather Service.
            HttpURLConnection urlConnection =
                (HttpURLConnection) url.openConnection();

            // Sends the GET request and reads the Json results.
            try (InputStream in =
                 new BufferedInputStream(urlConnection.getInputStream())) {
                 // Create the parser.
                 final WeatherJSONParser parser =
                     new WeatherJSONParser();

                // Parse the Json results and create JsonWeather data
                // objects.
                jsonWeathers = parser.parseJsonStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (jsonWeathers != null) {
            // Convert the JsonWeather data objects to our WeatherData
            // object, which can be passed between processes.
            for (JsonWeather jsonWeather : jsonWeathers) {
                returnList.add(new WeatherData(jsonWeather.getName(),
                        jsonWeather.getWind().getSpeed(),
                        jsonWeather.getWind().getDeg(),
                        jsonWeather.getMain().getTemp(),
                        jsonWeather.getMain().getHumidity(),
                        jsonWeather.getSys().getSunrise(),
                        jsonWeather.getSys().getSunset()));
            }
        }

        return returnList;
    }

    /**
     * This method is used to hide a keyboard after a user has
     * finished typing the url.
     */
    public static void hideKeyboard(Activity activity,
                                    IBinder windowToken) {
        InputMethodManager mgr =
           (InputMethodManager) activity.getSystemService
            (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken,
                                    0);
    }

    /**
     * Show a toast message.
     */
    public static void showToast(Context context,
                                 String message) {
        Toast.makeText(context,
                       message,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * Ensure this class is only used as a utility.
     */
    private Utils() {
        throw new AssertionError();
    } 
}
