package vandy.mooc.operations;

import android.content.res.Configuration;
import android.view.View;

/**
 * This class defines all the weather-related operations.
 */
public interface WeatherOps {
    /**
     * Initiate the service binding protocol.
     */
    public void bindService();

    /**
     * Initiate the service unbinding protocol.
     */
    public void unbindService();

    /*
     * Initiate the synchronous weahter lookup when the user presses
     * the "Look Up Sync" button.
     */
    public void getWeatherSync(View v);

    /*
     * Initiate the asynchronous weather lookup when the user presses
     * the "Look Up Async" button.
     */
    public void getWeatherAsync(View v);

    /**
     * Called after a runtime configuration change occurs.
     */
    public void onConfigurationChanged(Configuration newConfig);
}
