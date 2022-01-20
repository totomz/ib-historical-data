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

        if (writer != null) {
            writer.close();
        }

    }

    @Override
    public boolean haveData(String symbol, LocalDateTime from, int duration_qty, ChronoUnit duration_time) {
        String sday = from.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = this.folder + "/" + sday + "-" + symbol.toUpperCase() + ".csv";
        Path path = Path.of(fileName);
            
        if (!Files.exists(path)) {
            return false;
        }

        if (cache.getOrDefault(sday, Boolean.FALSE)) {
            return true;
        }
        
        String lastLine = tail(path.toFile());

        if (lastLine.startsWith(String.format("%s  21:59:59,", sday))) {
            System.out.println("FULL FILE FOUND!");
            cache.put(sday, Boolean.TRUE);
            return true;
        }
        
        // If I'm here, the file exists but it's not full
        // I'll delete it before appending the bars
        try {
            Files.delete(path);
            System.out.println("### DELETED because not full");
        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;
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
