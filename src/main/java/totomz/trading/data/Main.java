package totomz.trading.data;

import com.google.common.util.concurrent.RateLimiter;
import com.ib.client.Bar;
import com.ib.client.ContractDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import totomz.trading.data.ibapi.IbApiSync;
import totomz.trading.data.serializers.BarSerializer;
import totomz.trading.data.serializers.CSVSerializer;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimeZone;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private final static RateLimiter rateLimiter = RateLimiter.create(0.09); // little less than 1 request every 10 seconds
    public final static DateTimeFormatter barTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");

    private final static LocalTime openingTimeEST = LocalTime.of(9, 30);
    private final static LocalTime closingTimeEST = LocalTime.of(16, 0);
    private final static ZoneId tzRome = ZoneId.of("Europe/Rome");
    private final static ZoneId tzEst = ZoneId.of("America/New_York");
        
    public static boolean isTradingHours(LocalDateTime dateTime) {
        if( dateTime.getDayOfWeek() == DayOfWeek.SATURDAY || dateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        ZonedDateTime nasdaqDateTime = dateTime.atZone(tzRome).withZoneSameInstant(tzEst);
        LocalTime nasdaqTime = nasdaqDateTime.toLocalTime();
        
        if (nasdaqTime.compareTo(openingTimeEST) < 0) { //--> se <0, nasdaqTime < openingTimeEST
            return false;
        }
                
        if (nasdaqTime.compareTo(closingTimeEST) > 0 ) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) throws Exception {

        log.info("Starting");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

        BarSerializer serializer = new CSVSerializer(Settings.getCsvFolder());

        // LocalDateTime inst = Settings.from();
        // LocalDateTime to = Settings.to();
        
        log.info(String.format("Download data in range %s --> %s", Settings.from(), Settings.to()));
        
        String barSize = "1 secs";
        int duration_qty = 1800;
        ChronoUnit duration_time = ChronoUnit.SECONDS;

        String duration = String.format("%s %s", duration_qty, duration_time.toString().charAt(0));

        try(IbApiSync ibapi = new IbApiSync()) {
            log.info("...connecting");
            ibapi.connect();


            for(String symbol : Settings.symbols()) {

                /*
                    this is the interval we're going to download bars for;
                    ibapi retrieve candles BACKWARD from <endInterval> - "1800 s".  
                    endInterval=16:00 correspond to the interval [15h30, 16h00) <-- not a typo, the last second is excluded!
                    
                    both datetimes are in the LOCAL timezone;
                 */
                LocalDateTime endInterval = Settings.from();
                LocalDateTime fromInterval = Settings.from().plus(-1 * duration_qty, ChronoUnit.SECONDS);;

                log.info("#### Processing " + symbol);
                ContractDetails c = ibapi.searchContract(symbol);

                do {
                    fromInterval = fromInterval.plus(duration_qty, ChronoUnit.SECONDS);
                    endInterval = endInterval.plus(duration_qty, ChronoUnit.SECONDS);
                    
                    if( !isTradingHours(fromInterval)) {
                       log.info(String.format("        -->   [%s, %s) outside trading hours (EST)", fromInterval, endInterval));
                        continue;
                    }
                    
                    if(serializer.haveData(symbol, fromInterval, endInterval)) {
                        log.info(String.format("        -->   [%s, %s) data already in the db", fromInterval, endInterval));
                        continue;
                    }

                    rateLimiter.acquire(1);
                    mustSleep(1000);
                    
                    log.info(String.format("Downloading %s of [%s] up to [%s, %s)", duration, symbol, fromInterval, endInterval));
                    String endFormatted = endInterval.format(format);
                    List<Bar> bars = ibapi.getHistoricalData(c.contract(), endFormatted, duration, barSize);

                    // Check if the day of the bar is the day we've requested.
                    // From could by holiday. In this case, IB returns us the last bars
                    // fro the previous trading day.
                    // So, if we have bars for a day which is not what we have requested, we move forward
                    // Actually, I don't think this is working.
                    // The code is probably bugged. The insert in postgres
                    // is ignoring duplicate timestamp.
                    LocalDateTime barTime = LocalDateTime.parse(bars.get(0).time(), barTimeFormat);
                    if(barTime.getDayOfMonth() != fromInterval.getDayOfMonth() ) {
                        log.info("    +++> bartime is " + barTime + ". Moving to the next day!");

                        // Remove the duration (to reset to the first time interval)
                        // and set the time to the Nasdaq opening hours
                        ZonedDateTime nasdaqDateTime = fromInterval.plus(1, ChronoUnit.DAYS)
                                .atZone(tzEst)
                                .withHour(9)
                                .withMinute(30)
                                .withSecond(0)
                                .withZoneSameInstant(tzRome);
                        
                        fromInterval = nasdaqDateTime.toLocalDateTime();
                        // fromInterval = fromInterval.plus(1, ChronoUnit.DAYS).withHour(15).withMinute(30).withSecond(0);
                        continue;
                    }

                    serializer.serialize(bars, symbol);

                    log.info("Done");
                }
                while (endInterval.isBefore(Settings.to()));
            }

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
    
    public static void mustSleep(long millis){
        try {
            Thread.sleep(millis);
        }
        catch (Exception e) {}
    }

}
