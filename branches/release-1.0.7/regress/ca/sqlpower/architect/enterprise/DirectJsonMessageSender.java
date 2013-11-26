package ca.sqlpower.architect.enterprise;

import org.json.JSONArray;
import org.json.JSONObject;

import ca.sqlpower.dao.MessageSender;
import ca.sqlpower.dao.SPPersistenceException;
import ca.sqlpower.dao.json.SPJSONMessageDecoder;

/**
 * Sends JSON messages directly to the JSON decoder
 */
public class DirectJsonMessageSender implements MessageSender<JSONObject> {

    private final SPJSONMessageDecoder decoder;

    private JSONArray array;

    public DirectJsonMessageSender(SPJSONMessageDecoder decoder) {
        this.decoder = decoder;
        this.array = new JSONArray();
    }

    public void clear() {
        // messages get sent directly so no need to 'clear'
    }

    public void flush() throws SPPersistenceException {
        decoder.decode(array.toString());
        array = new JSONArray();
        // I wish JSONArray had a clear, but since this is just a test
        // class, I'm not that concerned of any performance hit from
        // creating a new object at this point.
    }

    public void send(JSONObject content) throws SPPersistenceException {
        array.put(content);
    }

}