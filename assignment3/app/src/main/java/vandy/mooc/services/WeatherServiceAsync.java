package vandy.mooc.services;

import java.util.ArrayList;
import java.util.List;

import vandy.mooc.aidl.WeatherData;
import vandy.mooc.aidl.WeatherRequest;
import vandy.mooc.aidl.WeatherResults;
import vandy.mooc.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * @class WeatherServiceAsync
 * 
 * @brief This class uses asynchronous AIDL interactions to query
 *        weather info via a Weather Web service.  The WeatherActivity
 *        that binds to this Service will receive an IBinder that's an
 *        instance of WeatherRequest, which extends IBinder.  The
 *        Activity can then interact with this Service by making
 *        one-way method calls on the WeatherRequest object asking
 *        this Service to lookup the weather, passing in a
 *        WeatherResults object and the location string.  After the
 *        lookup is finished, this Service sends the weather info results
 *        back to the Activity by calling sendResults() on the
 *        WeatherResults object.
 * 
 *        AIDL is an example of the Broker Pattern, in which all
 *        interprocess communication details are hidden behind the
 *        AIDL interfaces.
 */
public class WeatherServiceAsync extends LifecycleLoggingService {
    /**
     * Factory method that makes an Intent used to start the
     * WeatherServiceAsync when passed to bindService().
     * 
     * @param context
     *            The context of the calling component.
     */
    public static Intent makeIntent(Context context) {
        return new Intent(context,
                          WeatherServiceAsync.class);
    }

    /**
     * Called when a client (e.g., WeatherActivity) calls
     * bindService() with the proper Intent.  Returns the
     * implementation of WeatherRequest, which is implicitly cast as
     * an IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mWeatherRequestImpl;
    }

    /**
     * The concrete implementation of the AIDL Interface
     * WeatherRequest, which extends the Stub class that implements
     * WeatherRequest, thereby allowing Android to handle calls across
     * process boundaries.  This method runs in a separate Thread as
     * part of the Android Binder framework.
     * 
     * This implementation plays the role of Invoker in the Broker
     * Pattern.
     */
    WeatherRequest.Stub mWeatherRequestImpl = new WeatherRequest.Stub() {
        /**
         * Implement the AIDL WeatherRequest getCurrentWeather()
         * method, which forwards to Utils getResults() to
         * obtain the results from the Weather Web service and
         * then sends the results back to the Activity via a
         * callback.
         */
        @Override
        public void getCurrentWeather(String location,
                                      WeatherResults callback)
            throws RemoteException {

            // Call the Weather Web service to get the list of
            // possible weather info of the designated location.
            List<WeatherData> weatherResults =
                Utils.getResults(location);

            // Invoke a one-way callback to send list of weather
            // info back to the WeatherActivity.
            if (weatherResults != null) {
                Log.d(TAG, ""
                      + weatherResults.size()
                      + " results for location: "
                      + location);

            } else {
                Log.e(TAG, "Weather service is not available");
            }
            callback.sendResults(weatherResults);
        }
	};
}
