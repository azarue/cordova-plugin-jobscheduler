package me.errietta.cordova.plugin.jobscheduler;

import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.util.Log;
import android.webkit.WebView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import me.errietta.cordova.plugin.jobscheduler.MyJobService;

//import de.appplant.cordova.plugin.background.ForegroundService.ForegroundBinder;

public class JobSchedulerPlugin extends CordovaPlugin {
    private ComponentName mServiceComponent;

    private IncomingMessageHandler mHandler;

    public static final String MESSENGER_INTENT_KEY
            = "cordova-plugin-jobscheduler.MESSENGER_INTENT_KEY";
    public static final String JOB_ID_KEY
            = "cordova-plugin-jobscheduler.JOB_ID_KEY";

    private static final String TAG = "JobSchedulerPlugin";
    public static final int SCHEDULED_JOB_START = 0;

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
            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            fireEvent(Event.FAILURE, "'service disconnected'");
        }
    };

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "pluginInitialize");

        Context ctx = cordova.getActivity().getApplicationContext();

        mHandler = new IncomingMessageHandler(cordova.getActivity(), webView);
        Intent startServiceIntent = new Intent(ctx, MyJobService.class);
        Messenger messengerIncoming = new Messenger(mHandler);
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
        ctx.startService(startServiceIntent);
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
        int jobid;

        try {
             mServiceComponent = new ComponentName(act, MyJobService.class);
        } catch (Exception e) {
            Log.e(TAG, "Can't get ComponentName", e);
            return;
        }

        try {
            jobid = params.getInt("jobId");
            builder = new JobInfo.Builder(jobid, mServiceComponent);
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

            PersistableBundle extras = new PersistableBundle();
            extras.putInt (JOB_ID_KEY, jobid);

            builder.setExtras(extras);
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

    /**
     * A {@link Handler} allows you to send messages associated with a thread. A {@link Messenger}
     * uses this handler to communicate from {@link MyJobService}. It's also used to make
     * the start and stop views blink for a short period of time.
     */
    private static class IncomingMessageHandler extends Handler {
        // Prevent possible leaks with a weak reference.
        private WeakReference<Activity> mActivity;
        private WeakReference<CordovaWebView> webview;

        IncomingMessageHandler(Activity activity, CordovaWebView wv) {
            super();
            this.mActivity = new WeakReference<Activity>(activity);
            this.webview = new WeakReference<CordovaWebView>(wv);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity mainActivity = mActivity.get();
            if (mainActivity == null) {
                // Activity is no longer available, exit.
                return;
            }

            final int jobid = msg.arg1;
            final CordovaWebView wv = this.webview.get();

            switch (msg.what) {
                case SCHEDULED_JOB_START:
                    Handler handler = new Handler(mainActivity.getApplicationContext().getMainLooper());

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "inside the main thingy: " + jobid);
                            wv.loadUrl("javascript:cordova.plugins.jobScheduler.onrun(" + jobid + ");");
                        }
                    });

                    Log.d(TAG, "Got start from service!");
                    break;
            }
        }
    }
}
