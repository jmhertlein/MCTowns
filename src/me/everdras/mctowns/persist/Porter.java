package me.everdras.mctowns.persist;

import java.io.*;

/**
 * A simple tool to do IO with deflated/serialized objects in files.
 * @author Josh
 */
public class Porter {

    private File target;
    private FileOutputStream fos;
    private ObjectOutputStream oos;
    private FileInputStream fis;
    private ObjectInputStream ois;

    /**
     * Makes a new Porter, pointed at the tarfile
     * @param tarfile the file to target
     */
    public Porter(String tarfile) {

        target = new File(tarfile);
        fos = null;
        oos = null;
        fis = null;
        ois = null;

    }

    /**
     * Prepares the porter for output
     * @return true if the porter is ready, false if it is not because some exception occurred
     * @see close()
     */
    public boolean primeOutput() {

        try {
            fos = new FileOutputStream(target);
            oos = new ObjectOutputStream(fos);
        } catch (Exception ex) {
            return false;
        }
        return true;

    }

    /**
     * Prepares the porter for input
     * @return true if the porter is ready, false if it is not because some exception occurred
     * @see close()
     */
    public boolean primeInput() {
        try {
            fis = new FileInputStream(target);
            ois = new ObjectInputStream(fis);
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /**
     * Outputs the Object as a serialized java object
     * @param o the object to serialize
     * @return true if output was successful, false otherwise
     */
    public boolean output(Object o) {
        try {
            oos.writeObject(o);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Reads in the object from the file
     * @return the Object, or null if any error occurred
     */
    public Object input() {
        Object o = null;

        try {
            o = ois.readObject();
        } catch (Exception e) {
        }

        return o;
    }

    /**
     * Closes the porter's open connections to the file.
     * Should be used if primeOutput or primeInput were used.
     */
    public void close() {
        try {
            if (oos != null) {
                oos.close();
            }

            if (ois != null) {
                ois.close();
            }

            if (fos != null) {
                fos.close();
            }

            if (fis != null) {
                fis.close();
            }
        } catch (IOException ex) {
        }

    }

    /**
     * 
     * @return the path of the file to which which Porter is pointing
     */
    public String getPath() {
        return target.toString();
    }
}
