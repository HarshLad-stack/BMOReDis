import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AOFWriter{
    private PrintWriter printWriter;
    private  String filename;



    public  AOFWriter(String filename) throws IOException{
        this.filename=filename;
        this.printWriter=new PrintWriter(new FileWriter(filename,true));
    }

    public synchronized  void log(String command){
        printWriter.println(command);
        printWriter.flush();
    }
    public  void close(){
        if(printWriter!=null){
            printWriter.close();
        }
    }
}