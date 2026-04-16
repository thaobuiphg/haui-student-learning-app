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
import bthao.com.database.model.Document;
import bthao.com.database.model.Subject;

public class EditDocumentDialog extends Dialog {

    private EditText editDocumentName, editType;
    private TextView txtPath;
    private AutoCompleteTextView autoCompleteSubject;
    private Button btnChooseFile, btnSave;

    private DatabaseHelper db;
    private Document document;
    private String currentPath = "";
    private Runnable onSuccess;
    private Context context;

    private static final int PICK_FILE_REQUEST = 101;

    public EditDocumentDialog(Context context, Document document, Runnable onSuccess) {
        super(context);
        this.context = context;
        this.document = document;
        this.currentPath = document.getPath();
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

        // Ánh xạ view
        editDocumentName = findViewById(R.id.editDocumentName);
        txtPath = findViewById(R.id.txtPath);
        editType = findViewById(R.id.editFileType);
        autoCompleteSubject = findViewById(R.id.autoCompleteSubject);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSave = findViewById(R.id.btnSave);

        db = new DatabaseHelper(context);

        // Thiết lập giao diện
        btnChooseFile.setText("Chọn file mới (tuỳ chọn)");
        btnSave.setText("Cập nhật");

        // Hiển thị dữ liệu hiện tại
        editDocumentName.setText(document.getDocumentName());
        editType.setText(document.getFileType());
        txtPath.setText("File hiện tại: " + new File(currentPath).getName());

        // Load danh sách môn học + chọn môn hiện tại
        loadSubjectListAndSelectCurrent();

        // Nút chọn file mới
        btnChooseFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (context instanceof DocumentActivity) {
                ((DocumentActivity) context).startActivityForResult(
                        Intent.createChooser(intent, "Chọn file mới"), PICK_FILE_REQUEST);
            }
        });

        // Nút Cập nhật
        btnSave.setOnClickListener(v -> updateTaiLieu());
    }

    private void loadSubjectListAndSelectCurrent() {
        List<Subject> list = db.getAllMonHoc();
        List<String> names = new ArrayList<>();
        int currentPosition = 0;

        for (int i = 0; i < list.size(); i++) {
            Subject s = list.get(i);
            names.add(s.getName());
            if (s.getId() == document.getSubjectID()) {
                currentPosition = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_dropdown_item_1line, names);
        autoCompleteSubject.setAdapter(adapter);
        if (currentPosition < names.size()) {
            autoCompleteSubject.setText(names.get(currentPosition), false);
        }
    }

    public void handleFileResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST && resultCode == -1 && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String fileName = getFileName(uri);
            currentPath = copyFileToInternal(uri, fileName);

            txtPath.setText("File mới: " + fileName);
            Toast.makeText(context, "Đã chọn file mới", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTaiLieu() {
        String newName = editDocumentName.getText().toString().trim();
        String newType = editType.getText().toString().trim();
        String tenMonHoc = autoCompleteSubject.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập tên tài liệu!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tenMonHoc.isEmpty()) {
            Toast.makeText(context, "Vui lòng chọn hoặc nhập môn học!", Toast.LENGTH_SHORT).show();
            return;
        }

        Subject monHoc = db.getSubjectByName(tenMonHoc);
        if (monHoc == null) {
            long newId = db.insertMonHoc(tenMonHoc);
            if (newId > 0) {
                monHoc = new Subject((int) newId, tenMonHoc,0,0,0,0);
                Toast.makeText(context, "Đã thêm môn mới: " + tenMonHoc, Toast.LENGTH_SHORT).show();
                loadSubjectListAndSelectCurrent(); // Cập nhật gợi ý
            } else {
                Toast.makeText(context, "Lỗi thêm môn học!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        document.setDocumentName(newName);
        document.setFileType(newType);
        document.setSubjectID(monHoc.getId());

        if (!currentPath.equals(document.getPath())) {
            new File(document.getPath()).delete();
            document.setPath(currentPath);
        }

        if (db.updateTaiLieu(document)) {
            Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            dismiss();
            if (onSuccess != null) onSuccess.run(); // Reload danh sách + spinner
        } else {
            Toast.makeText(context, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show();
        }
    }

    private String copyFileToInternal(Uri uri, String fileName) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            if (in == null) return document.getPath();

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
            Toast.makeText(context, "Lỗi lưu file mới!", Toast.LENGTH_SHORT).show();
            return document.getPath(); // Giữ file cũ nếu lỗi
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