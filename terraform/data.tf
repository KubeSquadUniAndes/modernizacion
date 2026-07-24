data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }

  # EKS no soporta control plane en us-east-1e (AWS lo bloquea explícitamente).
  # Se restringe a las AZs donde sí crea el cluster.
  filter {
    name   = "availability-zone"
    values = ["us-east-1a", "us-east-1b", "us-east-1c", "us-east-1d", "us-east-1f"]
  }
}

# AWS Academy Learner Lab: no se pueden crear roles IAM nuevos.
# LabRole ya existe con las trust policies y políticas administradas
# necesarias para EKS (cluster role y node role).
data "aws_iam_role" "lab_role" {
  name = "LabRole"
}
