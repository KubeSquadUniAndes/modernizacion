resource "aws_db_subnet_group" "main" {
  name       = "${var.project}-${var.environment}-db-subnet-group"
  subnet_ids = data.aws_subnets.default.ids
  tags       = { Name = "${var.project}-${var.environment}-db-subnet-group" }
}

resource "aws_db_instance" "main" {
  identifier     = "${var.project}-${var.environment}-postgres"
  engine         = "postgres"
  engine_version = "16.14"
  instance_class = "db.t3.micro"

  allocated_storage = 20
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username

  # AWS genera y guarda la master password directamente en Secrets Manager —
  # nunca pasa por Terraform state, por la shell, ni la escribe una persona.
  manage_master_user_password = true

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az                = false
  publicly_accessible     = false
  deletion_protection     = false
  skip_final_snapshot     = true
  backup_retention_period = 0
  apply_immediately       = true

  tags = {
    Project     = var.project
    Environment = var.environment
  }
}
