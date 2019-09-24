package totomz.trading.data.ibapi;

import com.ib.client.Bar;
import com.ib.client.ContractDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {


    public static void main(String[] args) throws InterruptedException {
        //test();
        System.out.println(LocalDateTime.now().getDayOfWeek().getValue());

        LocalDate d = LocalDate.parse("20171219", DateTimeFormatter.BASIC_ISO_DATE);
        System.out.println(d);
//        searchContract();
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
