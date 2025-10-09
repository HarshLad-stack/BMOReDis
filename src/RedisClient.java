        import java.io.BufferedReader;
        import java.io.InputStreamReader;
        import java.io.PrintWriter;
        import java.net.Socket;

        public class RedisClient {
            public static void main(String[] args) {
                try{
                    System.out.println("Client Started");
                    Socket clientSocket= new Socket("localhost",7777);
                    BufferedReader userinput= new BufferedReader(new InputStreamReader(System.in));
                    PrintWriter serverOutput=new PrintWriter(clientSocket.getOutputStream(),true);
                    BufferedReader serverInput= new BufferedReader( new InputStreamReader(clientSocket.getInputStream()));
                    System.out.println("Enter PING (if connection is established you will receive PONG):");
                    String str;
                    while (true) {
                        System.out.println("Enter command (PING, Harsh, or 'quit' to exit):");
                        str = userinput.readLine();
                        serverOutput.println(str); // send to server

                        String response = serverInput.readLine(); // get response
                        System.out.println("Server says: " + response);

                        if ("quit".equalsIgnoreCase(str)) {
                            break; // exit loop
                        }
                    }

                    clientSocket.close();
                    System.out.println("Exiting client...");

                }catch ( Exception e){
                    e.printStackTrace();
                }


            }
        }
