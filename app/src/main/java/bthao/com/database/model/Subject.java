package bthao.com.database.model;

public class Subject {
    private int id;
    private String name;
    private double tx1, tx2, ck;
    private double tbm;
    private int credits;

    public Subject() {}

    public Subject(String name, double tx1, double tx2, double ck, int credits) {
        this.name = name;
        this.tx1 = tx1;
        this.tx2 = tx2;
        this.ck = ck;
        this.credits = credits;
    }

    public Subject(int id, String name, double tx1, double tx2, double ck, int credits) {
        this.id = id;
        this.name = name;
        this.tx1 = tx1;
        this.tx2 = tx2;
        this.ck = ck;
        this.credits = credits;
    }

    public Subject(int id, String name, double tx1, double tx2, double ck, int credits, double tbm) {
        this.id = id;
        this.name = name;
        this.tx1 = tx1;
        this.tx2 = tx2;
        this.ck = ck;
        this.credits = credits;
        this.tbm = tbm;
    }

    public double getTbm4() {
        if (tbm >= 8.5) return 4.0;
        else if (tbm >= 7.7) return 3.5;
        else if (tbm >= 7.0) return 3.0;
        else if (tbm >= 6.2) return 2.5;
        else if (tbm >= 5.5) return 2.0;
        else if (tbm >= 4.7) return 1.5;
        else if (tbm >= 4.0) return 1.0;
        else return 0.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTx1() {
        return tx1;
    }

    public void setTx1(double tx1) {
        this.tx1 = tx1;
    }

    public double getTx2() {
        return tx2;
    }

    public void setTx2(double tx2) {
        this.tx2 = tx2;
    }

    public double getCk() {
        return ck;
    }

    public void setCk(double ck) {
        this.ck = ck;
    }

    public double getTbm() {
        return tbm;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return getName();   // Chỉ hiện tên môn học trong Spinner
    }
}