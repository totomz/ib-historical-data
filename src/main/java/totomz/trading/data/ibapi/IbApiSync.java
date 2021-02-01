package totomz.trading.data.ibapi;

import com.ib.client.Bar;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * "convenient" wrapper to access this fucking crazy merda di api
 */
public class IbApiSync implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(IbApiSync.class);
    private Wrapper ibapi;
    private EClientSocket client;

    public IbApiSync() {
        ibapi = new Wrapper();
        client = ibapi.client();
    }

    public void connect() {
        ibapi.connect();
    }

    /**
     *
     * @param contract Contract to get data for
     * @param endDate  The request's end date and time (the empty string indicates current present moment) 'yyyyMMdd HH:mm:ss'.
     * @param duration The amount of time to go back from the request's given end date and time.
     * @param barSize bar size
     *
     * @return
     */
    public List<Bar> getHistoricalData(Contract contract, String endDate, String duration, String barSize) {

        int reqId = ibapi.nextValidId();
        // useRTH := 1 only regular hours, 0 all
        client.reqHistoricalData(reqId, contract, endDate, duration, barSize, "TRADES", 1, 1, false, null);

        List<Bar> bars = (List<Bar>)ibapi.peekResult(reqId);

        return bars;
    }

    public ContractDetails searchContract(String symbol) {
        // Get a valid id
        int reqId = ibapi.nextValidId();

        Contract contract = new Contract();
        contract.symbol(symbol);
        contract.secType("STK");
        contract.currency("USD");
        contract.exchange("SMART");
        contract.primaryExch("NASDAQ");

        //  issue a request
        client.reqContractDetails(reqId, contract);

        // wait for the result...

        ContractDetails result = (ContractDetails)ibapi.peekResult(reqId);

        return result;
    }


    @Override
    public void close() throws IOException {
        ibapi.disconnect();
    }
}
