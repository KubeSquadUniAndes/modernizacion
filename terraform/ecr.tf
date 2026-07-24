resource "aws_ecr_repository" "catalog_service" {
  name                 = "${var.project}-service"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = false
  }

  tags = {
    Project     = var.project
    Environment = var.environment
  }
}
