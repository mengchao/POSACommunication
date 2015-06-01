package vandy.mooc.services;

import java.util.ArrayList;
import java.util.List;

import vandy.mooc.aidl.WeatherData;
import vandy.mooc.aidl.WeatherCall;
import vandy.mooc.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @class WeatherServiceSync
 * 
 * @brief This class uses synchronous AIDL interactions to query
 *        weather info via an Weather Web service.  The WeatherActivity
 *        that binds to this Service will receive an IBinder that's an
 *        instance of WeatherCall, which extends IBinder.  The
 *        Activity can then interact with this Service by making
 *        two-way method calls on the WeatherCall object asking this
 *        Service to lookup the weather of the location string.  After
 *        the lookup is finished, this Service sends the weather info
 *        results back to the Activity by returning a List of
 *        WeatherData.
 * 
 *        AIDL is an example of the Broker Pattern, in which all
 *        interprocess communication details are hidden behind the
 *        AIDL interfaces.
 */
public class WeatherServiceSync extends LifecycleLoggingService {
    /**
     * Factory method that makes an Intent used to start the
     * WeatherServiceSync when passed to bindService().
     * 
     * @param context
     *            The context of the calling component.
     */
    public static Intent makeIntent(Context context) {
        return new Intent(context,
                          WeatherServiceSync.class);
    }

    /**
     * Called when a client (e.g., WeatherActivity) calls
     * bindService() with the proper Intent.  Returns the
     * implementation of WeatherCall, which is implicitly cast as an
     * IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mWeatherCallImpl;
    }

    /**
     * The concrete implementation of the AIDL Interface WeatherCall,
     * which extends the Stub class that implements WeatherCall,
     * thereby allowing Android to handle calls across process
     * boundaries.  This method runs in a separate Thread as part of
     * the Android Binder framework.
     * 
     * This implementation plays the role of Invoker in the Broker
     * Pattern.
     */
    WeatherCall.Stub mWeatherCallImpl = new WeatherCall.Stub() {
        /**
         * Implement the AIDL WeatherCall getCurrentWeather() method,
         * which forwards to Utils getResults() to obtain
         * the results from the Weather Web service and then
         * returns the results back to the Activity.
         */
        @Override
        public List<WeatherData> getCurrentWeather(String location)
                throws RemoteException {

        // Call the Weather Web service to get the list of
        // possible expansions of the designated location.
        List<WeatherData> weatherResults =
                Utils.getResults(location);

        if (weatherResults != null) {
            Log.d(TAG, ""
                    + weatherResults.size()
                    + " results for location: "
                    + location);

            // Return the list of weather info back to the
            // WeatherActivity.
            return weatherResults;
        } else {
            // Create a zero-sized weatherResults object to
            // indicate to the caller that the location had no
            // weather info.
            weatherResults = new ArrayList<WeatherData>();
            return weatherResults;
        }
        }
    };
}
