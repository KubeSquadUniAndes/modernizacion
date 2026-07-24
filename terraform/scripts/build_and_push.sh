#!/usr/bin/env bash
# Build catalog-service y publica la imagen en el ECR creado por Terraform.
# Ejecutar desde cualquier lado; se autoubica en terraform/.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="$SCRIPT_DIR/.."
APP_DIR="$TF_DIR/../catalog-service"
TAG="${1:-latest}"

cd "$TF_DIR"
REPO_URL=$(terraform output -raw ecr_repository_url)
REGION=$(terraform output -raw aws_region)
PROFILE=$(terraform output -raw aws_profile)
IMAGE="$REPO_URL:$TAG"

echo "→ Login en ECR ($REGION, perfil $PROFILE)..."
aws ecr get-login-password --region "$REGION" --profile "$PROFILE" \
  | docker login --username AWS --password-stdin "${REPO_URL%%/*}"

echo "→ Build de la imagen: $IMAGE"
# --platform linux/amd64: el node group EKS (t3.small) es x86_64; sin esto,
# en Macs Apple Silicon se construye arm64 y el pod falla con "exec format error".
docker build --platform linux/amd64 -t "$IMAGE" "$APP_DIR"

echo "→ Push a ECR..."
docker push "$IMAGE"

echo "✅ Imagen publicada: $IMAGE"
