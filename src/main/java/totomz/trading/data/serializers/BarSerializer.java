package totomz.trading.data.serializers;

import com.ib.client.Bar;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public abstract class BarSerializer {

    public abstract void serialize(List<Bar> bars, String symbol) throws IOException, Exception;

    /**
     *
     * TRUE se nel db ci sono dati nell'intervallo [ from.minus(qty,duration), from]
     */
    public abstract boolean haveData(String symbol, LocalDateTime from, int duration_qty, ChronoUnit duration_time);
}
