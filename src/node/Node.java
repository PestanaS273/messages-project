package node;

import messages.Messages;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Node {
    private String  nodeName;
    private int port;
    private DatagramSocket socket;
    private ConcurrentHashMap<String, InetAddress> peers;
    private PriorityBlockingQueue<Messages> messageQueue;

    public Node(String nodeName, int port) throws Exception {
        this.nodeName = nodeName;
        this.port = port;
        this.socket = new DatagramSocket(port);
        this.peers = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityBlockingQueue<>(10, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
    }

    public void addPeer(String nodeName, InetAddress address) {
        peers.put(nodeName, address);
    }


}
