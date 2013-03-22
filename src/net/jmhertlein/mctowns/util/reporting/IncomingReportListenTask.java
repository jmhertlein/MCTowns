package net.jmhertlein.mctowns.util.reporting;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class IncomingReportListenTask implements Runnable {

    private boolean stop;
    private Set<BugReport> reports;
    private ServerSocket s;

    public IncomingReportListenTask(Set<BugReport> reports) {
        stop = false;
        this.reports = reports;
    }

    @Override
    public void run() {
        try {
            s = new ServerSocket(9001);
        } catch (IOException ex) {
            Logger.getLogger(BugReportServerDaemon.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        while (!stop) {
            Socket client;

            try {
                client = s.accept();
            } catch (Exception ignore) {
                continue;
            }
            
            System.out.println("Client connection opened");
            ReceiveBugReportTask task = new ReceiveBugReportTask(reports, client);
            Thread t = new Thread(task);
            t.start();
            System.out.println("Client task forked.");
        }
    }

    public void stop() {
        stop = true;
        try {
            s.close();
        } catch (IOException ignore) {
        }
    }
}
