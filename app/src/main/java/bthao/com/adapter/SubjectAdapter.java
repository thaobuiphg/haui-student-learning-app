package bthao.com.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

import bthao.com.R;
import bthao.com.activity.ScoreInputActivity;
import bthao.com.database.model.Subject;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<Subject> subjectList = new ArrayList<>();
    private OnDeleteListener deleteListener;

    public SubjectAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void updateData(List<Subject> newList) {
        this.subjectList.clear();
        if (newList != null) {
            this.subjectList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject s = subjectList.get(position);
        holder.tvSubjectName.setText(s.getName());
        holder.tvTX1.setText(String.format("%.1f", s.getTx1()));
        holder.tvTX2.setText(String.format("%.1f", s.getTx2()));
        holder.tvCK.setText(String.format("%.1f", s.getCk()));

        double tbm = s.getTbm();

        if (s.getTx1() == 0 && s.getTx2() == 0 && s.getCk() == 0) tbm = 0;

        holder.tvTBM.setText(String.format("%.1f", tbm));

        holder.tvTBM.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                tbm >= 8.0 ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        boolean daNhapDu = (s.getTx1() > 0 && s.getTx2() > 0 && s.getCk() > 0);
        holder.btnAddScore.setVisibility(daNhapDu ? View.GONE : View.VISIBLE);
        holder.btnEditScore.setVisibility(daNhapDu ? View.VISIBLE : View.GONE);

        holder.btnAddScore.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ScoreInputActivity.class);
            intent.putExtra(ScoreInputActivity.EXTRA_SUBJECT_ID, s.getId());
            v.getContext().startActivity(intent);
        });

        holder.btnEditScore.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ScoreInputActivity.class);
            intent.putExtra(ScoreInputActivity.EXTRA_SUBJECT_ID, s.getId());
            v.getContext().startActivity(intent);
        });

        holder.btnDeleteSubject.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteSubject(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvTX1, tvTX2, tvCK, tvTBM;
        MaterialButton btnAddScore, btnEditScore, btnDeleteSubject;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvTX1 = itemView.findViewById(R.id.tvTX1);
            tvTX2 = itemView.findViewById(R.id.tvTX2);
            tvCK = itemView.findViewById(R.id.tvCK);
            tvTBM = itemView.findViewById(R.id.tvTBM);
            btnAddScore = itemView.findViewById(R.id.btnAddScore);
            btnEditScore = itemView.findViewById(R.id.btnEditScore);
            btnDeleteSubject = itemView.findViewById(R.id.btnDeleteSubject);
        }
    }

    public interface OnDeleteListener {
        void onDeleteSubject(int position);
    }
}