package bthao.com.activity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import bthao.com.R;
import bthao.com.adapter.ScheduleAdapter;
import bthao.com.database.DatabaseHelper;
import bthao.com.database.model.Schedule;
import bthao.com.dialog.AddScheduleDialog;
import bthao.com.dialog.EditScheduleDialog;
import bthao.com.receiver.RemindReceiver;

public class ScheduleActivity extends AppCompatActivity implements ScheduleAdapter.OnScheduleListener {

    private DatabaseHelper db;
    private ScheduleAdapter adapter;
    private TextView tvWeek;
    private Calendar currentCalendar;
    private SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMM dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        db = new DatabaseHelper(this);
        currentCalendar = Calendar.getInstance();

        RecyclerView rv = findViewById(R.id.rvSchedule);
        adapter = new ScheduleAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        tvWeek = findViewById(R.id.tvWeek);
        Button btnPrev = findViewById(R.id.btnPrevWeek);
        Button btnNext = findViewById(R.id.btnNextWeek);
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_YEAR, -7);
            updateWeek();
        });

        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.DAY_OF_YEAR, 7);
            updateWeek();
        });

        btnAdd.setOnClickListener(v -> {
            new AddScheduleDialog(s -> {
                db.insertSchedule(s);
                updateWeek();
//                Log.d("DEBUG", "dateStr = '" + s.getDate() + "'");
//                Log.d("DEBUG", "timeStr = '" + s.getStartTime() + "'");
                long startTimeMillis = parseDateTime(s.getDate(), s.getStartTime());
                setReminder(startTimeMillis, s.getTitle(), s.getId());
            }).show(getSupportFragmentManager(), "add");
        });
//        Button btnTest = findViewById(R.id.btnTest);
//        btnTest.setOnClickListener(v -> {
//            setReminder(System.currentTimeMillis() + 5000, "Test 5s");
//        });
//        SQLiteDatabase dbFix = db.getWritableDatabase();
//        Cursor c = dbFix.rawQuery("SELECT maThoiGianBieu, gioBatDau, gioKetThuc FROM ThoiGianBieu", null);
//        if (c.moveToFirst()) {
//            do {
//                int id = c.getInt(0);
//                String start = c.getString(1);
//                String end = c.getString(2);
//
//                String newStart = extractTimeOnly(start);
//                String newEnd = extractTimeOnly(end);
//
//                if (!newStart.equals(start) || !newEnd.equals(end)) {
//                    ContentValues cv = new ContentValues();
//                    cv.put("gioBatDau", newStart);
//                    cv.put("gioKetThuc", newEnd);
//                    dbFix.update("ThoiGianBieu", cv, "maThoiGianBieu = ?", new String[]{String.valueOf(id)});
//                    Log.d("DB_FIX", "Sửa ID " + id + ": " + start + " → " + newStart);
//                }
//            } while (c.moveToNext());
//        }
//        c.close();
        requestNotificationPermission();
        requestExactAlarmPermission();
        requestIgnoreBatteryOptimizations();
        createChannel();
        updateWeek();
    }
//    private String extractTimeOnly(String timeStr) {
//        if (timeStr == null || timeStr.isEmpty()) return "";
//        String cleaned = timeStr.trim();
//        int spaceIndex = cleaned.indexOf(' ');
//        if (spaceIndex != -1) {
//            cleaned = cleaned.substring(spaceIndex + 1);
//        }
//        return cleaned.replace(":", "").substring(0, Math.min(4, cleaned.replace(":", "").length()));
//    }
    private long parseDateTime(String dateStr, String timeStr) {
        Log.d("PARSE_INPUT", "→ date: '" + dateStr + "', time: '" + timeStr + "'");
        try {

            int hour, minute;
            if (timeStr.length() == 3) {
                return -1;
            }

            if (timeStr.length() == 4) {
                hour = Integer.parseInt(timeStr.substring(0, 2));
                minute = Integer.parseInt(timeStr.substring(2, 4));
            } else {
                return -1;
            }

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return -1;

            String time24 = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            String dateTimeStr = dateStr + " " + time24;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            return sdf.parse(dateTimeStr).getTime();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        android.Manifest.permission.POST_NOTIFICATIONS
                }, 1001);
            }
        }
    }
    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
    private void requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    private void updateWeek() {
        Calendar start = getStartOfWeek(currentCalendar);
        Calendar end = getEndOfWeek(currentCalendar);

        List<Schedule> allInWeek = new ArrayList<>();
        Calendar temp = (Calendar) start.clone();

        while (!temp.after(end)) {
            String dateStr = sdfDate.format(temp.getTime());
            List<Schedule> dayList = db.getScheduleByDate(dateStr);
            if (dayList != null) {
                allInWeek.addAll(dayList);
            }
            temp.add(Calendar.DAY_OF_YEAR, 1);
        }

        tvWeek.setText(sdfDisplay.format(start.getTime()) + " - " + sdfDisplay.format(end.getTime()));
        adapter.setData(allInWeek);
//        Log.d("DB_CHECK", "=== TUẦN: " + tvWeek.getText() + " ===");
//        for (Schedule s : allInWeek) {
//            Log.d("DB_CHECK", "ID: " + s.getId() +
//                    " | Title: '" + s.getTitle() +
//                    "' | Content: '" + s.getContent() +
//                    "' | Date: '" + s.getDate() +
//                    "' | Start: '" + s.getStartTime() +
//                    "' | End: '" + s.getEndTime() +
//                    "' | Loc: '" + s.getLocation() +
//                    "' | Note: '" + s.getNote() +
//                    "' | Type: '" + s.getType() + "'");
//        }
    }

    private Calendar getStartOfWeek(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return c;
    }

    private Calendar getEndOfWeek(Calendar cal) {
        Calendar c = (Calendar) cal.clone();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return c;
    }

    @Override
    public void onEdit(Schedule s) {
        new EditScheduleDialog(s, updated -> {
            cancelReminder(s.getId());
            db.updateSchedule(updated);
            updateWeek();
            Log.d("DEBUG", "dateStr = '" + s.getDate() + "'");
            Log.d("DEBUG", "timeStr = '" + s.getStartTime() + "'");
            long startTimeMillis = parseDateTime(updated.getDate(), updated.getStartTime());
            setReminder(startTimeMillis, updated.getTitle(), updated.getId());
        }).show(getSupportFragmentManager(), "edit");
    }

    @Override
    public void onDelete(Schedule s) {
        cancelReminder(s.getId());
        db.deleteSchedule(s.getId());
        updateWeek();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "reminder_channel",
                    "Nhắc lịch",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc lịch học tập");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
            Log.d("NOTIF", "Kênh thông báo đã tạo");
        }
    }

private void setReminder(long time, String title, int scheduleId) {
    long reminderTime = time - 60 * 60 * 1000; // 1 giờ trước

//    Log.d("ALARM", "Sự kiện: " + title);
//    Log.d("ALARM", "Thời gian sự kiện: " + new java.util.Date(time));
//    Log.d("ALARM", "Thời gian nhắc: " + new java.util.Date(reminderTime));
//    Log.d("ALARM", "Hiện tại: " + new java.util.Date(System.currentTimeMillis()));

    if (reminderTime < System.currentTimeMillis()) {
        Log.e("ALARM", "Reminder quá khứ → bỏ qua");
        return;
    }

    Intent intent = new Intent(this, RemindReceiver.class);
    intent.putExtra("title", title);
    intent.putExtra("scheduleId", scheduleId);

    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this,
            scheduleId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
    );

    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            pendingIntent
    );

    Log.d("ALARM", "Đặt nhắc lúc: " + new java.util.Date(reminderTime));
}
    private void cancelReminder(int scheduleId) {
        Intent intent = new Intent(this, RemindReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                scheduleId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        Log.d("ALARM", "Hủy nhắc ID: " + scheduleId);
    }

}