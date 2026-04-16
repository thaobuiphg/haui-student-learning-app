package bthao.com.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import bthao.com.R;
import bthao.com.adapter.RoadmapAdapter;
import bthao.com.database.model.Roadmapweek;

public class RoadmapActivity extends AppCompatActivity {

    public static final String EXTRA_SUBJECT_ID = "subject_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning_roadmap);

        Toolbar toolbar = findViewById(R.id.toolbarRoadmap);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("LỘ TRÌNH HỌC TẬP");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        int subjectId = 0;
        Intent intent = getIntent();
        if (intent != null) {
            subjectId = intent.getIntExtra(EXTRA_SUBJECT_ID, 0);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerRoadmap);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Roadmapweek> roadmapList = loadRoadmapFromRawFile(subjectId);

        if (roadmapList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy lộ trình hoặc dữ liệu bị lỗi/trống.", Toast.LENGTH_LONG).show();
        }

        RoadmapAdapter adapter = new RoadmapAdapter(roadmapList);
        recyclerView.setAdapter(adapter);
    }

    private List<Roadmapweek> loadRoadmapFromRawFile(int subjectId) {
        List<Roadmapweek> list = new ArrayList<>();
        String filename;

        if (subjectId == 1) {
            filename = "toan_cao_cap";
        } else if (subjectId == 2) {
            filename = "vat_ly_dai_cuong";
        } else if (subjectId == 3) {
            filename ="lap_trinh_java";
        } else if (subjectId == 4) {
            filename = "co_so_du_lieu";
        }else if (subjectId == 5) {
            filename = "tieng_anh_1";
        }
        else {
            Toast.makeText(this, "Môn học này chưa có file lộ trình cố định (ID: " + subjectId + ").", Toast.LENGTH_LONG).show();
            return list;
        }

        int resourceId = getResources().getIdentifier(filename, "raw", getPackageName());

        if (resourceId == 0) {
            Toast.makeText(this, "LỖI: File " + filename + ".txt không tìm thấy trong res/raw!", Toast.LENGTH_LONG).show();
            return list;
        }

        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = getResources().openRawResource(resourceId);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("-", 2);

                if (parts.length == 2) {
                    try {
                        int weekNumber = Integer.parseInt(parts[0].trim());
                        String content = parts[1].trim();

                        list.add(new Roadmapweek(weekNumber, content));
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "LỖI: Tuần không phải là số trong file.", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "LỖI: Định dạng dòng thiếu dấu '-' trong file.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "LỖI ĐỌC FILE: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}