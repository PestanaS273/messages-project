package ChatApp;
import messages.Messages;
import node.Node;

import java.net.InetAddress;

public class ChatApplication {
    public static void main(String args[]) throws Exception{
        Node node1 = new Node("Node1", 5000);
        Node node2 = new Node("Node2", 5001);
        Node node3 = new Node("Node3", 5002);
        Node node4 = new Node("Node4", 5003);
        Node node5 = new Node("Node5", 5004);


        node1.addPeer("Node2", InetAddress.getByName("localhost"));
        node1.addPeer("Node3", InetAddress.getByName("localhost"));
        node1.addPeer("Node4", InetAddress.getByName("localhost"));
        node1.addPeer("Node5", InetAddress.getByName("localhost"));

        node2.addPeer("Node1", InetAddress.getByName("localhost"));
        node2.addPeer("Node3", InetAddress.getByName("localhost"));
        node2.addPeer("Node4", InetAddress.getByName("localhost"));
        node2.addPeer("Node5", InetAddress.getByName("localhost"));

        node3.addPeer("Node1", InetAddress.getByName("localhost"));
        node3.addPeer("Node2", InetAddress.getByName("localhost"));
        node3.addPeer("Node4", InetAddress.getByName("localhost"));
        node3.addPeer("Node5", InetAddress.getByName("localhost"));

        node4.addPeer("Node1", InetAddress.getByName("localhost"));
        node4.addPeer("Node2", InetAddress.getByName("localhost"));
        node4.addPeer("Node3", InetAddress.getByName("localhost"));
        node4.addPeer("Node5", InetAddress.getByName("localhost"));

        node5.addPeer("Node1", InetAddress.getByName("localhost"));
        node5.addPeer("Node2", InetAddress.getByName("localhost"));
        node5.addPeer("Node3", InetAddress.getByName("localhost"));
        node5.addPeer("Node4", InetAddress.getByName("localhost"));

        new Thread(() -> {
            try {
                node1.receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                node2.receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                node3.receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                node4.receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                node5.receiveMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        node1.sendMessage(new Messages("Node1", "Hello from Node1", System.currentTimeMillis(), false), "Node2");
        node2.sendMessage(new Messages("Node2", "Hello from Node2", System.currentTimeMillis(), false), "Node3");
        node3.broadcastMessage(new Messages("Node3", "Hello from Node3", System.currentTimeMillis(), true));
    }
}
