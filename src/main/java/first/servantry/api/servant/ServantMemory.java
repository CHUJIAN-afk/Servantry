package first.servantry.api.servant;

import java.util.HashMap;
import java.util.Map;

public class ServantMemory {
    private final Map<String, Object> data = new HashMap<>();

    public <T> T get(String key, T defaultValue) {
        return (T) data.getOrDefault(key, defaultValue);
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
    }
}