package vandy.mooc.activities;

import vandy.mooc.operations.WeatherOps;
import vandy.mooc.operations.WeatherOpsImpl;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

/**
 * The main Activity that prompts the user for city name to query weather info
 * via various implementations of WeatherServiceSync and
 * WeatherServiceAsync and view via the results.  Extends
 * LifecycleLoggingActivity so its lifecycle hook methods are logged
 * automatically.
 */
public class MainActivity extends LifecycleLoggingActivity {
    /**
     * Provides weather-related operations.
     */
    private WeatherOps mWeatherOps;

    /**
     * Hook method called when a new instance of Activity is created.
     * One time initialization code goes here, e.g., runtime
     * configuration changes.
     *
     * @param Bundle object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Create the WeatherOps object one time.
        mWeatherOps = new WeatherOpsImpl(this);

        // Initiate the service binding protocol.
        mWeatherOps.bindService();
    }

    /**
     * Hook method called by Android when this Activity is
     * destroyed.
     */
    @Override
    protected void onDestroy() {
        // Unbind from the Service.
        mWeatherOps.unbindService();

        // Always call super class for necessary operations when an
        // Activity is destroyed.
        super.onDestroy();
    }

    /**
     * Hook method invoked when the screen orientation changes.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mWeatherOps.onConfigurationChanged(newConfig);
    }

    /*
     * Initiate the synchronous weather lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void getWeatherSync(View v) {
        mWeatherOps.getWeatherSync(v);
    }

    /*
     * Initiate the asynchronous weather lookup when the user presses
     * the "Look Up Async" button.
     */
    public void getWeatherAsync(View v) {
        mWeatherOps.getWeatherAsync(v);
    }
}
