import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.Remote;

public class RemoteInputStream extends InputStream implements Remote, Serializable {
    private InputStream input;

    public RemoteInputStream(InputStream input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        return input.read();
    }

    @Override
    public int available() throws IOException {
        return this.input.available();
    }

    @Override
    public void close() throws IOException {
        this.close();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return input.read(b, off, len);
    }

}

