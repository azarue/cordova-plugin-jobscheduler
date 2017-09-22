cordova-plugin-jobscheduler 
=======

##Forked version by Ronald Crooy:

 - adding `setPeriodic` for newer phones
 - clean up code a bit


## How to use

Example:

    cordova.plugins.jobScheduler.schedule({
       interval: 10, // seconds
       requiredNetworkType: 1,
       requiresDeviceIdle: 1,
       requiresCharging: 1,       
       overrideDeadline: 10, //seconds, pre android 7.0 phone only
       callback: function() { } // what to do when the job is fired
     });
     
     
Provides an interface to [android job scheduler](https://developer.android.com/reference/android/app/job/JobScheduler.html).
   
 Heavily based on [Google's Android JobScheduler Sample
](https://github.com/googlesamples/android-JobScheduler) and [cordova-plugin-background-mode](https://github.com/katzer/cordova-plugin-background-mode)
