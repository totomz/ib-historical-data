# ib-historical-data

Download historical data and stores them as CSV

## Usage

```
gradle fatJar 

java -jar \
    -Dsymbols=FB,AMZN \
    -Dib.port=7496 \
    -Dib.host=127.0.0.1 \
    -Dfromd=2 \
    # -Dfrom=2021.02.01 \
    # -Dto=2021.02.01 \
    -Dcsv=sec1 \
    -Dfromd=10 \
    build/libs/ib-historical-data-all-1.0-SNAPSHOT.jar    
```

It is possible to use `-Dfromd` in alternative to `-Dfrom` and `-Dto`
`-Dfromd=<int>` returns the 1s OHCL candles in the range [today-fromd days, now]  