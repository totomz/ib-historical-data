package totomz.trading.data.ibapi;

import com.ib.client.Bar;
import com.ib.client.ContractDetails;
import totomz.trading.data.serializers.PostgresSerializer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Main {


    public static void main(String[] args) throws Exception {

        PostgresSerializer diocane = new PostgresSerializer();
        List<Bar> bars = Arrays.asList(
                new Bar("20190422  15:32:21", 1d, 1d, 1d, 1d, 1l, 1, 1d),
                new Bar("20190422  15:32:23", 2d, 2d, 2d, 2d, 2l, 3, 2d)
        );
        diocane.serialize(bars, "amzn");

    }

    public static void test() {

        try(IbApiSync ibapi = new IbApiSync()) {
            ibapi.connect();

            ContractDetails c = ibapi.searchContract("MSFT");
            List<Bar> bars = ibapi.getHistoricalData(c.contract(), "", "30 D", "1 min");

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
