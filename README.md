# ib-historical-data

## Run postgres
```
docker volume create trading_pgdata
docker run  -e POSTGRES_PASSWORD=pippuz \
            -e POSTGRES_USER=trading \
            -v trading_pgdata:/var/lib/postgresql/data \
            --network trading \
            --name trading_db \
            -p 5432:5432 \
            -d postgres 

docker run -p 80:80 \
    -e "PGADMIN_DEFAULT_EMAIL=user@domain.com" \
    -e "PGADMIN_DEFAULT_PASSWORD=lopilopi" \
    -v trading_pgadmin:/var/lib/pgadmin
    --network trading \
    -d dpage/pgadmin4
``
