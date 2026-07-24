# Infraestructura AWS — catalog-service

Terraform para desplegar `catalog-service` en AWS: RDS PostgreSQL + un
cluster EKS que corre el contenedor y lo expone por un Load Balancer.

Diseñado para una cuenta **AWS Academy Learner Lab** (perfil `universidad`):
no permite crear roles IAM propios, así que todo (cluster EKS, node group)
reusa el rol `LabRole` que el Lab ya trae preconfigurado. Se usa además la
VPC default de la cuenta (con subnets públicas ya existentes) en vez de crear
una VPC/NAT Gateway propios — evita ~$32/mes de NAT y simplifica el setup.

El perfil de AWS a usar está fijado en el propio Terraform
(`variable "aws_profile"`, default `"universidad"`, ver `versions.tf` /
`variables.tf`) — no depende de que exportes `AWS_PROFILE` en la shell. Los
scripts (`build_and_push.sh`, `deploy_k8s.sh`) leen ese mismo perfil desde
`terraform output -raw aws_profile`, así que todo el flujo usa siempre el
mismo perfil de forma consistente. Si tu perfil se llama distinto, cambia el
default o pásalo con `-var="aws_profile=otro"`.

## Arquitectura

```
Internet → Service LoadBalancer (ELB, :8081) → EKS Node Group (1x t3.small)
                                                     │ pod catalog-service
                                                     ▼
                                    RDS PostgreSQL db.t3.micro (privada)
```

## Prerrequisitos

- [Terraform](https://developer.hashicorp.com/terraform/install) >= 1.6
- AWS CLI con el perfil `universidad` creado en `~/.aws/credentials` /
  `~/.aws/config` (credenciales de AWS Academy) — no hace falta exportar
  `AWS_PROFILE`, Terraform y los scripts ya usan ese perfil por default.
- `kubectl`
- `docker`
- `envsubst` (paquete `gettext` — en Mac: `brew install gettext && brew link --force gettext`)
- `python3` (usado por `deploy_k8s.sh` para parsear el secreto de RDS)

La sesión de AWS Academy **expira** (normalmente a las 4h). Si un comando
falla con error de credenciales, vuelve al portal de Academy, arranca el Lab
de nuevo y copia las credenciales nuevas a `~/.aws/credentials` (perfil
`universidad`) antes de reintentar.

## 1. Provisionar la infraestructura

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

No hay ninguna password que escribir ni exportar: la RDS se crea con
`manage_master_user_password = true`, así que es **AWS quien genera la
master password y la guarda directamente en Secrets Manager** (secreto
`rds!db-...`, gestionado y rotable por AWS). Terraform nunca ve el valor en
texto plano, no queda en el `.tfstate`, y no hay ningún prompt interactivo.

`apply` tarda **~10–15 minutos** (la creación del cluster EKS es lo que más
demora). Al terminar, Terraform habrá creado:

- Repositorio ECR para la imagen (`catalog-service`)
- Cluster EKS + node group de 1 nodo `t3.small`
- Instancia RDS PostgreSQL `db.t3.micro` (privada, sólo alcanzable desde el
  cluster) con su master password en Secrets Manager

## 2. Build + push de la imagen

```bash
./scripts/build_and_push.sh
```

Construye `catalog-service/Dockerfile` y publica la imagen en el ECR recién
creado (tag `latest` por defecto; pasa otro tag como primer argumento si
quieres versionar).

## 3. Desplegar en el cluster

```bash
./scripts/deploy_k8s.sh
```

Este script:

1. Configura `kubectl` contra el cluster (`aws eks update-kubeconfig`).
2. Lee la master password desde Secrets Manager (`aws secretsmanager
   get-secret-value` sobre el ARN expuesto en el output `db_secret_arn`) —
   no se pide ni se pasa a mano en ningún momento.
3. Rellena `k8s/secret.yaml` y `k8s/deployment.yaml` a partir de los outputs
   de Terraform (host/puerto/usuario/nombre de la RDS + esa password) y de
   la imagen recién pusheada.
4. Aplica `secret.yaml`, `deployment.yaml` y `service.yaml`.

Las migraciones Flyway (`catalog-service/src/main/resources/db/migration`)
corren automáticamente al arrancar el pod, contra la RDS.

## 4. Verificar

```bash
kubectl get pods -w                      # esperar 1/1 Running
kubectl get svc catalog-service -w       # esperar EXTERNAL-IP (unos minutos)
curl http://<EXTERNAL-IP>:8081/actuator/health
```

Actualiza la variable `baseUrl` del Postman collection
(`../catalog-service-collection.postman_collection.json`, hoy apunta a
`http://localhost:8081`) con esa IP/host para probar el resto de endpoints.

Si el pod queda en `CrashLoopBackOff`: `kubectl logs deploy/catalog-service`
— casi siempre es que la RDS todavía no aceptaba conexiones o el security
group no está bien enlazado (revisa `terraform apply` haya terminado sin
errores antes del deploy).

## Costos aproximados (cuenta con presupuesto limitado)

| Recurso | Costo aprox. |
|---|---|
| EKS control plane | ~$0.10/h (~$73/mes) — fijo, dominante |
| Nodo `t3.small` | ~$0.021/h |
| RDS `db.t3.micro` | ~$0.017/h |
| Load Balancer (ELB) | ~$0.025/h + tráfico |

El control plane de EKS es el costo dominante y es fijo sin importar el
tamaño del nodo — por eso vale la pena **destruir todo apenas termine la
demo/entrega**.

## Destruir (orden importa)

El `Service` de Kubernetes crea un Elastic Load Balancer que **Terraform no
gestiona** (lo crea el controlador in-tree de AWS al aplicar el manifest).
Si no lo borras antes de `terraform destroy`, el ELB queda huérfano,
bloquea el borrado de las subnets/security groups del cluster, y **sigue
cobrando**.

```bash
kubectl delete -f k8s/service.yaml   # borra el ELB primero
terraform destroy
```

El secreto de Secrets Manager que crea `manage_master_user_password` lo borra
AWS automáticamente al eliminar la instancia RDS — no requiere limpieza manual.

## Variables (`variables.tf`)

| Variable | Default | Descripción |
|---|---|---|
| `aws_region` | `us-east-1` | Región AWS |
| `project` | `catalog` | Prefijo de nombres |
| `environment` | `dev` | Sufijo de nombres |
| `cluster_version` | `1.33` | Versión de Kubernetes en EKS |
| `node_instance_type` | `t3.small` | Tipo de instancia del node group |
| `db_name` | `catalogdb` | Base de datos creada en RDS |
| `db_username` | `catalogadmin` | Usuario master de RDS (`catalog` es palabra reservada en Postgres/RDS) |

No existe variable `db_password` — ver la nota sobre `manage_master_user_password`
más arriba. Ver `terraform.tfvars.example` para una plantilla del resto.
