import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class DataProcessingSystem {
    static class Task {
        int id;
        Task(int id) { this.id = id; }
    }

    static class SharedQueue {
        private final Queue<Task> tasks = new LinkedList<>();
        private final ReentrantLock lock = new ReentrantLock();

        public void addTask(Task task) {
            lock.lock();
            try {
                tasks.offer(task);
            } finally {
                lock.unlock();
            }
        }

        public Task getTask() {
            lock.lock();
            try {
                return tasks.poll();
            } finally {
                lock.unlock();
            }
        }
    }

    static class Worker extends Thread {
        private final int id;
        private final SharedQueue queue;

        Worker(int id, SharedQueue queue) {
            this.id = id;
            this.queue = queue;
        }

        public void run() {
            while (true) {
                Task task = queue.getTask();
                if (task == null) break;
                try {
                    System.out.println("Worker " + id + " processing task " + task.id);
                    Thread.sleep((int)(Math.random() * 1000)); // Simulate processing delay
                    System.out.println("Worker " + id + " completed task " + task.id);
                } catch (InterruptedException e) {
                    System.err.println("Worker " + id + " interrupted.");
                    break;
                } catch (Exception e) {
                    System.err.println("Worker " + id + " encountered error: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        SharedQueue queue = new SharedQueue();
        for (int i = 1; i <= 20; i++) {
            queue.addTask(new Task(i));
        }

        int numWorkers = 5;
        Thread[] workers = new Thread[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new Worker(i + 1, queue);
            workers[i].start();
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                System.err.println("Main thread interrupted.");
            }
        }

        System.out.println("\nAll tasks completed.");
    }
}
