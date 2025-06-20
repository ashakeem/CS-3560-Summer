// Staff subclass
public class Staff extends Employee {
    private int role;

    public Staff(String name, int hours, int role) {
        super(name, hours);
        this.role = role;
    }
}
