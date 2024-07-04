package node;

import messages.Messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class Node {
    private String nodeName;
    private int port;
    private DatagramSocket socket;
    private ConcurrentHashMap<String, InetAddress> peers;
    private PriorityBlockingQueue<Messages> messageQueue;
    private HashSet<String> receivedMessages;
    private ConcurrentHashMap<String, Set<String>> pendingAcks;

    private int lamportClock;
    private Map<String, Integer> vectorClock;

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

    public Node(String nodeName, int port, String[] allNodes) throws Exception {
        this.nodeName = nodeName;
        this.port = port;
        this.socket = new DatagramSocket(port);
        this.peers = new ConcurrentHashMap<>();
        this.messageQueue = new PriorityBlockingQueue<>(10, (m1, m2) -> Long.compare(m1.getTimestamp(), m2.getTimestamp()));
        this.receivedMessages = new HashSet<>();
        this.pendingAcks = new ConcurrentHashMap<>();
        this.lamportClock = 0;

        this.vectorClock = new HashMap<>();
        for (String node : allNodes) {
            vectorClock.put(node, 0);
        }
    }

    public void addPeer(String nodeName, InetAddress address) {
        peers.put(nodeName, address);
    }

    public void sendMessage(Messages message, String recipientNode) throws Exception {
        lamportClock++;
        message.setTimestamp(lamportClock);
        message.setVectorClock(null); // Clear vector clock for direct messages
        InetAddress address = peers.get(recipientNode);
        if (address != null) {
            byte[] buffer = serializeMessage(message);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println(this.nodeName + " sent direct message to " + recipientNode + ": " + message.getContent() + " [Lamport Clock: " + lamportClock + "]");
        }
    }

    public void broadcastMessage(Messages message) throws Exception {
        vectorClock.put(nodeName, vectorClock.get(nodeName) + 1);
        message.setVectorClock(new HashMap<>(vectorClock));
        message.setTimestamp(0); // Clear Lamport clock for broadcast messages

        String messageId = message.getSender() + "-" + message.getVectorClock();
        Set<String> acksPending = new HashSet<>(peers.keySet());
        pendingAcks.put(messageId, acksPending);

        // Send message to all peers
        for (String peer : peers.keySet()) {
            sendBroadcastMessage(message, peer);
        }

        // Retry mechanism for broadcasting messages until acknowledgments are received
        while (!acksPending.isEmpty()) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            byte[] data = packet.getData();
            String receivedString = new String(data, 0, packet.getLength());

            if (receivedString.startsWith("ACK:")) {
                String ackMessageId = receivedString.substring(4);
                if (pendingAcks.containsKey(messageId) && acksPending.contains(ackMessageId)) {
                    acksPending.remove(ackMessageId);
                    System.out.println(this.nodeName + " received ACK for " + ackMessageId);
                }
            } else {
                Messages receivedMessage = deserializeMessage(data);
                if (receivedMessages.add(receivedMessage.getSender() + receivedMessage.getVectorClock().toString())) {
                    updateClocks(receivedMessage);
                    handleMessage(receivedMessage);
                    sendAcknowledgment(packet.getAddress(), packet.getPort(), receivedMessage);
                }
            }
        }
    }

    private void sendBroadcastMessage(Messages message, String recipientNode) throws Exception {
        InetAddress address = peers.get(recipientNode);
        if (address != null) {
            byte[] buffer = serializeMessage(message);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println(this.nodeName + " sent broadcast message to " + recipientNode + ": " + message.getContent() + " [Vector Clock: " + message.getVectorClock() + "]");
        }
    }

    public void receiveMessages() throws Exception {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true) {
            socket.receive(packet);
            byte[] data = packet.getData();
            String receivedString = new String(data, 0, packet.getLength());

            if (receivedString.startsWith("ACK:")) {
                // Handle acknowledgment
                String ackMessageId = receivedString.substring(4);
                receivedMessages.add(ackMessageId);
                System.out.println(this.nodeName + " received ACK for " + ackMessageId);
            } else {
                // Handle normal message
                Messages message = deserializeMessage(data);
                if (message.isBroadcast()) {
                    if (receivedMessages.add(message.getSender() + message.getVectorClock().toString())) {
                        updateClocks(message);
                        handleMessage(message);
                        sendAcknowledgment(packet.getAddress(), packet.getPort(), message);
                    }
                } else {
                    if (receivedMessages.add(message.getSender() + message.getTimestamp())) {
                        updateClocks(message);
                        handleMessage(message);
                        sendAcknowledgment(packet.getAddress(), packet.getPort(), message);
                    }
                }
            }
        }
    }

    private void updateClocks(Messages message) {
        if (message.isBroadcast()) {
            // Update vector clock for broadcast messages
            Map<String, Integer> receivedVectorClock = message.getVectorClock();
            for (String node : vectorClock.keySet()) {
                vectorClock.put(node, Math.max(vectorClock.get(node), receivedVectorClock.get(node)));
            }
        } else {
            // Update Lamport clock for direct messages
            lamportClock = (int) (Math.max(lamportClock, message.getTimestamp()) + 1);
        }
    }

    public String getNodeName() {
        return nodeName;
    }

    private void handleMessage(Messages message) {
        if (message.isBroadcast()) {
            messageQueue.add(message);
            while (!messageQueue.isEmpty()) {
                Messages msg = messageQueue.poll();
                System.out.println(nodeName + " received broadcast: " + msg.getSender() + ": " + msg.getContent() + " [Vector Clock: " + msg.getVectorClock() + "]");
            }
        } else {
            System.out.println(nodeName + " received direct message: " + message.getSender() + ": " + message.getContent() + " [Lamport Clock: " + lamportClock + "]");
        }
    }

    private void sendAcknowledgment(InetAddress address, int port, Messages message) throws Exception {
        String ack = message.isBroadcast() ?
                "ACK:" + message.getSender() + "-" + message.getVectorClock().toString() :
                "ACK:" + message.getSender() + "-" + message.getTimestamp();
        byte[] buffer = ack.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(packet);
        System.out.println(this.nodeName + " sent ACK for " + ack);
    }
}
