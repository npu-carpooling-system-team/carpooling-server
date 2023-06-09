#!/bin/bash
echo "Deploying Carpooling Server"

echo "Start to pull images from docker hub"
docker pull wangminan/carpooling-gateway:latest
docker pull wangminan/carpooling-auth:latest
docker pull wangminan/carpooling-user-api:latest
docker pull wangminan/carpooling-carpooling-api:latest
docker pull wangminan/carpooling-order-api:latest
docker pull wangminan/carpooling-payment-api:latest

echo "Start to stop and remove and restart old containers_1"
cd /root/carpooling-server || exit

docker-compose stop gateway_1
docker-compose rm -f gateway_1
docker-compose up -d gateway_1

docker-compose stop auth_1
docker-compose rm -f auth_1
docker-compose up -d auth_1

docker-compose stop user_1
docker-compose rm -f user_1
docker-compose up -d user_1

docker-compose stop carpooling_1
docker-compose rm -f carpooling_1
docker-compose up -d carpooling_1

docker-compose stop order_1
docker-compose rm -f order_1
docker-compose up -d order_1

docker-compose stop payment_1
docker-compose rm -f payment_1
docker-compose up -d payment_1

echo "Start to stop and remove and restart old containers_2"
docker-compose stop gateway_2
docker-compose rm -f gateway_2
docker-compose up -d gateway_2

docker-compose stop auth_2
docker-compose rm -f auth_2
docker-compose up -d auth_2

docker-compose stop user_2
docker-compose rm -f user_2
docker-compose up -d user_2

docker-compose stop carpooling_2
docker-compose rm -f carpooling_2
docker-compose up -d carpooling_2

docker-compose stop order_2
docker-compose rm -f order_2
docker-compose up -d order_2

docker-compose stop payment_2
docker-compose rm -f payment_2
docker-compose up -d payment_2

echo "begin to remove images without tag"
docker image prune -a -f

echo "restart successfully"
