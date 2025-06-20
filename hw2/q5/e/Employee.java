// Employee base class
public class Employee {
    protected String name;
    protected int hours;

    public Employee(String name, int hours) {
        this.name = name;
        this.hours = hours;
    }

    public double calculateSalary() {
        return hours * 20;
    }

    public int getHours() {
        return hours;
    }

    public String getName() {
        return name;
    }
}
