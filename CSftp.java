
import java.lang.System;
import java.io.IOException;
import java.util.regex.Pattern;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp {
    static final int MAX_LEN = 255;
    static final int ARG_CNT = 2;

    public static void main(String[] args) {
        byte cmdString[] = new byte[MAX_LEN];

        // Get command line arguments and connected to FTP
        // If the arguments are invalid or there aren't enough of them
        // then exit.

        if (args.length != ARG_CNT) {
            System.out.print("Usage: cmd ServerAddress ServerPort\n");
            return;
        }

        //TODO: WRITE down all requirements for assignment
        //TODO: Make exception classes

        //Establish a connection to an IPv4 server
        //Parse the first argument which is to connect to server
        String ipFormat = "([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})";
        //TODO: make DNS format regEx
        String DNSFormat = "([0-9])";
        //TODO: Connect to any port, but we need a default port
        //TODO: make sure
        if (Pattern.matches(args[0], ipFormat) || Pattern.matches(args[0], DNSFormat)) {
            //TODO: Connect to the server
            //TODO: Check for timeout
        } else {
            System.out.print("First Argument must be IP or DNS\n");
        }

        try {
            for (int len = 1; len > 0; ) {
                System.out.print("csftp> ");
                len = System.in.read(cmdString);
                if (len <= 0)
                    break;
                // Start processing the command here.
                System.out.println("900 Invalid command.");
            }
        } catch (IOException exception) {
            System.err.println("998 Input error while reading commands, terminating.");
        }
    }
}
