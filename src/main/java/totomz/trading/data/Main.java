package totomz.trading.data;

import com.google.common.util.concurrent.RateLimiter;
import com.ib.client.Bar;
import com.ib.client.ContractDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import totomz.trading.data.ibapi.IbApiSync;

import java.io.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);
    private final static RateLimiter rateLimiter = RateLimiter.create(0.1); // rate = 1 every 10 seconds

    private static void writeCsv(List<Bar> bars, String symbol) throws IOException {

        String lastDay = "";
        PrintWriter writer = null;


        for (Bar bar : bars) {
            String day = bar.time().split(" ")[0];

            if(!day.equals(lastDay)) {
                lastDay = day;
                String fileName = "data/" + symbol + "_" + lastDay + ".csv";

                if(writer != null) {
                    writer.close();
                }

                writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));

            }
            LocalDate d = LocalDate.parse(day, DateTimeFormatter.BASIC_ISO_DATE);
            writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    bar.time(),
                    d.getDayOfWeek().getValue(),
                    bar.open(),
                    bar.high(),
                    bar.low(),
                    bar.close(),
                    bar.volume()
                    ));
        }
    }

    public static void main(String[] args) throws Exception {

        log.info("Starting");

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");


        try(IbApiSync ibapi = new IbApiSync()) {
            ibapi.connect();


            for(String symbol : Settings.symbols()) {

                log.info("Processing " + symbol);
                ContractDetails c = ibapi.searchContract(symbol);

                // If you need to download 1 day, eg 23/11/2018
                // set duration = 1D, date = 2018/11/24, backtick = 0

                // The API download duration up tp inst, eg 30D before inst
                LocalDateTime inst = LocalDateTime.of(2018, 11, 24,0, 0);
                String duration = "3 D";
                int backtick = 30;

                do {
                    inst = inst.plusDays(backtick);
                    rateLimiter.acquire(1);

                    String end = inst.format(format);
                    log.info("Downloading up to " + end);

                    List<Bar> bars = ibapi.getHistoricalData(c.contract(), end, duration, "1 min");
                    writeCsv(bars, symbol);

                    log.info("Done");

                }while (inst.isBefore(LocalDateTime.now()));
            }

        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }




    }

}
