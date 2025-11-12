output "cluster_endpoint" {
  description = "Endpoint for EKS control plane"
  value       = module.eks.cluster_endpoint
}

output "cluster_security_group_id" {
  description = "Security group ids attached to the cluster control plane"
  value       = module.eks.cluster_security_group_id
}

output "cluster_iam_role_name" {
  description = "IAM role name associated with EKS cluster"
  value       = module.eks.cluster_iam_role_name
}

output "cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data required to communicate with the cluster"
  value       = module.eks.cluster_certificate_authority_data
}

output "cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "vpc_id" {
  description = "ID of the VPC where the cluster is deployed"
  value       = module.vpc.vpc_id
}

output "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  value       = module.vpc.vpc_cidr_block
}

output "private_subnets" {
  description = "List of IDs of private subnets"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "List of IDs of public subnets"
  value       = module.vpc.public_subnets
}

output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = module.rds.db_instance_endpoint
}

output "rds_port" {
  description = "RDS instance port"
  value       = module.rds.db_instance_port
}

output "rds_db_name" {
  description = "RDS database name"
  value       = module.rds.db_instance_name
}

output "elasticache_endpoint" {
  description = "ElastiCache Redis endpoint"
  value       = module.elasticache.cluster_address
}

output "elasticache_port" {
  description = "ElastiCache Redis port"
  value       = module.elasticache.cluster_port
}

output "kafka_bootstrap_brokers" {
  description = "MSK Kafka bootstrap brokers"
  value       = module.msk.bootstrap_brokers
}

output "kafka_bootstrap_brokers_tls" {
  description = "MSK Kafka bootstrap brokers TLS"
  value       = module.msk.bootstrap_brokers_tls
}

output "kafka_zookeeper_connect_string" {
  description = "MSK Kafka Zookeeper connect string"
  value       = module.msk.zookeeper_connect_string
}

