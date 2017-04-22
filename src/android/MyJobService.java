package me.errietta.cordova.plugin.jobscheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import static me.errietta.cordova.plugin.jobscheduler.JobSchedulerPlugin.JOB_ID_KEY;
import static me.errietta.cordova.plugin.jobscheduler.JobSchedulerPlugin.MESSENGER_INTENT_KEY;
import static me.errietta.cordova.plugin.jobscheduler.JobSchedulerPlugin.SCHEDULED_JOB_START;

/**
 * Service to handle callbacks from the JobScheduler. Requests scheduled with the JobScheduler
 * ultimately land on this service's "onStartJob" method. It runs jobs for a specific amount of time
 * and finishes them. It keeps the activity updated with changes via a Messenger.
 */
public class MyJobService extends JobService {

    private static final String TAG = "JobSchedulerPlugin";

    private Messenger mActivityMessenger;
    private int jobid;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);

        Log.i(TAG, "onStartCommand");

        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        PersistableBundle extras = params.getExtras();

        if (!extras.containsKey(JOB_ID_KEY)) {
            Log.e(TAG, "jobid is null in getExtras");
            jobid = 0;
        } else {
            jobid = extras.getInt(JOB_ID_KEY);
        }

        Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "on start job: " + jobid);
                sendMessage(SCHEDULED_JOB_START, jobid);
                jobFinished(params, false);
            }
        });

        // Return true as there's more work to be done with this job.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();

        if (!extras.containsKey(JOB_ID_KEY)) {
            Log.e(TAG, "jobid is null in stop");
            jobid = 0;
        } else {
            jobid = extras.getInt(JOB_ID_KEY);
        }

        Log.i(TAG, "on stop job: " + jobid);

        // Return false to drop the job.
        return false;
    }

    private void sendMessage(int messageID, int jobid) {
        // If this service is launched by the JobScheduler, there's no callback Messenger. It
        // only exists when the MainActivity calls startService() with the callback in the Intent.
        if (mActivityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.");
            return;
        }
        Message m = Message.obtain();
        m.what = messageID;
        m.arg1 = jobid;
        try {
            mActivityMessenger.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }
}
