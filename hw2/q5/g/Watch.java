// Watch link between Person and Movie
public class Watch {
    private Person person;
    private Movie movie;
    private int rating;

    public Watch(Person person, Movie movie, int rating) {
        this.person = person;
        this.movie = movie;
        this.rating = rating;
        person.addWatch(this);
        movie.addWatch(this);
    }

    public int getRating() {
        return rating;
    }

    public Person getPerson() {
        return person;
    }

    public Movie getMovie() {
        return movie;
    }
}
