
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System;
import java.io.IOException;
import java.net.Socket;
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
        PrintWriter out;
        BufferedReader reader;
        BufferedReader stdIn;
        Socket ftpSocket;

        // Get command line arguments and connected to FTP
        // If the arguments are invalid or there aren't enough of them
        // then exit.

        if (args.length != ARG_CNT) {
            System.out.print("Usage: cmd ServerAddress ServerPort\n");
            return;
        }
        //Establish a connection to an IPv4 server
        //Parse the first argument which is to connect to server
        String ipFormat = "([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})";
        String DNSFormat = "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
        if (Pattern.matches(ipFormat, args[0]) || Pattern.matches(DNSFormat, args[0])) {
            String hostname = args[0];
            int portNumber = (args[1] == null)? 21 : Integer.parseInt(args[1]);
            try {
                //TODO: Check for timeout
                //Connect to the ftp using hostname, port number
                ftpSocket = new Socket(hostname, portNumber);

                //Get the socket output stream
                out = new PrintWriter(ftpSocket.getOutputStream(),true);
                //get the socket input stream
                reader = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));
                // To send data to socket
                stdIn = new BufferedReader(new InputStreamReader(System.in));

                String userName;
                String password;
                //TODO: Need a check for logged into FTP before doing other commands

                for (int len = 1; len > 0; ) {
                    System.out.print("csftp> ");
                    //len = System.in.read(cmdString);
                    String userInput;
                    String[] userInputArray;
                    if ((userInput = stdIn.readLine()) != null) {
                        userInputArray = userInput.split(" ");
                        switch (userInputArray[0].toLowerCase()) {
                            case "user" :
                                // Login as user, pass user input array for args
                                logIn(userInputArray, out, reader);
                                break;

                            case "pw" :
                                enterPassword(userInputArray, out, reader);
                                break;

                            case "quit" :
                                quit(out, reader);
                                break;

                            case "get" :
                                getRemote(userInputArray, out, reader);
                                break;

                            case "cd" :
                                cdDirectory(userInputArray, out, reader);
                                break;

                            case "dir" :
                                showDir(out, reader);
                                break;
                            default:
                                // Start processing the command here.
                                System.out.println("900 Invalid command.");
                        }

                    }


                    if (len <= 0)
                        break;

                }
            } catch (IOException exception) {
                System.err.println("998 Input error while reading commands, terminating.");
            }

        } else {
            System.out.print("First Argument must be IP or DNS\n");
            return;
        }
    }

    /* TODO:
Establishes a data connection and retrieves a list of files in the current working directory
on the server. The list is printed to standard output.

FTP command: PASV, LIST
Application command: dir
*/
    private static void showDir(PrintWriter out, BufferedReader reader) {
    }

    /* TODO:
Changes the current working directory on the server to the directory indicated by DIRECTORY.

FTP command: CWD
Application Command: cd DIRECTORY
*/
    private static void cdDirectory(String[] userInputArray, PrintWriter out, BufferedReader reader) {
    }

    /* TODO:
Establishes a data connection and retrieves the file indicated by REMOTE,
saving it in a file of the same name on the local machine.

FTP command: PASV, RETR
Application command: get REMOTE
*/
    private static void getRemote(String[] userInputArray, PrintWriter out, BufferedReader reader) {
    }

    /* TODO:
If connected, sends a QUIT to the server, and closes any established connection
and then exits the program. This command is valid at any time.

FTP command: QUIT
Application command: quit
*/
    private static void quit(PrintWriter out, BufferedReader reader) {
    }

    /* TODO:
Sends the PASSWORD to the FTP server. For an anonymous server the user would typically enter an email
address or anonymous password command must be sent.
This typically the second command the user will enter.

FTP command to server: PASS
Application command: pw, PASSWORD
*/
    private static void enterPassword(String[] userInputArray, PrintWriter out, BufferedReader reader) {
    }

    /*TODO:
Sends the username to the FTP server. The user will need to pay attention to the response code
to determine if the password command must be sent.
This typically the first command the user will enter.

FTP command to Server: USER, PASS
Application command: user, USERNAME
*/
    private static void logIn(String[] userInputArray, PrintWriter out, BufferedReader reader) {
        out.println(userInputArray[0]);
        try {
            System.out.println("--> " + reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
