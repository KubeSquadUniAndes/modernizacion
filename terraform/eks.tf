# ── EKS Cluster ────────────────────────────────────────────────────────────
# role_arn y node_role_arn reusan LabRole (ver data.tf) — esta cuenta AWS
# Academy no permite crear roles IAM nuevos.
resource "aws_eks_cluster" "main" {
  name     = "${var.project}-${var.environment}"
  role_arn = data.aws_iam_role.lab_role.arn
  version  = var.cluster_version

  vpc_config {
    subnet_ids              = data.aws_subnets.default.ids
    endpoint_public_access  = true
    endpoint_private_access = true
  }

  tags = {
    Project     = var.project
    Environment = var.environment
  }
}

# ── Node Group ────────────────────────────────────────────────────────────
resource "aws_eks_node_group" "workloads" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.project}-${var.environment}-workloads"
  node_role_arn   = data.aws_iam_role.lab_role.arn
  subnet_ids      = data.aws_subnets.default.ids
  instance_types  = [var.node_instance_type]
  capacity_type   = "ON_DEMAND"

  scaling_config {
    desired_size = 1
    min_size     = 1
    max_size     = 1
  }

  update_config {
    max_unavailable = 1
  }

  tags = {
    Project     = var.project
    Environment = var.environment
  }
}
