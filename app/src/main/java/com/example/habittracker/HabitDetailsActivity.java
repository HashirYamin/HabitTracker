package com.example.habittracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class HabitDetailsActivity extends AppCompatActivity {

    private TextView tvHabitName, tvCompletionHistory;
    private RelativeLayout btnMarkComplete, btnDeleteHabit, btnSetReminder;
    private EditText etReminderTime;
    private DatabaseReference habitReference;
    private String habitId;
    private String habitName;

    public static final String CHANNEL_ID = "My Channel";
    public static final int NOTIFICATION_ID = 100;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_details);

        // Initialize views
        tvHabitName = findViewById(R.id.tvHabitDetailsName);
        tvCompletionHistory = findViewById(R.id.tvCompletionHistory);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        btnDeleteHabit = findViewById(R.id.btnDeleteHabit);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        etReminderTime = findViewById(R.id.etReminderTime);

        habitName = getIntent().getStringExtra("habitName");
        habitId = getIntent().getStringExtra("habitId");
        habitReference = FirebaseDatabase.getInstance().getReference("Habits").child(habitId);

        // Check and request notification permission
        requestNotificationPermission();

        // Load habit details
        loadHabitDetails();

        // Set up listeners
        btnSetReminder.setOnClickListener(view -> setReminder());
        btnMarkComplete.setOnClickListener(view -> markHabitAsComplete());
        btnDeleteHabit.setOnClickListener(view -> deleteHabit());
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 or higher
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    private void loadHabitDetails() {
        habitReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Habit habit = task.getResult().getValue(Habit.class);
                if (habit != null) {
                    tvHabitName.setText(habit.getName());
                    loadCompletionHistory();
                }
            } else {
                Toast.makeText(this, "Habit not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setReminder() {
        String reminderTimeStr = etReminderTime.getText().toString().trim();
        if (reminderTimeStr.isEmpty()) {
            Toast.makeText(this, "Please enter a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] timeParts = reminderTimeStr.split(":");
        if (timeParts.length != 2) {
            Toast.makeText(this, "Invalid time format", Toast.LENGTH_SHORT).show();
            return;
        }

        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager.canScheduleExactAlarms()) {
                scheduleExactAlarm(calendar);
            } else {
                Toast.makeText(this, "Enable exact alarms in settings to use reminders", Toast.LENGTH_LONG).show();
            }
        } else {
            scheduleExactAlarm(calendar);
        }
    }

    private void scheduleExactAlarm(Calendar calendar) {
        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Reminder set for " + calendar.getTime(), Toast.LENGTH_SHORT).show();
        }
    }


    private void loadCompletionHistory() {
        habitReference.child("completionHistory").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                StringBuilder history = new StringBuilder();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String date = snapshot.getValue(String.class);
                    history.append("- ").append(date).append("\n");
                }
                tvCompletionHistory.setText(history.length() > 0 ? history.toString() : "No completion history yet.");
            } else {
                Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markHabitAsComplete() {
        String completionDate = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        habitReference.child("completionHistory").push().setValue(completionDate).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Habit marked as completed!", Toast.LENGTH_SHORT).show();
                loadCompletionHistory();
            } else {
                Toast.makeText(this, "Failed to mark habit as completed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteHabit() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    habitReference.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Habit deleted successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Failed to delete habit", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
