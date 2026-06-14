# 🦂 X Downloader — AkrepIndirici v2.2

**Twitter / X Video İndirici** — Android uygulaması

## Özellikler

- Twitter/X videolarını farklı kalitelerde indir (1080p, 720p, 480p, 360p)
- X (Twitter) hesabıyla WebView üzerinden giriş yapma
- İndirilen videoları galeri ekranında görüntüleme
- Video oynatıcı ekranı
- Pano (clipboard) otomatik URL algılama
- Arka plan indirme servisi (ForegroundService)
- AMOLED siyah + turuncu modern tema

## Teknik Detaylar

| Özellik | Değer |
|---|---|
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Dil | Java |
| Build Sistemi | Gradle 8.4 |
| Namespace | `com.akrep.xdownloader` |

## Kullanılan Kütüphaneler

- **OkHttp 4.12** — HTTP istekleri ve video URL çözümleme
- **Gson 2.10** — JSON ayrıştırma
- **Glide 4.16** — Thumbnail yükleme
- **Lottie 6.3** — Animasyonlar
- **WorkManager 2.9** — Arka plan indirme
- **AndroidX Lifecycle** — ViewModel & LiveData

## Kurulum

1. Android Studio'da projeyi açın
2. `gradlew assembleDebug` ile APK derleyin
3. Cihaza yükleyin

## GitHub Actions

APK otomatik olarak her `push` işleminde derlenir.  
`Actions` sekmesinden APK artifact'ini indirebilirsiniz.

---

*🦂 AkrepIndirici — 2050'nin Uygulaması*
