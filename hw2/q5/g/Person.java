// Person class
import java.util.ArrayList;
import java.util.List;

public class Person {
    private String name;
    private List<Watch> watches;

    public Person() {
        this.watches = new ArrayList<>();
    }

    public void addWatch(Watch watch) {
        if (watch != null && !watches.contains(watch)) {
            watches.add(watch);
        }
    }

    public Movie[] getMovies() {
        return watches.stream()
                      .map(Watch::getMovie)
                      .toArray(Movie[]::new);
    }
}
