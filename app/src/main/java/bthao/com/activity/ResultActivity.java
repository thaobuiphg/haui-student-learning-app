package bthao.com.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

import bthao.com.R;
import bthao.com.adapter.SubjectAdapter;
import bthao.com.database.DatabaseHelper;
import bthao.com.database.model.Subject;
import bthao.com.dialog.AddSubjectDialog;

public class ResultActivity extends AppCompatActivity implements SubjectAdapter.OnDeleteListener {

    private RecyclerView recyclerSubjects;
    private SubjectAdapter adapter;
    private List<Subject> subjectList;
    private TextView tvAverage, tvRank;
    private AutoCompleteTextView spinnerSemester;

    private DatabaseHelper dbHelper;
    private int currentHocKy = 1;

    private final String[] hocKyArray = {
            "Học kỳ 1", "Học kỳ 2", "Học kỳ 3", "Học kỳ 4",
            "Học kỳ 5", "Học kỳ 6", "Học kỳ 7", "Học kỳ 8"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("KẾT QUẢ HỌC TẬP");
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
        dbHelper = new DatabaseHelper(this);
        recyclerSubjects = findViewById(R.id.recyclerSubjects);
        tvAverage = findViewById(R.id.tvAverage);
        tvRank = findViewById(R.id.tvRank);
        spinnerSemester = findViewById(R.id.spinnerSemester);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        dbHelper.createMissingDiemSoRecords();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, hocKyArray);
        spinnerSemester.setAdapter(spinnerAdapter);
        spinnerSemester.setText(hocKyArray[0], false);

        spinnerSemester.setOnItemClickListener((parent, view, position, id) -> {
            currentHocKy = position + 1;
            loadCurrentSemester();
        });

        recyclerSubjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SubjectAdapter(this);
        recyclerSubjects.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            AddSubjectDialog dialog = new AddSubjectDialog();
            dialog.setListener((name, credits, weightTX1, weightTX2, weightCK) -> {
                long result = dbHelper.addMonHocWithWeights(
                        name,
                        credits,
                        currentHocKy,
                        weightTX1,
                        weightTX2,
                        weightCK
                );

                if (result != -1) {
                    Toast.makeText(this, "Đã thêm môn: " + name, Toast.LENGTH_SHORT).show();
                    loadCurrentSemester(); // reload lại danh sách
                } else {
                    Toast.makeText(this, "Lỗi khi thêm môn!", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show(getSupportFragmentManager(), "add_subject");
        });
        Button btnStatistic = findViewById(R.id.btnStatistic);
        btnStatistic.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, StatActivity.class);
            intent.putExtra("hocKy", currentHocKy); // nếu muốn truyền học kỳ
            startActivity(intent);
        });

    }
    private void loadCurrentSemester() {
//        Log.d("RESULT_ACTIVITY", "loadCurrentSemester called - HocKy: " + currentHocKy);
        subjectList = dbHelper.getSubjectsByHocKy(currentHocKy);
//        Log.d("RESULT_ACTIVITY", "Loaded " + subjectList.size() + " subjects");
        adapter.updateData(subjectList);
        updateAverage();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteSubject(int position) {
        if (subjectList != null && position >= 0 && position < subjectList.size()) {
            Subject subjectToDelete = subjectList.get(position);

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("XÁC NHẬN XÓA")
                    .setMessage("Bạn có chắc chắn muốn xóa môn học: " + subjectToDelete.getName() + " không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        boolean isDeleted = dbHelper.deleteSubject(subjectToDelete.getId());

                        if (isDeleted) {
                            Toast.makeText(this, "Đã xóa môn: " + subjectToDelete.getName(), Toast.LENGTH_SHORT).show();
                            loadCurrentSemester();
                        } else {
                            Toast.makeText(this, "Lỗi: Không thể xóa môn học!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .show();
        }
    }

    private void updateAverage() {
        if (subjectList == null || subjectList.isEmpty()) {
            tvAverage.setText("Điểm trung bình kỳ: 0.00/4");
            tvRank.setText("Xếp loại: Chưa có");
            return;
        }

        double totalPoints = 0;
        int totalCredits = 0;
        for (Subject s : subjectList) {
            totalPoints += s.getTbm4() * s.getCredits();
            totalCredits += s.getCredits();
        }
        double avg4 = totalCredits > 0 ? totalPoints / totalCredits : 0;

        String rank = avg4 >= 3.6 ? "Xuất sắc" :
                avg4 >= 3.2 ? "Giỏi" :
                        avg4 >= 2.5 ? "Khá" :
                                avg4 >= 2.0 ? "Trung bình" :
                                        avg4 >= 1.0 ? "Yếu" : "Kém";

        tvAverage.setText(String.format("Điểm trung bình kỳ: %.2f/4", avg4));
        tvRank.setText("Xếp loại: " + rank);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCurrentSemester();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        loadCurrentSemester();
    }

}