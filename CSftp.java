
import java.io.*;
import java.lang.System;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp {
    private static final int MAX_LEN = 255;
    private static final int ARG_CNT = 2;
    private static String temp;
    private static String[] userInputArray;
    private static String[] updatedUserInputArray;
    private static String response;

    private static void clearByteArray(byte[] cmdString) {
        for (int i = 0; i < cmdString.length; i++) {
            cmdString[i] = 0;
        }
    }

    public static void main(String[] args) {
        byte cmdString[] = new byte[MAX_LEN];
//        PrintWriter out;
//        BufferedReader reader;
//        BufferedReader stdIn;
//        Socket ftpSocket;



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
            try (
                    //TODO: Check for timeout
                    //Connect to the ftp using hostname, port number
                    Socket ftpSocket = new Socket(hostname, portNumber);

                    //Get the socket output stream
                    PrintWriter out = new PrintWriter(ftpSocket.getOutputStream(),true);
                    //get the socket input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));
                    // To send data to socket
//                stdIn = new BufferedReader(new InputStreamReader(System.in));

            ){
                while ((response = reader.readLine()) != null) {
                    System.out.print("<-- " + response + "\n");
                    if(response.startsWith("2")) {
                        break;
                    } else if (response.startsWith("4")) {
                        System.err.println("920 Control connection to " + hostname + " on port " + portNumber + " failed to open.");
                    }
                }

                String userName;
                String password;
                //TODO: Need a check for logged into FTP before doing other commands

                for (int len = 1; len > 0; ) {

                    System.out.print("csftp> ");

                    //listens to the length of the user input string
                    len = System.in.read(cmdString);
                    temp = new String(cmdString, Charset.forName("UTF-8"));

                    //split on 1 or many white spaces
                    userInputArray = temp.split("\\s+");
                    //userInputArray gives empty string at the end, so make a copy of the array
                    // with last element in the array(which is an empty string) removed
                    updatedUserInputArray = new String[userInputArray.length - 1];
                    System.arraycopy(userInputArray, 0, updatedUserInputArray, 0, userInputArray.length - 1);

                    switch (updatedUserInputArray[0].toLowerCase()) {
                        case "user" :
                            // Login as user, pass user input array for args
                            logIn(updatedUserInputArray, out, reader, ftpSocket);
                            clearByteArray(cmdString);
                            break;

                        case "pw" :
                            enterPassword(updatedUserInputArray, out, reader, ftpSocket);
                            clearByteArray(cmdString);
                            break;

                        case "quit" :
                            quit(out, reader);
                            System.exit(0);
                            break;

                        case "get" :
                            getRemote(updatedUserInputArray, out, reader);
                            break;

                        case "cd" :
                            cdDirectory(updatedUserInputArray, out, reader);
                            break;

                        case "dir" :
                            showDir(out, reader);
                            break;

                        case "":
                            System.out.println("\n");
                            break;

                        default:
                            // Start processing the command here.
                            System.out.println("900 Invalid command.");
                    }

//                    }
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
        out.println("CWD " + userInputArray[1]);

        try {
            String response = reader.readLine();
            System.out.println("--> " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
Application command: pw
*/
    private static void enterPassword(String[] userInputArray, PrintWriter out, BufferedReader reader, Socket ftpSocket) {
        System.out.print("--> PASS " + userInputArray[1] + "\n");
        out.println("PASS " + userInputArray[1]);

        try {
            String response = reader.readLine();
            
            String responseCode = response.substring(0, 3);

            //Todo:
            // processResponse(String responseCode) to handle all the server response
            // Output: Boolean
            //         if any one of the bad ones => print the error statements inside this method e.g. 900 , 901, and so on
            //         if any one of the good ones => you return true or something
            //      and then we can just System.out.println("<-- " + response) outside
            //
            System.out.println("<-- " + response);
//                reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*TODO:
Sends the username to the FTP server. The user will need to pay attention to the response code
to determine if the password command must be sent.
This typically the first command the user will enter.

FTP command to Server: USER, PASS
Application command: user, USERNAME
*/
    private static void logIn(String[] userInputArray, PrintWriter out, BufferedReader reader, Socket ftpSocket) {

        if(userInputArray.length != 2) {
            System.out.print("invalid username" + "\n");
        } else {
            System.out.print("--> USER " + userInputArray[1] + "\n");
            out.println("USER " + userInputArray[1]);

            try {

                String response = reader.readLine();

                String responseCode = response.substring(0,3);

                //Todo:
                // processResponse(String responseCode) to handle all the server response
                // Output: Boolean
                //         if any one of the bad ones => print the error statements inside this method e.g. 900 , 901, and so on
                //         if any one of the good ones => you return true or something
                //      and then we can just System.out.println("<-- " + response) outside
                //

                System.out.println("<-- " + response);

//                    reader.close();



            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

//    private static boolean processResponse(String responseCode, String hostName, String portNumber) {
//
//        switch (responseCode) {
//            case "331" :
//                System.out.print("User name okay, need password.");
//                break;
//
//
//            //When an attempt to establish the connection can't be completed within a reasonable time (say 30 seconds),
//            // or the socket cannot be created, then print this message, replacing xxx and yyy with the hostname and port number of the target ftp server you are trying to establish the control connection to,.
//            case "425" :
//                System.out.print("920 Control connection to " + hostName + " on port " + portNumber + " failed to open");
//                break;
//
//            case "333" :
//                break;
//
//            case "334" :
//                break;
//
//            case "335" :
//                break;
//
//            case "336" :
//                break;
//
//            case "332" :
//                break;
//
//            case "332" :
//                break;
//
//            case "332" :
//                break;
//
//            case "332" :
//                break;
//        }
//
//    }
}