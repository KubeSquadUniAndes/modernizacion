variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "aws_profile" {
  description = "Perfil de AWS CLI a usar (credenciales de AWS Academy)"
  type        = string
  default     = "universidad"
}

variable "project" {
  description = "Project name used for resource naming"
  type        = string
  default     = "catalog"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "cluster_version" {
  description = "EKS Kubernetes version"
  type        = string
  default     = "1.33"
}

variable "node_instance_type" {
  description = "Instance type for the EKS managed node group"
  type        = string
  default     = "t3.small"
}

variable "db_name" {
  description = "Database name created on the RDS instance"
  type        = string
  default     = "catalogdb"
}

variable "db_username" {
  description = "Master username for the RDS instance"
  type        = string
  # "catalog" es palabra reservada para el engine Postgres en RDS.
  default = "catalogadmin"
}
