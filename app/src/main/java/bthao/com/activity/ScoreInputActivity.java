package bthao.com.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import bthao.com.R;
import bthao.com.adapter.GoalAdapter;
import bthao.com.database.DatabaseHelper;
import bthao.com.database.model.Goal;
import bthao.com.database.model.Subject;

public class ScoreInputActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";
    public static final String EXTRA_MODE_EDIT = "mode_edit";

    private DatabaseHelper dbHelper;

    private TextInputEditText etTX1, etTX2, etCK;
    private TextView tvTitle, tvWeight, tvGoalsTitle;
    private RecyclerView recyclerGoals;
    private GoalAdapter goalAdapter;
    private List<Goal> goalList = new ArrayList<>();

    private Subject currentSubject;
    private double[] currentWeights;

    private static final double GOAL_A = 8.5;
    private static final double GOAL_B = 7.0;
    private static final double GOAL_C = 5.5;
    private static final double GOAL_D = 4.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_input);

        dbHelper = new DatabaseHelper(this);
        int subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FORM ĐIỀN");

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvTitle = findViewById(R.id.tvTitle);
        tvWeight = findViewById(R.id.tvWeight);
        etTX1 = findViewById(R.id.etTX1);
        etTX2 = findViewById(R.id.etTX2);
        etCK = findViewById(R.id.etCK);
        recyclerGoals = findViewById(R.id.recyclerGoals);
        tvGoalsTitle = findViewById(R.id.tvGoalsTitle);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnLearningPath = findViewById(R.id.btnLearningPath);

        setupGoalsRecyclerView();
        createGoalTemplates();

        if (subjectId != -1) {
            loadSubjectData(subjectId);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID môn học.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập TextWatcher để tự động tính toán và kiểm tra
        TextWatcher scoreTextWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Kiểm tra và ẩn/hiện mục tiêu
                handleScoreChange();
            }
        };

        etTX1.addTextChangedListener(scoreTextWatcher);
        etTX2.addTextChangedListener(scoreTextWatcher);
        etCK.addTextChangedListener(scoreTextWatcher);

        btnSave.setOnClickListener(v -> saveScore());

        btnLearningPath.setOnClickListener(v -> {
            if (currentSubject != null) {
                Intent intent = new Intent(this, RoadmapActivity.class);
                intent.putExtra(RoadmapActivity.EXTRA_SUBJECT_ID, currentSubject.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Không thể mở lộ trình: Dữ liệu môn học chưa được tải.", Toast.LENGTH_SHORT).show();
            }
        });
    }


// Ẩn hiện mục tiêu
    private void handleScoreChange() {
        if (checkIfScoresAreComplete()) {
            tvGoalsTitle.setVisibility(View.GONE);
            recyclerGoals.setVisibility(View.GONE);
        } else {
            tvGoalsTitle.setVisibility(View.VISIBLE);
            recyclerGoals.setVisibility(View.VISIBLE);
            calculateAndDisplayGoals();
        }
    }

    private boolean checkIfScoresAreComplete() {
        double tx1 = parseScore(etTX1.getText().toString());
        double tx2 = parseScore(etTX2.getText().toString());
        double ck = parseScore(etCK.getText().toString());

        return tx1 > 0 && tx2 > 0 && ck > 0;
    }


    private void loadSubjectData(int subjectId) {
        currentSubject = dbHelper.getSubjectById(subjectId);
        currentWeights = dbHelper.getWeightsForSubject(subjectId);

        if (currentSubject != null) {
            tvTitle.setText("ĐIỂM - " + currentSubject.getName());

            etTX1.setText(formatScore(currentSubject.getTx1()));
            etTX2.setText(formatScore(currentSubject.getTx2()));
            etCK.setText(formatScore(currentSubject.getCk()));

            tvWeight.setText(String.format("Trọng số: TX1: %.0f%% | TX2: %.0f%% | CK: %.0f%%",
                    currentWeights[0] * 100, currentWeights[1] * 100, currentWeights[2] * 100));

            handleScoreChange();
        }
    }

    private void saveScore() {
        if (currentSubject == null) return;

        try {
            double tx1 = parseScore(etTX1.getText().toString());
            double tx2 = parseScore(etTX2.getText().toString());
            double ck = parseScore(etCK.getText().toString());

            boolean success = dbHelper.updateScore(currentSubject.getId(), tx1, tx2, ck);

            if (success) {
                Toast.makeText(this, "Đã lưu điểm thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi lưu điểm!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi: Điểm không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }


    private void createGoalTemplates() {
        goalList.clear();
        goalList.add(new Goal("A", GOAL_A));
        goalList.add(new Goal("B", GOAL_B));
        goalList.add(new Goal("C", GOAL_C));
        goalList.add(new Goal("D", GOAL_D));

        if (goalAdapter != null) {
            goalAdapter.notifyDataSetChanged();
        }
    }

    private void setupGoalsRecyclerView() {
        goalAdapter = new GoalAdapter(goalList);
        recyclerGoals.setLayoutManager(new LinearLayoutManager(this));
        recyclerGoals.setAdapter(goalAdapter);
    }

    private void calculateAndDisplayGoals() {
        double tx1 = parseScore(etTX1.getText().toString());
        double tx2 = parseScore(etTX2.getText().toString());

        double weightTX1 = currentWeights != null ? currentWeights[0] : 0.3;
        double weightTX2 = currentWeights != null ? currentWeights[1] : 0.3;
        double weightCK = currentWeights != null ? currentWeights[2] : 0.4;

        double diemDaCo = (tx1 * weightTX1) + (tx2 * weightTX2);

        for (Goal goal : goalList) {
            double targetTBM = goal.requiredTBM;
            double ckCanThiet = (targetTBM - diemDaCo) / weightCK;
            goal.updateRequiredCK(ckCanThiet);
        }

        if (goalAdapter != null) {
            goalAdapter.notifyDataSetChanged();
        }
    }


    private double parseScore(String scoreStr) {
        if (scoreStr == null) return 0;

        scoreStr = scoreStr.replace(",", ".");

        try {
            double score = Double.parseDouble(scoreStr);
            return Math.max(0, Math.min(score, 10.0));
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    private String formatScore(double score) {
        if (score == 0.0) return "";
        return String.format("%.1f", score);
    }
}