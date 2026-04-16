package bthao.com.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

import bthao.com.database.model.Document;
import bthao.com.database.model.Schedule;
import bthao.com.database.model.Subject;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "mydb.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng Môn học
        db.execSQL("CREATE TABLE MonHoc (" +
                "maMonHoc INTEGER PRIMARY KEY, " +
                "tenMonHoc TEXT NOT NULL, " +
                "soTinChi INTEGER DEFAULT 3, " +
                "hocKy INTEGER, " +
                "soTietHoc INTEGER DEFAULT 45)");

        // 2. Bảng Tài liệu
        db.execSQL("CREATE TABLE TaiLieu (" +
                "maTaiLieu INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tenTaiLieu TEXT NOT NULL, " +
                "duongDan TEXT, " +
                "loaiTep TEXT DEFAULT 'Tài liệu học tập', " +
                "MonHocmaMonHoc INTEGER, " +
                "FOREIGN KEY(MonHocmaMonHoc) REFERENCES MonHoc(maMonHoc) ON DELETE CASCADE)");

        // 3. Bảng Mục tiêu điểm
        db.execSQL("CREATE TABLE MucTieuDiem (" +
                "maMucTieu INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MucTieuMonHoc TEXT, " +
                "MucTieu INTEGER, " +
                "MonHocmaMonHoc INTEGER, " +
                "FOREIGN KEY(MonHocmaMonHoc) REFERENCES MonHoc(maMonHoc) ON DELETE CASCADE)");

        // 4. Bảng Điểm số
        db.execSQL("CREATE TABLE DiemSo (" +
                "maDiem INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MonHocmaMonHoc INTEGER, " +
                "diemTX1 REAL, " +
                "diemTX2 REAL, " +
                "diemGK REAL, " +
                "diemCK REAL, " +
                "diemTB REAL, " +
                "xepLoai TEXT, " +
                "heSoTX1 REAL DEFAULT 1, " +
                "heSoTX2 REAL DEFAULT 1, " +
                "heSoGK REAL DEFAULT 2, " +
                "heSoCK REAL DEFAULT 3, " +
                "FOREIGN KEY(MonHocmaMonHoc) REFERENCES MonHoc(maMonHoc) ON DELETE CASCADE"+")");

        // 5. Bảng Thời gian biểu
        db.execSQL("CREATE TABLE ThoiGianBieu (" +
                "maThoiGianBieu INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "MonHocmaMonHoc INTEGER, " +
                "tieuDe TEXT, " +
                "noiDung TEXT, " +
                "ngay TEXT, " +
                "gioBatDau TEXT, " +
                "gioKetThuc TEXT, " +
                "diaDiem TEXT, " +
                "ghiChu TEXT, " +
                "loaiLich TEXT, " +
                "FOREIGN KEY(MonHocmaMonHoc) REFERENCES MonHoc(maMonHoc) ON DELETE CASCADE)");

        db.execSQL("INSERT INTO MonHoc (maMonHoc, tenMonHoc, soTinChi, hocKy, soTietHoc) VALUES " +
                "(1, 'Toán cao cấp', 4, 1, 60), " +
                "(2, 'Vật lý đại cương', 3, 1, 45), " +
                "(3, 'Lập trình Java', 4, 2, 60), " +
                "(4, 'Cơ sở dữ liệu', 3, 2, 45), " +
                "(5, 'Tiếng Anh 1', 3, 1, 45)");

        db.execSQL("INSERT INTO ThoiGianBieu (tieuDe, noiDung, ngay, gioBatDau, gioKetThuc, diaDiem, ghiChu, loaiLich) VALUES " +
                "('Java Lecture', 'Lecture', '2025-12-10', '0800', '1000', 'A101', 'Bring laptop', 'Lịch học'), " +
                "('Team Meeting', 'Meeting', '2025-12-10', '1400', '1530', 'B202', 'Project update', 'Khác'), " +
                "('Database Lab', 'Lab', '2025-12-15', '0900', '1100', 'Lab 301', 'SQL', 'Lịch học'), " +
                "('Math Exam', 'Exam', '2025-11-30', '1300', '1500', 'Hall C', 'Calculator allowed', 'Lịch thi'), " +
                "('English Class', 'Lecture', '2025-11-13', '1000', '1200', 'D105', 'Unit 5', 'Lịch học'), " +
                "('Project Deadline', 'Deadline', '2025-12-17', '2359', '2359', 'Online', 'Submit on Moodle', 'Khác')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS ThoiGianBieu");
            db.execSQL("DROP TABLE IF EXISTS DiemSo");
            db.execSQL("DROP TABLE IF EXISTS MucTieuDiem");
            db.execSQL("DROP TABLE IF EXISTS TaiLieu");
            db.execSQL("DROP TABLE IF EXISTS MonHoc");
            onCreate(db);
        }
    }
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys=ON;");
    }
    public List<Subject> getAllMonHoc() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT maMonHoc, tenMonHoc, soTinChi FROM MonHoc ORDER BY tenMonHoc";
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
            Subject s = new Subject();
            s.setId(c.getInt(0));
            s.setName(c.getString(1));
            s.setCredits(c.getInt(2));
            list.add(s);
        }
        c.close();
        return list;
    }

    // Lấy tất cả tài liệu
    public List<Document> getAllTaiLieu() {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT t.maTaiLieu, t.tenTaiLieu, t.duongDan, t.loaiTep, t.MonHocmaMonHoc, m.tenMonHoc " +
                "FROM TaiLieu t LEFT JOIN MonHoc m ON t.MonHocmaMonHoc = m.maMonHoc " +
                "ORDER BY t.tenTaiLieu";
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        while (c.moveToNext()) {
            Document tl = new Document();
            tl.setDocumentID(c.getInt(0));
            tl.setDocumentName(c.getString(1));
            tl.setPath(c.getString(2));
            tl.setFileType(c.getString(3) != null ? c.getString(3) : "Tài liệu học tập"); // tránh null
            tl.setSubjectID(c.getInt(4));
            tl.setSubjectName(c.getString(5));
            list.add(tl);
        }
        c.close();
        return list;

    }

    // Thêm tài liệu mới
    public long insertTaiLieu(Document tl) {
        ContentValues cv = new ContentValues();
        cv.put("tenTaiLieu", tl.getDocumentName());
        cv.put("duongDan", tl.getPath());
        cv.put("loaiTep", tl.getFileType());
        cv.put("MonHocmaMonHoc", tl.getSubjectID());
        return getWritableDatabase().insert("TaiLieu", null, cv);
    }

    // Cập nhật tài liệu
    public boolean updateTaiLieu(Document tl) {
        ContentValues cv = new ContentValues();
        cv.put("tenTaiLieu", tl.getDocumentName());
        cv.put("duongDan", tl.getPath());
        cv.put("loaiTep", tl.getFileType());
        cv.put("MonHocmaMonHoc", tl.getSubjectID());
        int rows = getWritableDatabase().update("TaiLieu", cv, "maTaiLieu = ?",
                new String[]{String.valueOf(tl.getDocumentID())});
        return rows > 0;
    }

    // Xóa tài liệu
    public boolean deleteTaiLieu(int maTaiLieu) {
        int rows = getWritableDatabase().delete("TaiLieu", "maTaiLieu = ?",
                new String[]{String.valueOf(maTaiLieu)});
        return rows > 0;
    }

    public long insertMonHoc(String tenMon) {
        ContentValues values = new ContentValues();
        values.put("tenMonHoc", tenMon.trim());
        values.put("soTinChi", 3);
        values.put("hocKy", 1);
        return getWritableDatabase().insert("MonHoc", null, values);
    }

    public Subject getSubjectByName(String tenMon) {
        if (tenMon == null || tenMon.trim().isEmpty()) return null;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("MonHoc",
                new String[]{"maMonHoc", "tenMonHoc", "soTinChi"},
                "tenMonHoc = ? COLLATE NOCASE",
                new String[]{tenMon.trim()},
                null, null, null);

        Subject subject = null;
        if (cursor != null && cursor.moveToFirst()) {
            subject = new Subject();
            subject.setId(cursor.getInt(0));
            subject.setName(cursor.getString(1));
            subject.setCredits(cursor.getInt(2));
        }
        if (cursor != null) cursor.close();
        return subject;
    }

    public void deleteOrphanSubjects() {
        getWritableDatabase().execSQL(
                "DELETE FROM MonHoc "+
                        "Where maMonHoc NOT IN (SELECT DISTINCT MonHocmaMonHoc FROM TaiLieu WHERE MonHocmaMonHoc IS NOT NULL)"
        );
    }

    // LẤY LỊCH THEO NGÀY
    public List<Schedule> getScheduleByDate(String date) {
        List<Schedule> list = new ArrayList<>();
        String sql = "SELECT maThoiGianBieu, tieuDe, noiDung, ngay, gioBatDau, gioKetThuc, diaDiem, ghiChu, loaiLich " +
                "FROM ThoiGianBieu WHERE ngay = ? ORDER BY gioBatDau";
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{date});

        if (c != null && c.moveToFirst()) {
            do {
                Schedule s = new Schedule();
                s.setId(c.getInt(0));
                s.setTitle(c.getString(1));
                s.setContent(c.getString(2));
                s.setDate(c.getString(3));
                s.setStartTime(c.getString(4));
                s.setEndTime(c.getString(5));
                s.setLocation(c.getString(6));
                s.setNote(c.getString(7));
                s.setType(c.getString(8));
                list.add(s);
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return list;
    }

    // THÊM LỊCH
    public long insertSchedule(Schedule s) {
        ContentValues cv = new ContentValues();
        cv.put("tieuDe", s.getTitle() != null ? s.getTitle() : "No Title");
        cv.put("noiDung", s.getContent() != null ? s.getContent() : "");
        cv.put("ngay", s.getDate() != null ? s.getDate() : "");
        cv.put("gioBatDau", s.getStartTime() != null ? s.getStartTime() : "");
        cv.put("gioKetThuc", s.getEndTime() != null ? s.getEndTime() : "");
        cv.put("diaDiem", s.getLocation() != null ? s.getLocation() : "");
        cv.put("ghiChu", s.getNote() != null ? s.getNote() : "");
        cv.put("loaiLich", s.getType() != null ? s.getType() : "Event");
        return getWritableDatabase().insert("ThoiGianBieu", null, cv);
    }

    // SỬA LỊCH
    public boolean updateSchedule(Schedule s) {
        ContentValues cv = new ContentValues();
        cv.put("tieuDe", s.getTitle());
        cv.put("noiDung", s.getContent());
        cv.put("ngay", s.getDate());
        cv.put("gioBatDau", s.getStartTime());
        cv.put("gioKetThuc", s.getEndTime());
        cv.put("diaDiem", s.getLocation());
        cv.put("ghiChu", s.getNote());
        cv.put("loaiLich", s.getType());
        return getWritableDatabase().update("ThoiGianBieu", cv,
                "maThoiGianBieu = ?", new String[]{String.valueOf(s.getId())}) > 0;
    }

    // XÓA LỊCH
    public boolean deleteSchedule(int id) {
        return getWritableDatabase().delete("ThoiGianBieu",
                "maThoiGianBieu = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public List<Subject> getSubjectsByHocKy(int hocKy) {
        List<Subject> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT m.maMonHoc, m.tenMonHoc, m.soTinChi, " +
                "COALESCE(d.diemTX1, 0), COALESCE(d.diemTX2, 0), COALESCE(d.diemCK, 0), " +
                "COALESCE(d.heSoTX1, 0.3), COALESCE(d.heSoTX2, 0.3), COALESCE(d.heSoCK, 0.4) " +
                "FROM MonHoc m " +
                "LEFT JOIN DiemSo d ON m.maMonHoc = d.MonHocmaMonHoc " +
                "WHERE m.hocKy = ? " +
                "GROUP BY m.maMonHoc " +
                "ORDER BY m.tenMonHoc";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(hocKy)});
        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            int credits = cursor.getInt(2);
            double tx1 = cursor.getDouble(3);
            double tx2 = cursor.getDouble(4);
            double ck = cursor.getDouble(5);
            double w1 = cursor.getDouble(6);
            double w2 = cursor.getDouble(7);
            double w3 = cursor.getDouble(8);

            double tbm = tx1 * w1 + tx2 * w2 + ck * w3;

            Subject subject = new Subject(id, name, tx1, tx2, ck, credits, tbm);
            list.add(subject);
        }
        cursor.close();
        return list;
    }

    public long addMonHocWithWeights(String tenMon, int soTinChi, int hocKy,
                                     double heSoTX1, double heSoTX2, double heSoCK) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cvMon = new ContentValues();
            cvMon.put("tenMonHoc", tenMon);
            cvMon.put("soTinChi", soTinChi);
            cvMon.put("hocKy", hocKy);
            long id = db.insert("MonHoc", null, cvMon);
            if (id != -1) {
                ContentValues cvDiem = new ContentValues();
                cvDiem.put("MonHocmaMonHoc", id);
                cvDiem.put("heSoTX1", heSoTX1);
                cvDiem.put("heSoTX2", heSoTX2);
                cvDiem.put("heSoCK", heSoCK);
                db.insert("DiemSo", null, cvDiem);
            }
            db.setTransactionSuccessful();
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    public void createMissingDiemSoRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            Cursor c = db.rawQuery(
                    "SELECT m.maMonHoc FROM MonHoc m " +
                            "LEFT JOIN DiemSo d ON m.maMonHoc = d.MonHocmaMonHoc " +
                            "WHERE d.MonHocmaMonHoc IS NULL", null);

            while (c.moveToNext()) {
                long monId = c.getLong(0);
                ContentValues cv = new ContentValues();
                cv.put("MonHocmaMonHoc", monId);
                cv.put("heSoTX1", 0.3);
                cv.put("heSoTX2", 0.3);
                cv.put("heSoCK", 0.4);
                db.insert("DiemSo", null, cv);
            }
            c.close();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
    public Subject getSubjectById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT m.maMonHoc, m.tenMonHoc, m.soTinChi, " +
                "COALESCE(d.diemTX1, 0), COALESCE(d.diemTX2, 0), COALESCE(d.diemCK, 0), " +
                "COALESCE(d.heSoTX1, 0.3), COALESCE(d.heSoTX2, 0.3), COALESCE(d.heSoCK, 0.4) " +
                "FROM MonHoc m " +
                "LEFT JOIN DiemSo d ON m.maMonHoc = d.MonHocmaMonHoc " +
                "WHERE m.maMonHoc = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        Subject subject = null;
        if (cursor.moveToFirst()) {
            int sid = cursor.getInt(0);
            String name = cursor.getString(1);
            int credits = cursor.getInt(2);
            double tx1 = cursor.getDouble(3);
            double tx2 = cursor.getDouble(4);
            double ck = cursor.getDouble(5);
            double w1 = cursor.getDouble(6);
            double w2 = cursor.getDouble(7);
            double w3 = cursor.getDouble(8);

            double tbm = tx1 * w1 + tx2 * w2 + ck * w3;

            subject = new Subject(sid, name, tx1, tx2, ck, credits, tbm);
        }
        cursor.close();
        return subject;
    }

    // Lấy trọng số
    public double[] getWeightsForSubject(int subjectId) {
        double[] w = {0.3, 0.3, 0.4};
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COALESCE(heSoTX1,0.3), COALESCE(heSoTX2,0.3), COALESCE(heSoCK,0.4) " +
                        "FROM DiemSo WHERE MonHocmaMonHoc = ?",
                new String[]{String.valueOf(subjectId)});
        if (c.moveToFirst()) {
            w[0] = c.getDouble(0);
            w[1] = c.getDouble(1);
            w[2] = c.getDouble(2);
        }
        c.close();
        return w;
    }

    // Cập nhật điểm
    public boolean updateScore(int subjectId, double tx1, double tx2, double ck) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("diemTX1", tx1);
        cv.put("diemTX2", tx2);
        cv.put("diemCK", ck);

        double[] w = getWeightsForSubject(subjectId);
        double tbm = tx1 * w[0] + tx2 * w[1] + ck * w[2];
        cv.put("diemTB", tbm);

        int rows = db.update("DiemSo", cv, "MonHocmaMonHoc = ?", new String[]{String.valueOf(subjectId)});
        return rows > 0;
    }
    public boolean deleteSubject(int maMonHoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("MonHoc", "maMonHoc = ?",
                new String[]{String.valueOf(maMonHoc)});
        return rows > 0;
    }
    public void clearAllUserData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("TaiLieu", null, null);
            db.delete("DiemSo", null, null);
            db.delete("MucTieuDiem", null, null);
            db.delete("ThoiGianBieu", null, null);
            db.delete("MonHoc", "maMonHoc > 5", null);
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='TaiLieu'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='ThoiGianBieu'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='MucTieuDiem'");
            db.execSQL("DELETE FROM sqlite_sequence WHERE name='DiemSo'");
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }
}