output "cluster_name" {
  description = "Nombre del cluster EKS"
  value       = aws_eks_cluster.main.name
}

output "cluster_endpoint" {
  description = "Endpoint del API server de EKS"
  value       = aws_eks_cluster.main.endpoint
}

output "ecr_repository_url" {
  description = "URL del repositorio ECR para catalog-service"
  value       = aws_ecr_repository.catalog_service.repository_url
}

output "rds_address" {
  description = "Host de la instancia RDS (sin puerto)"
  value       = aws_db_instance.main.address
  sensitive   = true
}

output "rds_port" {
  value = aws_db_instance.main.port
}

output "db_name" {
  value = aws_db_instance.main.db_name
}

output "db_username" {
  value = aws_db_instance.main.username
}

output "db_secret_arn" {
  description = "ARN del secreto en Secrets Manager con la master password de RDS (gestionado por AWS)"
  value       = aws_db_instance.main.master_user_secret[0].secret_arn
}

output "aws_region" {
  value = var.aws_region
}

output "aws_profile" {
  value = var.aws_profile
}

output "configure_kubectl_command" {
  description = "Comando para apuntar kubectl al cluster recién creado"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${aws_eks_cluster.main.name}"
}
