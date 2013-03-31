package net.jmhertlein.mctowns.util.reporting;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class ConnectionListenTask implements Runnable {

    private boolean stop;
    private Set<BugReport> reports;
    private ServerSocket s;
    private final LinkedList<Socket> clients;
    private List<ReceiveBugReportsTask> workers;

    public ConnectionListenTask(Set<BugReport> reports) {
        stop = false;
        this.reports = reports;
        clients = new LinkedList<>();
        workers = new LinkedList<>();
    }

    @Override
    public void run() {
        try {
            s = new ServerSocket(9001);
        } catch (IOException ex) {
            Logger.getLogger(BugReportServerDaemon.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        //start worker thread(s)
        ReceiveBugReportsTask task = new ReceiveBugReportsTask(reports, clients);
        Thread t = new Thread(task);
        t.start();
        workers.add(task);
        System.out.println("Downloader thread(s) started.");

        while (!stop) {
            Socket client;

            try {
                client = s.accept();
            } catch (Exception ignore) {
                System.out.println("Ignored exception.");
                continue;
            }

            synchronized (clients) {
                clients.add(client);
            }
            System.out.println("Client connection opened");
        }
    }

    public void stop() {
        stop = true;
        
        for(ReceiveBugReportsTask t : workers) {
            t.stop();
        }
        
        try {
            s.close();
        } catch (IOException ignore) {
        }
        
    }
}
