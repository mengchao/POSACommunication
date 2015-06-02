package vandy.mooc.operations;

import java.lang.ref.WeakReference;
import java.util.List;

import vandy.mooc.R;
import vandy.mooc.activities.MainActivity;
import vandy.mooc.aidl.WeatherCall;
import vandy.mooc.aidl.WeatherData;
import vandy.mooc.aidl.WeatherRequest;
import vandy.mooc.aidl.WeatherResults;
import vandy.mooc.services.WeatherServiceAsync;
import vandy.mooc.services.WeatherServiceSync;
import vandy.mooc.utils.GenericServiceConnection;
import vandy.mooc.utils.Utils;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class implements all the weather-related operations defined in
 * the WeatherOps interface.
 */
public class WeatherOpsImpl implements WeatherOps {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG = getClass().getSimpleName();

    /**
     * Used to enable garbage collection.
     */
    protected WeakReference<MainActivity> mActivity;
    	
    /**
     * The TextView that will display the results to the user.
     */
    protected WeakReference<TextView> mWeatherData;

    /**
     * Location entered by the user.
     */
    protected WeakReference<EditText> mEditText;

    /**
     * List of results to display (if any).
     */
    protected WeatherData mResult;

    /**
     * This GenericServiceConnection is used to receive results after
     * binding to the WeatherServiceSync Service using bindService().
     */
    private GenericServiceConnection<WeatherCall> mServiceConnectionSync;

    /**
     * This GenericServiceConnection is used to receive results after
     * binding to the WeatherServiceAsync Service using bindService().
     */
    private GenericServiceConnection<WeatherRequest> mServiceConnectionAsync;

    /**
     * Constructor initializes the fields.
     */
    public WeatherOpsImpl(MainActivity activity) {
        // Initialize the WeakReference.
        mActivity = new WeakReference<>(activity);

        // Finish the initialization steps.
        initializeViewFields();
        initializeNonViewFields();
    }

    /**
     * Initialize the View fields, which are all stored as
     * WeakReferences to enable garbage collection.
     */
    private void initializeViewFields() {
        // Get references to the UI components.
        mActivity.get().setContentView(R.layout.main_activity);

        // Store the EditText that holds the location entered by the user
        // (if any).
        mEditText = new WeakReference<>
            ((EditText) mActivity.get().findViewById(R.id.editText1));

        // Store the TextView for displaying the results entered.
        mWeatherData = new WeakReference<>
            ((TextView) mActivity.get().findViewById(R.id.weatherData));

    }

    /**
     * (Re)initialize the non-view fields (e.g.,
     * GenericServiceConnection objects).
     */
    private void initializeNonViewFields() {
        mServiceConnectionSync = 
            new GenericServiceConnection<WeatherCall>(WeatherCall.class);

        mServiceConnectionAsync =
            new GenericServiceConnection<WeatherRequest>(WeatherRequest.class);

        // Display results if any (due to runtime configuration change).
        displayResults();
    }

    /**
     * Called after a runtime configuration change occurs.
     */
    public void onConfigurationChanged(Configuration newConfig) {
        // Checks the orientation of the screen.
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 
            Log.d(TAG,
                  "Now running in landscape mode");
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
            Log.d(TAG,
                  "Now running in portrait mode");
    }

    /**
     * Initiate the service binding protocol.
     */
    @Override
    public void bindService() {
        Log.d(TAG, "calling bindService()");

        // Launch the Weather Bound Services if they aren't already
        // running via a call to bindService(), which binds this
        // activity to the WeatherService* if they aren't already
        // bound.
        if (mServiceConnectionSync.getInterface() == null) 
            mActivity.get().bindService
                (WeatherServiceSync.makeIntent(mActivity.get()),
                 mServiceConnectionSync,
                 Context.BIND_AUTO_CREATE);

        if (mServiceConnectionAsync.getInterface() == null) 
            mActivity.get().bindService
                (WeatherServiceAsync.makeIntent(mActivity.get()),
                 mServiceConnectionAsync,
                 Context.BIND_AUTO_CREATE);
    }

    /**
     * Initiate the service unbinding protocol.
     */
    @Override
    public void unbindService() {
        Log.d(TAG, "calling unbindService()");

        // Unbind the Async Service if it is connected.
        if (mServiceConnectionAsync.getInterface() != null)
            mActivity.get().unbindService
                (mServiceConnectionAsync);

        // Unbind the Sync Service if it is connected.
        if (mServiceConnectionSync.getInterface() != null)
            mActivity.get().unbindService
                (mServiceConnectionSync);
    }

    /*
     * Initiate the asynchronous weather lookup when the user presses
     * the "Look Up Async" button.
     */
    public void getWeatherAsync(View v) {
        WeatherRequest weatherRequest =
            mServiceConnectionAsync.getInterface();

        if (weatherRequest != null) {
            // Get the location entered by the user.
            final String location =
                mEditText.get().getText().toString();

            resetDisplay();

            try {
                // Invoke a one-way AIDL call, which does not block
                // the client.  The results are returned via the
                // sendResults() method of the mWeatherResults
                // callback object, which runs in a Thread from the
                // Thread pool managed by the Binder framework.
                weatherRequest.getCurrentWeather(location,
                                                 mWeatherResults);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException:" + e.getMessage());
            }
        } else {
            Log.d(TAG, "weatherRequest was null.");
        }
    }

    /*
     * Initiate the synchronous weather lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void getWeatherSync(View v) {
        final WeatherCall weatherCall =
            mServiceConnectionSync.getInterface();

        if (weatherCall != null) {
            // Get the location entered by the user.
            final String location =
                mEditText.get().getText().toString();

            resetDisplay();

            // Use an anonymous AsyncTask to download the weather data
            // in a separate thread and then display any results in
            // the UI thread.
            new AsyncTask<String, Void, List<WeatherData>> () {
                /**
                 * Location where we are querying for weather
                 */
                private String mLocation;

                /**
                 * Retrieve the weather lookup results via a
                 * synchronous two-way method call, which runs in a
                 * background thread to avoid blocking the UI thread.
                 */
                protected List<WeatherData> doInBackground(String... locations) {
                    try {
                        mLocation = locations[0];
                        return weatherCall.getCurrentWeather(mLocation);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                /**
                 * Display the results in the UI Thread.
                 */
                protected void onPostExecute(List<WeatherData> weatherDataList) {
                    if (weatherDataList == null) {
                        mResult = null;
                        displayResults();
                        Utils.showToast(mActivity.get(),
                                "weather service is not available");
                    }
                    else if (weatherDataList.size() == 0) {
                        mResult = null;
                        displayResults();
                        Utils.showToast(mActivity.get(),
                                "no weather info for "
                                        + mLocation
                                        + " found");
                    }
                    else {
                        mResult = weatherDataList.get(0);
                        displayResults();
                    }
                }
                // Execute the AsyncTask to lookup the weather without
                // blocking the caller.
            }.execute(location);
        } else {
            Log.d(TAG, "mWeatherCall was null.");
        }
    }

    /**
     * The implementation of the WeatherResults AIDL Interface, which
     * will be passed to the Weather Web service using the
     * WeatherRequest.getCurrentWeather() method.
     * 
     * This implementation of WeatherResults.Stub plays the role of
     * Invoker in the Broker Pattern since it dispatches the upcall to
     * sendResults().
     */
    private WeatherResults.Stub mWeatherResults = new WeatherResults.Stub() {
            /**
             * This method is invoked by the WeatherServiceAsync to
             * return the results back to the WeatherActivity.
             */
            @Override
            public void sendResults(final List<WeatherData> weatherDataList)
                throws RemoteException {
        // Since the Android Binder framework dispatches this
        // method in a background Thread we need to explicitly
        // post a runnable containing the results to the UI
        // Thread, where it's displayed.
        mActivity.get().runOnUiThread(new Runnable() {
                        public void run() {
            displayResults();
            if (weatherDataList == null) {
                mResult = null;
                Utils.showToast(mActivity.get(),
                        "weather service is not available");
                }
                else if (weatherDataList.size() == 0) {
                    mResult = null;
                    Utils.showToast(mActivity.get(),
                        "no weather info for "
                        + mEditText.get().getText()
                        + " found");
                    }
                else {
                    mResult = weatherDataList.get(0);
                }
                displayResults();
        }
              });
            }
	};

    /**
     * Display the results to the screen.
     * 
     * @param results
     *            List of Results to be displayed.
     */
    private void displayResults() {
        if (mResult != null) {
            mWeatherData.get().setText(mResult.toString());
        } else {
            mWeatherData.get().setText("");
        }
    }

    /**
     * Reset the display prior to attempting to lookup weather for a new location.
     */
    private void resetDisplay() {
        Utils.hideKeyboard(mActivity.get(),
                           mEditText.get().getWindowToken());
        mResult = null;
    }
}
