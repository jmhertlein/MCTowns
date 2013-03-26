package net.jmhertlein.mctowns.util.reporting;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class ReceiveBugReportsTask implements Runnable {

    private final Set<BugReport> reports;
    private final LinkedList<Socket> clientSockets;
    private boolean stop;

    public ReceiveBugReportsTask(Set<BugReport> reports, LinkedList<Socket> clients) {
        this.reports = reports;
        this.clientSockets = clients;
        stop = false;
    }

    @Override
    public void run() {
        while (!stop) {
            if (!clientSockets.isEmpty()) {
                Socket cur;
                synchronized (clientSockets) {
                    cur = clientSockets.removeFirst();
                }
                System.out.println("Worker received job.");
                receiveReport(cur);
                System.out.println("Worker finished job.");
                try {
                    cur.close();
                    System.out.println("Worker disconnected client.");
                } catch (IOException ex) {
                    Logger.getLogger(ReceiveBugReportsTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ex) {
                }
            }
        }
        System.out.println("Worker thread closing.");
    }

    private void receiveReport(Socket client) {
        try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
            Object rawReceived = ois.readObject();
            if (!(rawReceived instanceof BugReport)) {
                System.err.println("Received object was not a BugReport!");
                return;
            }

            BugReport received = (BugReport) rawReceived;

            received.setIp(client.getInetAddress().getHostAddress());

            System.out.println("Received report from " + client.getInetAddress());
            if (reports.contains(received)) {
                System.out.println("Report was a duplicate, dropped.");
            } else {
                synchronized (reports) {
                    reports.add(received);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ReceiveBugReportsTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ReceiveBugReportsTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        this.stop = true;
    }
}
