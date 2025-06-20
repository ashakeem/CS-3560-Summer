// Movie class
import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String name;
    private String genre;
    private List<Watch> watches;

    public Movie() {
        this.watches = new ArrayList<>();
    }

    public void addWatch(Watch watch) {
        if (watch != null && !watches.contains(watch)) {
            watches.add(watch);
        }
    }

    public Person[] getPersons() {
        return watches.stream()
                      .map(Watch::getPerson)
                      .toArray(Person[]::new);
    }
}
