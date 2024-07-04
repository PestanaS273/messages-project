package messages;

import java.io.Serializable;
import java.util.Map;

public class Messages implements Serializable {

    private String sender;
    private String content;
    private long timestamp;
    private boolean isBroadcast;
    private Map<String, Integer> vectorClock;

    public Messages(String sender, String content, long timestamp, boolean isBroadcast) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.isBroadcast = isBroadcast;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isBroadcast() {
        return isBroadcast;
    }

    public Map<String, Integer> getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(Map<String, Integer> vectorClock) {
        this.vectorClock = vectorClock;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
