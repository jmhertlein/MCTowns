package net.jmhertlein.mctowns.util.reporting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
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

        printMenu();

        while (!done) {
            String resp = scan.nextLine().trim();

            switch (resp) {
                default:
                    System.out.println("Invalid command");
                    printMenu();
                    break;
                case "stop":
                    if (connectionListener == null) {
                        System.out.println("Already stopped.");
                        break;
                    }
                    connectionListener.stop();
                    connectionListener = null;
                    th = null;

                    System.out.println("Stopped.");
                    break;
                case "start":
                    if (connectionListener != null) {
                        System.out.println("Already listening.");
                        break;
                    }
                    connectionListener = new IncomingReportListenTask(reports);
                    th = new Thread(connectionListener);
                    th.start();
                    System.out.println("Started.");
                    break;
                case "dump":
                    dumpReportsToFile(reports);
                    break;
                case "exit":
                    System.out.println("Stopping");
                    if (connectionListener != null) {
                        connectionListener.stop();
                        System.out.println("Waiting for connection thread to join...");
                        try {
                            th.join();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(BugReportServerDaemon.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("Joined.");
                    }
                    done = true;
                    System.exit(0);
                    break;
                case "help":
                    printMenu();
                    break;

                case "info":
                    System.out.println("Currently holding " + reports.size() + " reports in volatile memory.");
                    break;

                case "clear":
                    reports.clear();
                    System.out.println("Deleted reports.");
                    break;

                case "print":
                    System.out.println("Printing reports...");
                    for (BugReport report : reports) {
                        System.out.println(report.toString());
                    }
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
        System.out.println("info - prints info about current state");
        System.out.println("clear - delete currently held reports");
        System.out.println("print - print all currently held reports");
    }

    private static void dumpReportsToFile(Set<BugReport> reports) {
        File f = new File("./bug_reports0");
        int c = 0;
        while (f.exists()) {
            c++;
            f = new File("./bug_reports" + c);
        }

        try {
            f.createNewFile();
        } catch (IOException ex) {
            System.err.println("Error making file: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        try (PrintStream ps = new PrintStream(f)) {
            for (BugReport report : reports) {
                ps.println(report.toString());
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BugReportServerDaemon.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        System.out.println("Reports dumped.");
    }
}
