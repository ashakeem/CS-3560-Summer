// Team class
import java.util.ArrayList;
import java.util.List;

public class Team {
    private int code;
    private List<Player> players;

    public Team() {
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        if (player != null) {
            players.add(player);
        }
    }

    public List<Player> getPlayers() {
        return players;
    }
}
