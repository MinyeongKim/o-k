package com.example.o_k.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

import static android.content.Context.MODE_PRIVATE;


public class JobSchedulerStart {
    private static final int JOB_ID = 1111;
    private static FirebaseJobDispatcher dispatcher;
    private static Context Mycontext;

    public static void start(Context context) {
        Mycontext = context;
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        Job myJob = dispatcher.newJobBuilder()
                .setService(NotificationJobFireBaseService.class) // 잡서비스 등록
                .setTag("TSLetterNotification")        // 태그 등록
                .setRecurring(true) //재활용
                .setLifetime(Lifetime.FOREVER) //다시켜도 작동을 시킬껀지?
                .setTrigger(Trigger.executionWindow(0, 60)) //트리거 시간
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();
        dispatcher.mustSchedule(myJob);
        SharedPreferences sh_Pref = Mycontext.getSharedPreferences("Time", MODE_PRIVATE);
        SharedPreferences.Editor toEdit = sh_Pref.edit();
        toEdit.putInt("alert", 1);
        toEdit.apply();
    }

    public static void destroy() {
        dispatcher.cancelAll();
        SharedPreferences sh_Pref = Mycontext.getSharedPreferences("Time", MODE_PRIVATE);
        SharedPreferences.Editor toEdit = sh_Pref.edit();
        toEdit.remove("alert");
        toEdit.apply();
    }
}