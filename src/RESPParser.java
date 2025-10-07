import java.io.IOException;
import java.io.InputStream;

public class RESPParser {

    public static String[] parse(InputStream in) throws IOException {
        in.mark(1);
        int firstByte = in.read();
        if (firstByte == -1) return new String[0];

        // 👇 NEW: Support plain text commands (not strict RESP)
        if (firstByte != '*') {
            StringBuilder sb = new StringBuilder();
            sb.append((char) firstByte);

            int c;
            while ((c = in.read()) != -1 && c != '\n') {
                sb.append((char) c);
            }

            // Trim + split by spaces (handles "set name value")
            String[] parts = sb.toString().trim().split("\\s+");
            return parts.length == 0 ? new String[0] : parts;
        }

        // 👇 RESP array format starts with '*'
        String line = readLine(in);
        int numElements;
        try {
            numElements = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid array length: " + line);
        }

        String[] result = new String[numElements];
        for (int i = 0; i < numElements; i++) {
            int dollar = in.read();
            if (dollar != '$') {
                throw new IOException("Expected bulk string ($), got: " + (char) dollar);
            }

            int len;
            try {
                len = Integer.parseInt(readLine(in));
            } catch (NumberFormatException e) {
                throw new IOException("Invalid bulk string length");
            }

            byte[] data = new byte[len];
            int bytesRead = 0;
            while (bytesRead < len) {
                int r = in.read(data, bytesRead, len - bytesRead);
                if (r == -1) throw new IOException("Unexpected end of stream");
                bytesRead += r;
            }

            readLine(in); // consume \r\n
            result[i] = new String(data);
        }

        return result;
    }

    // Helper function to read lines ending with \r\n (Redis protocol)
    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while (true) {
            c = in.read();
            if (c == -1) throw new IOException("Unexpected end of stream");
            if (c == '\r') {
                int next = in.read();
                if (next == '\n') break;
                else throw new IOException("Expected \\n after \\r");
            }
            sb.append((char) c);
        }
        return sb.toString();
    }
}
