package bthao.com.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import bthao.com.R;
import bthao.com.database.model.Roadmapweek;

public class RoadmapAdapter extends RecyclerView.Adapter<RoadmapAdapter.RoadmapViewHolder> {

    private final List<Roadmapweek> roadmapList;

    public RoadmapAdapter(List<Roadmapweek> roadmapList) {
        this.roadmapList = roadmapList;
    }

    @NonNull
    @Override
    public RoadmapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_roadmap_week, parent, false);
        return new RoadmapViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoadmapViewHolder holder, int position) {
        Roadmapweek week = roadmapList.get(position);
        holder.tvWeekNumber.setText(String.format("Tuần %d:", week.getWeekNumber()));
        holder.tvWeekContent.setText(week.getContent());
    }

    @Override
    public int getItemCount() {
        return roadmapList.size();
    }

    public static class RoadmapViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekNumber, tvWeekContent;
        public RoadmapViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekNumber = itemView.findViewById(R.id.tvWeekNumber);
            tvWeekContent = itemView.findViewById(R.id.tvWeekContent);
        }
    }
}