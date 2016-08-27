package com.ferran.photogallery.Presenter;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.ferran.photogallery.Model.GalleryItem;
import com.ferran.photogallery.R;

import java.util.List;

public class PollService2 extends JobService {
    private static final String TAG = "PollService2";
    private static final int JOB_ID = 1;
    public static final String ACTION_SHOW_NOTIFICATION =
        "com.ferran.android.presenter.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE =
        "com.ferran.photogallery.Presenter.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";
    private PollTask mCurrentTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return true;

    }

    public static void setService(Context context, boolean isOn) {

        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        JobInfo jobInfo = new JobInfo.Builder(JOB_ID, new ComponentName(context, PollService2.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
            .setPeriodic(1000 * 30)
            .setPersisted(false)
            .build();
        if (isOn) {
            scheduler.schedule(jobInfo);
            QueryPreferences.setAlarmOn(context, isOn);
        } else {
            scheduler.cancel(JOB_ID);
            jobInfo = null;
        }
    }

    public static boolean isService(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for (JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == JOB_ID) {
                hasBeenScheduled = true;
            }
        }
        return hasBeenScheduled;
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    private class PollTask extends AsyncTask<JobParameters, Void, Void> {
        @Override
        protected Void doInBackground(JobParameters... params) {
            JobParameters jobParameters = params[0];

            // poll flickr for new images
            String query = QueryPreferences.getStoredQuery(getApplicationContext());
            String lastResultId = QueryPreferences.getLastResultId(getApplicationContext());
            List<GalleryItem> items;

            if (query == null) {
                items = new FlickrFetchr().fetchRecentPhotos();
            } else {
                items = new FlickrFetchr().searchPhotos(query);
            }

            if (items.size() == 0) {
                return null;
            }

            String resultId = items.get(0).getId();
            if (resultId.equals(lastResultId)) {
                Log.i(TAG, "Got an old result: " + resultId);
            } else {
                Log.i(TAG, "Got a new result:" + resultId);

                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(getApplicationContext());
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);
                Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
                showBackgroundNotification(0, notification);
            }
            QueryPreferences.setLastResultId(getApplicationContext(), resultId);


            jobFinished(jobParameters, false);
            return null;
        }
    }
}

