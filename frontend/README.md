# Ares Web

Frontend administrativo do Ares, construído com Next.js, React, TypeScript e Material UI.

## Executar

1. Copie `.env.example` para `.env.local` se o backend não estiver em `http://localhost:8080`.
2. Execute `npm install`.
3. Execute `npm run dev` e acesse `http://localhost:3000`.

As chamadas passam por uma camada BFF do Next.js. Os tokens JWT ficam em cookies `httpOnly` e não são expostos ao JavaScript do navegador.

## Whitelabel

A identidade inicial vem de `GET /api/v1/branding?slug=...`. Usuários autenticados podem ajustar nome, logo e cores em **Aparência**. Como a API atual não possui endpoint de atualização de branding, os ajustes feitos na tela ficam salvos no navegador; o botão de restauração volta aos dados cadastrados no tenant.
