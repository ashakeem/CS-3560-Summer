// Dog class
import java.util.ArrayList;
import java.util.List;

public class Dog {
    private String breed;
    private String name;
    private List<Paw> paws;

    public Dog() {
        this.paws = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            this.paws.add(new Paw(i));
        }
    }

    public List<Paw> getPaws() {
        return paws;
    }
}
