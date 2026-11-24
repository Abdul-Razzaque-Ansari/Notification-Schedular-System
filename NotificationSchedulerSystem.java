import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

// Main Class
public class NotificationSchedulerSystem {

    static Scanner sc = new Scanner(System.in);

    static PriorityQueueManager priorityQueue =
            new PriorityQueueManager();

    static NormalQueueManager normalQueue =
            new NormalQueueManager();

    static CircularQueueManager circularQueue =
            new CircularQueueManager(10);

    static Scheduler scheduler =
            new Scheduler(priorityQueue, normalQueue,
                          circularQueue, sc);

    static ArrayList<Notification> allNotifications =
            new ArrayList<>();

    public static void main(String[] args) {

        int choice;

        do {
            System.out.println("\n===== NOTIFICATION SCHEDULER =====");
            System.out.println("1. Add Notification");
            System.out.println("2. Process Notification");
            System.out.println("3. Retry Failed Notification");
            System.out.println("4. Display Queues");
            System.out.println("5. Display All Notifications");
            System.out.println("6. Exit");

            System.out.print("Enter choice: ");

            while (!sc.hasNextInt()) {
                System.out.println("Enter a valid number.");
                sc.next();
            }

            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    addNotification();
                    break;

                case 2:
                    scheduler.processNotification();
                    break;

                case 3:
                    scheduler.retryNotification();
                    break;

                case 4:
                    priorityQueue.display();
                    normalQueue.display();
                    circularQueue.display();
                    break;

                case 5:
                    displayAll();
                    break;

                case 6:
                    System.out.println("Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 6);

        sc.close();
    }

    static void addNotification() {

        System.out.print("Enter ID: ");
        String id = sc.nextLine().trim();

        for (Notification n : allNotifications) {
            if (n.getId().equalsIgnoreCase(id)) {
                System.out.println("ID already exists.");
                return;
            }
        }

        System.out.print("Enter Message: ");
        String message = sc.nextLine().trim();

        if (message.isEmpty() || id.isEmpty()) {
            System.out.println("ID and message cannot be empty.");
            return;
        }

        System.out.print("Priority (HIGH/NORMAL): ");
        String p = sc.nextLine().trim();

        NotificationPriority priority;

        if (p.equalsIgnoreCase("HIGH")) {
            priority = NotificationPriority.HIGH;
        } else if (p.equalsIgnoreCase("NORMAL")) {
            priority = NotificationPriority.NORMAL;
        } else {
            System.out.println("Invalid priority.");
            return;
        }

        System.out.print("Scheduled Time (yyyy-MM-dd HH:mm): ");
        String time = sc.nextLine().trim();

        LocalDateTime dateTime;

        try {
            dateTime = LocalDateTime.parse(
                time,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            );
        } catch (Exception e) {
            System.out.println("Invalid date format.");
            return;
        }

        Notification n =
            new Notification(id, message, priority, dateTime);

        if (priority == NotificationPriority.HIGH) {
            priorityQueue.add(n);
            System.out.println("Added to Priority Queue.");
        } else {
            normalQueue.add(n);
            System.out.println("Added to Normal Queue.");
        }

        allNotifications.add(n);
        System.out.println("Notification added successfully.");
    }

    static void displayAll() {

        if (allNotifications.isEmpty()) {
            System.out.println("No notifications available.");
            return;
        }

        for (Notification n : allNotifications) {
            System.out.println(n);
        }
    }
}

// Notification Priority
enum NotificationPriority {
    HIGH, NORMAL
}

// Notification Status
enum NotificationStatus {
    PENDING, COMPLETED, RETRY_PENDING, FAILED
}

// Notification Class
class Notification {

    private String id;
    private String message;
    private NotificationPriority priority;
    private LocalDateTime scheduledTime;
    private NotificationStatus status;
    private int retryCount;

    public Notification(String id, String message,
                        NotificationPriority priority,
                        LocalDateTime scheduledTime) {

        this.id = id;
        this.message = message;
        this.priority = priority;
        this.scheduledTime = scheduledTime;
        this.status = NotificationStatus.PENDING;
        this.retryCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public int increaseRetry() {
        return ++retryCount;
    }

    public String toString() {
        return "ID: " + id
            + " | Message: " + message
            + " | Priority: " + priority
            + " | Time: " + scheduledTime
            + " | Status: " + status
            + " | Retries: " + retryCount;
    }
}

// Priority Queue Manager
class PriorityQueueManager {

    PriorityQueue<Notification> queue =
        new PriorityQueue<>(
            Comparator.comparing(Notification::getScheduledTime)
                      .thenComparing(Notification::getId)
        );

    void add(Notification n) {
        queue.offer(n);
    }

    Notification peek() {
        return queue.peek();
    }

    Notification remove() {
        return queue.poll();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }

    void display() {
        System.out.println("\n--- Priority Queue ---");

        if (queue.isEmpty()) {
            System.out.println("Empty");
            return;
        }

        ArrayList<Notification> list =
            new ArrayList<>(queue);

        list.sort(
            Comparator.comparing(Notification::getScheduledTime)
        );

        for (Notification n : list) {
            System.out.println(n);
        }
    }
}

// Normal Queue Manager
class NormalQueueManager {

    Queue<Notification> queue = new LinkedList<>();

    void add(Notification n) {
        queue.offer(n);
    }

    Notification peek() {
        return queue.peek();
    }

    Notification remove() {
        return queue.poll();
    }

    boolean isEmpty() {
        return queue.isEmpty();
    }

    void display() {
        System.out.println("\n--- Normal Queue ---");

        if (queue.isEmpty()) {
            System.out.println("Empty");
            return;
        }

        for (Notification n : queue) {
            System.out.println(n);
        }
    }
}

// Circular Queue Manager
class CircularQueueManager {

    Notification[] queue;
    int front = 0;
    int rear = -1;
    int size = 0;

    CircularQueueManager(int capacity) {
        queue = new Notification[capacity];
    }

    boolean add(Notification n) {

        if (size == queue.length) {
            return false;
        }

        rear = (rear + 1) % queue.length;
        queue[rear] = n;
        size++;

        return true;
    }

    Notification remove() {

        if (size == 0) {
            return null;
        }

        Notification n = queue[front];
        queue[front] = null;

        front = (front + 1) % queue.length;
        size--;

        if (size == 0) {
            front = 0;
            rear = -1;
        }

        return n;
    }

    boolean isEmpty() {
        return size == 0;
    }

    void display() {

        System.out.println("\n--- Circular Queue ---");

        if (isEmpty()) {
            System.out.println("Empty");
            return;
        }

        for (int i = 0; i < size; i++) {
            int index = (front + i) % queue.length;
            System.out.println(queue[index]);
        }
    }
}

// Greedy Algorithm
class GreedyAlgorithm {

    Notification select(
        PriorityQueueManager high,
        NormalQueueManager normal,
        LocalDateTime currentTime) {

        Notification n = high.peek();

        // First preference: eligible HIGH priority
        if (n != null &&
            !n.getScheduledTime().isAfter(currentTime)) {

            return high.remove();
        }

        // Second preference: eligible NORMAL notification
        n = normal.peek();

        if (n != null &&
            !n.getScheduledTime().isAfter(currentTime)) {

            return normal.remove();
        }

        return null;
    }
}

// Scheduler Class
class Scheduler {

    PriorityQueueManager high;
    NormalQueueManager normal;
    CircularQueueManager retry;
    Scanner sc;

    GreedyAlgorithm greedy = new GreedyAlgorithm();

    final int MAX_RETRIES = 3;

    Scheduler(PriorityQueueManager h,
              NormalQueueManager n,
              CircularQueueManager r,
              Scanner sc) {

        high = h;
        normal = n;
        retry = r;
        this.sc = sc;
    }

    void processNotification() {

        Notification n = greedy.select(
            high, normal, LocalDateTime.now()
        );

        if (n == null) {
            System.out.println("No eligible notification.");
            return;
        }

        System.out.println("\nSelected Notification:");
        System.out.println(n);

        process(n);
    }

    void retryNotification() {

        Notification n = retry.remove();

        if (n == null) {
            System.out.println("No notification to retry.");
            return;
        }

        System.out.println("\nRetrying Notification:");
        System.out.println(n);

        process(n);
    }

    void process(Notification n) {

        System.out.println("\n1. Success");
        System.out.println("2. Failure");
        System.out.print("Enter result: ");

        int result;

        if (!sc.hasNextInt()) {
            System.out.println("Invalid input.");
            sc.nextLine();
            return;
        }

        result = sc.nextInt();
        sc.nextLine();

        if (result == 1) {

            n.setStatus(NotificationStatus.COMPLETED);

            System.out.println("Notification completed.");
        }

        else if (result == 2) {

            int count = n.increaseRetry();

            if (count <= MAX_RETRIES) {

                if (retry.add(n)) {
                    n.setStatus(NotificationStatus.RETRY_PENDING);

                    System.out.println(
                        "Moved to Circular Queue for retry."
                    );
                } else {
                    n.setStatus(NotificationStatus.FAILED);
                    System.out.println("Retry queue is full.");
                }

            } else {

                n.setStatus(NotificationStatus.FAILED);

                System.out.println("Maximum retries reached.");
            }
        }

        else {
            System.out.println("Invalid result.");
        }
    }
}