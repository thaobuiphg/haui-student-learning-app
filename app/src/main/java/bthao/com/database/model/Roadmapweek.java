package bthao.com.database.model;

public class Roadmapweek {
    private int weekNumber;
    private String content;

    public Roadmapweek(int weekNumber, String content) {
        this.weekNumber = weekNumber;
        this.content = content;
    }

    public int getWeekNumber() { return weekNumber; }
    public String getContent() { return content; }
}