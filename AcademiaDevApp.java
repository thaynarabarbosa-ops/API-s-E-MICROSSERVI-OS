import java.util.*;
import java.util.stream.Collectors;
import java.lang.reflect.Field;

public class AcademiaDevApp {

    enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    enum CourseStatus {
        ACTIVE, INACTIVE
    }

    static abstract class User {
        private String name;
        private String email;

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }

    static class Admin extends User {
        public Admin(String name, String email) {
            super(name, email);
        }
    }

    static class Student extends User {

        private SubscriptionPlan subscriptionPlan;
        private List<Enrollment> enrollments = new ArrayList<>();

        public Student(String name, String email, SubscriptionPlan subscriptionPlan) {
            super(name, email);
            this.subscriptionPlan = subscriptionPlan;
        }

        public SubscriptionPlan getSubscriptionPlan() {
            return subscriptionPlan;
        }

        public void setSubscriptionPlan(SubscriptionPlan subscriptionPlan) {
            this.subscriptionPlan = subscriptionPlan;
        }

        public List<Enrollment> getEnrollments() {
            return enrollments;
        }

        public long getActiveEnrollmentsCount() {
            return enrollments.size();
        }
    }

    static class Course {

        private String title;
        private String description;
        private String instructorName;
        private int durationInHours;
        private DifficultyLevel difficultyLevel;
        private CourseStatus status;

        public Course(String title, String description, String instructorName,
                      int durationInHours, DifficultyLevel difficultyLevel,
                      CourseStatus status) {
            this.title = title;
            this.description = description;
            this.instructorName = instructorName;
            this.durationInHours = durationInHours;
            this.difficultyLevel = difficultyLevel;
            this.status = status;
        }

        public String getTitle() { return title; }
        public String getInstructorName() { return instructorName; }
        public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
        public CourseStatus getStatus() { return status; }

        public void setStatus(CourseStatus status) {
            this.status = status;
        }
    }

    interface SubscriptionPlan {
        boolean canEnroll(Student student);
        String getPlanName();
    }

    static class BasicPlan implements SubscriptionPlan {
        @Override
        public boolean canEnroll(Student student) {
            return student.getActiveEnrollmentsCount() < 3;
        }

        @Override
        public String getPlanName() {
            return "Basic";
        }
    }

    static class PremiumPlan implements SubscriptionPlan {
        @Override
        public boolean canEnroll(Student student) {
            return true;
        }

        @Override
        public String getPlanName() {
            return "Premium";
        }
    }

    static class Enrollment {

        private Student student;
        private Course course;
        private double progress;

        public Enrollment(Student student, Course course) {
            this.student = student;
            this.course = course;
            this.progress = 0.0;
        }

        public Course getCourse() { return course; }
        public double getProgress() { return progress; }

        public void updateProgress(double progress) {
            if (progress < 0 || progress > 100)
                throw new IllegalArgumentException("Progresso deve estar entre 0 e 100.");
            this.progress = progress;
        }
    }

    static class SupportTicket {
        private String title;
        private String message;
        private User openedBy;

        public SupportTicket(String title, String message, User openedBy) {
            this.title = title;
            this.message = message;
            this.openedBy = openedBy;
        }

        public String getTitle() { return title; }
        public User getOpenedBy() { return openedBy; }
    }

    static class EnrollmentException extends RuntimeException {
        public EnrollmentException(String message) {
            super(message);
        }
    }

    static class GenericCsvExporter {

        public static <T> String export(List<T> data, List<String> fields) {
            StringBuilder sb = new StringBuilder();

            sb.append(String.join(",", fields)).append("\n");

            for (T obj : data) {
                for (String fieldName : fields) {
                    try {
                        Field field = obj.getClass().getDeclaredField(fieldName);
                        field.setAccessible(true);
                        Object value = field.get(obj);
                        sb.append(value != null ? value : "").append(",");
                    } catch (Exception e) {
                        sb.append(",");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append("\n");
            }

            return sb.toString();
        }
    }

    private static Map<String, Course> courses = new HashMap<>();
    private static Map<String, User> users = new HashMap<>();
    private static Queue<SupportTicket> tickets = new ArrayDeque<>();

    private static void loadInitialData() {

        Course c1 = new Course("Java Básico", "Intro Java", "Carlos Silva",
                20, DifficultyLevel.BEGINNER, CourseStatus.ACTIVE);

        Course c2 = new Course("Spring Boot", "API REST", "Ana Lima",
                30, DifficultyLevel.INTERMEDIATE, CourseStatus.ACTIVE);

        Course c3 = new Course("Arquitetura Avançada", "DDD e Microsserviços",
                "Carlos Silva", 40, DifficultyLevel.ADVANCED, CourseStatus.INACTIVE);

        courses.put(c1.getTitle(), c1);
        courses.put(c2.getTitle(), c2);
        courses.put(c3.getTitle(), c3);

        Admin admin = new Admin("Admin Master", "admin@dev.com");
        Student s1 = new Student("João", "joao@dev.com", new BasicPlan());
        Student s2 = new Student("Maria", "maria@dev.com", new PremiumPlan());

        users.put(admin.getEmail(), admin);
        users.put(s1.getEmail(), s1);
        users.put(s2.getEmail(), s2);
    }

    private static void enroll(Student student, Course course) {

        if (course.getStatus() == CourseStatus.INACTIVE)
            throw new EnrollmentException("Curso inativo.");

        boolean alreadyEnrolled = student.getEnrollments().stream()
                .anyMatch(e -> e.getCourse().equals(course));

        if (alreadyEnrolled)
            throw new EnrollmentException("Aluno já matriculado.");

        if (!student.getSubscriptionPlan().canEnroll(student))
            throw new EnrollmentException("Limite do plano atingido.");

        student.getEnrollments().add(new Enrollment(student, course));
    }

    private static void processTicket() {
        SupportTicket ticket = tickets.poll();
        if (ticket == null) {
            System.out.println("Nenhum ticket na fila.");
        } else {
            System.out.println("Processando ticket: " + ticket.getTitle());
        }
    }

    private static void generateReports() {

        System.out.println("\n--- Cursos BEGINNER ordenados ---");
        courses.values().stream()
                .filter(c -> c.getDifficultyLevel() == DifficultyLevel.BEGINNER)
                .sorted(Comparator.comparing(Course::getTitle))
                .forEach(c -> System.out.println(c.getTitle()));

        System.out.println("\n--- Instrutores únicos de cursos ativos ---");
        Set<String> instructors = courses.values().stream()
                .filter(c -> c.getStatus() == CourseStatus.ACTIVE)
                .map(Course::getInstructorName)
                .collect(Collectors.toSet());

        instructors.forEach(System.out::println);

        System.out.println("\n--- Agrupamento por plano ---");
        Map<String, List<Student>> grouped = users.values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .collect(Collectors.groupingBy(
                        s -> s.getSubscriptionPlan().getPlanName()
                ));

        grouped.forEach((plan, list) ->
                System.out.println(plan + ": " + list.size() + " alunos"));

        System.out.println("\n--- Média geral de progresso ---");
        double avg = users.values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .flatMap(s -> s.getEnrollments().stream())
                .mapToDouble(Enrollment::getProgress)
                .average()
                .orElse(0.0);

        System.out.println(avg);

        System.out.println("\n--- Aluno com mais matrículas ---");
        Optional<Student> top = users.values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .max(Comparator.comparingLong(Student::getActiveEnrollmentsCount));

        top.ifPresent(s -> System.out.println(s.getName()));
    }

    public static void main(String[] args) {

        loadInitialData();

        Student joao = (Student) users.get("joao@dev.com");
        Course java = courses.get("Java Básico");

        enroll(joao, java);

        joao.getEnrollments().get(0).updateProgress(50);

        tickets.add(new SupportTicket("Erro no vídeo", "Não carrega", joao));

        generateReports();

        processTicket();

        System.out.println("\n--- Export CSV Students ---");
        List<Student> students = users.values().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .toList();

        String csv = GenericCsvExporter.export(
                students,
                List.of("name", "email")
        );

        System.out.println(csv);
    }
}