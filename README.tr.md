# Bracketbow

IntelliJ tabanlı IDE'ler için Rainbow Brackets pluginine ücretsiz ve açık kaynaklı bir alternatif.
Parantezleri `( )`, `[ ]`, `{ }` iç içe geçme seviyesine göre renklendirir.

→ [English README](README.md)

## Gereksinimler

- **JDK 17+** (JDK 21 önerilir)
- **IntelliJ IDEA** Community (ücretsiz) veya Ultimate
- İlk Gradle çalıştırması için internet bağlantısı (~500 MB indirme)

## Hızlı Başlangıç

```bash
# Projeyi IntelliJ'de aç (File → Open → bu klasör)
# Gradle senkronizasyonunu bekle, sonra:

./gradlew runIde      # Plugin'in yüklü olduğu sandbox IDE'yi başlatır
./gradlew buildPlugin # Dağıtılabilir .zip üretir
```

Üretilen `build/distributions/bracketbow-0.1.0.zip` dosyasını gerçek IDE'de:
`Settings → Plugins → ⚙ → Install Plugin from Disk...` ile yükleyebilirsin.

> Windows'taysan `gradlew.bat runIde` kullan.

## Ayarlar

`Settings → Tools → Bracketbow`

- **Etkinleştir/devre dışı bırak** — plugin'i tamamen kapatır
- **Renk seviyesi sayısı** — kaç seviyeden sonra renklerin döngüye gireceği (3–10)
- **Parantez türleri** — yuvarlak `( )`, köşeli `[ ]`, süslü `{ }` ayrı ayrı açılıp kapatılabilir
- **Diller** — dil bazında etkinleştirme (Java, Kotlin, XML, HTML, JSON)
- **Renkleri düzenle** — `Settings → Editor → Color Scheme → Bracketbow` sayfasına gider

## Mimari

Kod sorumluluğa göre paketlere ayrılmış:

```
src/main/kotlin/com/bracketbow/
├── psi/
│   ├── BracketDetector.kt    ← Bir eleman parantez mi? (saf mantık)
│   └── DepthCalculator.kt    ← İç içe geçme derinliği ne? (saf mantık)
├── colors/
│   ├── BracketbowColors.kt              ← Renk seviyeleri tanımı
│   └── BracketbowColorSettingsPage.kt   ← Renk şeması ayar sayfası
├── settings/
│   ├── BracketbowSettings.kt            ← PersistentStateComponent
│   └── BracketbowConfigurable.kt        ← Ayarlar UI'ı (Kotlin UI DSL v2)
└── annotator/
    └── BracketbowAnnotator.kt           ← İnce koordinatör
```

Her dosya tek bir şey yapıyor. `psi/` paketindeki sınıfların `PsiElement` dışında IntelliJ bağımlılığı yok; kolayca birim test edilebilir. Annotator sadece parçaları birleştiriyor.

### Veri akışı

```
IntelliJ              BracketbowAnnotator     psi/                  colors/
  │                          │                   │                      │
  │── annotate(element) ────►│                   │                      │
  │                          │── isBracketLeaf() ►│                      │
  │                          │◄── true/false ─────│                      │
  │                          │── depthOf(element) ►│                      │
  │                          │◄── int ─────────────│                      │
  │                          │── forDepth(depth) ───────────────────────►│
  │                          │◄── TextAttributesKey ────────────────────│
  │◄── annotation ───────────│                   │                      │
```

### Derinlik nasıl hesaplanır?

`((()))` örneğinde en içteki `(` için:

1. `BracketDetector.isBracketLeaf` → `true`
2. `DepthCalculator.depthOf`:
   - parent #1: en içteki paren ifadesi → `isBracketGroup` true → derinlik 0
   - parent #2: ortadaki paren ifadesi → true → derinlik 1
   - parent #3: en dıştaki paren ifadesi → true → derinlik 2
   - parent #4: dosya/statement → false → dur
3. `BracketbowColors.forDepth(2)` → `BRACKETBOW_LEVEL_2`
4. IntelliJ ilgili rengi uygular

## Geliştirme

```bash
./gradlew runIde       # Sandbox IDE'de canlı test
./gradlew buildPlugin  # Dağıtılabilir zip oluştur
./gradlew verifyPlugin # JetBrains uyumluluk kontrolü
./gradlew clean        # Gradle takılırsa temizle
```

> JDK 17 gereklidir. Aktif JDK daha yeniyse ve Gradle hata veriyorsa:
> `JAVA_HOME=/path/to/jdk17 ./gradlew ...`

## Lisans

MIT — [LICENSE](LICENSE) dosyasına bak.
