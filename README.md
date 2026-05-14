# Bracketbow

IntelliJ tabanlı IDE'ler için Rainbow Brackets pluginine ücretsiz ve açık kaynaklı bir alternatif. Parantezleri ( ), [ ], { } iç içe geçme seviyesine göre renklendirir.

## Gereksinimler

- **JDK 17+** (JDK 21 önerilir)
- **IntelliJ IDEA** Community (ücretsiz) veya Ultimate
- İlk Gradle çalıştırması için internet (~500 MB indirme)

## Hızlı Başlangıç

```bash
# Projeyi IntelliJ'de aç (File → Open → bu klasör)
# Gradle senkronizasyonunu bekle, sonra:

./gradlew runIde      # Plugin'in yüklü olduğu sandbox IDE'yi başlatır
./gradlew buildPlugin # Dağıtılabilir .zip üretir
```

Üretilen `build/distributions/bracketbow-0.1.0.zip` dosyasını gerçek IDE'de:
`Settings → Plugins → ⚙ → Install Plugin from Disk...` ile yükleyebilirsin.

> Windows'taysan `gradlew.bat` kullan: `gradlew.bat runIde`

## Mimari

Kod tek bir devasa sınıf yerine, sorumluluğa göre paketlere ayrılmış:

```
src/main/kotlin/com/bracketbow/
├── psi/
│   ├── BracketDetector.kt    ← Bir eleman parantez mi? (saf mantık)
│   └── DepthCalculator.kt    ← İç içe geçme derinliği ne? (saf mantık)
├── colors/
│   ├── BracketbowColors.kt              ← Renk seviyeleri tanımı
│   └── BracketbowColorSettingsPage.kt   ← IDE Settings sayfası
└── annotator/
    └── BracketbowAnnotator.kt           ← İnce koordinatör
```

Bu yapının faydası: her dosya bir tek şey yapıyor. `psi/` paketindeki sınıflar IntelliJ API'sine ihtiyaç duymaktan başka bir bağımlılığı yok ve kolayca birim test edilebilir. Annotator sadece bu parçaları birleştiriyor.

### Veri akışı

```
IntelliJ                 BracketbowAnnotator       psi/                     colors/
  │                              │                        │                         │
  │── annotate(element) ────────►│                        │                         │
  │                              │── isBracketLeaf() ────►│                         │
  │                              │◄── true/false ─────────│                         │
  │                              │── depthOf(element) ───►│                         │
  │                              │◄── int ────────────────│                         │
  │                              │── forDepth(depth) ───────────────────────────────►│
  │                              │◄── TextAttributesKey ────────────────────────────│
  │◄── annotation ───────────────│                        │                         │
```

### Derinlik nasıl hesaplanır?

`((()))` örneği üzerinden, en içteki `(` karakteri için:

1. `BracketDetector.isBracketLeaf` → `true` (tek karakter, parantez)
2. `DepthCalculator.depthOf`:
   - parent #1: en içteki paren ifadesi → `isBracketGroup` true → derinlik 0
   - parent #2: ortadaki paren ifadesi → true → derinlik 1
   - parent #3: en dıştaki paren ifadesi → true → derinlik 2
   - parent #4: dosya/statement → false → dur
3. `BracketbowColors.forDepth(2)` → `BRACKETBOW_LEVEL_2`
4. IntelliJ bu pozisyona ilgili rengi uygular

## Dosya Yapısı

```
bracketbow/
├── .gitignore
├── LICENSE                          ← MIT
├── README.md                        ← Bu dosya
├── CHANGELOG.md                     ← Sürüm geçmişi
├── build.gradle.kts                 ← Build konfigürasyonu
├── settings.gradle.kts
├── gradle.properties
├── gradlew                          ← Linux/macOS launcher
├── gradlew.bat                      ← Windows launcher
├── gradle/wrapper/                  ← Gradle wrapper
└── src/main/
    ├── kotlin/com/bracketbow/
    │   ├── psi/                     ← Saf PSI mantığı
    │   ├── colors/                  ← Renk yönetimi
    │   └── annotator/               ← IDE entegrasyonu
    └── resources/
        ├── META-INF/                ← Plugin manifest'leri
        └── colorSchemes/            ← Varsayılan renkler
```

## Geliştirme

### Sandbox IDE'de canlı test

```bash
./gradlew runIde
```

Yeni bir IntelliJ penceresi açılır. İçinde plugin yüklüdür. Bir `.kt` veya `.java` dosyası aç, parantezleri renkli görmelisin.

### Plugin'i derle ve paketle

```bash
./gradlew buildPlugin
```

Çıktı: `build/distributions/bracketbow-0.1.0.zip`

### Plugin'i resmi olarak doğrula

```bash
./gradlew verifyPlugin
```

JetBrains'in IDE uyumluluk kontrolünü çalıştırır.

## Yapılacaklar

[CHANGELOG.md](CHANGELOG.md) dosyasındaki "Planlanan" bölümüne bak.

## Lisans

MIT — [LICENSE](LICENSE) dosyasına bak.
