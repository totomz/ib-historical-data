package totomz.trading.data;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public final class Settings {

    private Settings() {
        super();
    }


    public static List<String> symbols() {
        String symbols = System.getProperty("symbols", "AMZN,DIS,TSLA,MSFT");
        return Arrays.asList(symbols.split(","));
    }

//    public static LocalDateTime from() {
//        return LocalDateTime.of(2018, 1, 1, 8,0);
//    }

    public static LocalDateTime to() {
        return LocalDateTime.now();
    }

    public static int clientId() {
        return 1;
    }

    public static int ib_port() {
        String ibport = System.getProperty("ib.port", "7496");
        return Integer.parseInt(ibport);
    }

    public static String id_host() {
        return System.getProperty("ib.host", "127.0.0.1");
    }

    public static String getCsvFolder() {
        return System.getProperty("csv", "data/sec_1");
    }
    
    public static int backdaysFromNow() {
        String days = System.getProperty("fromd", "10");
        return Integer.parseInt(days);
    }
}
