import java.io.IOException;
import java.io.OutputStream;

public class RESPWriter {
    public static  void writeSimpleString(OutputStream out,String msg) throws IOException{
        out.write(("+"+msg+"\r\n").getBytes());
    }
    public static void   writeError(OutputStream out,String msg) throws IOException{
        out.write(("-" + msg + "\r\n").getBytes());
    }
    public static void writeInteger(OutputStream out, long value) throws IOException {
        out.write((":" + value + "\r\n").getBytes());
    }

    public static void writeBulkString(OutputStream out, String msg) throws IOException {
        if (msg == null) {
            out.write("$-1\r\n".getBytes()); // nil value
        } else {
            out.write(("$" + msg.length() + "\r\n").getBytes());
            out.write((msg + "\r\n").getBytes());
        }
    }
}
