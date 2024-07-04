package node;

import messages.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

    private byte[] serializeMessage(Messages message) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(message);
        oos.flush();
        return bos.toByteArray();
    }

    private Messages deserializeMessage(byte[] buffer) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (Messages) ois.readObject();
    }

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

    public void sendMessage(Messages message, String recipientNode) throws Exception {
        InetAddress address = peers.get(recipientNode);
        if (address != null) {
            byte[] buffer = serializeMessage(message);
        }
    }

    public void broadcastMessage(Messages message) throws Exception {
        for (InetAddress address : peers.values()) {
            byte[] buffer = serializeMessage(message);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        }
    }

    public void receiveMessage() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            socket.receive(packet);
            Messages message = deserializeMessage(buffer);
            handleMessage(message);
        }
    }

    private void handleMessage(Messages message) {
        if (message.isBroadcast()) {
            messageQueue.add(message);
            while (!messageQueue.isEmpty()) {
                Messages msg = messageQueue.poll();
                System.out.println(msg.getSender() + ": " + msg.getContent());
            }
        }
    }
}
