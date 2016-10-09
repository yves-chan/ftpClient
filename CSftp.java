
import java.io.*;
import java.lang.System;
import java.net.InetSocketAddress;
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
    private static final int DEFAULT_PORT = 21;
    private static final int SOCKET_TIMEOUT = 30000;
    private static String hostName;
    private static int portNumber;

    private static void clearByteArray(byte[] cmdString) {
        for (int i = 0; i < cmdString.length; i++) {
            cmdString[i] = 0;
        }
    }

    public static void main(String[] args) {
        byte cmdString[] = new byte[MAX_LEN];

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
        String DNSFormat = "(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*" +
                "([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
        if (Pattern.matches(ipFormat, args[0]) || Pattern.matches(DNSFormat, args[0])) {
            hostName = args[0];
            portNumber = (args[1] == null)? DEFAULT_PORT : Integer.parseInt(args[1]);
            try {
                //Connect to the ftp using hostName, port number
                Socket ftpSocket = new Socket();
                ftpSocket.connect(new InetSocketAddress(hostName,portNumber), SOCKET_TIMEOUT);
                //Get the socket output stream
                PrintWriter out = new PrintWriter(ftpSocket.getOutputStream(),true);
                //get the socket input stream
                BufferedReader reader = new BufferedReader(new InputStreamReader(ftpSocket.getInputStream()));

                String response;

                while ((response = reader.readLine()) != null) {
                    if(response.startsWith("2")) {
                        String responseCode = response.substring(0,3);
                        if(responseCode.equals("220")) {
                            System.out.println("Welcome to Jongrin Kim and Yves Chan's FTP client");
                            System.out.print("<-- " + response + "\n");
                        }
                        break;
                    } else if (response.startsWith("4")) {
                        System.out.println("920 Control connection to " + hostName + " on port " +
                                portNumber + " failed to open.");
                    }
                }


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

                    String command = updatedUserInputArray[0].toLowerCase();

                    switch (command) {
                        case "user" :
                            if (checkArgs(2, updatedUserInputArray)) {
                                logIn(updatedUserInputArray, out, reader);
                                clearByteArray(cmdString);
                            }
                            break;

                        case "pw" :
                            if (checkArgs(2, updatedUserInputArray)) {
                                enterPassword(updatedUserInputArray, out, reader);
                                clearByteArray(cmdString);
                            }
                            break;

                        case "quit" :
                            if (checkArgs(1, updatedUserInputArray)) {
                                quit(updatedUserInputArray, out, reader);
                                System.exit(0);
                            }
                            break;

                        case "get" :
                            if (checkArgs(2, updatedUserInputArray)) {
                                getRemote(updatedUserInputArray, out, reader, cmdString);
                                clearByteArray(cmdString);
                            }
                            break;

                        case "cd" :
                            if (checkArgs(2, updatedUserInputArray)) {
                                cdDirectory(updatedUserInputArray, out, reader);
                                clearByteArray(cmdString);
                            }
                            break;

                        case "dir" :
                            if (checkArgs(1, updatedUserInputArray)) {
                                showDir(updatedUserInputArray, out, reader, cmdString);
                                clearByteArray(cmdString);
                            }
                            break;

                        case "":
                            clearByteArray(cmdString);
                            break;

                        case "#":
                            clearByteArray(cmdString);
                            break;

                        default:
                            // Start processing the command here.
                            if (command.equals("") || command.startsWith("#"))
                                break;
                            System.out.println("900 Invalid command.");
                    }

                    if (len <= 0)
                        break;

                }
            } catch (IOException exception) {
                System.out.println("920 Control connection to "+ hostName+" on port "+portNumber+" failed to open");
                System.exit(1);

            }

        } else {
            System.out.print("First Argument must be IP or DNS\n");
            return;
        }
    }

    /*
    Establishes a data connection and retrieves a list of files in the current working directory
    on the server. The list is printed to standard output.

    FTP command: PASV, LIST
    Application command: dir
    */
    private static void showDir(String[] userInputArray, PrintWriter out, BufferedReader reader, byte[] cmdString) {

        System.out.print("--> PASV"+"\n");
        out.println("PASV");

        try {
            String PASVresponse = reader.readLine();

            if (processResponse(PASVresponse, hostName, portNumber, userInputArray)) {
                System.out.println("<-- " + PASVresponse);
                PASVresponse = PASVresponse.split("[\\(\\)]")[1];
                String[] PASVresponseIP = PASVresponse.split(",");
                String pasvIp = PASVresponseIP[0] + "." + PASVresponseIP[1] + "." + PASVresponseIP[2] + "." +
                        PASVresponseIP[3];
                int pasvPort = Integer.parseInt(PASVresponseIP[4]) * 256 + Integer.parseInt(PASVresponseIP[5]);

                clearByteArray(cmdString);

                try {
                    Socket pasvSocket = new Socket();
                    pasvSocket.connect(new InetSocketAddress(pasvIp, pasvPort), SOCKET_TIMEOUT);
                    //get the socket input stream
                    BufferedReader PASVreader = new BufferedReader(new InputStreamReader(
                            pasvSocket.getInputStream()));

                    if (pasvSocket.isConnected()) {
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
                    System.out.println("925 Control connection I/O error, closing control connection");
                    System.exit(1);
                }
            }

        } catch (IOException e) {
            System.out.println("998 Input error while reading commands, terminating.");
            System.exit(1);
        }

    }

    /*
    Changes the current working directory on the server to the directory indicated by DIRECTORY.

    FTP command: CWD
    Application Command: cd DIRECTORY
    */

    private static void cdDirectory(String[] userInputArray, PrintWriter out, BufferedReader reader) {
        System.out.print("--> CWD" + "\n");
        out.println("CWD " + userInputArray[1]);

        try {
            String response = reader.readLine();

            if (processResponse(response, hostName, portNumber, userInputArray)){
                System.out.println("<-- " + response);
            }

        } catch (IOException e) {
            System.out.println("998 Input error while reading commands, terminating.");
            System.exit(1);
        }
    }

    /* TODO:
    Establishes a data connection and retrieves the file indicated by REMOTE,
    saving it in a file of the same name on the local machine.

    FTP command: PASV, RETR
    Application command: get REMOTE
    */
    private static void getRemote(String[] userInputArray, PrintWriter out, BufferedReader reader, byte[] cmdString) {
        System.out.print("--> PASV"+"\n");
        out.println("PASV");

        try {
            String PASVresponse = reader.readLine();

            if (processResponse(PASVresponse,hostName,portNumber,userInputArray)) {

                System.out.println("<-- " + PASVresponse);

                PASVresponse = PASVresponse.split("[\\(\\)]")[1];
                String[] PASVresponseIP = PASVresponse.split(",");
                String pasvIp = PASVresponseIP[0] + "." + PASVresponseIP[1] + "." + PASVresponseIP[2] + "." +
                        PASVresponseIP[3];
                int pasvPort = Integer.parseInt(PASVresponseIP[4]) * 256 + Integer.parseInt(PASVresponseIP[5]);

                clearByteArray(cmdString);

                try {
                    Socket PASVsocket = new Socket();
                    PASVsocket.connect(new InetSocketAddress(pasvIp,pasvPort), SOCKET_TIMEOUT);
                    //get the socket input stream

                    if (PASVsocket.isConnected()) {

                        System.out.print("--> RETR " + userInputArray[1] + "\n");
                        out.println("RETR " + userInputArray[1]);

                        String RETRresponse = reader.readLine();
                        if (processResponse(RETRresponse,pasvIp,pasvPort,userInputArray)) {

                            System.out.println("<-- " + RETRresponse);

                            File file = new File("./" + userInputArray[1]);
                            if (!file.exists()) {
                                file.createNewFile();
                            }

                            InputStream in = PASVsocket.getInputStream();
                            FileOutputStream fileOut = new FileOutputStream(file);

                            byte[] buf = new byte[4096];

                            try {
                                int length;
                                while ((length = in.read(buf)) > 0) {
                                    fileOut.write(buf, 0, length);
                                }
                                in.close();
                                fileOut.close();
                                PASVsocket.close();
                                System.out.println("<-- " + reader.readLine());
                            } catch (Exception e) {
                                System.out.println("935 Data transfer connection I/O error, closing data connection");
                                return;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("920 Control connection to "+ pasvIp +" on port "+ pasvPort +" failed to open");
                    return;
                }
            }
        } catch (IOException e) {
            System.out.println("998 Input error while reading commands, terminating.");
            System.exit(1);
        }
    }

    /*
    If connected, sends a QUIT to the server, and closes any established connection
    and then exits the program. This command is valid at any time.

    FTP command: QUIT
    Application command: quit
    */
    private static void quit(String[] userInputArray, PrintWriter out, BufferedReader reader) {
        System.out.print("--> QUIT" + "\n");
        out.println("QUIT");

        try {
            String response = reader.readLine();
            if(processResponse(response,hostName,portNumber,userInputArray)) {
                System.out.print(response + "\n");
                System.exit(0);
            }
        } catch (IOException e) {
            System.out.println("998 Input error while reading commands, terminating.");
            System.exit(1);
        }

    }


    /*
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

            if(processResponse(response,hostName,portNumber,userInputArray)) {
                System.out.println("<-- " + response);
            }

        } catch (IOException e) {
            System.out.println("998 Input error while reading commands, terminating.");
            System.exit(1);
        }

    }

    /*
    Sends the username to the FTP server. The user will need to pay attention to the response code
    to determine if the password command must be sent.
    This typically the first command the user will enter.

    FTP command to Server: USER, PASS
    Application command: user, USERNAME
    */
    private static void logIn(String[] userInputArray, PrintWriter out, BufferedReader reader) {
        System.out.print("--> USER " + userInputArray[1] + "\n");
        out.println("USER " + userInputArray[1]);

        try {

            String response = reader.readLine();
            if (processResponse(response,hostName,portNumber,userInputArray)) {
                System.out.println("<-- " + response);
            }

        } catch (IOException e) {
            System.out.println("998 Input error while reading commands, terminating.");
            System.exit(1);
        }
    }

    /* Check arguments for each FTP command
    * i = number of args needed
    * input = user args
    */
    private static boolean checkArgs(int i, String[] input) {
        if (i != input.length) {
            System.out.println("901 Incorrect number of arguments");
            return false;
        }
        return true;
    }

    private static boolean processResponse(String response, String hostName, int portNumber, String[] userInputArray) {

        String responseCode = response.substring(0,3);

        switch (responseCode) {
            //Requested file action not taken.
            case "450":
                System.out.println("910 Access to local file " + userInputArray[1]+" denied.");
                return false;

            //Requested action aborted. Local error in processing.
            case "451":
                System.out.println("999 Processing error. " + response);
                return false;

            // Could Not Connect to Server - Policy Requires SSL
            case "534":
                System.out.println("920 Control connection to " + hostName + " on port " + portNumber +
                        " failed to open");
                return false;

            //Not logged in
            case "530":
                System.out.println("999 Processing error. " + response);
                return false;

            //Fail to open file
            case "550":
                System.out.println("999 Processing error. " + response);
                return false;

            //Requested action aborted. Page type unknown.
            case "551":
                System.out.println("999 Processing error. " + response);
                return false;

            //Requested file action aborted. Exceeded storage allocation (for current directory or dataset).
            case "552":
                System.out.println("999 Processing error. " + response);
                return false;

            //Requested action not taken.Insufficient storage space in system.File unavailable (e.g., file busy).
            case "553":
                System.out.println("999 Processing error. " + response);
                return false;

            // 998 Input error while reading commands, terminating.
            // This error message is printed if an exception is thrown while the client is reading its commands
            // (i.e., standard input). After printing this message the client will terminate.
            case "452":
                System.out.println("999 Processing error. " + response);
                return false;

            // Service not available
            case "421":
                System.out.println("925 Control connection I/O error, closing control connection.");
                System.exit(1);
                break;

            //When an attempt to establish the connection can't be completed within a reasonable time (say 30 seconds),
            // or the socket cannot be created, then print this message, replacing xxx and yyy with the
            // hostName and port number of the target ftp server you are trying to establish the control connection to,.
            case "425":
                System.out.println("920 Control connection to " + hostName + " on port " + portNumber +
                        " failed to open");
                System.exit(1);
                break;

            //925 Control connection I/O error, closing control connection.
            // If at any point an error while attempting to read from, or write to, the open control connection occurs,
            // this message is to printed, and the socket closed/destroyed. The client is then to exit.
            case "426":
                System.out.println("930 Data transfer connection to " + hostName + " on port " + portNumber +
                        " failed to open");
                System.exit(1);
                break;

            default  :
                return true;
        }
        return true;
    }
}