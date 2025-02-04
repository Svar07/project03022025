import java.io.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class WriteAndRead {

    private static final String FILE_NAME = "numbers.txt";
    private static String lastRead = "";
    private static final ReentrantLock fileLock = new ReentrantLock();

    public static void main(String[] args) {
        try (ExecutorService pool = Executors.newFixedThreadPool(3)) {
            pool.submit(new EvenTask());
            pool.submit(new OddTask());
            pool.submit(new ReadTask());
            pool.shutdown();
        }
    }

    private static void saveNumber(int num) {
        fileLock.lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(num + "\n");
        } catch (IOException e) {
            System.err.println("Ошибка при записи в файл: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }

    private static String getLastLine() {
        fileLock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            String lastLine = "";
            while ((line = reader.readLine()) != null) {
                lastLine = line;
            }
            return lastLine;
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return "";
        } finally {
            fileLock.unlock();
        }
    }

    private static void pause() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Поток прерван во время паузы: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private static class EvenTask implements Runnable {
        @Override
        public void run() {
            System.out.println("Запущен поток для записи четных чисел.");
            Random rand = new Random();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int num = rand.nextInt(10) * 2;
                    saveNumber(num);
                    System.out.println("Сохранено чётное число: " + num);
                    pause();
                } catch (Exception e) {
                    System.err.println("Ошибка в потоке записи четных чисел: " + e.getMessage());
                }
            }
        }
    }

    private static class OddTask implements Runnable {
        @Override
        public void run() {
            System.out.println("Запущен поток для записи НЕчетных чисел.");
            Random rand = new Random();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int num = rand.nextInt(10) * 2 + 1;
                    saveNumber(num);
                    System.out.println("Сохранено НЕчётное число: " + num);
                    pause();
                } catch (Exception e) {
                    System.err.println("Ошибка в потоке НЕчетных чисел: " + e.getMessage());
                }
            }
        }
    }

    private static class ReadTask implements Runnable {
        @Override
        public void run() {
            System.out.println("Запущен поток для чтения файла.");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String value = getLastLine();
                    if (value != null && !value.isEmpty() && !value.equals(lastRead)) {
                        lastRead = value;
                        System.out.println("Последнее число в файле: " + lastRead);
                    }
                    pause();
                } catch (Exception e) {
                    System.err.println("Ошибка в потоке чтения файла: " + e.getMessage());
                }
            }
        }
    }
}
