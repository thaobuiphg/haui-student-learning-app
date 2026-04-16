package bthao.com.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import bthao.com.R;
import bthao.com.database.DatabaseHelper;
import bthao.com.database.model.Subject;

public class CompareActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText edtGPAKy, edtGPAKyTruoc, edtBienDong;
    private  LineChart lineChart;
    private BarChart barChart;
    private DatabaseHelper dbHelper;
    private View lineChartPlaceholder, barChartPlaceholder;
    private int kyHienTai, kyTruoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compare);

        dbHelper = new DatabaseHelper(this);

        initViews();
        setupListeners();

        kyHienTai = getIntent().getIntExtra("kyHienTai", 1);
        kyTruoc=kyHienTai -1;
        if (kyTruoc < 1) {
            Toast.makeText(this, "Không có kỳ trước để so sánh!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        loadData();
      drawCharts();
    }

    private void drawCharts() {
        int[] prevLetter = demXepLoai(kyTruoc);
        int[] currLetter = demXepLoai(kyHienTai);
        String[] labels = {"A", "B+", "B", "C+", "C", "D+", "D", "F"};

        ArrayList<Entry> linePrev = new ArrayList<>();
        ArrayList<Entry> lineCurr = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            linePrev.add(new Entry(i, prevLetter[i]));
            lineCurr.add(new Entry(i, currLetter[i]));
        }

        LineDataSet setPrev = new LineDataSet(linePrev, "Kỳ trước");
        setPrev.setColor(0xFF64B5F6);
        setPrev.setCircleColor(0xFF2196F3);
        setPrev.setLineWidth(3f);

        LineDataSet setCurr = new LineDataSet(lineCurr, "Kỳ hiện tại");
        setCurr.setColor(getResources().getColor(R.color.purple_500));
        setCurr.setCircleColor(getResources().getColor(R.color.purple_700));
        setCurr.setLineWidth(3f);

        LineData lineData = new LineData(setPrev, setCurr);
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getDescription().setEnabled(false);
        lineChart.animateY(1000);
        lineChart.invalidate();

        int[] prevScore = demKhoangDiem(kyTruoc);
        int[] currScore = demKhoangDiem(kyHienTai);

        ArrayList<BarEntry> barPrev = new ArrayList<>();
        ArrayList<BarEntry> barCurr = new ArrayList<>();
        ArrayList<String> xLabels = new ArrayList<>();

        for (int i = 0; i <= 10; i++) {
            float x = i; // vị trí gốc cho mỗi khoảng điểm
            barPrev.add(new BarEntry(x - 0.2f, prevScore[i]));     // kỳ trước: lệch trái
            barCurr.add(new BarEntry(x + 0.2f, currScore[i]));     // kỳ hiện tại: lệch phải
            xLabels.add(i < 10 ? i + "-" + (i + 1) : "10");
        }

        BarDataSet set1 = new BarDataSet(barPrev, "Kỳ trước");
        set1.setColor(0xFF90CAF9);

        BarDataSet set2 = new BarDataSet(barCurr, "Kỳ hiện tại");
        set2.setColor(getResources().getColor(R.color.purple_500));

        BarData barData = new BarData(set1, set2);
        barData.setBarWidth(0.45f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xLabels));
        barChart.groupBars(0f, 0.1f, 0.02f);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private int[] demXepLoai(int ky) {
        int[] count = new int[8];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT ds.diemTB FROM MonHoc m JOIN DiemSo ds ON m.maMonHoc = ds.MonHocmaMonHoc WHERE m.hocKy = ?", new String[]{String.valueOf(ky)});
        while (c.moveToNext()) {
            String xl = diemToXepLoai(c.getDouble(0));
            switch (xl) {
                case "A": count[0]++; break;
                case "B+": count[1]++; break;
                case "B": count[2]++; break;
                case "C+": count[3]++; break;
                case "C": count[4]++; break;
                case "D+": count[5]++; break;
                case "D": count[6]++; break;
                case "F": count[7]++; break;
            }
        }
        c.close();
        return count;
    }
    private int[] demKhoangDiem(int ky) {
        int[] count = new int[11];
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT ds.diemTB FROM MonHoc m JOIN DiemSo ds ON m.maMonHoc = ds.MonHocmaMonHoc WHERE m.hocKy = ?", new String[]{String.valueOf(ky)});
        while (c.moveToNext()) {
            int khoang = (int) Math.floor(c.getDouble(0));
            if (khoang >= 0 && khoang <= 10) count[khoang]++;
        }
        c.close();
        return count;
    }

    private String diemToXepLoai(double diem) {
        if (diem >= 8.5) return "A";
        else if (diem >= 7.7) return "B+";
        else if (diem >= 7.0) return "B";
        else if (diem >= 6.2) return "C+";
        else if (diem >= 5.5) return "C";
        else if (diem >= 4.7) return "D+";
        else if (diem >= 4.0) return "D";
        else return "F";
    }

    private void loadData() {
        double gpaHienTai = tinhGPAKy(kyHienTai);
        double gpaTruoc = tinhGPAKy(kyTruoc);
        double bienDong = gpaHienTai - gpaTruoc;

        edtGPAKy.setText(String.format("%.2f", gpaHienTai));
        edtGPAKyTruoc.setText(String.format("%.2f", gpaTruoc));

        edtBienDong.setText(String.format("%+.2f", bienDong));
        edtBienDong.setTextColor(bienDong >= 0 ? Color.parseColor("#4CAF50")  : Color.parseColor("#E91E63")); // xanh nếu tăng, đỏ nếu giảm
    }

    private double tinhGPAKy(int ky) {
        List<Subject> subjects = dbHelper.getSubjectsByHocKy(ky);
        if (subjects == null || subjects.isEmpty()) return 0.0;

        double totalPoints = 0.0;
        int totalCredits = 0;

        for (Subject s : subjects) {
            totalPoints += s.getTbm4() * s.getCredits();
            totalCredits += s.getCredits();
        }
        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        edtGPAKy = findViewById(R.id.edtGPAKy);
        edtGPAKyTruoc = findViewById(R.id.edtGPAKyTruoc);
        edtBienDong = findViewById(R.id.edtBienDong);
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
    }
}