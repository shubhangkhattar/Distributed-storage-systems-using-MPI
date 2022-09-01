import mpi.MPI;

public class RMIDemo {
    public static void main(String[] args) {

        MPI.Init(args);
        int me = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        System.out.println(me+" "+size);
        if(me==0){
            MasterNode.main(args);
        }
        else{
            ChildNode.main(args);
        }
        MPI.Finalize();
    }
}
