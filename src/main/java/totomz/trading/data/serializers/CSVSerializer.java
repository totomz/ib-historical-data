package totomz.trading.data.serializers;

import com.ib.client.Bar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CSVSerializer extends BarSerializer {

    private final String folder;

    public CSVSerializer(String folder) {
        this.folder = folder;
    }

    public void serialize(List<Bar> bars, String symbol) throws IOException {

        String lastDay = "";
        PrintWriter writer = null;

        for (Bar bar : bars) {
            String day = bar.time().split(" ")[0];

            if(!day.equals(lastDay)) {
                lastDay = day;
                String fileName = this.folder + "/" + lastDay + "-" + symbol.toUpperCase() + ".csv";

                if(writer != null) {
                    writer.close();
                }

                writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

            }
//            LocalDate d = LocalDate.parse(day, DateTimeFormatter.BASIC_ISO_DATE);

            writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    bar.time(),
                    bar.open(),
                    bar.high(),
                    bar.low(),
                    bar.close(),
                    bar.volume()
//                    d.getDayOfWeek().getValue(),
            ));
        }

        if(writer != null) {
            writer.close();
        }

    }

    @Override
    public boolean haveData(String symbol, LocalDateTime from, int duration_qty, ChronoUnit duration_time) {
        return false;
    }


}
