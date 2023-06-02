package com.example.task41;

// Property of S221006433- Krunal Hemnani

// Import all important libraries
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import com.example.task41.databinding.ActivityMainBinding;

// Class initialization
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    private CountDownTimer workoutTimer;
    private CountDownTimer restTimer;
    private boolean workoutTimerRunning = false;
    private boolean restTimerRunning = false;
    private static boolean cancel = false;

    int i = 0;

    private NotificationManagerCompat notificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Notifications Section

        notificationManager = NotificationManagerCompat.from(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                long workoutDuration = Long.parseLong(binding.editTextWorkDuration.getText().toString()) * 1000;
                long restDuration = Long.parseLong(binding.editTextRestDuration.getText().toString()) * 1000;
                long rounds = Long.parseLong(binding.editTextRounds.getText().toString());


                workoutTimer = new CountDownTimer(workoutDuration + 25, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        long minutes = (millisUntilFinished / 60000) % 60;
                        long seconds = (millisUntilFinished / 1000) % 60;

                        binding.buttonStart.setClickable(false);
                        binding.buttonStart.setBackgroundColor(Color.YELLOW);

                        workoutTimerRunning = true;

                        binding.imageView.setImageResource(R.drawable.runicon);
                        binding.activityTextView.setText("WORKOUT");

                        binding.countDownTextView.setText(String.format("%02d:%02d", minutes, seconds));

                        int progress = (int) (100 - (millisUntilFinished * 100) / (workoutDuration));
                        binding.progressBar.setProgress(progress);

                        if(cancel == true){
                            restTimer.cancel();
                            workoutTimer.cancel();
                            workoutTimerRunning = false;
                            restTimerRunning = false;
                            cancel = false;
                            binding.buttonStart.setClickable(true);
                            binding.buttonStart.setBackgroundColor(Color.parseColor("#6200ee"));
                            notificationManager.cancel(1);
                        }


                    }

                    @Override
                    public void onFinish() {
                        //Beep-sound addition
                        workoutTimerRunning = false;
                        MediaPlayer.create(MainActivity.this, R.raw.beeptone).start();
                        binding.imageView.setImageResource(R.drawable.resticon2);
                        binding.activityTextView.setText("REST");

                        long minutes = (restDuration / 60000) % 60;
                        long seconds = (restDuration / 1000) % 60;

                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        intent.setAction(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_LAUNCHER);

                        Intent cancelIntent = new Intent(MainActivity.this, NotificationActionService.class)
                                .setAction("cancel");
                        cancelIntent.putExtra("action", "continue");

                        PendingIntent pendingIntentCancel = PendingIntent.getService(MainActivity.this, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
                        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                        //Display notification
                        Notification notification = new NotificationCompat.Builder(MainActivity.this, ChannelBuild.CHANNEL_1_ID)
                                .setSmallIcon(R.drawable.pngegg)
                                .setContentTitle("Begin Rest")
                                .setContentText("Total Rest Time: " + String.format("%02d:%02d", minutes, seconds))
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                .setContentIntent(pendingIntent)
                                .addAction(R.drawable.pngegg,"Cancel Timer", pendingIntentCancel)
                                .build();

                        notificationManager.notify(1,notification);

                        //Rest Timer Section
                        restTimer = new CountDownTimer(restDuration + 50, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                long minutes = (millisUntilFinished / 60000) % 60;
                                long seconds = (millisUntilFinished / 1000) % 60;

                                restTimerRunning = true;

                                binding.countDownTextView.setText(String.format("%02d:%02d", minutes, seconds));

                                int progress = (int) (100 - (millisUntilFinished * 100) / (restDuration));
                                binding.progressBar.setProgress(progress);

                                if(cancel == true){
                                    restTimer.cancel();
                                    workoutTimer.cancel();
                                    workoutTimerRunning = false;
                                    restTimerRunning = false;
                                    cancel = false;
                                    binding.buttonStart.setClickable(true);
                                    binding.buttonStart.setBackgroundColor(Color.parseColor("#6200ee"));
                                    notificationManager.cancel(1);
                                }
                            }

                            @Override
                            public void onFinish() {
                                i++;
                                MediaPlayer.create(MainActivity.this, R.raw.whistletone).start();
                                if (i < rounds) {
                                    workoutTimer.start();
                                        long minutes = (workoutDuration / 60000) % 60;
                                        long seconds = (workoutDuration / 1000) % 60;
                                        Notification notification = new NotificationCompat.Builder(MainActivity.this, ChannelBuild.CHANNEL_1_ID)
                                                .setSmallIcon(R.drawable.pngegg)
                                                .setContentTitle("Begin Workout")
                                                .setContentText("Total Workout Time: " + String.format("%02d:%02d", minutes, seconds))
                                                .setPriority(Notification.PRIORITY_HIGH)
                                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                                .setContentIntent(pendingIntent)
                                                .addAction(R.drawable.pngegg,"Cancel Timer", pendingIntentCancel)
                                                .build();

                                        notificationManager.notify(1,notification);
                                } else {
                                    i = 0;
                                    restTimerRunning = false;
                                    binding.buttonStart.setClickable(true);
                                    binding.buttonStart.setBackgroundColor(Color.parseColor("#6200ee"));
                                }
                            }
                        };
                        restTimer.start();
                    }
                };
                workoutTimer.start();
            }
        });

        binding.buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(workoutTimerRunning == true) {
                    workoutTimer.cancel();
                    workoutTimerRunning = false;
                } else if (restTimerRunning == true) {
                    restTimer.cancel();
                    restTimerRunning = false;
                }
                binding.buttonStart.setClickable(true);
                binding.buttonStart.setBackgroundColor(Color.parseColor("#6200ee"));
            }
        });
    }

    public static class NotificationActionService extends IntentService {
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            String action = intent.getAction();
            if (action == "cancel") {
                cancel = true;
            }
        }
    }
}


