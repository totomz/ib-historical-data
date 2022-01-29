# ib-historical-data

Download historical data and stores them as CSV

## Usage

```
gradle fatJar 

java -jar \
    -Dsymbols=FB,AMZN,TSLA,DIS,NFLX,GME,AMC, \
    -Dib.port=7496 \
    -Dib.host=192.168.10.180 \
    -Dfrom=2021.01.04 \
    -Dto=2021.12.31 \
    -Dcsv=sec1 \
    ib-historical-data-all-1.0-SNAPSHOT.jar    
    
    build/libs/ib-historical-data-all-1.0-SNAPSHOT.jar    
    -Dfromd=2 \
```

It is possible to use `-Dfromd` in alternative to `-Dfrom` and `-Dto`
`-Dfromd=<int>` returns the 1s OHCL candles in the range [today-fromd days, now]  