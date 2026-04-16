package bthao.com.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddSubjectDialog extends DialogFragment {

    public interface OnSubjectAddedListener {
        void onSubjectAdded(String name, int credits, double weightTX1, double weightTX2, double weightCK);
    }

    private OnSubjectAddedListener listener;

    public void setListener(OnSubjectAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        androidx.appcompat.widget.LinearLayoutCompat layout = new androidx.appcompat.widget.LinearLayoutCompat(requireActivity());
        layout.setOrientation(androidx.appcompat.widget.LinearLayoutCompat.VERTICAL);
        layout.setPadding(60, 60, 60, 40);

        // Tên môn học
        TextInputLayout tilName = new TextInputLayout(requireActivity());
        TextInputEditText etName = new TextInputEditText(requireActivity());
        etName.setHint("Tên môn học (ví dụ: Karate 1)");

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        tilName.setLayoutParams(params);

        tilName.addView(etName);
        layout.addView(tilName);

        //  Số tín chỉ
        TextInputLayout tilCredits = new TextInputLayout(requireActivity());
        TextInputEditText etCredits = new TextInputEditText(requireActivity());
        etCredits.setHint("Số tín chỉ (ví dụ: 3)");
        etCredits.setInputType(InputType.TYPE_CLASS_NUMBER);

        tilCredits.setLayoutParams(params);
        tilCredits.addView(etCredits);
        layout.addView(tilCredits);

        //  Trọng số TX1
        TextInputLayout tilTX1 = new TextInputLayout(requireActivity());
        TextInputEditText etTX1 = new TextInputEditText(requireActivity());
        etTX1.setHint("Trọng số TX1 (%)");
        etTX1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        tilTX1.setLayoutParams(params);
        tilTX1.addView(etTX1);
        layout.addView(tilTX1);

        // Trọng số TX2
        TextInputLayout tilTX2 = new TextInputLayout(requireActivity());
        TextInputEditText etTX2 = new TextInputEditText(requireActivity());
        etTX2.setHint("Trọng số TX2 (%)");
        etTX2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        tilTX2.setLayoutParams(params);
        tilTX2.addView(etTX2);
        layout.addView(tilTX2);

        // Trọng số CK
        TextInputLayout tilCK = new TextInputLayout(requireActivity());
        TextInputEditText etCK = new TextInputEditText(requireActivity());
        etCK.setHint("Trọng số CK (%)");
        etCK.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        tilCK.setLayoutParams(params);
        tilCK.addView(etCK);
        layout.addView(tilCK);

        builder.setTitle("THÊM MÔN HỌC")
                .setView(layout)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .setPositiveButton("Thêm", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String creditsStr = etCredits.getText().toString().trim();
                String tx1Str = etTX1.getText().toString().trim();
                String tx2Str = etTX2.getText().toString().trim();
                String ckStr = etCK.getText().toString().trim();

                Button btnAdd1 = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                Button btnCancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                int colorBlack = getResources().getColor(android.R.color.black);

                btnAdd1.setTextColor(colorBlack);
                btnCancel.setTextColor(colorBlack);

                if (name.isEmpty() || creditsStr.isEmpty() || tx1Str.isEmpty() || tx2Str.isEmpty() || ckStr.isEmpty()) {
                    new AlertDialog.Builder(requireActivity())
                            .setTitle("Lỗi")
                            .setMessage("Vui lòng điền đầy đủ thông tin!")
                            .setPositiveButton("OK", null).show();
                    return;
                }

                int credits = 3;
                double w1, w2, w3;
                try {
                    credits = Integer.parseInt(creditsStr);
                    w1 = Double.parseDouble(tx1Str);
                    w2 = Double.parseDouble(tx2Str);
                    w3 = Double.parseDouble(ckStr);
                } catch (Exception e) {
                    new AlertDialog.Builder(requireActivity())
                            .setMessage("Dữ liệu không hợp lệ!")
                            .setPositiveButton("OK", null).show();
                    return;
                }

                if (w1 + w2 + w3 != 100) {
                    new AlertDialog.Builder(requireActivity())
                            .setTitle("Lỗi")
                            .setMessage("Tổng trọng số phải bằng 100%!\nHiện tại: " + (w1 + w2 + w3) + "%")
                            .setPositiveButton("OK", null).show();
                    return;
                }
                if (listener != null) {
                    listener.onSubjectAdded(name, credits, w1 / 100.0, w2 / 100.0, w3 / 100.0);
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }
}