# Implantação do Ares em uma VPS com Docker

Esta configuração sobe quatro contêineres:

- `postgres`: banco de dados persistente e sem porta pública;
- `api`: Spring Boot/Flyway, acessível somente pela rede Docker;
- `web`: Next.js em modo `standalone`, acessível somente pela rede Docker;
- `caddy`: único serviço público, responsável por HTTPS e proxy para o frontend
  e para a API/Swagger.

## 1. Preparar domínio e VPS

Antes de subir os contêineres:

1. Crie registros DNS `A` para os domínios do frontend e da API, ambos apontando
   para o IPv4 da VPS (por exemplo, `app.seudominio.com.br` e
   `api.seudominio.com.br`).
2. Crie um registro `AAAA` somente se a VPS estiver corretamente configurada para IPv6.
3. Libere no firewall as portas TCP `22`, `80` e `443`, além da UDP `443` para HTTP/3.
4. Não libere as portas `5432`, `8080` ou `3000`.

Uma organização simples na VPS é:

```text
/opt/ares/
├── ares-api/
└── aresweb/
```

Clone ou copie os dois projetos como pastas irmãs. O arquivo
`compose.production.yml`, que fica em `ares-api`, usa `../aresweb` como contexto
de build do frontend.

Para enviar pelo terminal da sua máquina, crie primeiro `/opt/ares` na VPS:

```bash
sudo mkdir -p /opt/ares
sudo chown -R "$USER":"$USER" /opt/ares
```

Depois, na máquina onde estão os projetos, envie somente os fontes necessários:

```bash
rsync -az \
  --exclude=.git \
  --exclude=node_modules \
  --exclude=.next \
  --exclude=target \
  --exclude=.env \
  --exclude=.env.production \
  ares-api aresweb USUARIO@IP_DA_VPS:/opt/ares/
```

Também é possível usar FileZilla/SFTP, mantendo exatamente os nomes das duas
pastas e sem enviar `node_modules`, `.next`, `target` ou arquivos `.env` locais.

## 2. Criar as variáveis de produção

Na VPS:

```bash
cd /opt/ares/ares-api
cp .env.production.example .env.production
chmod 600 .env.production
```

Gere valores inéditos para o banco e para o JWT:

```bash
openssl rand -hex 32
openssl rand -base64 48 | tr -d '\n'
```

Edite `.env.production` e coloque, respectivamente, o primeiro resultado em
`POSTGRES_PASSWORD` e o segundo em `JWT_SECRET_BASE64`. Preencha também domínio,
domínio da API, e-mail, identificação do responsável pelos dados e configurações
do cupom.

Não reutilize as credenciais mostradas anteriormente em telas, mensagens ou
commits. Valores `NEXT_PUBLIC_*` ficam visíveis no JavaScript do navegador e não
podem conter segredos.

## 3. Validar e fazer o primeiro deploy

Valide a interpolação das variáveis antes do build:

```bash
docker compose --env-file .env.production -f compose.production.yml config --quiet
```

Construa e inicie tudo:

```bash
docker compose --env-file .env.production -f compose.production.yml up -d --build
```

Confira o estado e os logs:

```bash
docker compose --env-file .env.production -f compose.production.yml ps
docker compose --env-file .env.production -f compose.production.yml logs --tail=200 api web caddy
```

O Postgres precisa ficar saudável antes da API. Na primeira inicialização, o
Flyway cria/atualiza o esquema; depois a API fica saudável, o frontend inicia e
o Caddy emite o certificado HTTPS. A emissão depende de o DNS já apontar para a
VPS e de as portas `80` e `443` estarem acessíveis pela internet.

Depois que todos os serviços estiverem saudáveis, verifique:

```text
https://app.aresapp.tech
https://api.aresapp.tech/actuator/health
https://api.aresapp.tech/swagger-ui/index.html
```

## 4. Publicar uma atualização

Depois de atualizar os dois repositórios na VPS, execute:

```bash
cd /opt/ares/ares-api
docker compose --env-file .env.production -f compose.production.yml up -d --build
docker compose --env-file .env.production -f compose.production.yml ps
```

Quando qualquer valor `NEXT_PUBLIC_*` mudar, é necessário reconstruir o
frontend. O comando acima já faz isso quando o contexto ou argumentos de build
mudarem.

Para acompanhar uma falha em tempo real:

```bash
docker compose --env-file .env.production -f compose.production.yml logs -f --tail=200 api
```

## 5. Backup do banco

Crie uma pasta protegida e faça um dump lógico periódico:

```bash
cd /opt/ares/ares-api
mkdir -p backups
chmod 700 backups
docker compose --env-file .env.production -f compose.production.yml exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > "backups/ares-$(date +%F-%H%M%S).sql"
```

Copie esses backups para outro servidor ou armazenamento. Um backup mantido
somente na mesma VPS não protege contra perda do disco ou da própria VPS.

Nunca execute `docker compose down -v` em produção: a opção `-v` remove o volume
do PostgreSQL. Um `docker compose down` sem `-v` mantém os dados, mas interrompe
o sistema.

## 6. Observações sobre credenciais e migração

- As variáveis `POSTGRES_*` da imagem oficial só criam banco, usuário e senha
  quando o volume está vazio. Alterar `POSTGRES_PASSWORD` no arquivo depois não
  troca automaticamente a senha de um banco já inicializado.
- Para migrar dados existentes do EasyPanel, gere um `pg_dump` no banco antigo e
  restaure-o no novo banco antes de liberar o domínio. Não crie um banco vazio
  por cima da única cópia existente.
- Guarde `.env.production` somente na VPS e em um gerenciador de senhas. O arquivo
  está ignorado pelo Git.
- Para cobrança real, mantenha a simulação desligada e configure as credenciais
  do provedor de pagamento quando essa integração existir.

## 7. Deploy automático pelo GitHub Actions

Existem workflows nos dois repositórios:

- `ares-api/.github/workflows/deploy-vps.yml` é executado ao enviar alterações
  para a branch `development`;
- `aresweb/.github/workflows/deploy-vps.yml` é executado ao enviar alterações
  para a branch `develop`.

Os dois workflows baixam API e frontend, validam as imagens, enviam uma release
completa para `root@2.25.121.228` e executam o mesmo Compose. O PostgreSQL não é
recriado: seus dados permanecem no volume nomeado `ares_postgres_data`.

### Preparação única da VPS

Conecte-se à VPS e crie o arquivo de ambiente compartilhado:

```bash
ssh root@2.25.121.228
mkdir -p /opt/ares/shared /opt/ares/releases
nano /opt/ares/shared/.env.production
chmod 600 /opt/ares/shared/.env.production
```

Use como conteúdo o modelo `.env.production.example`, preenchendo senhas e
domínios reais. Esse arquivo fica apenas na VPS e é reutilizado por todas as
releases.

Se o EasyPanel/Traefik ainda estiver usando as portas `80` e `443`, encerre essa
publicação antes do primeiro deploy do Caddy. Os dois proxies não conseguem
escutar as mesmas portas ao mesmo tempo.

### Chave SSH do GitHub Actions

Em uma máquina confiável, gere uma chave exclusiva para o deploy:

```bash
ssh-keygen -t ed25519 -C "github-actions-ares" -f ./ares_github_actions -N ""
ssh-copy-id -i ./ares_github_actions.pub root@2.25.121.228
ssh-keyscan -H 2.25.121.228 > ./ares_known_hosts
base64 < ./ares_github_actions | tr -d '\n' > ./ares_github_actions.b64
```

Não adicione nenhum desses arquivos ao Git. Guarde a chave privada em local
seguro.

### Secrets dos dois repositórios

Nos repositórios `ares-api` e `aresweb`, abra **Settings → Environments**, crie
o ambiente `production` e adicione os mesmos secrets:

| Secret | Conteúdo |
| --- | --- |
| `VPS_SSH_PRIVATE_KEY_B64` | Conteúdo de `ares_github_actions.b64` |
| `VPS_KNOWN_HOSTS` | Conteúdo completo do arquivo `ares_known_hosts` |
| `CROSS_REPOSITORY_TOKEN` | Fine-grained PAT com `Contents: Read` nos repositórios `ares-api` e `aresweb` |

O token de repositório cruzado é necessário porque cada workflow baixa o outro
repositório privado. Não conceda permissão de escrita a esse token.

A chave privada é armazenada em Base64 para preservar exatamente as quebras de
linha do arquivo OpenSSH. O workflow decodifica e valida a chave com
`ssh-keygen` antes de tentar a conexão.

Antes do primeiro push, deixe a automação desativada. Depois que os arquivos
estiverem enviados aos dois repositórios e os secrets estiverem configurados,
abra **Settings → Secrets and variables → Actions → Variables** em cada
repositório e crie:

```text
DEPLOY_ENABLED=true
```

Execute o primeiro deploy manualmente em **Actions → Deploy production VPS →
Run workflow**. Depois disso, um `push` para `development` na API ou `develop` no
frontend executará o deploy completo. Para pausar implantações sem apagar o
workflow, altere a variável para `false`.

O script remoto usa `flock`, quando disponível, para impedir dois deploys
simultâneos. As releases ficam em `/opt/ares/releases` e o link
`/opt/ares/current` aponta para a última implantação concluída.
