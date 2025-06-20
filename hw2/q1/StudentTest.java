package q1;
public class StudentTest {
    public static void main(String[] args) {
        Student s1 = new Student("John", "CS", 3.5);

        Student s2 = new Student();
        s2.setName("Mary Ann");
        s2.setMajor("CE");
        s2.setGpa(3.3);

        System.out.println(s1.toString());
        System.out.println(s2.toString());
    }
}
