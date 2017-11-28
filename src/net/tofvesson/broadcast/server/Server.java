package net.tofvesson.broadcast.server;

import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Broadcasts a signature on the current subnet
 */
public class Server {

    private final Runnable serve;
    private Thread serverThread;
    private final int port;

    protected final DatagramSocket serverSocket;
    protected final DatagramPacket packet;

    protected final AtomicBoolean isAlive = new AtomicBoolean(false);

    /**
     * Create broadcaster
     * @param port Port to broadcast to
     * @param delay Millisecond delay between broadcasts
     * @param sig Signature to broadcast
     * @param offset Offset in the signature to broadcast
     * @param length Length of signature to broadcast
     * @throws SocketException Thrown if broadcast socket could not be created
     */
    public Server(int port, long delay, byte[] sig, int offset, int length) throws SocketException {
        try {
            this.serverSocket = new DatagramSocket();
            this.packet = new DatagramPacket(sig, offset, length, InetAddress.getByAddress(new byte[]{-1, -1, -1, -1}), this.port = port);
            serve = () -> {
                while(getIsAlive()){
                    try {
                        serverSocket.send(packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                    try { Thread.sleep(delay); } catch (InterruptedException e) { }
                }
            };
        } catch (UnknownHostException e) {
            throw new RuntimeException(e); // This is an internal Java error and should not be a declared exception
        }
    }

    /**
     * Create broadcaster
     * @param port Port to broadcast to
     * @param delay Millisecond delay between broadcasts
     * @param sig Signature to broadcast
     * @throws SocketException Thrown if broadcast socket could not be created
     */
    public Server(int port, long delay, byte[] sig) throws SocketException { this(port, delay, sig, 0, sig.length); }

    protected boolean getIsAlive(){
        boolean b;
        synchronized (isAlive){ b = isAlive.get(); }
        return b;
    }

    protected void setIsAlive(boolean b){ synchronized (isAlive){ isAlive.set(b); } }

    /**
     * Start broadcasting signature
     */
    public void start(){
        if(serverSocket.isClosed()) throw new IllegalStateException("Socket is closed");
        if(serverThread!=null && serverThread.isAlive()) throw new IllegalStateException("Server is still alive");
        setIsAlive(true);
        serverThread = new Thread(serve);
        serverThread.setDaemon(true);
        serverThread.setPriority(Thread.MAX_PRIORITY);
        serverThread.setName("Server-"+port);
        serverThread.start();
    }

    /**
     * Pause broadcasting of signature. Can be resumed by calling {@link #start()}
     */
    public void pause(){
        if(serverSocket.isClosed() && (serverThread==null || !serverThread.isAlive())) throw new IllegalStateException("Socket is closed");
        setIsAlive(false);
        serverThread.interrupt();
        try { serverThread.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }

    /**
     * Stop broadcasting and close port. Cannot be resumed from
     */
    public void stop(){
        pause();
        serverSocket.close();
    }
}
