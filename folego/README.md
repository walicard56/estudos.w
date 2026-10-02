# Fôlego — app Android

Controle financeiro para quem vive de renda variável: quanto sobra no mês, reserva de emergência e dívidas.
O app funciona **100% offline** e os dados ficam **só no aparelho**.

```
folego/
├── web/        o app em si (HTML/CSS/JS) — é a mesma base para a versão web (PWA) e para o Android
└── android/    projeto Android nativo que empacota o web/ dentro do APK/AAB
```

O app Android é um projeto nativo (Java + WebView), sem precisar hospedar nada na internet:
as telas de `web/` vão dentro do APK e são servidas localmente por
`https://appassets.androidplatform.net`. Altere `web/` e o próximo build do Android já inclui a mudança.

## O que mudou em relação ao PWA original

**Novidades**
- **Contas pagas no mês**: cada gasto fixo tem um ✓ para marcar como paga e o cabeçalho mostra "3/6 pagas". No mês seguinte as marcações começam zeradas.
- **Desfazer exclusão**: ao apagar um item ou dívida aparece "Desfazer" por alguns segundos (antes um toque errado no × apagava sem volta).
- **Backup e restauração** (Ajustes ⚙️): salva um arquivo `.json` com todos os dados e restaura depois. No Android, o arquivo vai para onde você escolher (Downloads, Google Drive…). Serve também para **levar os dados do PWA para o app Android**.
- **Guardar a sobra do mês** na Reserva com um toque, sem guardar a mesma sobra duas vezes.
- **Histórico da reserva**: últimas movimentações (guardei / usei / sobra de outubro).
- **Tema claro / escuro / automático** escolhido no app.
- Ao adicionar um item, o campo do nome já vem selecionado para digitar.
- Confirmações em janela própria do app no lugar do `confirm()` do navegador.

**Correções**
- As fontes (Inter e Space Grotesk) agora vão **dentro do app**: antes vinham do Google Fonts, então offline o visual quebrava e havia uma conexão externa. A política de privacidade foi atualizada.
- Referências a elementos da tela eram salvas junto com as dívidas (`_el`), sujando os dados salvos; agora não são mais (e o lixo antigo é limpo ao abrir).
- O Histórico não quebra mais (`NaN`) quando não há meses com dados.
- Os dados de exemplo eram pessoais (Claude Code, Rende.com, MEI…); agora são genéricos, adequados para publicar na loja.
- O service worker (versão web) passou a atualizar o cache em segundo plano: versões novas chegam sem precisar limpar dados.

**Específico do Android**
- Botão voltar: fecha janelas abertas → volta para a aba Mês → sai do app.
- Barras do sistema acompanham o tema; layout respeita câmera/entalhe, barra de navegação e teclado.
- Nenhuma permissão pedida. Links externos abrem no navegador.
- Backup automático do Android (Google) incluído para os dados do app.

## Instalar no celular para testar

1. Baixe o APK de teste:
   - aba **Actions** do GitHub → workflow **Fôlego Android** → última execução → artefato `folego-apk-teste`; ou
   - gere localmente (veja abaixo): `folego/android/app/build/outputs/apk/debug/app-debug.apk`.
2. Copie o `.apk` para o celular e abra. O Android vai pedir para permitir "instalar apps desconhecidos" — permita para o app que você usou para abrir o arquivo.

> Para trazer seus dados do PWA: publique a nova pasta `web/` **no mesmo endereço** onde o PWA já está (assim os dados continuam lá), abra-o → Ajustes ⚙️ → **Salvar backup**; depois, no app Android → Ajustes → **Restaurar backup**.

> O APK de teste é assinado com uma chave de desenvolvimento. Antes de instalar a versão da Play Store, faça um backup e desinstale o de teste (assinaturas diferentes não se atualizam por cima).

## Compilar localmente

Precisa de JDK 17+ e do Android SDK (o Android Studio instala os dois). Abra a pasta `folego/android` no Android Studio, ou pelo terminal:

```bash
cd folego/android
./gradlew assembleDebug      # APK de teste
./gradlew bundleRelease      # AAB para a Play Store (precisa da chave, abaixo)
```

## Publicar na Google Play

### 1. Criar a chave de assinatura (uma vez só)

```bash
keytool -genkeypair -v -keystore folego-upload.jks -alias folego \
  -keyalg RSA -keysize 2048 -validity 10000
```

**Guarde esse arquivo e as senhas em local seguro** (fora do Git). Sem eles você não consegue publicar atualizações.

### 2a. Gerar o AAB no GitHub (recomendado)

Em **Settings → Secrets and variables → Actions** do repositório, crie:

| Secret | Valor |
|---|---|
| `FOLEGO_KEYSTORE_BASE64` | o conteúdo de `base64 -w0 folego-upload.jks` |
| `FOLEGO_KEYSTORE_PASSWORD` | senha do keystore |
| `FOLEGO_KEY_ALIAS` | `folego` |
| `FOLEGO_KEY_PASSWORD` | senha da chave |

A cada push em `folego/` (ou em Actions → Run workflow), o artefato `folego-play-store` traz o `app-release.aab`.

### 2b. Ou gerar no computador

Crie `folego/android/keystore.properties` (já está no `.gitignore`):

```properties
storeFile=/caminho/para/folego-upload.jks
storePassword=...
keyAlias=folego
keyPassword=...
```

e rode `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`.

### 3. Enviar para a Play Console

1. Crie a conta em <https://play.google.com/console> (taxa única de US$ 25) e faça a verificação de identidade.
2. **Criar app** → nome Fôlego, idioma pt-BR, app gratuito.
3. Envie o `.aab` em um **teste fechado** primeiro. Contas pessoais novas precisam de 12 testadores usando o app por 14 dias antes de liberar produção.
4. Ficha da loja: ícone (`web/icon-512.png`), capturas de tela, descrição e o link da política de privacidade (`web/privacy.html` publicada em algum endereço, por exemplo GitHub Pages ou Netlify).
5. Em **Segurança dos dados**, declare que o app não coleta nem compartilha dados.

### Atualizações

A cada nova versão, aumente em `android/app/build.gradle.kts`:
`versionCode` (+1, obrigatório) e `versionName`; e `APP_VERSION` em `web/index.html`.
Na versão web, troque também o nome do cache em `web/sw.js`.

O identificador do app é `com.folego.financas` (`applicationId`). Se quiser outro, troque **antes** da primeira publicação — depois ele não pode mudar.

## Versão web (PWA)

A pasta `web/` continua sendo um PWA completo: basta hospedá-la (Netlify Drop, GitHub Pages, Vercel…) para usar no navegador e instalar pela opção "Adicionar à tela inicial".

## Licenças

Fontes Inter e Space Grotesk: SIL Open Font License 1.1 (textos em `web/fonts/`).
