if [[ $# -ne 0 ]]
then
   EDP=$1
   Nb=$2
else
  EDP=localhost:8080
  Nb=2
fi

curl -X POST http://$EDP/api/v1/orders/control -H 'accept: application/json' -H 'Content-Type: application/json' \
  -d '{"backend": "Kafka", "records": '$Nb' }'