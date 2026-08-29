#!/usr/bin/env bash
set -Eeuo pipefail

release_dir="${1:?Informe o diretório da release}"
env_file="${2:-/opt/ares/shared/.env.production}"
deploy_root="$(dirname "$(dirname "${release_dir}")")"
compose_file="${release_dir}/ares-api/compose.production.yml"

if [[ ! -f "${compose_file}" ]]; then
  echo "Compose não encontrado em ${compose_file}" >&2
  exit 1
fi

if [[ ! -s "${env_file}" ]]; then
  echo "Arquivo de produção ausente ou vazio: ${env_file}" >&2
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker não está instalado na VPS" >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose v2 não está disponível na VPS" >&2
  exit 1
fi

install -d -m 755 "${deploy_root}"
chmod 600 "${env_file}"

if command -v flock >/dev/null 2>&1; then
  exec 9>"${deploy_root}/.deploy.lock"
  flock -w 900 9 || {
    echo "Outro deploy ainda está em execução" >&2
    exit 1
  }
fi

docker compose \
  --env-file "${env_file}" \
  -f "${compose_file}" \
  config --quiet

docker compose \
  --env-file "${env_file}" \
  -f "${compose_file}" \
  pull postgres caddy

docker compose \
  --env-file "${env_file}" \
  -f "${compose_file}" \
  up -d --build --remove-orphans --wait --wait-timeout 300

ln -sfn "${release_dir}" "${deploy_root}/current"

docker compose \
  --env-file "${env_file}" \
  -f "${compose_file}" \
  ps
