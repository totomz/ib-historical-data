package totomz.trading.data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public final class Settings {

    private Settings() {
        super();
    }


    public static List<String> symbols() {
        return Arrays.asList("AMZN", "DIS", "TSLA", "MSFT", "AMD", "GOOGL", "NFLX");
    }

    public static LocalDateTime from() {
        return LocalDateTime.of(2018, 1, 1, 8,0);
    }

    public static LocalDateTime to() {
        return LocalDateTime.now();
    }

    public static int clientId() {
        return 1;
    }

    public static int ib_port() {
        return 7497;
    }

    public static String id_host() {
        return "127.0.0.1";
//        return "192.168.100.106";
    }
}
