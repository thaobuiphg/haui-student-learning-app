package bthao.com.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import bthao.com.R;
import bthao.com.activity.DocumentActivity;
import bthao.com.database.DatabaseHelper;
import bthao.com.database.model.Subject;
import bthao.com.database.model.Document;

public class AddDocumentDialog extends Dialog {

    private EditText editDocumentName, editType;
    private TextView txtPath;
    private AutoCompleteTextView autoCompleteSubject;
    private Button btnChooseFile, btnSave;

    private DatabaseHelper db;
    private String currentPath = "";
    private Runnable onSuccess;
    private Context context;

    private static final int PICK_FILE_REQUEST = 100;

    public AddDocumentDialog(Context context, Runnable onSuccess) {
        super(context);
        this.context = context;
        this.onSuccess = onSuccess;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add);

        Window window = getWindow();
        if (window != null) {
            window.setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        editDocumentName = findViewById(R.id.editDocumentName);
        txtPath = findViewById(R.id.txtPath);
        editType = findViewById(R.id.editFileType);
        autoCompleteSubject = findViewById(R.id.autoCompleteSubject);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSave = findViewById(R.id.btnSave);

        db = new DatabaseHelper(context);

        loadSubjectList(); // Load gợi ý môn học

        btnChooseFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (context instanceof DocumentActivity) {
                ((DocumentActivity) context).startActivityForResult(
                        Intent.createChooser(intent, "Chọn tài liệu"), PICK_FILE_REQUEST);
            }
        });

        btnSave.setOnClickListener(v -> saveTaiLieu());
    }

    private void loadSubjectList() {
        List<Subject> list = db.getAllMonHoc();
        List<String> names = new ArrayList<>();
        for (Subject s : list) {
            if (s != null && s.getName() != null) {
                names.add(s.getName());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_dropdown_item_1line, names);
        autoCompleteSubject.setAdapter(adapter);
    }

    public void handleFileResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String fileName = getFileName(uri);
            currentPath = copyFileToInternal(uri, fileName);
            txtPath.setText("Đã chọn: " + fileName);

            String nameNoExt = fileName.contains(".")
                    ? fileName.substring(0, fileName.lastIndexOf('.'))
                    : fileName;
            editDocumentName.setText(nameNoExt);
        }
    }

    private void saveTaiLieu() {
        String tenTaiLieu = editDocumentName.getText().toString().trim();
        String loai = editType.getText().toString().trim();
        String tenMonHoc = autoCompleteSubject.getText().toString().trim();

        if (tenTaiLieu.isEmpty()) {
            Toast.makeText(context, "Nhập tên tài liệu!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPath.isEmpty()) {
            Toast.makeText(context, "Chưa chọn file!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tenMonHoc.isEmpty()) {
            Toast.makeText(context, "Nhập hoặc chọn môn học!", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject monHoc = db.getSubjectByName(tenMonHoc);
        if (monHoc == null) {
            long newId = db.insertMonHoc(tenMonHoc);
            if (newId > 0) {
                monHoc = new Subject((int) newId, tenMonHoc,0,0,0,0);
                Toast.makeText(context, "Đã thêm môn mới: " + tenMonHoc, Toast.LENGTH_SHORT).show();
                loadSubjectList();
            } else {
                Toast.makeText(context, "Lỗi thêm môn học!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Document doc = new Document();
        doc.setDocumentName(tenTaiLieu);
        doc.setPath(currentPath);
        doc.setFileType(loai);
        doc.setSubjectID(monHoc.getId());

        if (db.insertTaiLieu(doc) > 0) {
            Toast.makeText(context, "Thêm tài liệu thành công!", Toast.LENGTH_SHORT).show();
            dismiss();
            if (onSuccess != null) onSuccess.run();
        } else {
            Toast.makeText(context, "Lỗi thêm tài liệu!", Toast.LENGTH_SHORT).show();
        }
    }

    private String copyFileToInternal(Uri uri, String fileName) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return "";
            File outFile = new File(context.getFilesDir(), fileName);
            FileOutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.close();
            return outFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Lỗi lưu file!", Toast.LENGTH_SHORT).show();
            return "";
        }
    }

    private String getFileName(Uri uri) {
        String result = "file_" + System.currentTimeMillis();
        try {
            var cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                if (index != -1) result = cursor.getString(index);
                cursor.close();
            }
        } catch (Exception ignored) {}
        return result;
    }
}