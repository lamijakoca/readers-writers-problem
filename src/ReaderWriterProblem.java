import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ReaderWriterProblem{

    static int readerCount = 0;// no need for writerCounter as we have only 1 writer
    static Semaphore x = new Semaphore(1);
    static Semaphore y = new Semaphore(1);
    static Semaphore readSemaf = new Semaphore(1);
    static Semaphore writeSemaf = new Semaphore(1);
    static Scanner sc = new Scanner(System.in);
    static boolean append;

    /*
     * The Runnable Reader Class
     */
    static class Read implements Runnable {
        @Override
        public void run() {
            try {
                // random generator for sleep value to get random value in the interval
                // [1000-2500]
                Random rgen = new Random();
                int sleepT = 1000 + rgen.nextInt(500);
                // Putting the lock
                y.acquire();
                readSemaf.acquire();
                x.acquire();
                readerCount++;
                if (readerCount == 1)// checks for first reader
                    writeSemaf.acquire();
                x.release();// Releasing entries for other readers
                readSemaf.release();
                y.release();

                // Critical section when reading is performed
                System.out.println("Thread " + Thread.currentThread().getName() + " is reading the file");
                read();
                Thread.sleep(sleepT);
                System.out.println("Thread " + Thread.currentThread().getName() + " finished reading the file");

                x.acquire();
                readerCount--;
                System.out.println("Number of Readers Left: " + readerCount);
                if (readerCount == 0)// check for last reader
                    writeSemaf.release();
                x.release();

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        /*
         * read method performs reading from 3 copies of the file. Each reader accesses
         * a randomly assigned file. While loop prints the internals of the files into
         * the console.
         */
        private void read() {
            BufferedReader br = null;
            Random rgen = new Random();
            try {
                int num1 = 1 + rgen.nextInt(2);
                if (num1 == 1) {
                    br = new BufferedReader(new FileReader("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test"));
                } else if (num1 == 2) {
                    br = new BufferedReader(new FileReader("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test1"));
                }
                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    System.out.println(line);
                }

            } catch (FileNotFoundException e) {
                System.err.println("The file you specified does not exist.");
            } catch (IOException e) {
                System.err.println("Some other IO exception occured. Message: " + e.getMessage());
            } finally {
                try {
                    if (br != null)
                        br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * The Runnable Writer Class
     */
    static class Write implements Runnable {
        @Override
        public void run() {
            try {
                // random generator for sleep value to get random value in the interval
                // [1000-2500]
                Random rgen = new Random();
                int sleepT = 1000 + rgen.nextInt(1501);
                readSemaf.acquire();// locking the readers
                writeSemaf.acquire();// reserving the place for the writers

                // Critical section when reading is performed
                System.out.println(
                        "Thread " + Thread.currentThread().getName() + " is writing to the file. Enter the String: ");
                write();
                Thread.sleep(sleepT);

                System.out.println("Thread " + Thread.currentThread().getName() + " finished writing to the file");
                writeSemaf.release();
                readSemaf.release();
                // as we have only 1 writer no need to check for first and last writer
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

        /*
         * write method performs writing to all 3 copies of the files. After the String
         * is written to the console by a user type THE END to close the scanner.
         */
        private void write() {
            try {
                append = true;
                PrintWriter pw = new PrintWriter(
                        new FileWriter("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test", append));
                PrintWriter pw1 = new PrintWriter(
                        new FileWriter("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test1", append));

                while (true) {
                    String line = sc.nextLine();
                    if (line.contains("THE END"))
                        break;

                    pw.println(line);
                    pw1.println(line);
                }

                pw.close();
                pw1.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            sc.close();

        }
    }

    public static void main(String[] args) throws Exception {
        // creating reader and writer
        Read read = new Read();
        Write write = new Write();
        boolean stop = false;
        // writer thread
        Thread t3 = new Thread(write);
        t3.setName("threadWriter");
        t3.start();
        // in the following while loop we create an infinite amount of readers trying to
        // access the txt files
        int i = 1;
        while (!stop) {
            Thread t1 = new Thread(read);
            t1.setName("thread" + i);
            i++;
            stop = true;
            if (stop) {
                t1.start();
                stop = false;
            }
        }
    }

}
