package totomz.trading.data.serializers;

import com.ib.client.Bar;

import java.io.IOException;
import java.util.List;

public abstract class BarSerializer {

    public abstract void serialize(List<Bar> bars, String symbol) throws IOException, Exception;
}
