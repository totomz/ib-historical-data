# ib-historical-data

Download historical data and stores them as CSV




## Usage

```
java -jar \
    -Dsymbols=FB,AMZN \
    -Dib.port=7496 \
    -Dib.host=127.0.0.1 \
    -Dcsv=sec1 \
    -Dfromd=30 \
    ib-historical-data-all-1.0-SNAPSHOT.jar

```