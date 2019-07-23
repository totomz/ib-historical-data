package totomz.trading.data;

import com.google.common.util.concurrent.RateLimiter;
import com.ib.client.Bar;
import com.ib.client.ContractDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import totomz.trading.data.ibapi.IbApiSync;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private final static RateLimiter rateLimiter = RateLimiter.create(0.1); // rate = 1 every 10 seconds

    public static void main(String[] args) {

        log.info("Starting");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

        try(IbApiSync ibapi = new IbApiSync()) {
            ibapi.connect();

            Settings.symbols().forEach(symbol -> {

                log.info("Processing " + symbol);

                ContractDetails c = ibapi.searchContract(symbol);
                LocalDateTime inst = LocalDateTime.now();

                /*
                    Devo prendere 1 anno di dati.

                    inst = now
                    getData(from := now-30D, to := now)
                    inst = inst - 30D
                 */

                rateLimiter.acquire(1);


                for (int i=0; i<12; i++) {
                    String end = inst.format(format);
                    System.out.println("Scarico fino al " + end);

                    inst = inst.minusDays(30);
                }



            });



        }
        catch (Exception e) {

        }




    }

}
