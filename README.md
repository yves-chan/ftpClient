#How To use:
1. Make
2. java -jar CSftp.jar <hostname> <portnumber>

##Application Command	Description	FTP command to server
- user USERNAME
  - Sends the username to the FTP server. The user will need to pay attention to the response code to determine if the password command must be sent. This typically the first command the user will enter.
- pw PASSWORD
  - Sends the PASSWORD to the FTP server. For an anonymous server the user would typically enter an email address or anonymous password command must be sent. This typically the second command the user will enter.
- quit
  - If connected, sends a QUIT to the server, and closes any established connection and then exits the program. This command is valid at any time.
- get REMOTE
  - Establishes a data connection and retrieves the file indicated by REMOTE, saving it in a file of the same name on the local machine.
- cd DIRECTORY
  - Changes the current working directory on the server to the directory indicated by DIRECTORY.
- dir
  - Establishes a data connection and retrieves a list of files in the current working directory on the server. The list is printed to standard output.

