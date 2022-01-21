package totomz.trading.data.serializers;

import com.ib.client.Bar;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class CSVSerializer extends BarSerializer {

    private final String folder;
    private HashMap<String, Boolean> cache = new HashMap<>();
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");
    public CSVSerializer(String folder) {
        this.folder = folder;
    }

    public void serialize(List<Bar> bars, String symbol) throws IOException {

        String lastDay = "";
        PrintWriter writer = null;

        for (Bar bar : bars) {
            String day = bar.time().split(" ")[0];

            if (!day.equals(lastDay)) {
                lastDay = day;
                String fileName = this.folder + "/" + lastDay + "-" + symbol.toUpperCase() + ".csv";

                if (writer != null) {
                    writer.close();
                }

                writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

            }

            writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                    bar.time(),
                    bar.open(),
                    bar.high(),
                    bar.low(),
                    bar.close(),
                    bar.volume()
            ));
        }

        if (writer != null) {
            writer.close();
        }

    }

    @Override
    public boolean haveData(String symbol, LocalDateTime from, LocalDateTime to) {
        String sday = from.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = this.folder + "/" + sday + "-" + symbol.toUpperCase() + ".csv";
        Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            return false;
        }
        
        String lastLine = tail(path.toFile());
        String lastDateString = lastLine.split(",")[0];
        LocalDateTime lastInFile =  LocalDateTime.parse(lastDateString, format);

        return lastInFile.isAfter(from);
    }


    public String tail(File file) {
        RandomAccessFile fileHandler = null;
        try {
            fileHandler = new RandomAccessFile(file, "r");
            long fileLength = fileHandler.length() - 1;
            StringBuilder sb = new StringBuilder();

            for (long filePointer = fileLength; filePointer != -1; filePointer--) {
                fileHandler.seek(filePointer);
                int readByte = fileHandler.readByte();

                if (readByte == 0xA) {
                    if (filePointer == fileLength) {
                        continue;
                    }
                    break;

                } else if (readByte == 0xD) {
                    if (filePointer == fileLength - 1) {
                        continue;
                    }
                    break;
                }

                sb.append((char) readByte);
            }

            String lastLine = sb.reverse().toString();
            return lastLine;
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileHandler != null)
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    /* ignore */
                }
        }
    }
}
