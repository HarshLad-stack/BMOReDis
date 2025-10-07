import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.*;

public class RedisServer {

    private static ConcurrentHashMap<String, String> database = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Long> expirations = new ConcurrentHashMap<>();

    private static void handleClient(Socket clientSocket) throws IOException {
        InputStream in = clientSocket.getInputStream();
        OutputStream out = clientSocket.getOutputStream();

        while (true) {
            try {
                // Parse command in RESP format
                String[] commandParts = RESPParser.parse(in);
                if (commandParts.length == 0) continue;

                String command = commandParts[0].toUpperCase();

                if ("QUIT".equals(command)) {
                    RESPWriter.writeSimpleString(out, "Goodbye!");
                    break;
                }

                // Process command
                String result = processCommandRESP(commandParts);
                System.out.println("Received: " + Arrays.toString(commandParts));


                // Write result back in RESP format
                RESPWriter.writeSimpleString(out, result);

            } catch (IOException e) {
                RESPWriter.writeError(out, e.getMessage());
            }
        }

        clientSocket.close();
    }

    private static String processCommandRESP(String[] parts) {
        String command = parts[0].toUpperCase();

        switch (command) {
            case "SET":

                return handleSET(parts);
            case "GET":
                return handleGET(parts);
            case "DEL":
                return handleDEL(parts);
            case "PING":
                return "PONG";
            case "INCR":
                return handleINCR(parts);
            case "EXISTS":
                return handleEXISTS(parts);
            case "INCRBY":
                return handleINCRBY(parts);
            case "DECRBY":
                return handleDECRBY(parts);
            case "EXPIRE":
                return handleExpire(parts);
            case "TTL":
                return handleTTL(parts);
            case "PERSIST":
                return handlePERSIST(parts);
            case "HARSH":
                return "Hello Harsh, nice try!";
            default:
                return "Unrecognized command: " + command;
        }
    }

    private static String handleSET(String[] parts) {
        if (parts.length < 3) return "ERROR: SET requires key and value";
        database.put(parts[1], parts[2]);
        return "OK";
    }

    private static String handleGET(String[] parts) {
        if (parts.length < 2) return "ERROR: GET requires key";
        return database.getOrDefault(parts[1], "(nil)");
    }

    private static String handleDEL(String[] parts) {
        if (parts.length < 2) return "ERROR: DEL requires key";
        return database.remove(parts[1]) != null ? "1" : "0";
    }

    private static String handleEXISTS(String[] parts) {
        if (parts.length < 2) return "ERROR: EXISTS requires key";
        return database.containsKey(parts[1]) ? "1" : "0";
    }

    private static String handleINCR(String[] parts) {
        if (parts.length < 2) return "ERROR: INCR requires key";
        return handleINCRBY(new String[]{parts[0], parts[1], "1"});
    }

    private static String handleINCRBY(String[] parts) {
        if (parts.length < 3) return "ERROR: INCRBY requires key and increment value";
        String key = parts[1];
        int increment;
        try {
            increment = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "ERROR: increment must be integer";
        }

        int current = 0;
        String currValue = database.get(key);
        if (currValue != null) {
            try {
                current = Integer.parseInt(currValue);
            } catch (NumberFormatException e) {
                return "ERROR: current value is not integer";
            }
        }

        current += increment;
        database.put(key, String.valueOf(current));
        return String.valueOf(current);
    }

    private static String handleDECRBY(String[] parts) {
        if (parts.length < 3) return "ERROR: DECRBY requires key and decrement value";
        String key = parts[1];
        int decrement;
        try {
            decrement = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "ERROR: decrement must be integer";
        }

        int current = 0;
        String currValue = database.get(key);
        if (currValue != null) {
            try {
                current = Integer.parseInt(currValue);
            } catch (NumberFormatException e) {
                return "ERROR: current value is not integer";
            }
        }

        current -= decrement;
        database.put(key, String.valueOf(current));
        return String.valueOf(current);
    }

    private static String handleExpire(String[] parts) {
        if (parts.length < 3) return "ERROR: EXPIRE requires key and seconds";
        String key = parts[1];
        if (!database.containsKey(key)) return "0";

        int seconds;
        try {
            seconds = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return "ERROR: seconds must be integer";
        }

        expirations.put(key, System.currentTimeMillis() + seconds * 1000L);
        return "1";
    }

    private static String handleTTL(String[] parts) {
        if (parts.length < 2) return "ERROR: TTL requires key";
        String key = parts[1];
        if (!database.containsKey(key)) return "-2";
        if (!expirations.containsKey(key)) return "-1";

        long remainingMillis = expirations.get(key) - System.currentTimeMillis();
        return String.valueOf(Math.max(remainingMillis / 1000, 0));
    }

    private static String handlePERSIST(String[] parts) {
        if (parts.length < 2) return "ERROR: PERSIST requires key";
        String key = parts[1];
        if (!database.containsKey(key)) return "0";
        if (!expirations.containsKey(key)) return "-1";

        expirations.remove(key);
        return "1";
    }

    private static void cleanUpExpiredKeys() {
        long now = System.currentTimeMillis();
        expirations.entrySet().removeIf(entry -> {
            if (entry.getValue() <= now) {
                database.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public static void main(String[] args) throws IOException {
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        ScheduledExecutorService cleanupScheduler = Executors.newScheduledThreadPool(1);
        cleanupScheduler.scheduleAtFixedRate(RedisServer::cleanUpExpiredKeys, 1, 1, TimeUnit.SECONDS);

        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Redis Server started on port 7777");
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("New client connected: " + client.getInetAddress());
                threadPool.submit(() -> {
                    try {
                        handleClient(client);
                    } catch (IOException e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                });
            }
        }
    }
}
