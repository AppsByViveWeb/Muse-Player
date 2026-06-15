# 🎵 Muse Player - Player de Música MP3 Minimalista

> 🚀 **DOWNLOAD DIRETO DO APK:** O aplicativo já foi compilado! Baixe o instalador `.apk` diretamente da pasta do repositório:
> **👉 [`/releases/app-debug.apk`](./releases/app-debug.apk)** 👈

**Muse Player** é um reprodutor de música MP3 elegante, fluído e moderno desenvolvido nativamente para Android utilizando Kotlin e **Jetpack Compose**. Projetado sob os conceitos mais refinados do **Material Design 3 (Elegant Dark)**, o aplicativo oferece uma experiência imersiva com suporte completo a playlists locais e um equalizador de áudio de alta precisão integrado.

---

## ✨ Recursos Principais

### 🎨 Design Minimalista "Elegant Dark"
- **Paleta Atemporal:** Fundo em obsidian profundo (`#1C1B1F`) e superfícies slate (`#25232A`) com contrastes em azul glacial moderno.
- **Micro-interações Suaves:** Animações dinâmicas de ondas sonoras e barras de visualização saltitantes responsivas ao estado de reprodução de áudio. Letras grandes e limpas que trazem conforto visual em ambientes escuros.

### 🎧 Áudio & Performance
- **Equalizador Físico Nativo:** Configuração direta de 5 bandas integradas via hardware do aparelho ou emulação precisa por software.
- **Presets Profissionais:** Suporte a perfis de áudio clássicos (como *Rock*, *Pop*, *Jazz*, *Clássica*, *Reforço de Graves* e *Flat*).
- **Formatos:** Otimizado para reprodução de arquivos MP3, tanto locais quanto remotos via URLs seguras de streaming.
- **Modos de Reprodução:** Funções de Shuffle (Misturar), Repeat (Repetir Lista) e Loop de uma única faixa.

### 🗄️ Persistência de Dados (Local Room DB)
- **Fila Dinâmica & Histórico:** Banco de dados integrado com o framework **Room** e arquitetura reativa por meio de `Flow` do Kotlin.
- **Playlists Customizadas:** Crie, exclua e configure suas playlists favoritas adicionando arquivos diretamente na lista principal.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Kotlin
- **Interface Gráfica (UI):** Jetpack Compose (Declarativo)
- **Componentes visuais:** Material Design 3 (M3)
- **Serviço de Banco de Dados:** Room Database (SQLite Engine)
- **Player Engine:** MediaPlayer & Equalizer nativos da API do Android
- **Gerenciamento de Arquivos/Fluxo:** Coroutines & StateFlow (Arquitetura reativa MVVM)

---

## 📦 Como gerar ou obter o APK (.apk) do projeto?

O código do repositório pode ser compilado para produzir o instalador diretamente seguindo estas instruções:

### 1. Construir o APK usando a plataforma AI Studio
Caso você esteja visualizando e testando o aplicativo através do ambiente virtual do **Google AI Studio Build**:
- Abra o menu principal no canto superior superior direito ou o menu de **Configurações/Exportar**.
- Clique na opção correspondente em **Generate APK / AAB** ou **Build APK**.
- O sistema gerará o arquivo e oferecerá um botão de download para instalar diretamente no seu smartphone ou emulador pessoal.

### 2. Compilar localmente via Android Studio ou Terminal
Se você baixou os códigos ou clonou o repositório do GitHub em seu computador local:
1. Abra o projeto no **Android Studio (Ladybug ou superior)**.
2. Certifique-se de que o SDK e o Gradle sincronizem com sucesso.
3. No painel superior, selecione a opção `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
4. O arquivo gerado estará localizado no seguinte endereço interno do seu diretório:
   ```bash
   app/build/outputs/apk/debug/app-debug.apk
   ```
5. Alternativamente, você pode usar a linha de comando do terminal executando:
   ```bash
   gradle assembleDebug
   ```

---

## 🚀 Requisitos Mínimos

- **Android SDK:** Mínimo API 24 (Android 7.0 Nougat) ou superior.
- **Conectividade:** Permissão de internet declarada no manifesto para streaming de testes online de MP3.

Desenvolvido com carinho para oferecer fidelidade de reprodução sonora e design minimalista em uma única tela. 🖤🎶
