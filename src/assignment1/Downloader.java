package assignment1;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author samuellouvan
 */
public class Downloader extends SwingWorker<Void, Void> {

    private File file;  // The file to be read
    private long start; // The start location of the file block for this thread
    private long end;   // The end location of the file block for this thread
    private int id;     // The ID of this thread
    private final boolean[] statuses; // Shared array to store the finish status of all the threads

    /*
     * Initialize all the instance variables
     */
    public Downloader(int id, long start, long end, File file, boolean[] statuses) {
        this.file = file;
        this.start = start;
        this.end = end;
        this.id = id;
        this.statuses = statuses;
    }

    /*
     * This method will merge all the file blocks into one file
     */
    protected void merge() throws FileNotFoundException, IOException {
        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        FileOutputStream fout = new FileOutputStream(file.getName() + ".downloaded");

        System.out.println("Merge in progress...");
        for (int i = 0; i < statuses.length; i++) {
            System.out.println(i);
            FileInputStream fin = new FileInputStream(file.getName() + "." + i);
            int data;
            while ((data = fin.read()) != -1) {
                byte byteData = (byte) data;
                fout.write(byteData);
            }
            fin.close();
        }
        System.out.println("Merge finish...");
        fout.close();
    }

    /*
     * return the id of the thread
     */
    public int getID() {
        return id;
    }

    @Override
    /*
     * This method must perform the following things - Reading the data using
     * the method read() from RandomAccessFile class and write the data into a
     * file block - While reading, update the progress bar - After it finishes
     * it must set the finish[i] to true E.g. if the thread id is 1 then it must
     * set finish[0] to true
     *
     * - It checks whether it is the last thread to finish by checking the other
     * values in array finish. - If it is the last thread that finishes then it
     * will continue with the merging process of all file blocks
     */
    public Void doInBackground() throws Exception {
        System.out.println("Thread " + getID() + " starts" + start+" :"+end);

        try {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            FileOutputStream fout;
            if (statuses.length > 1) {
                fout = new FileOutputStream(file.getName() + "." + id);
            } else {
                fout = new FileOutputStream(file.getName() + ".downloaded");
            }
            int cnt = 0;
            raf.seek(start);
            int data;
            setProgress(0);
            while (raf.getFilePointer() <= end) {
                data = raf.read();
                byte byteData = (byte) data;
                cnt++;
                setProgress((int) (100 * cnt / (double) (end - start + 1)));
                int percent = (int) (100 * cnt / (double) (end - start + 1));
                if (percent == 100)
                System.out.println("Thread " + id + " progress :" + percent);
                fout.write(byteData);

            }
            fout.close();
            raf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (statuses.length > 1) {
            synchronized (statuses) {
                statuses[id] = true;
                if (isLastToFinish()) {

                    System.out.println("Thread " + id + " finished last");
                    merge();
                } else {
                    System.out.println("Thread " + id + " finished");
                }
            }
        } else {
            System.out.println("Thread " + id + " finished");
        }

        return null;
    }
    /*
     * return true if the current thread is the last thread to finish. check
     * this by examining the other values in array finish, whether the other
     * threads already finish their task or not.
     */

    protected boolean isLastToFinish() {
        for (int i = 0; i < statuses.length; i++) {
            if (i != id) {
                if (!statuses[i]) {
                    return false;
                }
            }
        }
        return true;
    }
}
