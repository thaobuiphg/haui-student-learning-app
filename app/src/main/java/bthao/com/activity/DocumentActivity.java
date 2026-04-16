package bthao.com.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import bthao.com.R;
import bthao.com.adapter.DocumentAdapter;
import bthao.com.database.DatabaseHelper;
import bthao.com.database.model.Document;
import bthao.com.database.model.Subject;
import bthao.com.dialog.AddDocumentDialog;
import bthao.com.dialog.EditDocumentDialog;

public class DocumentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Spinner spinnerSubject;
    private Button btnFind;
    private FloatingActionButton fabAdd;
    private DatabaseHelper db;
    private DocumentAdapter adapter;
    private List<Document> fullList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private Object currentDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách tài liệu");
        }

        recyclerView = findViewById(R.id.recyclerView);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        btnFind = findViewById(R.id.btnFind);
        fabAdd = findViewById(R.id.fabAdd);
        db = new DatabaseHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> {
            currentDialog = new AddDocumentDialog(this, this::loadSpinnerAndData);
            ((AddDocumentDialog) currentDialog).show();
        });

        loadSpinnerAndData();

        btnFind.setOnClickListener(v -> {
            Subject selected = (Subject) spinnerSubject.getSelectedItem();
            if (selected != null) {
                filterBySubject(selected.getId());
            }
        });
    }

    private void loadSpinnerAndData() {
        subjectList = db.getAllMonHoc();
        subjectList.add(0, new Subject() {{
            setId(0);
            setName("Tất cả môn học");
            setCredits(0);
        }});

        ArrayAdapter<Subject> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, subjectList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(spinnerAdapter);

        loadData();
    }

    private void loadData() {
        fullList = db.getAllTaiLieu();
        updateAdapter(fullList);
    }

    private void filterBySubject(int maMon) {
        List<Document> filtered = new ArrayList<>();
        for (Document tl : fullList) {
            if (maMon == 0 || tl.getSubjectID() == maMon) {
                filtered.add(tl);
            }
        }
        updateAdapter(filtered);
    }

    private String getMimeType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "application/msword";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp)")) return "image/*";
        return "*/*";
    }

    private void updateAdapter(List<Document> list) {
        adapter = new DocumentAdapter(this, list, new DocumentAdapter.OnItemActionListener() {

            @Override
            public void onViewClick(Document tl) {
                File file = new File(tl.getPath());
                if (!file.exists()) {
                    Toast.makeText(DocumentActivity.this, "File không tồn tại!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Uri uri = FileProvider.getUriForFile(
                        DocumentActivity.this,
                        "bthao.com.provider",
                        file
                );

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, getMimeType(tl.getDocumentName()));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(Intent.createChooser(intent, "Mở bằng..."));
                } catch (Exception e) {
                    Toast.makeText(DocumentActivity.this, "Không có ứng dụng mở file này!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onEditClick(Document tl) {
                currentDialog = new EditDocumentDialog(DocumentActivity.this, tl, DocumentActivity.this::loadSpinnerAndData);
                ((EditDocumentDialog) currentDialog).show();
            }

            @Override
            public void onShareClick(Document tl) {
                File file = new File(tl.getPath());
                Uri uri = FileProvider.getUriForFile(DocumentActivity.this,
                        getPackageName() + ".provider", file);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("*/*");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(share, "Chia sẻ " + tl.getDocumentName()));
            }

            @Override
            public void onDeleteClick(Document tl) {
                new AlertDialog.Builder(DocumentActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Xóa tài liệu: " + tl.getDocumentName() + "?")
                        .setPositiveButton("Xóa", (d, w) -> {
                            db.deleteTaiLieu(tl.getDocumentID());
                            new File(tl.getPath()).delete();
                            loadSpinnerAndData(); // Cập nhật lại danh sách + spinner
                            Toast.makeText(DocumentActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentDialog != null) {
            if (currentDialog instanceof AddDocumentDialog) {
                ((AddDocumentDialog) currentDialog).handleFileResult(requestCode, resultCode, data);
            } else if (currentDialog instanceof EditDocumentDialog) {
                ((EditDocumentDialog) currentDialog).handleFileResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}