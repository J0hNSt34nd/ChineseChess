package Tool;

public class PlayerRecord {
    private int wins;
    private int draws;
    private int losses;

    public PlayerRecord() {}

    public PlayerRecord(int wins, int draws, int losses) {
        this.wins = wins;
        this.draws = draws;
        this.losses = losses;
    }

    public int getWins() {return wins; }
    public void setWins(int wins) {this.wins = wins; }

    public int getDraws() { return draws; }
    public void setDraws(int draws) {this.draws = draws; }

    public int getLosses() {return losses; }
    public void setLosses(int losses) {this.losses = losses; }

    public void addWin() {this.wins++; }
    public void addDraw() {this.draws++; }
    public void addLoss() { this.losses++; }

    @Override
    public String toString() {
        return String.format("胜:%d 和:%d 负:%d", wins, draws, losses);
    }
}
