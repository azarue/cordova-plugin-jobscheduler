cordova-plugin-jobscheduler (WIP)
=======

Provides an interface to [android job scheduler](https://developer.android.com/reference/android/app/job/JobScheduler.html).

Example:

    cordova.plugins.jobScheduler.schedule({
       minimumLatency: 10, // seconds
       overrideDeadline: 10, //seconds
       callback: function() { } // what to do when the job is fired
     });
   
  Heavily based on [Google's Android JobScheduler Sample
](https://github.com/googlesamples/android-JobScheduler) and [cordova-plugin-background-mode](https://github.com/katzer/cordova-plugin-background-mode)
