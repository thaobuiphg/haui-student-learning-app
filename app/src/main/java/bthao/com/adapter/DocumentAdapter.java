package bthao.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import bthao.com.R;
import bthao.com.database.model.Document;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private List<Document> list;
    private Context context;
    private OnItemActionListener listener;
    public interface OnItemActionListener {
        void onViewClick(Document tl);     // Click tên → Xem
        void onEditClick(Document tl);     // Bút chì → Sửa
        void onDeleteClick(Document tl);   // Thùng rác → Xóa
        void onShareClick(Document tl);    // Nút Share → Chia sẻ ngay
    }

    public DocumentAdapter(Context context, List<Document> list, OnItemActionListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_document, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Document tl = list.get(position);

        holder.tvNameFile.setText(tl.getDocumentName());
        holder.tvSubject.setText(tl.getSubjectName());
        holder.tvType.setText(tl.getFileType());

        holder.layoutClick.setOnClickListener(v -> listener.onViewClick(tl));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(tl));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(tl));
        holder.btnShare.setOnClickListener(v -> listener.onShareClick(tl));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameFile, tvSubject, tvType;
        LinearLayout layoutClick;
        ImageButton btnEdit, btnDelete, btnShare;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameFile = itemView.findViewById(R.id.txtDocumentName);
            tvSubject = itemView.findViewById(R.id.txtSubject);
            tvType = itemView.findViewById(R.id.txtFileType);
            layoutClick = itemView.findViewById(R.id.layoutClick);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
}