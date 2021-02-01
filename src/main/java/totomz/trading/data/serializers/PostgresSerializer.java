package totomz.trading.data.serializers;

import com.ib.client.Bar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import totomz.trading.data.Main;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PostgresSerializer extends BarSerializer implements Closeable {

    private static Logger log = LoggerFactory.getLogger(PostgresSerializer.class);

    private final Connection conn;
    private final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd  HH:mm:ss");

    public PostgresSerializer() throws SQLException {
        String url = "jdbc:postgresql://localhost/trading";
        Properties props = new Properties();
        props.setProperty("user","trading");
        props.setProperty("password","pippuz");

        this.conn = DriverManager.getConnection(url, props);
        conn.setAutoCommit(false);
    }

    public static void main(String[] args) throws SQLException, IOException {
        PostgresSerializer pg = new PostgresSerializer();
        pg.exportCsv("amzn");
        System.out.println("bomba");
    }

    public void exportCsv(String symbol) throws IOException {

        List<Date> dates = new ArrayList<>();
        try(Statement stmt = conn.createStatement();) {
            try(ResultSet rs =  stmt.executeQuery("select distinct date(datetime) from " + symbol + " order by date asc");) {
                while (rs.next()) {
                    dates.add(rs.getDate("date"));
                }
            }
        }
        catch (SQLException e) {
            log.error("Dio java", e);
        }

        // parallel stream al cazzo

        // Tutto questo ti pare strano perche' e' di 20 anni fa.
        // Ma try-with-resources era una figata all'epoca
        CSVSerializer csv = new CSVSerializer("data/bomba");

        for(Date date : dates) {
            try(Statement stmt = conn.createStatement()) {
                log.info("Dumping " + date.toString());
                try(ResultSet rs =  stmt.executeQuery("select datetime,open,high,low,close,volume from " + symbol + " where date(datetime) = '" + date.toString() + "' order by datetime asc")) {
                    List<Bar> bars = new ArrayList<>();
                    int i = 0;  // boh
                    while (rs.next()) {
                        System.out.println(rs.getString("datetime"));
                        LocalDateTime t = LocalDateTime.parse(rs.getString("datetime"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        bars.add(new Bar(
                                t.format(format),
                                rs.getDouble("open"),
                                rs.getDouble("high"),
                                rs.getDouble("low"),
                                rs.getDouble("close"),
                                rs.getLong("volume"),
                                i++,
                                2d
                        ));

                    }
                    log.info("Found " + i + " bars");

                    log.info("Writing CSV");
                    csv.serialize(bars, symbol);
                }
                catch (Exception e) {
                    log.error("Che cojoni", e);
                }
            }
            catch (SQLException e) {
                log.error("Dio java", e);
            }



        }

    }

    @Override
    public void serialize(List<Bar> bars, String symbol) throws Exception {

        try(PreparedStatement stmt =  this.conn.prepareStatement("INSERT INTO " + symbol.toLowerCase() + " " +
                "(datetime, open, high, low, close, volume ) " +
                "VALUES (?,?,?,?,?,?) ON CONFLICT (datetime) DO NOTHING" +
                "" );) {
            for(Bar bar : bars) {
                LocalDateTime bomba = LocalDateTime.parse(bar.time(), format);
                stmt.setObject(1, bomba);
                stmt.setDouble(2, bar.open());
                stmt.setDouble(3, bar.high());
                stmt.setDouble(4, bar.low());
                stmt.setDouble(5, bar.close());
                stmt.setDouble(6, bar.volume());

                stmt.addBatch();
            }


            log.info("Sending query");
            stmt.executeBatch();
            conn.commit();
            log.info("    --> Ok" );
        }
        catch (Exception e) {
            log.error("Fucking Error", e);
            throw e;
        }

    }

    @Override
    public boolean haveData(String symbol, LocalDateTime from, int duration_qty, ChronoUnit duration_time) {

        String fromTime = from.minus(duration_qty, duration_time).format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
        String toTime = from.format(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss"));
        String sql = "select count(*) from " + symbol.toLowerCase() + " where datetime > '" + fromTime + "' and datetime < '" + toTime + "'";
        System.out.println("    " + sql);
        try(Statement stmt = conn.createStatement();) {
            try(ResultSet rs =  stmt.executeQuery(sql);) {
                while (rs.next()) {
                    int rows = rs.getInt(1);
                    if(rows > 0) {
                        log.info("Rows for " + symbol + " at " + from + ":" + rows);
                        return true;
                    }
                }
            }
        }
        catch (SQLException e) {
            log.error("Dio java", e);
        }

        return false;
    }

    @Override
    public void close() throws IOException {
        try {
            this.conn.close();
        } catch (SQLException e) {
            log.error("Can't close connection", e);
        }
    }
}
