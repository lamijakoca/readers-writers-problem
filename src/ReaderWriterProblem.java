import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ReaderWriterProblem {

    static int readerCount = 0;
    static Semaphore x = new Semaphore(1);
    static Semaphore y = new Semaphore(1);
    static Semaphore readSem = new Semaphore(1);
    static Semaphore writeSem = new Semaphore(1);
    static Scanner sc = new Scanner(System.in);
    static boolean append;

    static class Read implements Runnable {
        @Override
        public void run() {
            try {
                y.acquire();
                readSem.acquire();
                x.acquire();
                readerCount++;
                if (readerCount == 1)// checks for first reader
                    writeSem.acquire();
                x.release();// Releasing entries for other readers
                readSem.release();
                y.release();

                // Critical section when reading is performed
                System.out.println("Thread " + Thread.currentThread().getName() + " is reading the file");
                read();
                Thread.sleep(1500);
                System.out.println("Thread " + Thread.currentThread().getName() + " finished reading the file");

                x.acquire();
                readerCount--;
                System.out.println("Number of Readers Left: " + readerCount);
                if (readerCount == 0)// check for last reader
                    writeSem.release();
                x.release();

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }



        //Citanje iz 2 kopije fajla. Svaki citac pristupa random fajlu..
//        private void read() {
//            BufferedReader br = null;
//            Random rgen = new Random();
//            try {
//                int num1 = 1 + rgen.nextInt(2);
//                if (num1 == 1) {
//                    br = new BufferedReader(new FileReader("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test"));
//                } else if (num1 == 2) {
//                    br = new BufferedReader(new FileReader("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test1"));
//                }
//                while (true) {
//                    String line = br.readLine();
//                    if (line == null)
//                        break;
//                    System.out.println(line);
//                }
//
//            } catch (FileNotFoundException e) {
//                System.err.println("The file you specified does not exist.");
//            } catch (IOException e) {
//                System.err.println("Some other IO exception occured. Message: " + e.getMessage());
//            } finally {
//                try {
//                    if (br != null)
//                        br.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

        private void read() {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader("/run/media/lamija/9C0863F60863CDB8/Code/Semaphores/RW-Problem/src/test"));
                String contentLine = br.readLine();
                while (contentLine != null) {
                    System.out.println(contentLine);
                    contentLine = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    static class Write implements Runnable {
        @Override
        public void run() {
            try {
                readSem.acquire();// locking the readers
                writeSem.acquire();// reserving the place for the writers

                System.out.println(
                        "Thread " + Thread.currentThread().getName() + " is writing to the file. Enter the String: ");
                write();
                Thread.sleep(2500);

                System.out.println("Thread " + Thread.currentThread().getName() + " finished writing to the file");
                writeSem.release();
                readSem.release();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

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
        Thread t1 = new Thread(read);
        t1.setName("Prvi");

        Thread t2 = new Thread(read);
        t2.setName("Drugi");

        Thread t3 = new Thread(write);
        t3.setName("Treci");

        Thread t4 = new Thread(read);
        t4.setName("Cetvrti");

        t1.start();
        t2.start();
        t3.start();
        t4.start();
    }
}