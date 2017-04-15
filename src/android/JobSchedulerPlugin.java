package me.errietta.cordova.plugin.jobscheduler;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import me.errietta.cordova.plugin.jobscheduler.MyJobService;

import java.lang.ref.WeakReference;
import java.util.List;




//import de.appplant.cordova.plugin.background.ForegroundService.ForegroundBinder;

import static android.content.Context.BIND_AUTO_CREATE;

public class JobSchedulerPlugin extends CordovaPlugin {
    private ComponentName mServiceComponent;
    private int mJobId = 0;

    private static final String TAG = JobSchedulerPlugin.class.getSimpleName();

    // Event types for callbacks
    private enum Event {
        ACTIVATE, DEACTIVATE, FAILURE
    }

    // Plugin namespace
    private static final String JS_NAMESPACE =
            "cordova.plugins.jobScheduler";

    // Default settings for the notification
    private static JSONObject defaultSettings = new JSONObject();

    // Used to (un)bind the service to with the activity
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            fireEvent(Event.FAILURE, "'service disconnected'");
        }
    };

    @Override
    protected void pluginInitialize() {
    //    BackgroundExt.addWindowFlags(cordova.getActivity());
    }

    // codebeat:disable[ABC]

    /**
     * Executes the request.
     *
     * @param action   The action to execute.
     * @param args     The exec() arguments.
     * @param callback The callback context used when
     *                 calling back into JavaScript.
     *
     * @return Returning false results in a "MethodNotFound" error.
     *
     * @throws JSONException
     */
    @Override
    public boolean execute (String action, JSONArray args,
                            CallbackContext callback) throws JSONException {

        if (action.equalsIgnoreCase("schedule")) {
            schedule (args.getJSONObject(0));
            callback.success();
            return true;
        }

        callback.error("Unrecognised method");
        return false;
    }

    private void schedule (JSONObject params) {
        JobInfo.Builder builder;

        Activity act = cordova.getActivity();

        try {
             mServiceComponent = new ComponentName(act, MyJobService.class);
        } catch (Exception e) {
            Log.e(TAG, "Can't get ComponentName", e);
            return;
        }

        try {
            builder = new JobInfo.Builder(mJobId++, mServiceComponent);
        } catch (Exception e) {
            Log.e(TAG, "Error creating builder", e);
            return;
        }
        try {
            if (params.has("minimumLatency")) {
                builder.setMinimumLatency(params.getLong("minimumLatency") * 1000);
            }

            if (params.has("overrideDeadline")) {
                builder.setOverrideDeadline(params.getLong("overrideDeadline") * 1000);
            }

            if (params.has("requiredNetworkType")) {
                String connectivity = params.getString("requiredNetworkType");

                if (connectivity.equals("unmetered")) {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
                } else if (connectivity.equals("any")) {
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                }
            }

            if (params.has("requiresDeviceIdle") && params.getBoolean("requiresDeviceIdle")) {
                builder.setRequiresDeviceIdle(true);
            }
            if (params.has("requiresCharging") && params.getBoolean("requiresCharging")) {
                builder.setRequiresCharging(true);
            }

            /*
            // Extras, work duration.
            PersistableBundle extras = new PersistableBundle();
            String workDuration = mDurationTimeEditText.getText().toString();
            if (TextUtils.isEmpty(workDuration)) {
                workDuration = "1";
            }
            extras.putLong(WORK_DURATION_KEY, Long.valueOf(workDuration) * 1000);

            builder.setExtras(extras);
            */
        } catch (Exception e) {
            Log.e(TAG, "Error setting params", e);
            return;
        }

        try {
            // Schedule job
            Log.d(TAG, "Scheduling job");

            JobScheduler tm = (JobScheduler) act.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            tm.schedule(builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling!", e);
        }
    }


    // codebeat:enable[ABC]

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app.
     */
    @Override    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app.
     */
    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
    }

    /**
     * Called when the activity will be destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    /**
     * Fire vent with some parameters inside the web view.
     *
     * @param event The name of the event
     * @param params Optional arguments for the event
     */
    private void fireEvent (Event event, String params) {
        String eventName = event.name().toLowerCase();

        String str = String.format("%s.on%s(%s)",
                JS_NAMESPACE, eventName, params);

        str = String.format("%s;%s.fireEvent('%s',%s);",
                str, JS_NAMESPACE, eventName, params);

        final String js = str;

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + js);
            }
        });
    }

}
