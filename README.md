TEST PARA 1 PC:

docker compose up --build --scale crawler=3 --scale indexer=3 --scale search-node=3

API:

http://localhost/search?q=(palabra clave)

