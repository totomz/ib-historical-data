package totomz.trading.data.ibapi;

import ch.qos.logback.classic.pattern.ClassOfCallerConverter;
import com.ib.client.Bar;
import com.ib.client.ContractDetails;
import totomz.trading.data.serializers.PostgresSerializer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;

public class Main {


    public static void main(String[] args) throws Exception {

        Main.test();

    }

    public static void test() {

        try(IbApiSync ibapi = new IbApiSync()) {
            ibapi.connect();

            ContractDetails c = ibapi.searchContract("MSFT");
            List<Bar> bars = ibapi.getHistoricalData(c.contract(), "", "1 D", "1 min");
        
            for (Bar b : bars) {
                System.out.println(b.time());
                System.out.println(b.close());
            }
            
            System.out.println(":::: TROVATE LE BARRE :::::");


        }
        catch (Exception e) {

        }

        System.out.println("ciao");


    }


    public static void searchContract() {
        IbApiSync ibapi = new IbApiSync();
        ibapi.connect();

        ContractDetails c = ibapi.searchContract("MSFT");

        System.out.println(c);
    }

}
