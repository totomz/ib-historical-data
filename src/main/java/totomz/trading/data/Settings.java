package totomz.trading.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public final class Settings {

    private static Logger log = LoggerFactory.getLogger(Settings.class);
    
    private Settings() {
        super();
    }

    public static List<String> symbols() {
        String symbols = System.getProperty("symbols", "AMD,AMZN,DIS,TSLA,MSFT");
        return Arrays.asList(symbols.split(","));
    }

    public static LocalDateTime from() {
        
        String fromd = System.getProperty("fromd");
        if (fromd != null) {
            log.info("Using relative from as start time");
            int n = Integer.parseInt(fromd);
            return LocalDateTime.now().minus(n, ChronoUnit.DAYS).withHour(8).withMinute(0);
        }
        
        String[] dates = System.getProperty("from", "2021.01.04").split("\\.");
        return LocalDateTime.of(Integer.parseInt(dates[0]), 
                                Integer.parseInt(dates[1]), 
                                Integer.parseInt(dates[2]), 8,0);
    }

    public static LocalDateTime to() {
        
        String fromd = System.getProperty("fromd");
        if (fromd != null) {
            log.info("Using now as end time");
            return LocalDateTime.now().withHour(8).withMinute(0);
        }
        
        String[] dates = System.getProperty("to", "2021.02.01").split("\\.");
        return LocalDateTime.of(Integer.parseInt(dates[0]), 
                                Integer.parseInt(dates[1]), 
                                Integer.parseInt(dates[2]), 8,0);
    }

    public static int clientId() {
        return 1;
    }

    public static int ib_port() {
        String ibport = System.getProperty("ib.port", "7496");
        return Integer.parseInt(ibport);
    }

    public static String id_host() {
        return System.getProperty("ib.host", "192.168.10.180");
    }

    public static String getCsvFolder() {
        return System.getProperty("csv", "data/sec_1");
    }
    
}
