package net.jmhertlein.mctowns.util.reporting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class ReceiveBugReportTask implements Runnable {

    private final Set<BugReport> reports;
    private Socket clientSocket;

    public ReceiveBugReportTask(Set<BugReport> reports, Socket client) {
        this.reports = reports;
        this.clientSocket = client;
    }

    @Override
    public void run() {
        try (Socket client = this.clientSocket; ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
            Object rawReceived = ois.readObject();
            if (!(rawReceived instanceof BugReport)) {
                System.err.println("Received object was not a BugReport!");
                return;
            }

            BugReport received = (BugReport) rawReceived;

            System.out.println("Received report from " + client.getInetAddress());
            synchronized(reports) {
                reports.add(received);
            }

        } catch (IOException ex) {
            Logger.getLogger(ReceiveBugReportTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ReceiveBugReportTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
