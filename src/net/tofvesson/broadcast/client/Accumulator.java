package net.tofvesson.broadcast.client;

import net.tofvesson.broadcast.support.ImmutableArray;
import net.tofvesson.broadcast.support.ImmutableReferenceMap;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Accumulates a list of hosts that are broadcasting to a given port.
 */
public class Accumulator {

    /**
     * Timeout (in milliseconds) until a host is considered to no longer be active.
     */
    public static final long DEFAULT_HOST_TIMEOUT = 60_000;

    /**
     * Minimum timeout until a host is considered inactive.
     */
    public static final long MINIMUM_HOST_TIMEOUT = 50;

    private final Runnable accumulate;
    private Thread accumulatorThread;
    private final int port;

    protected final DatagramSocket socket;
    protected final DatagramPacket packet;

    protected volatile long continueUntil = -1;

    protected final Map<InetAddress, Long> hosts = new HashMap<>();
    protected final ImmutableReferenceMap<InetAddress, Long> accessHosts = new ImmutableReferenceMap<>(hosts);
    protected final long hostTimeout;
    protected final ImmutableArray<Byte> signature;

    public OnHostTimeoutListener timeoutListener;
    public OnNewHostListener newHostListener;

    /**
     * Create an accumulator
     * @param port Port to listen on.
     * @param hostTimeout Timeout (milliseconds) until a host is lost (unless another broadcast is detected)
     * @param sig Signature to check if a broadcast is originating from a compatible source.
     * @throws SocketException Thrown if port is already bound
     * @throws SecurityException Thrown if program isn't allowed to accept broadcasts
     */
    public Accumulator(int port, long hostTimeout, byte[] sig) throws SocketException, SecurityException {
        if(System.getSecurityManager()!=null) System.getSecurityManager().checkAccept("255.255.255.255", port); // Do a permission check

        this.socket = new DatagramSocket(this.port = port);
        socket.setSoTimeout(125);
        this.packet = new DatagramPacket(new byte[sig.length + 1], sig.length + 1); // One extra byte implicitly serves as metadata during packet comparison
        this.hostTimeout = hostTimeout;
        this.signature = ImmutableArray.from(sig);

        accumulate = () -> {
            while(continueUntil==-1 || System.currentTimeMillis()<continueUntil){
                try {
                    socket.receive(packet);
                    if(signature.length() == packet.getLength() && signature.compare(ImmutableArray.from(packet.getData()), packet.getOffset(), 0, packet.getLength()))
                        synchronized (hosts){
                            if(hosts.containsKey(packet.getAddress())) hosts.replace(packet.getAddress(), System.currentTimeMillis());
                            else{
                                hosts.put(packet.getAddress(), System.currentTimeMillis());
                                if(newHostListener!=null) newHostListener.onNewHost(packet.getAddress(), port);
                            }
                        }
                } catch (SocketTimeoutException e) {
                    // NOP
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    /**
     * Create an accumulator
     * @param port Port to listen on.
     * @param signature Signature to check if a broadcast is originating from a compatible source.
     * @throws SocketException Thrown if port is already bound
     * @throws SecurityException Thrown if program isn't allowed to accept broadcasts
     */
    public Accumulator(int port, byte[] signature) throws SocketException, SecurityException { this(port, DEFAULT_HOST_TIMEOUT, signature); }


    /**
     * Start the accumulator and let it run indefinitely
     */
    public void start(){ startFor(-1); }

    /**
     * Start accumulator
     * @param timeMillis Milliseconds until accumulator should automatically stop
     */
    public void startFor(long timeMillis){
        continueUntil = timeMillis==-1?-1:System.currentTimeMillis()+timeMillis;
        TimeoutManager.theManager.addAccumulator(this);
        if(accumulatorThread!=null && accumulatorThread.isAlive()) throw new IllegalStateException("Thread is still alive!");
        accumulatorThread = new Thread(accumulate);
        accumulatorThread.setName("Accumulator-"+port);
        accumulatorThread.setPriority(Thread.MAX_PRIORITY);
        accumulatorThread.setDaemon(true);
        accumulatorThread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            // NOP
        });
        accumulatorThread.start();
    }

    /**
     * Pause the accumulator. Resume by calling {@link #start()}
     */
    public void pause(){
        continueUntil = 0;
        if(accumulatorThread!=null && accumulatorThread.isAlive())
            try {
                accumulatorThread.join();
            }
            catch (InterruptedException e) { e.printStackTrace(); }
    }

    /**
     * Stop accumulating hosts and disable host timeout checks. Cannot be resumed from
     */
    public void stop(){
        pause();
        TimeoutManager.theManager.removeAccumulator(this);
        socket.close();
    }

    /**
     * Get an immutable map of the currently available hosts.
     * @return Immutable host map.
     */
    public ImmutableReferenceMap<InetAddress, Long> getHosts() { return accessHosts; }

    /**
     * Callback for when a new host is found
     */
    public interface OnNewHostListener{
        /**
         * Called when a new host is found.
         * @param host The IP address of the host
         * @param port Listener port
         */
        void onNewHost(InetAddress host, int port);
    }

    /**
     * Callback for timed out remote hosts
     */
    public interface OnHostTimeoutListener{
        /**
         * Called when a host is timed out.
         * @param host The IP address of the host
         * @param port Listener port
         */
        void onTimeout(InetAddress host, int port);
    }



    private static class TimeoutManager{
        static TimeoutManager theManager = new TimeoutManager();

        private final List<Accumulator> checks = new ArrayList<>();

        public TimeoutManager() {
            Thread t = new Thread(() -> {
                //noinspection InfiniteLoopStatement
                while(true){
                    try{ Thread.sleep(Accumulator.MINIMUM_HOST_TIMEOUT); }catch(Exception e){}
                    Accumulator[] a1;
                    synchronized (checks){ a1 = checks.toArray(new Accumulator[checks.size()]); }
                    for(Accumulator a : a1)
                        synchronized (a.hosts){
                            for(InetAddress addr : a.hosts.keySet())
                                if(a.hosts.get(addr) <=System.currentTimeMillis()-a.hostTimeout) {
                                    a.hosts.remove(addr);
                                    if(a.timeoutListener!=null) a.timeoutListener.onTimeout(addr, a.port);
                                }
                        }
                }
            });
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            t.setName("Accumulator-TimeoutManager");
            t.setUncaughtExceptionHandler((r, e)->{});
            t.start();
        }

        void addAccumulator(Accumulator a){ checks.removeIf(it -> it==a); synchronized (checks){ checks.add(a); } }
        void removeAccumulator(Accumulator a){ checks.removeIf(it -> it==a); synchronized (checks){ checks.remove(a); } }

    }
}
