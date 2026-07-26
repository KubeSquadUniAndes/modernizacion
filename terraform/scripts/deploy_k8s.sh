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

# Configura kubectl contra el cluster EKS recién creado y aplica los
# manifests de catalog-service, rellenando los placeholders con los outputs
# de Terraform. La password de la RDS se lee de Secrets Manager (nunca se
# pide ni se exporta a mano). Requiere: aws cli, kubectl, envsubst
# (paquete gettext), python3.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="$SCRIPT_DIR/.."
K8S_DIR="$TF_DIR/k8s"
TAG="${1:-latest}"

cd "$TF_DIR"

CLUSTER_NAME=$(terraform output -raw cluster_name)
REGION=$(terraform output -raw aws_region)
PROFILE=$(terraform output -raw aws_profile)
REPO_URL=$(terraform output -raw ecr_repository_url)
SECRET_ARN=$(terraform output -raw db_secret_arn)

echo "→ Configurando kubectl para $CLUSTER_NAME (perfil $PROFILE)..."
aws eks update-kubeconfig --region "$REGION" --name "$CLUSTER_NAME" --profile "$PROFILE"

echo "→ Leyendo la password de la RDS desde Secrets Manager..."
DB_SECRET_JSON=$(aws secretsmanager get-secret-value \
  --region "$REGION" --profile "$PROFILE" \
  --secret-id "$SECRET_ARN" --query SecretString --output text)

export DB_HOST
export DB_PORT
export DB_NAME
export DB_USER
export DB_PASSWORD
export ECR_IMAGE

DB_HOST=$(terraform output -raw rds_address)
DB_PORT=$(terraform output -raw rds_port)
DB_NAME=$(terraform output -raw db_name)
DB_USER=$(terraform output -raw db_username)
DB_PASSWORD=$(python3 -c "import json,sys; print(json.load(sys.stdin)['password'])" <<< "$DB_SECRET_JSON")
ECR_IMAGE="$REPO_URL:$TAG"

echo "→ Generando manifests con los valores actuales..."
envsubst < "$K8S_DIR/secret.yaml.tpl" > "$K8S_DIR/secret.yaml"
envsubst < "$K8S_DIR/deployment.yaml.tpl" > "$K8S_DIR/deployment.yaml"

echo "→ Aplicando manifests..."
kubectl apply -f "$K8S_DIR/secret.yaml"
kubectl apply -f "$K8S_DIR/deployment.yaml"
kubectl apply -f "$K8S_DIR/service.yaml"

echo "✅ Desplegado. Esperando IP del LoadBalancer (puede tardar unos minutos):"
echo "   kubectl get svc catalog-service -w"
