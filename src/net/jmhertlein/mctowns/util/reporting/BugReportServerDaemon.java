package net.jmhertlein.mctowns.util.reporting;

import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshua
 */
public class BugReportServerDaemon {

    public static void main(String[] args) {
        LinkedHashSet<BugReport> reports = new LinkedHashSet<>();

        IncomingReportListenTask connectionListener = new IncomingReportListenTask(reports);
        Thread th = new Thread(connectionListener);
        th.start();

        boolean done = false;
        Scanner scan = new Scanner(System.in);

        while (!done) {
            String resp = scan.nextLine().trim();

            switch (resp) {
                default:
                    System.out.println("Invalid command");
                    break;
                case "stop":
                    if(connectionListener == null) {
                        System.out.println("Already stopped.");
                        break;
                    }
                    connectionListener.stop();
                    connectionListener = null;
                    th = null;
                    
                    System.out.println("Stopped.");
                    break;
                case "start":
                    if(connectionListener != null) {
                        System.out.println("Already listening.");
                        break;
                    }
                    connectionListener = new IncomingReportListenTask(reports);
                    th = new Thread(connectionListener);
                    th.start();
                    System.out.println("Started.");
                    break;
                case "dump":
                    dumpReportsToFile();
                    break;
                case "exit":
                    System.out.println("Stopping");
                    if(connectionListener != null) {
                        connectionListener.stop();
                        System.out.println("Waiting for connection thread to join...");
                        try { th.join(); } catch (InterruptedException ex) { Logger.getLogger(BugReportServerDaemon.class.getName()).log(Level.SEVERE, null, ex); }
                        System.out.println("Joined.");
                    }
                    done = true;
                    System.exit(0);
                    break;
                case "help":
                    printMenu();
                    break;
            }
        }
        

    }

    private static void printMenu() {
        System.out.println("Options are:");
        System.out.println("dump - dumps all reports to file");
        System.out.println("stop - stop listening on port");
        System.out.println("exit - quit program");
        System.out.println("start - start listening on port");
    }

    private static void dumpReportsToFile() {
        System.out.println("Reports dumped.");
    }
}
