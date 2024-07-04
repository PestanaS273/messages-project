package messages;
import java.io.Serializable;

public class Messages implements Serializable{

    private String sender;
    private String content;
    private long timestamp;
    private boolean isBroadcast;


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
}
