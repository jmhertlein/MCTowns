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
    private static final int NUM_WORKER_THREADS = 2;

    private boolean stop;
    private Set<BugReport> reports;
    private ServerSocket s;
    private final LinkedList<Socket> clients;
    private List<ReceiveBugReportsWorkerThread> workerThreads;

    public ConnectionListenTask(Set<BugReport> reports) {
        stop = false;
        this.reports = reports;
        clients = new LinkedList<>();
        workerThreads = new LinkedList<>();
    }

    @Override
    public void run() {
        try {
            s = new ServerSocket(9001);
        } catch (IOException ex) {
            Logger.getLogger(BugReportDaemon.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        System.out.println("Starting " + NUM_WORKER_THREADS + " worker threads.");
        //start worker thread(s)
        for(int i = 0; i < NUM_WORKER_THREADS; i++) {
            ReceiveBugReportsWorkerThread t = new ReceiveBugReportsWorkerThread(reports, clients);
            workerThreads.add(t);
            t.start();
        }
        System.out.println("Downloader thread(s) started.");

        while (!stop) {
            Socket client;

            try {
                client = s.accept();
            } catch (IOException ignore) {
                System.out.println("Socket listener ignored exception.");
                continue;
            }

            synchronized (clients) {
                clients.add(client);
                clients.notify();
            }
            
            System.out.println("Client connection opened");
        }
    }

    public void stop() {
        stop = true;
        
        for(ReceiveBugReportsWorkerThread t : workerThreads) {
            t.setStopFlag();
            t.interrupt();
        } //interrupt all worker threads
        
        try {
            s.close();
        } catch (IOException ignore) {
        }
    }
}
