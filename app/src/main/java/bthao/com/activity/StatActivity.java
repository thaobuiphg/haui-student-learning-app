package bthao.com.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
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

public class StatActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private Spinner spinnerKy;
    //private EditText edtGPAKy, edtGPATichLuy;
    private TextView tvGPAKy, tvGPATichLuy;
    private Button btnCompare;
    private LineChart lineChart;
    private BarChart barChart;
    private DatabaseHelper dbHelper;
    private List<String> danhSachKy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stat);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupSpinner();
        setupCharts();
        btnBack = findViewById(R.id.btnBack);
        spinnerKy = findViewById(R.id.spinnerKy);
//        edtGPAKy = findViewById(R.id.edtGPAKy);
//        edtGPATichLuy = findViewById(R.id.edtGPATichLuy);
        tvGPAKy = findViewById(R.id.tvGPAKy);
        tvGPATichLuy = findViewById(R.id.tvGPATichLuy);
        btnCompare = findViewById(R.id.btnCompare);
        btnBack.setOnClickListener(v -> finish());
        btnCompare.setOnClickListener(v -> {
            Intent intent = new Intent(StatActivity.this, CompareActivity.class);
            // Truyền kỳ hiện tại (ví dụ đang chọn kỳ 3 thì truyền 3, kỳ trước là 2)
            intent.putExtra("kyHienTai", getHocKyHienTaiTuSpinner());
            startActivity(intent);
        });
//        btnCompare.setOnClickListener(v -> {
//            Intent intent = new Intent(StatActivity.this, CompareActivity.class);
//            intent.putExtra("gpa_ky_hien_tai", 3.2f);
//            intent.putExtra("gpa_ky_truoc", 2.9f);
//            startActivity(intent);
//        });
//        setupSpinner();
    }


    private void setupCharts() {
        // Cấu hình chung cho biểu đồ
        lineChart.getDescription().setEnabled(false);
        lineChart.setNoDataText("Chọn kỳ để xem thống kê");
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        barChart.getDescription().setEnabled(false);
        barChart.setNoDataText("Chưa có dữ liệu điểm");
        barChart.setFitBars(true);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        spinnerKy = findViewById(R.id.spinnerKy);
        tvGPAKy = findViewById(R.id.tvGPAKy);
        tvGPATichLuy = findViewById(R.id.tvGPATichLuy);
        btnCompare = findViewById(R.id.btnCompare);
        lineChart = findViewById(R.id.lineChart);
        barChart = findViewById(R.id.barChart);
    }

    private void setupSpinner() {
        List<String> danhSachKy = new ArrayList<>();
        danhSachKy.add("Kỳ 1");
        danhSachKy.add("Kỳ 2");
        danhSachKy.add("Kỳ 3");
        danhSachKy.add("Kỳ 4");
        danhSachKy.add("Kỳ 5");
        danhSachKy.add("Kỳ 6");
        danhSachKy.add("Kỳ 7");
        danhSachKy.add("Kỳ 8");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                danhSachKy
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKy.setAdapter(adapter);
        spinnerKy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              //  String kyChon = danhSachKy.get(position);
                int hocKy = position +1;

               // if (kyChon.contains("Kỳ 1")) hocKy = 1;
               // else if (kyChon.contains("Kỳ 2")) hocKy = 2;
               // else if (kyChon.contains("Kỳ 1 - 2024")) hocKy = 3;
               // else hocKy = 4;

                capNhatThongKeTheoKy(hocKy);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerKy.setSelection(0);
    }

    private void capNhatThongKeTheoKy(int hocKy) {
        tinhGPA(hocKy);
        veBieuDoPhanBoDiem(hocKy);
    }

    // Tính GPA kỳ và GPA tích lũy
    private void tinhGPA(int hocKyHienTai) {
        List<Subject> subjectsKy = dbHelper.getSubjectsByHocKy(hocKyHienTai);
        List<Subject> subjectsTichLuy = new ArrayList<>();

        for (int i = 1; i <= hocKyHienTai; i++) {
            subjectsTichLuy.addAll(dbHelper.getSubjectsByHocKy(i));
        }

        double gpaKy = 0.0;
        int totalCreditsKy = 0;
        if (!subjectsKy.isEmpty()) {
            double totalPoints = 0.0;
            for (Subject s : subjectsKy) {
                totalPoints += s.getTbm4() * s.getCredits();
                totalCreditsKy += s.getCredits();
            }
            gpaKy = totalPoints / totalCreditsKy;
        }

        double gpaTichLuy = 0.0;
        int totalCreditsTichLuy = 0;
        if (!subjectsTichLuy.isEmpty()) {
            double totalPoints = 0.0;
            for (Subject s : subjectsTichLuy) {
                totalPoints += s.getTbm4() * s.getCredits();
                totalCreditsTichLuy += s.getCredits();
            }
            gpaTichLuy = totalPoints / totalCreditsTichLuy;
        }

        String xepLoaiKy = gpaKy >= 3.6 ? "Xuất sắc" :
                gpaKy >= 3.2 ? "Giỏi" :
                        gpaKy >= 2.5 ? "Khá" :
                                gpaKy >= 2.0 ? "Trung bình" :
                                        gpaKy >= 1.0 ? "Yếu" : "Kém";

        tvGPAKy.setText(String.format("%.2f/4", gpaKy));
        tvGPATichLuy.setText(String.format("%.2f/4", gpaTichLuy));
        tvGPAKy.setText(String.format("%.2f/4\nXếp loại: %s", gpaKy, xepLoaiKy));
    }

    private void veBieuDoPhanBoDiem(int hocKy) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT ds.diemTB FROM MonHoc mh " +
                        "LEFT JOIN DiemSo ds ON mh.maMonHoc = ds.MonHocmaMonHoc " +
                        "WHERE mh.hocKy = ? AND ds.diemTB IS NOT NULL",
                new String[]{String.valueOf(hocKy)});

        int[] countXepLoai = new int[8];
        String[] labelsXepLoai = {"A", "B+", "B", "C+", "C", "D+", "D", "F"};
        int[] countKhoangDiem = new int[11];

        while (cursor.moveToNext()) {
            double diem = cursor.getDouble(0);
            String xl = diemToXepLoai(diem);
            switch (xl) {
                case "A": countXepLoai[0]++; break;
                case "B+": countXepLoai[1]++; break;
                case "B": countXepLoai[2]++; break;
                case "C+": countXepLoai[3]++; break;
                case "C": countXepLoai[4]++; break;
                case "D+": countXepLoai[5]++; break;
                case "D": countXepLoai[6]++; break;
                case "F": countXepLoai[7]++; break;
            }
            int khoang = (int) Math.floor(diem);
            if (khoang >= 0 && khoang <= 10) {
                countKhoangDiem[khoang]++;
            }
        }
        cursor.close();
        if (cursor.getCount() == 0) {
            lineChart.clear();
            barChart.clear();
            lineChart.setNoDataText("Chưa có điểm môn nào trong kỳ này");
            barChart.setNoDataText("Chưa có dữ liệu");
            lineChart.invalidate();
            barChart.invalidate();
            return;
        }
        // LINE CHART: Số lượng theo xếp loại
        ArrayList<Entry> lineEntries = new ArrayList<>();
        for (int i = 0; i < countXepLoai.length; i++) {
            lineEntries.add(new Entry(i, countXepLoai[i]));
        }

        LineDataSet lineSet = new LineDataSet(lineEntries, "Số môn theo xếp loại");
        lineSet.setColor(getResources().getColor(R.color.purple_500));
        lineSet.setCircleColor(getResources().getColor(R.color.purple_700));
        lineSet.setLineWidth(3f);
        lineSet.setValueTextSize(12f);
        lineSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(lineSet);
        lineChart.setData(lineData);
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labelsXepLoai));
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getAxisLeft().setAxisMinimum(0);
        //lineChart.animateY(800);
       // lineChart.invalidate();
        float maxY = 0f;
        for (Entry e : lineEntries) {
            if (e.getY() > maxY) maxY = e.getY();
        }
        if (maxY > 0) {
            lineChart.getAxisLeft().setAxisMaximum(maxY);
            lineChart.getAxisLeft().setLabelCount((int)maxY + 1, true); // chia 0,1,2,3
            lineChart.getAxisLeft().setGranularity(1f);
        }
        lineChart.getAxisRight().setEnabled(false);
        lineChart.animateY(800);
        lineChart.invalidate();
        // BAR CHART: Phân bố điểm 0-10
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> labelsKhoang = new ArrayList<>();
        for (int i = 0; i <= 10; i++) {
            barEntries.add(new BarEntry(i, countKhoangDiem[i]));
            labelsKhoang.add(i < 10 ? i + "-" + (i + 1) : "10");
        }

        BarDataSet barSet = new BarDataSet(barEntries, "Số môn");
        barSet.setColors(ColorTemplate.MATERIAL_COLORS);
        barSet.setValueTextSize(11f);

        BarData barData = new BarData(barSet);
        barData.setBarWidth(0.9f);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labelsKhoang));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setLabelRotationAngle(-45);
        barChart.getXAxis().setGranularity(1f);
        barChart.getAxisLeft().setAxisMinimum(0);
        barChart.animateY(800);
        barChart.invalidate();

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

    private int getHocKyHienTaiTuSpinner() {
        int position = spinnerKy.getSelectedItemPosition();
        return position + 1;
    }
    }


