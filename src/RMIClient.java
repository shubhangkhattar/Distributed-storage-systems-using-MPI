import java.io.*;
import java.rmi.Naming;
import java.util.Scanner;

public class RMIClient {
    static final int PORT = 6231;
    public static void main(String[] args) {
        try{
            System.out.println("Looking up");
            APIInterface remoteapi = (APIInterface) Naming.lookup(RMIServer.getURI(PORT, "FileRepositoryAPI"));
            Scanner scanner = new Scanner(System.in);
//            RemoteInputStream remoteInputStream = new RemoteInputStream(new FileInputStream("C:/Users/syp98/OneDrive/Desktop/sample.txt"));
            while(true){
                int choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice){
                    case 1:{
                        System.out.println("Please enter a filePath:");

                        String fileName = scanner.nextLine();
                        try (
                                InputStream inputStream = new BufferedInputStream(new FileInputStream(fileName));
                        ) {
                            byte[] buffer = new byte[1024];
                            int bytesRead = -1;

                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                System.out.println(bytesRead+" "+buffer);
                                remoteapi.upload(fileName, buffer);
                            }
                            System.out.println("we are done");
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    case 2:{
                        String listOfFiles = remoteapi.listFiles();
                        System.out.println("upload file List");
                        System.out.println(listOfFiles);
                        break;
                    }
                    case 3:{
                        System.out.println("Please enter filename you want to read");
                        String fileName = scanner.nextLine();
                        String data = remoteapi.downloadFile(fileName);
                        System.out.println("File Data:");
                        System.out.println(data);
                        System.out.println("-------------WE ARE DONE---------------");
                    }
                }

            }
        }
        catch (Exception e){
            System.out.println("Error:"+e.getMessage());
            e.printStackTrace();
        }
    }
}