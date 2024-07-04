package ChatApp;

import messages.Messages;
import node.Node;

import java.net.InetAddress;

public class ChatApplication {
    public static void main(String args[]) throws Exception {
        String[] allNodes = {"Node1", "Node2", "Node3", "Node4", "Node5", "Node6", "Node7", "Node8", "Node9", "Node10"};

        Node node1 = new Node("Node1", 5000, allNodes);
        Node node2 = new Node("Node2", 5001, allNodes);
        Node node3 = new Node("Node3", 5002, allNodes);
        Node node4 = new Node("Node4", 5003, allNodes);
        Node node5 = new Node("Node5", 5004, allNodes);
        Node node6 = new Node("Node6", 5005, allNodes);
        Node node7 = new Node("Node7", 5006, allNodes);
        Node node8 = new Node("Node8", 5007, allNodes);
        Node node9 = new Node("Node9", 5008, allNodes);
        Node node10 = new Node("Node10", 5009, allNodes);

        // Adding peers
        Node[] nodes = {node1, node2, node3, node4, node5, node6, node7, node8, node9, node10};
        for (Node node : nodes) {
            for (Node peer : nodes) {
                if (!node.equals(peer)) {
                    node.addPeer(peer.getNodeName(), InetAddress.getByName("localhost"));
                }
            }
        }

        // Start listening for messages on each node
        for (Node node : nodes) {
            new Thread(() -> {
                try {
                    node.receiveMessages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Sending private messages between two nodes
        node2.sendMessage(new Messages("Node2", "Private Hello from Node2 to Node3", System.currentTimeMillis(), false), "Node3");
        node4.sendMessage(new Messages("Node4", "Private Hello from Node4 to Node5", System.currentTimeMillis(), false), "Node5");

        // Sending a broadcast message from Node1 to all other nodes
        node1.broadcastMessage(new Messages("Node1", "Hello from Node1 to everyone", System.currentTimeMillis(), true));


    }
}
