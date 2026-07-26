#!/usr/bin/env bash
#
#    Copyright 2010-2026 the original author or authors.
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       https://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#

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
