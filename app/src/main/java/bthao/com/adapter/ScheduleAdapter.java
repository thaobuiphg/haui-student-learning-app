package bthao.com.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bthao.com.R;
import bthao.com.database.model.Schedule;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    private List<Schedule> list = new ArrayList<>();
    private OnScheduleListener listener;
    private SimpleDateFormat dateInput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat dateOutput = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());

    public ScheduleAdapter(OnScheduleListener listener) {
        this.listener = listener;
    }

    public void setData(List<Schedule> newList) {
        this.list = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int p) {
        Schedule s = list.get(p);
        String type = s.getType();
        if (type != null && !type.isEmpty()) {
            h.tvTitle.setText(s.getTitle() + " [" + type + "]");
        } else {
            h.tvTitle.setText(s.getTitle() != null ? s.getTitle() : "No Title");
        }
        CardView card = h.itemView.findViewById(R.id.cardScheduleItem);

        if (card != null) {
            switch (s.getType()) {
                case "Lịch học":
                    card.setCardBackgroundColor(0xFFE3F2FD);
                    break;
                case "Lịch thi":
                    card.setCardBackgroundColor(0xFFFFF3E0);
                    break;
                case "Hoạt động ngoại khóa":
                    card.setCardBackgroundColor(0xFFFFEBEE);
                    break;
                default:
                    card.setCardBackgroundColor(0xFFFDE7FF);
                    break;
            }
        }
        h.tvContent.setText(s.getContent());
        String displayDate = formatDate(s.getDate());
        h.tvDate.setText(displayDate);

        h.tvTime.setText(extractTime(s.getStartTime()) + " - " + extractTime(s.getEndTime()));

        h.tvLocation.setText(s.getLocation() != null ? s.getLocation() : "No Location");
        h.tvNote.setText(s.getNote() != null ? s.getNote() : "No Note");

//        h.itemView.setOnLongClickListener(v -> {
//            new AlertDialog.Builder(v.getContext())
//                    .setTitle(s.getTitle())
//                    .setItems(new String[]{"Sửa", "Xóa"}, (dialog, which) -> {
//                        if (which == 0) {
//                            listener.onEdit(s);
//                        } else {
//                            new AlertDialog.Builder(v.getContext())
//                                    .setMessage("Xóa \"" + s.getTitle() + "\"?")
//                                    .setPositiveButton("Xóa", (d, w) -> listener.onDelete(s))
//                                    .setNegativeButton("Hủy", null)
//                                    .show();
//                        }
//                    })
//                    .show();
//            return true;
//        });
        h.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.menu_schedule_item, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int id = menuItem.getItemId();
                if (id == R.id.action_edit) {
                    listener.onEdit(s);
                    return true;
                } else if (id == R.id.action_delete) {
                    new AlertDialog.Builder(v.getContext())
                            .setMessage("Xóa \"" + s.getTitle() + "\"?")
                            .setPositiveButton("Xóa", (d, w) -> listener.onDelete(s))
                            .setNegativeButton("Hủy", null)
                            .show();
                    return true;
                }
                return false;
            });

            popup.show();
            return true;
        });

    }
    private String extractTime(String time24) {
        if (time24 == null || time24.isEmpty()) return "??:??";

        String cleaned = time24.trim();

        int spaceIndex = cleaned.indexOf(' ');
        if (spaceIndex != -1) {
            cleaned = cleaned.substring(spaceIndex + 1); // "16:00"
        }

        cleaned = cleaned.replace(":", "");

        if (cleaned.length() != 4 || !cleaned.matches("\\d{4}")) {
            return "??:??";
        }

        try {
            int hour = Integer.parseInt(cleaned.substring(0, 2));
            int minute = Integer.parseInt(cleaned.substring(2, 4));
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return "??:??";
            }
            return String.format("%02d:%02d", hour, minute);
        } catch (Exception e) {
            return "??:??";
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || !dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) return "Unknown";
        try {
            return dateOutput.format(dateInput.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }
    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvDate, tvTime, tvLocation, tvNote;
        ViewHolder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvContent = v.findViewById(R.id.tvContent);
            tvDate = v.findViewById(R.id.tvDate);
            tvTime = v.findViewById(R.id.tvTime);
            tvLocation = v.findViewById(R.id.tvLocation);
            tvNote = v.findViewById(R.id.tvNote);
        }
    }

    public interface OnScheduleListener {
        void onEdit(Schedule s);
        void onDelete(Schedule s);
    }
}