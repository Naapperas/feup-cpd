import java.util.HashMap;
import java.util.Map;

/**
 * Configuration container and parser
 */
public class Config {

    /**
     * Exception thrown when attempting to retrieve a value from a {@link Config} object when the associated key is not present,
     */
    public static class UnknownConfigKeyException extends Exception {
        public UnknownConfigKeyException(String msg) {
            super(msg);
        }

        @Override
        public String getMessage() {
            var faultyKey = super.getMessage();

            return "ConfigKeyException: " + faultyKey + " is not a known configuration key";
        }
    }

    /**
     * Exception thrown when attempting to parse bad configuration values.
     */
    public static class InvalidConfigValuesException extends Exception {
        public InvalidConfigValuesException() {
            super("The provided configuration values are invalid or cannot be parsed");
        }
    }

    
    
    /**
     * The container for this {@link Config object}'s configuration values.
     */
    private final Map<String, String> configMap;

    // cannot directly instantiate Config objects
    private Config() {
        configMap = new HashMap<>();
    }

    /**
     * Retrieves a configuration value from this {@link Config} object.
     *
     * @param key the config key for which we want the respective value
     * @return the value associated with the given config key
     * @throws UnknownConfigKeyException if the key is not present in this {@link Config} object
     */
    public String get(String key) throws UnknownConfigKeyException {
        var value = this.configMap.get(key);

        if (value == null)
            throw new UnknownConfigKeyException(key);

        return value;
    }

    /**
     * Sets the given configuration key-value pair.
     *
     * @param key the key to set
     * @param value the value to set
     */
    public void set(String key, String value) {
        this.configMap.put(key, value);
    }

    /**
     * Parses the given arguments into a Config object.
     *
     * @param args the arguments to this app instance
     * @return a new Config object
     */
    public static Config parseConfig(String[] args) throws InvalidConfigValuesException {
        var conf = new Config();

        // TODO: modify how we parse config values

        if (args.length < 1) throw new InvalidConfigValuesException();
        
        conf.set("type", args[0]);

        return conf;
    }
}
