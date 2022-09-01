
import mpi.Datatype;
import mpi.MPI;
import mpi.MPIException;
import mpi.Request;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RMIServer {
    private static Registry reg;
    public static MPIProxy MPI_PROXY;
    private static final int NTHREADS = 2;
    private static Thread shutdown;

    public synchronized static void start(int port) throws RemoteException {
        // TODO you may want to change the following
        try {
            reg = LocateRegistry.createRegistry(port);
        }
        catch (Exception e) {
            reg = LocateRegistry.getRegistry(port);
        }
        MPI_PROXY = new MPIProxy(NTHREADS);
        System.out.println("OK listening");
        final Registry _reg = reg;
        shutdown = new Thread(() -> {
            try {
                System.out.println("TRY shutting down...");
                Thread.sleep(100);
                for (String name : _reg.list()) {
                    _reg.unbind(name);
                }

                MPI_PROXY.stop();
                MPI_PROXY = null;
                reg = null;
                System.out.println("OK Shut down");
            }
            catch (Exception e) {
                System.out.println("ERR " + e.getMessage());
            }
        });
    }

    public synchronized static <T extends Remote> void register(T obj) throws RemoteException {
        String name = obj.getClass().getSimpleName();
        reg.rebind(name, UnicastRemoteObject.exportObject(obj, 0));
        System.out.println(String.format("OK %s registered", name));
    }

    public synchronized static void stop() {
        if(shutdown != null) {
            shutdown.start();
            shutdown = null;
        }
    }
    public static String getURI(int port, String name) {
        return getURI("localhost", port, name);
    }

    public static String getURI(String host, int port, String name) {
        return String.format("rmi://%s:%d/%s", host, port, name);
    }

    public static class MPIProxy {
        private static ExecutorService executor;

        public MPIProxy(int nthreads) {
            executor = Executors.newFixedThreadPool(nthreads);
        }

        public void stop() {
            executor.shutdown();
        }

        private static <T> T invoke(Callable<T> c) throws MPIException {
            try {
                Future<T> f;
                synchronized (executor) {
                    f = executor.submit(c);
                }
                return f.get();
            }
            catch (MPIException me) {
                throw me;
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        private static void invoke(final Runnable r) {
            invoke((Callable<Void>) () -> { r.run(); return null; });
        }

        public Request ISend(final Object buf, final int offset, final int len, final Datatype datatype, final int dest, final int tag) throws MPIException {
            return invoke(() -> MPI.COMM_WORLD.Isend(buf, offset, len, datatype, dest, tag));
        }

        public static void Send(final Object buf, final int offset, final int len, final Datatype datatype, final int dest, final int tag) throws MPIException {
            invoke(() -> MPI.COMM_WORLD.Send(buf, offset, len, datatype, dest, tag));
        }

        public static void Sendrecv(final Object buf, final int offset, final int len, final Datatype stype, final int dest, final int stag, final Object rbuf, final int roffset, final int rlen, final Datatype rtype, final int src, final int rtag) throws MPIException {
            invoke(() -> MPI.COMM_WORLD.Sendrecv(buf, offset, len, stype, dest, stag, rbuf, roffset, rlen, rtype, src, rtag));
        }

        // TODO other MPI calls may be implemented here...
    }
}
