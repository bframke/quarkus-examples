#!/usr/bin/env bash
export KAFKA_DROP_PROPERTIES_BASE64=$(cat kafkadrop.properties | base64 -w 0)
export KAFKA_DROP_TRUSTSTORE_BASE64=$(cat kafka.truststore.jks | base64 -w 0)
#export KAFKA_DROP_KEYSTORE_BASE64=$(cat kafka.keystore.jks | base64 -w 0)
docker-compose down && docker-compose up -d
docker exec -i kafka_kafka_1  kafka-configs.sh --zookeeper kafka_zookeeper_1:2181 --alter --add-config 'SCRAM-SHA-256=[password=admin123],SCRAM-SHA-512=[password=admin123]' --entity-type users --entity-name admin