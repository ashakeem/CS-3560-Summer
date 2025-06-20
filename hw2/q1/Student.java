package q1;
public class Student {
    private String name;
    private String major;
    private double gpa;

    public Student() {
    }

    public Student(String name, String major) {
        this.name = name;
        this.major = major;
        this.gpa   = 0.0;
    }

    public Student(String name, String major, double gpa) {
        this.name  = name;
        this.major = major;
        this.gpa   = gpa;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }
    public void setMajor(String major) {
        this.major = major;
    }

    public double getGpa() {
        return gpa;
    }
    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    @Override
    public String toString() {
        return "Name: " + name
             + ", Major: " + major
             + ", GPA: " + gpa;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student)) return false;
        Student other = (Student) obj;
        if (name == null ? other.name != null : !name.equals(other.name))   return false;
        if (major == null ? other.major != null : !major.equals(other.major)) return false;
        return true;
    }
}
