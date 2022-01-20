package totomz.trading.data;

import com.google.common.util.concurrent.RateLimiter;
import com.ib.client.Bar;
import com.ib.client.ContractDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import totomz.trading.data.ibapi.IbApiSync;
import totomz.trading.data.serializers.BarSerializer;
import totomz.trading.data.serializers.CSVSerializer;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private final static RateLimiter rateLimiter = RateLimiter.create(0.09); // little less than 1 request every 10 seconds
    public final static DateTimeFormatter barTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");

    public static boolean isTradingHours(LocalDateTime time) {
        if( time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        if(time.getHour() < 15 || time.getHour() > 22) {
            return false;
        }

        if(time.getHour() == 15 && time.getMinute() <= 30) {
            return false;
        }

        return true;
    }

    public static void main(String[] args) throws Exception {

        log.info("Starting");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

        BarSerializer serializer = new CSVSerializer(Settings.getCsvFolder());

        LocalDateTime inst = Settings.from();
        LocalDateTime to = Settings.to();
        
        log.info(String.format("Download data in range %s --> %s", inst, to));
        
        String barSize = "1 secs";
        int duration_qty = 1800;
        ChronoUnit duration_time = ChronoUnit.SECONDS;

        String duration = String.format("%s %s", duration_qty, duration_time.toString().charAt(0));

        try(IbApiSync ibapi = new IbApiSync()) {
            log.info("...connecting");
            ibapi.connect();


            for(String symbol : Settings.symbols()) {

                LocalDateTime from = inst;

                log.info("#### Processing " + symbol);
                ContractDetails c = ibapi.searchContract(symbol);

                do {
                    from = from.plus(duration_qty, duration_time);

                    String end = from.format(format);
                    
                    if( !isTradingHours(from)) {
//                        log.info(String.format("        -->   %s outside trading hours", from));
                        continue;
                    }
                    
                    /*
                        from/end/inst are messy
                        ibapi retireve candles BACKWARD from <from> for "1800 s". From is actually the last instant. 
                        from=16:00 correspond to the interval [15h30, 16h00) <-- not a typo, the last second is excluded
                        
                        The serializer does not know about the last second that is missing.
                        This can fail in sooo many cases...
                     */
                    if(serializer.haveData(symbol, from, duration_qty, duration_time)) {
                        log.info(String.format("        -->   %s data already in the db", from));
                        continue;
                    }

                    rateLimiter.acquire(1);
                    mustSleep(1000);
                    
                    log.info(String.format("Downloading %s of [%s] up to %s", duration, symbol, from));

                    List<Bar> bars = ibapi.getHistoricalData(c.contract(), end, duration, barSize);
//                    List<Bar> bars = new ArrayList<>();

                    // Check if the day of the bar is the day we've requested.
                    // From could by holiday. In this case, IB returns us the last bars
                    // fro the previous trading day.
                    // So, if we have bars for a day which is not what we have requested, we move forward
                    // Actually, I don't think this is working.
                    // The code is probably bugged. The insert in postgres
                    // is ignoring duplicate timestamp.
                    LocalDateTime barTime = LocalDateTime.parse(bars.get(0).time(), barTimeFormat);
                    if(barTime.getDayOfMonth() != from.getDayOfMonth() ) {
                        log.info("    +++> bartime is " + barTime + ". Moving to the next day!");

                        // Remove the duration (to reset to the first time interval)
                        // and move on 1 day
                        from = from.plus(1, ChronoUnit.DAYS).withHour(15).withMinute(30).withSecond(0);
                        continue;
                    }

                    serializer.serialize(bars, symbol);

                    log.info("Done");
                }
                while (from.isBefore(to));
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
