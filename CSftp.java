
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

        //Establish a connection to an IPv4 server
        //Parse the first argument which is to connect to server
        String ipFormat = "([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})";
        //TODO: make DNS format regEx
        String DNSFormat = "([0-9])";
        if (Pattern.matches(args[0], ipFormat) || Pattern.matches(args[0], DNSFormat)) {
            //TODO: Connect to the server
            //TODO: Check for timeout
            //TODO: Connect to any port, but we need a default port, if not specified
        } else {
            System.out.print("First Argument must be IP or DNS\n");
        }


        try {
            for (int len = 1; len > 0; ) {
                System.out.print("csftp> ");
                len = System.in.read(cmdString);

                /*TODO:
                Sends the username to the FTP server. The user will need to pay attention to the response code
                to determine if the password command must be sent.
                This typically the first command the user will enter.

                FTP command to Server: USER, PASS
                Application command: user, USERNAME
                */

                /* TODO:
                Sends the PASSWORD to the FTP server. For an anonymous server the user would typically enter an email
                address or anonymous password command must be sent.
                This typically the second command the user will enter.

                FTP command to server: PASS
                Application command: pw, PASSWORD
                */

                /* TODO:
                If connected, sends a QUIT to the server, and closes any established connection
                and then exits the program. This command is valid at any time.

                FTP command: QUIT
                Application command: quit
                */

                /* TODO:
                Establishes a data connection and retrieves the file indicated by REMOTE,
                saving it in a file of the same name on the local machine.

                FTP command: PASV, RETR
                Application command: get REMOTE
                */

                /* TODO:
                Changes the current working directory on the server to the directory indicated by DIRECTORY.

                FTP command: CWD
                Application Command: cd DIRECTORY
                */

                /* TODO:
                Establishes a data connection and retrieves a list of files in the current working directory
                on the server. The list is printed to standard output.

                FTP command: PASV, LIST
                Application command: dir
                */


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
