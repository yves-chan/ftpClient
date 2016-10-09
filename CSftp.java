
import java.io.*;
import java.lang.System;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

//
// This is an implementation of a simplified version of a command 
// line ftp client. The program always takes two arguments
//


public class CSftp {
    private static final int MAX_LEN = 255;
    private static final int ARG_CNT = 2;

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
            String hostName = args[0];
            int portNumber = (args[1] == null)? 21 : Integer.parseInt(args[1]);
            try (
                    //TODO: Check for timeout
                    //Connect to the ftp using hostName, port number
                    Socket ftpSocket = new Socket(hostName, portNumber);
                    //Get the socket output stream
                    PrintWriter out = new PrintWriter(ftpSocket.getOutputStream(),true);
                    //get the socket input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));
                    // To send data to socket
//                stdIn = new BufferedReader(new InputStreamReader(System.in));

            ){
                String response;
                //This while loop takes care of the first socket connection response
                while ((response = reader.readLine()) != null) {

                    if(response.startsWith("2")) {
                        String responseCode = response.substring(0,3);
                        if(responseCode.equals("220")) {
                            System.out.print("<-- " + "connection successful" + "\n");
                        }
                        break;
                    } else if (response.startsWith("4")) {
                        System.err.println("920 Control connection to " + hostName + " on port " + portNumber + " failed to open.");
                    }
                }

                String userName;
                String password;
                //TODO: Need a check for logged into FTP before doing other commands

                for (int len = 1; len > 0; ) {

                    System.out.print("csftp> ");

                    //listens to the length of the user input string
                    len = System.in.read(cmdString);
                    String temp = new String(cmdString, Charset.forName("UTF-8"));

                    //split on 1 or many white spaces
                    String[] userInputArray = temp.split("\\s+");

                    //userInputArray gives empty string at the end, so make a copy of the array
                    // with last element in the array(which is an empty string) removed
                    String[] updatedUserInputArray = new String[userInputArray.length - 1];
                    System.arraycopy(userInputArray, 0, updatedUserInputArray, 0, userInputArray.length - 1);

                    switch (updatedUserInputArray[0].toLowerCase()) {
                        case "user" :
                            logIn(updatedUserInputArray, out, reader);
                            clearByteArray(cmdString);
                            break;

                        case "pw" :
                            enterPassword(updatedUserInputArray, out, reader);
                            clearByteArray(cmdString);
                            break;

                        case "quit" :
                            quit(out, reader);
                            System.exit(0);
                            break;

                        case "get" :
                            getRemote(updatedUserInputArray, out, reader);
                            clearByteArray(cmdString);
                            break;

                        case "cd" :
                            cdDirectory(updatedUserInputArray, out, reader);
                            clearByteArray(cmdString);
                            break;

                        case "dir" :
                            showDir(out, reader);
                            clearByteArray(cmdString);
                            break;

                        case "":
                            System.out.println("\n");
                            clearByteArray(cmdString);
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

        System.out.print("--> PASV"+"\n");
        out.println("PASV");

        try {
            String PASVresponse = reader.readLine();

            String responseCode = PASVresponse.substring(0, 3);

            //processResponseCode();

            System.out.println("<-- " + PASVresponse);
            //if return from PASV is 227, then parse ip and port
            PASVresponse = PASVresponse.split("[\\(\\)]")[1];
            String[] PASVresponseIP = PASVresponse.split(",");
            String PASV_IP = PASVresponseIP[0]+"."+PASVresponseIP[1]+"."+PASVresponseIP[2]+"."+
                    PASVresponseIP[3];
            int PASV_PORT = Integer.parseInt(PASVresponseIP[4])*256+Integer.parseInt(PASVresponseIP[5]);

            System.out.println(PASV_IP + ":" +PASV_PORT);

            try (
            Socket PASVsocket = new Socket(PASV_IP,PASV_PORT);
            //get the socket input stream
            BufferedReader PASVreader = new BufferedReader(new InputStreamReader(PASVsocket.getInputStream()));
            ) {

                if (PASVsocket.isConnected()) {
                    System.out.print("--> LIST" + "\n");
                    out.println("LIST");

                    System.out.println("<-- " + reader.readLine());
                    String LISTresponse;
                    while ((LISTresponse = PASVreader.readLine()) != null) {
                        System.out.println("<-- " + LISTresponse);
                    }
                    System.out.println("<-- " + reader.readLine());
                }
            } catch (SocketException e) {
                System.out.println("lost connection");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /* TODO:
Changes the current working directory on the server to the directory indicated by DIRECTORY.

FTP command: CWD
Application Command: cd DIRECTORY
*/

    private static void cdDirectory(String[] userInputArray, PrintWriter out, BufferedReader reader) {
        System.out.print("--> CWD" + "\n");
        out.println("CWD " + userInputArray[1]);

        try {
            String response = reader.readLine();

            String responseCode = response.substring(0, 3);

            if(responseCode.startsWith("4")) {
                System.out.print("925 Control connection I/O error, closing control connection.");
                System.exit(0);
            }

            System.out.println("<-- " + response);
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
        System.out.print("--> PASV"+"\n");
        out.println("PASV");

        try {
            String PASVresponse = reader.readLine();

            String responseCode = PASVresponse.substring(0, 3);

            //processResponseCode();

            System.out.println("<-- " + PASVresponse);
            //if return from PASV is 227, then parse ip and port
            PASVresponse = PASVresponse.split("[\\(\\)]")[1];
            String[] PASVresponseIP = PASVresponse.split(",");
            String PASV_IP = PASVresponseIP[0]+"."+PASVresponseIP[1]+"."+PASVresponseIP[2]+"."+
                    PASVresponseIP[3];
            int PASV_PORT = Integer.parseInt(PASVresponseIP[4])*256+Integer.parseInt(PASVresponseIP[5]);

            System.out.println(PASV_IP + ":" +PASV_PORT);

            Socket PASVsocket = new Socket(PASV_IP,PASV_PORT);
            //Get the socket output stream
            PrintWriter PASVout = new PrintWriter(PASVsocket.getOutputStream(),true);
            //get the socket input stream
            BufferedReader PASVreader = new BufferedReader(new InputStreamReader(PASVsocket.getInputStream()));

            if (PASVsocket.isConnected()) {
                System.out.print("--> RETR" + userInputArray[1] + "\n");
                PASVout.println("RETR " + userInputArray[1]);

                //TODO: Download file
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* TODO:
If connected, sends a QUIT to the server, and closes any established connection
and then exits the program. This command is valid at any time.

FTP command: QUIT
Application command: quit
*/
    private static void quit(PrintWriter out, BufferedReader reader) {
        System.out.print("--> QUIT" + "\n");
        out.println("QUIT");

        try {
            String response = reader.readLine();
            System.out.print(response + "\n");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /* TODO:
Sends the PASSWORD to the FTP server. For an anonymous server the user would typically enter an email
address or anonymous password command must be sent.
This typically the second command the user will enter.

FTP command to server: PASS
Application command: pw
*/
    private static void enterPassword(String[] userInputArray, PrintWriter out, BufferedReader reader) {
        System.out.print("--> PASS " + userInputArray[1] + "\n");
        out.println("PASS " + userInputArray[1]);

        try {
            String response = reader.readLine();

            String responseCode = response.substring(0, 3);

            if(responseCode.startsWith("4")) {
                System.out.print("925 Control connection I/O error, closing control connection.");
                System.exit(0);
            }

            System.out.println("<-- " + response);

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
    private static void logIn(String[] userInputArray, PrintWriter out, BufferedReader reader) {

        if(userInputArray.length != 2) {
            System.out.print("Invalid Username" + "\n");
        } else {
            System.out.print("--> USER " + userInputArray[1] + "\n");
            out.println("USER " + userInputArray[1]);

            try {

                String response = reader.readLine();
                String responseCode = response.substring(0,3);

                if(responseCode.startsWith("4")) {
                    System.out.print("925 Control connection I/O error, closing control connection.");
                    System.exit(0);
                }

                System.out.println("<-- " + response);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
//    private static void processResponse(String response, String hostName, int portNumber) {
//
//        String responseCode = response.substring(0,3);
//
//        switch (responseCode) {
//            case "331":
//                System.out.print("User name okay, need password.");
//                break;
//
//            //When an attempt to establish the connection can't be completed within a reasonable time (say 30 seconds),
//            // or the socket cannot be created, then print this message, replacing xxx and yyy with the
//            // hostName and port number of the target ftp server you are trying to establish the control connection to,.
//            case "425":
//                System.out.print("920 Control connection to " + hostName + " on port " + portNumber + " failed to open");
//                break;
//
//            //925 Control connection I/O error, closing control connection.
//            // If at any point an error while attempting to read from, or write to, the open control connection occurs,
//            // this message is to printed, and the socket closed/destroyed. The client is then to exit.
//            case "test":
//                System.out.print("925 Control connection I/O error, closing control connection.");
//                break;
//
//            //930 Data transfer connection to xxx on port yyy failed to open.
//            // This message is to be printed when a data connection cannot be established within a reasonable time
//            // (say 30 seconds) or the socket cannot be created. The xxx and yyy are to be replaced with the
//            // hostName and port number of the target ftp server respectively.
//            case "test2":
//                System.out.print("930 Data transfer connection to " + hostName + " on port " + portNumber + " failed to open");
//                break;
//
//            //935 Data transfer connection I/O error, closing data connection.
//            // If at any point an error while attempting to read (or write, but that should never be the case) from
//            // the open data transfer connection occurs, this message is to printed, and the data transfer socket closed/destroyed.
//            // The client is then to go back to accepting commands.
//            // Note that this error does not trigger the closing of the command connection.
//            case "test3":
//                System.out.print("935 Data transfer connection I/O error, closing data connection. ");
//                break;
//            // 998 Input error while reading commands, terminating.
//            // This error message is printed if an exception is thrown while the client is reading its commands
//            // (i.e., standard input). After printing this message the client will terminate.
//            case "test4":
//                System.out.print("998 Input error while reading commands, terminating. ");
//                break;
//
//            // Todo: 999 Processing error. yyyy.
//            // If for some reason you detect an error that isn't described above,
//            // print this message and replace yyyy with some appropriate text that briefly describes the error.
//            case "test5":
//                System.out.print("999 Processing error. ");
//                break;
//            default  :
//                System.out.println("<-- " + response);
//        }
//    }
}