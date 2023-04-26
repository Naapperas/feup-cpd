import client.Client;
import server.Server;

public class Main {
    public static void main(String[] args) {

        Config config = null;

        try {
            config = Config.parseConfig(args);
        } catch (Config.InvalidConfigValuesException e) {
            Logger.error(e.getMessage());
            System.exit(1);
        }

        // If we reach this point it means that we correctly parsed our config values.

        Runnable app = null;
        try {
            var appType = config.get("type");

            app = switch (appType.toLowerCase()) {
                case "server" -> new Server();
                case "client" -> new Client();
                default -> throw new Exception("Unknown app type");
            };
        } catch (Exception e) {
            Logger.error(e.getMessage());
            System.exit(1);
        }

        // if we reached this point, that means we are ready to roll

        app.run();
    }
}