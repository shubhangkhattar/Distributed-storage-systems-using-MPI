import mpi.MPI;

import java.io.IOException;
import java.rmi.RemoteException;

public final class FileRepositoryAPI implements APIInterface{

    private static FileRepositoryAPI api;

    private FileRepositoryAPI(){

    }

    public static FileRepositoryAPI getInstance(){
        if(api==null){
            api = new FileRepositoryAPI();
        }
        return api;
    }

    @Override
    public String listFiles() throws RemoteException {
        char[] data = "send files/list".toCharArray();
        byte[] resultData = new byte[1024];
        System.out.println("we are in list function");
        RMIServer.MPIProxy.Sendrecv(data, 0, data.length, MPI.CHAR, 0, 0, resultData, 0, resultData.length, MPI.BYTE, 0, 2);
//        RMIServer.MPIProxy.Send(data, 0, data.length, MPI.CHAR, 0, 0);
        System.out.println("we got some nerves here");

        return new String(resultData);
    }

    @Override
    public void upload(String fileName, byte[] data) throws RemoteException, IOException {
        try{
            System.out.println("we are in RMI APIs right now");
            String[] fileNameArray = fileName.split("/");
            fileNameArray[fileNameArray.length-1]+="/upload";
            char[] charArray = fileNameArray[fileNameArray.length-1].toCharArray();
            System.out.println("ready to send data");
            RMIServer.MPIProxy.Send(charArray, 0, charArray.length, MPI.CHAR, 0, 0);
            RMIServer.MPIProxy.Send(data, 0, data.length, MPI.BYTE, 0, 1);
        }
        catch(Exception e){
            System.out.println("Error in reading file");
            e.printStackTrace();
        }
    }

    @Override
    public void deleteFile(String url) throws RemoteException {

    }

    @Override
    public String downloadFile(String url) throws RemoteException {
        url += "/download";
        char[] data = url.toCharArray();
        byte[] resultData = new byte[1024000];
        System.out.println("we are in download function");
        RMIServer.MPIProxy.Sendrecv(data, 0, data.length, MPI.CHAR, 0, 0, resultData, 0, resultData.length, MPI.BYTE, 0, 4);
//        RMIServer.MPIProxy.Send(data, 0, data.length, MPI.CHAR, 0, 0);
        return new String(resultData).trim();
    }
}
