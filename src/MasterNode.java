import mpi.MPI;
import mpi.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class MasterNode {
    static final int PORT = 6231;
    static final int WORKER = 3;

    static final int BUF_LENGTH_TEST = 1024;

    public static void main(String[] args) {
        System.out.println("Master");
        System.out.println("TRY start listening");
        FileRepositoryAPI api = FileRepositoryAPI.getInstance();
        HashMap<String, ArrayList<Integer>> fileMap = new HashMap<>();
        HashMap<String, String> nameMap = new HashMap<>();

        try {
            RMIServer.start(PORT);
            RMIServer.register(api);
            System.out.println("we are in masterNode");
            try{
                while(true) {
                    //uplod section
                    byte[] data = new byte[1024];
                    char[] fileNameArray = new char[1024];
//                    System.out.println("---------upload-----------");
                    Status s = MPI.COMM_WORLD.Recv(fileNameArray, 0, fileNameArray.length, MPI.CHAR, 0, 0);

                    String[] fileNameAndAction = String.valueOf(fileNameArray).split("/");
                    String fileName = fileNameAndAction[0];
                    String action = fileNameAndAction[1].trim();
                    if(fileName!="send files"){
                        nameMap.put(fileName, fileName);
                    }
                    System.out.println("fileName:"+fileName+" action:"+ action+" "+action.equals("upload"));

                    if(action.equals("upload")){
                        MPI.COMM_WORLD.Recv(data, 0, data.length, MPI.BYTE, 0, 1);
                        System.out.println("data in master node is here");
//                    for (int i = 0; i < data.length; i++) {
//                        if (data[i] == 0) break;
//                        System.out.print((char) data[i]);
//                    }
                        Random random = new Random();
                        int node = random.nextInt(14) + 1;
                        System.out.println(node);
                        ArrayList<Integer> list;
                        if(fileMap.containsKey(fileName)){
                            list = fileMap.get(fileName);
                        }
                        else{
                            list = new ArrayList<>();
                        }
                        list.add(node);
                        fileMap.put(fileName, list);
                        MPI.COMM_WORLD.Send(fileNameArray, 0, fileNameArray.length, MPI.CHAR, node, 0);
                        MPI.COMM_WORLD.Send(data, 0, data.length, MPI.BYTE, node, 1);
                    }

                    if(action.equals("list")){
                        System.out.println("---------read-----------");
                        Set<String> fileList = nameMap.keySet();
                        String files = "";
                        for(String file:fileList){
                            files += file+"\n";
                        }
                        byte[] fileListBytes = files.getBytes();
                        System.out.println("files List:"+files+" source: "+s.source +" "+ fileListBytes.length);
                        RMIServer.MPI_PROXY.Send(fileListBytes, 0, fileListBytes.length, MPI.BYTE, 0 , 2);
                    }

                    if(action.equals("download")){
                        System.out.println("------------downloadMaster-------------");
                        ArrayList<Integer> nodes = fileMap.get(fileName);
                        String fileContent = "";
                        System.out.println(fileMap);
                        HashMap<Integer, Integer> count = new HashMap<>();
                        for(int i=1;i<15;i++){
                            count.put(i,0);
                        }

                        for(Integer node:nodes){
                            String file = String.valueOf(fileNameArray);
                            file+="/"+count.get(node);
                            fileNameArray = file.toCharArray();
                            System.out.println();
                            MPI.COMM_WORLD.Send(fileNameArray, 0, fileNameArray.length, MPI.CHAR, node, 0);
                            MPI.COMM_WORLD.Recv(data, 0, data.length, MPI.BYTE, node, 3);
                            fileContent += new String(data);
                        }
                        byte[] fileListBytes = fileContent.getBytes();
                        RMIServer.MPI_PROXY.Send(fileListBytes, 0, fileListBytes.length, MPI.BYTE, 0 , 4);
                    }

                }

            }
            catch (Exception e){
                System.out.println("error in master node:"+e.getMessage());
                e.printStackTrace();
            }
        }
        catch(Exception e){
            System.out.println("ERR " + e.getMessage());
            e.printStackTrace();
        }
    }
}
