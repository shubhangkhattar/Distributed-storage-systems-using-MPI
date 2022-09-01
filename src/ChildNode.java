import mpi.MPI;

import java.util.ArrayList;
import java.util.HashMap;

public class ChildNode {
    static final int PORT = 6231;
    static final int MASTER = 0;
    static final int DEST = 1;

    static final int NCLIENTS_TEST = 4;
    static final int BUF_LENGTH_TEST = 100;

    public static void main(String[] args) {
        byte[] data = new byte[1024];
        HashMap<String, ArrayList<String>> dataMap = new HashMap<>();
        HashMap<String, Integer> lengthMap = new HashMap<>();
        char[] fileNameArray = new char[1024000];
        while(true){
            try{
                MPI.COMM_WORLD.Recv(fileNameArray, 0, fileNameArray.length, MPI.CHAR, 0, 0);
                String[] fileNameAndAction = String.valueOf(fileNameArray).split("/");
                String fileName = fileNameAndAction[0].trim();
                String action = fileNameAndAction[1].trim();
                System.out.println("childNode: fileName:"+fileName+" action:"+ action.trim());

                if(action.trim().equals("upload")){
                    MPI.COMM_WORLD.Recv(data, 0, data.length, MPI.BYTE, 0, 1);
                    String s = "";
                    System.out.println("data in child node is here");
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] == 0) break;
                        s += (char)data[i];
                    }
                    System.out.println(lengthMap);
                    if(!dataMap.containsKey(fileName)){
                        ArrayList<String> list = new ArrayList<>();
                        list.add(s);
                        dataMap.put(fileName, list);
                        lengthMap.put(fileName, 1);
                    }
                    else{
                        ArrayList<String> list = dataMap.get(fileName);
                        list.add(s);
                        dataMap.put(fileName, list);
                        lengthMap.put(fileName, list.size());
                    }
                    System.out.println("we put string in childNode named:"+fileName);
                    System.out.println(MPI.COMM_WORLD.Rank()+" "+lengthMap);
                }

                if(action.trim().equals("download")){
                    System.out.println(fileNameAndAction[2].trim());
                    Integer index = Integer.parseInt(fileNameAndAction[2].trim());
                    ArrayList<String> outputs = dataMap.get(fileName);
                    String output = outputs.get(index);
                    data = output.getBytes();
                    MPI.COMM_WORLD.Send(data, 0, data.length, MPI.BYTE, 0, 3);

                }

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

