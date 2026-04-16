// File: Goal.java
package bthao.com.database.model;

import java.text.DecimalFormat;

public class Goal {
    public String level;
    public double requiredTBM;
    private double requiredCK;
    private static final DecimalFormat df = new DecimalFormat("#.1");

    public Goal(String level, double requiredTBM) {
        this.level = level;
        this.requiredTBM = requiredTBM;
        this.requiredCK = -1; // Đánh dấu chưa tính toán
    }

    public void updateRequiredCK(double ck) {
        this.requiredCK = ck;
    }

    public String getDisplayResult() {
        if (requiredCK == -1) {
            return "- Đạt " + level + ": Vui lòng nhập điểm TX1, TX2.";
        } else if (requiredCK <= 0) {
            return "- Đạt " + level + ": Đã đạt mục tiêu (hoặc chỉ cần 0 điểm CK)";
        } else if (requiredCK > 10) {
            return "- Đạt " + level + ": KHÔNG THỂ ĐẠT được, cần ít nhất " + df.format(requiredCK) + " điểm CK";
        } else {
            return "- Đạt " + level + ": cần " + df.format(requiredCK) + " điểm ở cuối kì";
        }
    }
}