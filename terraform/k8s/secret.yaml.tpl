apiVersion: v1
kind: Secret
metadata:
  name: catalog-service-db
type: Opaque
stringData:
  DB_HOST: "${DB_HOST}"
  DB_PORT: "${DB_PORT}"
  DB_NAME: "${DB_NAME}"
  DB_USER: "${DB_USER}"
  DB_PASSWORD: "${DB_PASSWORD}"
