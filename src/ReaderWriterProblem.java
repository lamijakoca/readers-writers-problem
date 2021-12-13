import javax.swing.*;
import java.awt.*;
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
                JFrame frame = new JFrame();
                JPanel panel = new JPanel();
                JTextArea area = new JTextArea();

                area.setSize(450,250);
                panel.add(area);

                frame.add(panel);
                frame.setVisible(true);
                frame.setSize(450, 250);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                y.acquire();
                readSem.acquire();
                x.acquire();
                readerCount++;
                if (readerCount == 1)// checks for first reader
                    writeSem.acquire();
                x.release();// Releasing entries for other readers
                readSem.release();
                y.release();

                panel.setName(Thread.currentThread().getName());
                // Critical section when reading is performed
                System.out.println("Thread " + Thread.currentThread().getName() + " is reading the file");
                area.setText("Thread " + Thread.currentThread().getName() + " is reading the file \n");

                //READ FILE
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader("D:/Code/Lamija/Java/readers-writers-problem/src/test"));
                    String contentLine = br.readLine();
                    while (contentLine != null) {
                        area.setText(contentLine);
                        contentLine = br.readLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Thread.sleep(1500);
                System.out.println("Thread " + Thread.currentThread().getName() + " finished reading the file");
                area.setText("Thread " + Thread.currentThread().getName() + " finished reading the file \n");
                x.acquire();
                readerCount--;
                System.out.println("Number of Readers Left: " + readerCount);
                area.setText("Number of Readers Left: " + readerCount + "\n");
                if (readerCount == 0)// check for last reader
                    writeSem.release();
                x.release();

            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    static class Write implements Runnable {
        @Override
        public void run() {
            try {
                JFrame frame = new JFrame();
                JPanel panel = new JPanel();
                JTextArea area = new JTextArea();

                area.setSize(450,250);
                panel.add(area);

                frame.add(panel);
                frame.setVisible(true);
                frame.setSize(450, 250);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                readSem.acquire();// locking the readers
                writeSem.acquire();// reserving the place for the writers

                System.out.println("Thread " + Thread.currentThread().getName() + " is writing to the file. Enter the String: ");
                area.setText("Thread " + Thread.currentThread().getName() + " is writing to the file. Enter the String: \n");
                write();
                Thread.sleep(2500);

                System.out.println("Thread " + Thread.currentThread().getName() + " finished writing to the file \n");
                area.setText("Thread " + Thread.currentThread().getName() + " finished writing to the file \n");
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
                        new FileWriter("D:/Code/Lamija/Java/readers-writers-problem/src/test", append));

                while (true) {
                    String line = sc.nextLine();
                    if (line.contains("THE END"))
                        break;
                    pw.println(line);
                }

                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sc.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Read read = new Read();
        Write write = new Write();
        Thread t1 = new Thread(read);
        t1.setName("Reader");

        Thread t2 = new Thread(write);
        t2.setName("Writer");


        t1.start();
        t2.start();
    }
}