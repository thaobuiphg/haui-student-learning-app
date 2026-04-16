package bthao.com.dialog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import bthao.com.R;
import bthao.com.database.model.Schedule;

public class EditScheduleDialog extends DialogFragment {

    private Schedule schedule;
    private OnScheduleSaved listener;

    public interface OnScheduleSaved {
        void onSave(Schedule s);
    }

    public EditScheduleDialog(Schedule schedule, OnScheduleSaved listener) {
        this.schedule = schedule;
        this.listener = listener;
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        var view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_schedule, null);

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etContent = view.findViewById(R.id.etContent);
        EditText etDate = view.findViewById(R.id.etDate);
        EditText etStart = view.findViewById(R.id.etStartTime);
        EditText etEnd = view.findViewById(R.id.etEndTime);
        EditText etLocation = view.findViewById(R.id.etLocation);
        EditText etNote = view.findViewById(R.id.etNote);

        etTitle.setText(schedule.getTitle());
        etContent.setText(schedule.getContent());
        etStart.setText(extractTime(schedule.getStartTime()));
        etEnd.setText(extractTime(schedule.getEndTime()));
        etLocation.setText(schedule.getLocation());
        etNote.setText(schedule.getNote());
        String[] dateParts = schedule.getDate().split("-");
        String displayDate = String.format("%s/%s/%s", dateParts[2], dateParts[1], dateParts[0]);
        etDate.setText(displayDate);
        Calendar cal = parseDate(schedule.getDate());
        setupDateTimePickers(etDate, etStart, etEnd, cal);
        Spinner spinnerType = view.findViewById(R.id.spinnerType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.schedule_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        String currentType = schedule.getType();
        int position = adapter.getPosition(currentType);
        if (position >= 0) {
            spinnerType.setSelection(position);
        }
        builder.setView(view)
                .setTitle("Chỉnh sửa sự kiện")
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        Button btnSave = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnSave.setOnClickListener(v -> {
            String selectedType = spinnerType.getSelectedItem().toString();

            Schedule s = validateAndCreateSchedule(etTitle, etContent, etDate, etStart, etEnd, etLocation, etNote);
            if (s != null) {
                s.setType(selectedType);
                listener.onSave(s);
                dialog.dismiss();
            }
        });

        return dialog;
    }
    private String extractTime(String time24) {
        if (time24 == null) return "??:??";
        String cleaned = time24.trim().replace(":", "");
        if (cleaned.length() != 4 || !cleaned.matches("\\d{4}")) {
            return "??:??";
        }
        try {
            int hour = Integer.parseInt(cleaned.substring(0, 2));
            int minute = Integer.parseInt(cleaned.substring(2, 4));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return "??:??";
            return String.format("%02d:%02d", hour, minute);
        } catch (Exception e) {
            return "??:??";
        }
    }

    private Calendar parseDate(String dateStr) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            cal.setTime(sdf.parse(dateStr));
        } catch (Exception e) {
            // fallback
        }
        return cal;
    }

    private void setupDateTimePickers(EditText etDate, EditText etStart, EditText etEnd, Calendar cal) {
        etDate.setOnClickListener(v -> new DatePickerDialog(getContext(),
                (v2, y, m, d) -> etDate.setText(String.format("%02d/%02d/%d", d, m + 1, y)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show());

        etStart.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (v2, h, m) -> etStart.setText(String.format("%02d:%02d", h, m)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show());

        etEnd.setOnClickListener(v -> new TimePickerDialog(getContext(),
                (v2, h, m) -> etEnd.setText(String.format("%02d:%02d", h, m)),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show());
    }

    private Schedule validateAndCreateSchedule(EditText... fields) {
        String title = fields[0].getText().toString().trim();
        String content = fields[1].getText().toString().trim();
        String dateInput = fields[2].getText().toString(); // "06/12/2025"
        String start = fields[3].getText().toString();
        String end = fields[4].getText().toString();
        String location = fields[5].getText().toString().trim();
        String note = fields[6].getText().toString().trim();

        if (title.isEmpty() || dateInput.isEmpty() || start.isEmpty() || end.isEmpty()) {
            return null;
        }

        String[] parts = dateInput.split("/");
        if (parts.length != 3) return null;
        String formattedDate;
        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);
            formattedDate = String.format("%d-%02d-%02d", year, month, day);
        } catch (Exception e) {
            return null;
        }

        Schedule s = new Schedule();
        s.setId(this.schedule.getId());
        s.setTitle(title);
        s.setContent(content);
        s.setDate(formattedDate);
        s.setStartTime(start.replace(":", ""));
        s.setEndTime(end.replace(":", ""));
        s.setLocation(location);
        s.setNote(note);
        s.setType("Event");
        return s;
    }
}