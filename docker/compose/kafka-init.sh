#!/bin/bash
echo "Waiting for Kafka to be ready..."
sleep 10

kafka-topics.sh --create --if-not-exists --bootstrap-server kafka:9092 --topic transfer.events --partitions 12 --replication-factor 1
kafka-topics.sh --create --if-not-exists --bootstrap-server kafka:9092 --topic transaction-events --partitions 12 --replication-factor 1
kafka-topics.sh --create --if-not-exists --bootstrap-server kafka:9092 --topic customer.events --partitions 6 --replication-factor 1
kafka-topics.sh --create --if-not-exists --bootstrap-server kafka:9092 --topic account.events --partitions 6 --replication-factor 1
kafka-topics.sh --create --if-not-exists --bootstrap-server kafka:9092 --topic notification.events --partitions 6 --replication-factor 1
kafka-topics.sh --create --if-not-exists --bootstrap-server kafka:9092 --topic compliance.events --partitions 6 --replication-factor 1

echo "Kafka topics created successfully"
