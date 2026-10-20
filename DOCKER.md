# Docker: desenvolvimento e produção

Os ambientes usam arquivos Compose e nomes de projeto diferentes. Passe sempre o arquivo explicitamente para não iniciar o ambiente errado.

Se os dois ambientes estiverem ligados ao mesmo Docker, é esperado aparecer um container `api` de cada ambiente. Os prefixos dos projetos (`ares-api` e `ares`) os distinguem; não são duas APIs duplicadas dentro da mesma stack.

## Desenvolvimento local

O projeto `ares-api` mantém os volumes locais existentes. O arquivo `.env` contém a configuração local e não deve ser enviado ao Git.

```bash
cd ares-api
docker compose --project-name ares-api --env-file .env -f compose.dev.yaml up -d --build
docker compose --project-name ares-api --env-file .env -f compose.dev.yaml ps
docker compose --project-name ares-api --env-file .env -f compose.dev.yaml logs -f
```

Frontend: <http://localhost:3001>. API: <http://localhost:8080>.

## Produção

O projeto de produção mantém o nome `ares` para preservar a identidade dos containers e volumes já usados no servidor. Use apenas no host de produção e mantenha `.env.production` fora do Git.

```bash
cd ares-api
docker compose --project-name ares --env-file .env.production -f compose.production.yml up -d --build
docker compose --project-name ares --env-file .env.production -f compose.production.yml ps
docker compose --project-name ares --env-file .env.production -f compose.production.yml logs -f api web caddy
```

Não use `docker compose down -v` em produção: isso remove os volumes persistentes.
