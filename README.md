# LyraApp

Jetpack Compose ile gelistirilmis, modern Android muzik streaming uygulamasi. MVI (Model-View-Intent) mimarisi, Hilt dependency injection, ExoPlayer ses oynatimi ve Material Design 3 tasarim sistemi uzerine insa edilmistir.

## Icindekiler

- [Ozellikler](#ozellikler)
- [Mimari](#mimari)
- [Teknoloji Yigini](#teknoloji-yigini)
- [Proje Yapisi](#proje-yapisi)
- [Ekranlar](#ekranlar)
- [Veri Akisi](#veri-akisi)
- [Navigasyon](#navigasyon)
- [Ses Oynatimi](#ses-oynatimi)
- [Dependency Injection](#dependency-injection)
- [Tema Sistemi](#tema-sistemi)
- [API](#api)
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)

## Ozellikler

- Kullanici girisi ve kayit (telefon numarasi + sifre)
- Ana sayfa: kisisellestirilmis karsilama, hizli secimler, son calananlar, onerileri calisma listeleri
- Muzik arama
- Kutuphane yonetimi (calisma listeleri, sanatcilar, albumler)
- Begenilen sarkilar listesi
- Tam ekran muzik oynatici (NowPlaying) ve mini player
- Arka plan ses oynatimi (MediaSessionService ile foreground service)
- Bildirim ve kilit ekrani kontrolleri (play/pause, onceki, sonraki)
- Calisma listesi olusturma
- Karanlik / Aydinlik tema destegi (DataStore ile kalici)
- Profil yonetimi

## Mimari

Proje **MVI (Model-View-Intent)** mimarisini takip eder. Her ekran uc temel bilesenden olusur:

| Bilesen | Sorumluluk |
|---------|-----------|
| **State** | Ekranin anlik durumunu temsil eden immutable data class |
| **Intent** | Kullanicinin UI uzerindeki eylemlerini temsil eden sealed class |
| **Effect** | Navigasyon, snackbar gibi tek seferlik olaylari temsil eden sealed class |

### Tek Yonlu Veri Akisi

```
Kullanici Etkilesimi
       |  onIntent(Intent)
       v
  ViewModel  ──(viewModelScope)──>  Repository (suspend)
       |                                  |
       |  _uiState.update { ... }         |  Result<...>
       v                                  v
  StateFlow<UiState>           Channel<Effect> (one-shot)
       |  collectAsStateWithLifecycle()   |  receiveAsFlow()
       v                                  v
  Screen (durumsuz composable)     Route -> LaunchedEffect
```

### Katman Sorumluliklari

| Katman | Yapabilecegi | Yapamayacagi |
|--------|-------------|-------------|
| **UI (Screen)** | Durumsuz cizim, Intent yayma | Is mantigi, state sahipligi |
| **UI (Route)** | ViewModel alma, state toplama, Effect tuketme | Is mantigi |
| **ViewModel** | Intent isleme, state uretme, Effect gonderme | Android/Context bagimliligi |
| **Repository** | Veri kaynagi soyutlamasi (ag/yerel) | UI/ViewModel bagimliligi |
| **DI Module** | Bagimlilik baglama (`@Binds`/`@Provides`) | Is mantigi |

Detayli MVI kurallari icin: [docs/architecture/mvi-overview.md](docs/architecture/mvi-overview.md)

## Teknoloji Yigini

| Kategori | Teknoloji | Surum |
|----------|-----------|-------|
| Dil | Kotlin | 2.2.10 |
| UI Framework | Jetpack Compose (Material 3) | BOM 2026.02.01 |
| Mimari | MVI (Model-View-Intent) | - |
| Dependency Injection | Hilt (KSP ile) | 2.59.2 |
| Navigasyon | Compose Navigation | 2.9.5 |
| Ag Katmani | Retrofit + Moshi + OkHttp | 2.11.0 / 1.15.2 / 4.12.0 |
| Medya Oynatici | ExoPlayer (Media3) | 1.5.1 |
| Medya Oturumu | Media3 Session | 1.5.1 |
| Veri Saklama | DataStore Preferences | 1.1.7 |
| Asenkron Islemler | Kotlin Coroutines | 1.11.0 |
| Build Sistemi | Gradle (Version Catalog) | AGP 9.2.1 |
| Annotation Processing | KSP | 2.3.2 |

## Proje Yapisi

```
app/src/main/java/com/turkcell/lyraapp/
|
|-- MainActivity.kt                 # Tek Activity, tema ve navigasyon yonetimi
|-- LyraApplication.kt              # @HiltAndroidApp giris noktasi
|
|-- ui/                              # Sunum katmani
|   |-- auth/
|   |   |-- login/                   # Giris ekrani (Contract, ViewModel, Screen)
|   |   +-- register/                # Kayit ekrani
|   |-- home/                        # Ana sayfa
|   |-- search/                      # Arama ekrani
|   |-- library/                     # Kutuphane ekrani
|   |-- likedsongs/                  # Begenilen sarkilar
|   |-- profile/                     # Profil ekrani
|   |-- nowplaying/                  # Tam ekran oynatici
|   |-- playlistdetail/              # Calisma listesi detayi
|   |-- createplaylist/              # Calisma listesi olusturma
|   |-- player/                      # MiniPlayer + PlayerViewModel
|   |-- navigation/
|   |   |-- LyraNavHost.kt           # NavHost + Scaffold (tek giris noktasi)
|   |   |-- LyraDestination.kt       # Rota tanimlari (enum)
|   |   +-- LyraBottomBar.kt         # Alt gezinme cubugu
|   |-- theme/                       # Material 3 tema (Color, Theme, Type)
|   +-- icons/                       # Ozel ikon seti
|
|-- data/                            # Veri katmani
|   |-- auth/                        # AuthRepository + FakeAuthRepository
|   |-- home/                        # HomeRepository + RemoteHomeRepository
|   |-- search/                      # SearchRepository + MockSearchRepository
|   |-- library/                     # LibraryRepository + MockLibraryRepository
|   |-- likedsongs/                  # LikedSongsRepository + MockLikedSongsRepository
|   |-- profile/                     # ProfileRepository + MockProfileRepository
|   |-- playback/                    # PlaybackRepository + ExoPlayerPlaybackRepository
|   |   |                              + PlaybackService (MediaSessionService)
|   |   +                              + MockPlaybackRepository
|   |-- playlistdetail/              # PlaylistDetailRepository + Mock
|   |-- createplaylist/              # CreatePlaylistRepository + Mock
|   |-- preferences/                 # ThemePreferenceRepository + DataStore impl.
|   +-- remote/
|       |-- SongApiService.kt        # Retrofit API arayuzu
|       +-- dto/SongDto.kt           # API veri transfer nesneleri
|
+-- di/                              # Hilt DI modulleri
    |-- NetworkModule.kt             # Retrofit, OkHttp, Moshi
    |-- AuthModule.kt
    |-- HomeModule.kt
    |-- PlaybackModule.kt
    |-- ThemePreferenceModule.kt
    +-- ...                          # Her feature icin ayri modul
```

### Her Ekranin Dosya Yapisi

Yeni bir ekran eklendiginde asagidaki yapi takip edilir:

```
ui/<feature>/
  |-- <Screen>Contract.kt       # UiState + Intent + Effect (tek dosya)
  |-- <Screen>ViewModel.kt      # @HiltViewModel
  +-- <Screen>Screen.kt         # Route (stateful) + Screen (stateless)

data/<feature>/
  |-- <Feature>Models.kt        # Domain modelleri
  |-- <Feature>Repository.kt    # Interface
  +-- <Impl>Repository.kt       # Implementasyon

di/
  +-- <Feature>Module.kt        # @Module @InstallIn(SingletonComponent)
```

## Ekranlar

### Kimlik Dogrulama

- **Login**: Telefon numarasi ve sifre ile giris. Basarili giriste Home ekranina yonlendirilir, back stack temizlenir.
- **Register**: Ad, soyad, telefon ve sifre ile kayit. Kayit sonrasi Home ekranina gecis.

### Ana Ekranlar (Bottom Navigation)

- **Home**: Gun vaktine gore kisisellestirilmis karsilama, hizli secimler (grid), son calananlar, onerilen calisma listeleri, tema degistirme butonu.
- **Search**: Arama cubugu ve sonuc listesi.
- **Library**: Calisma listeleri / Sanatcilar / Albumler sekmeleri, yeni calisma listesi olusturma ve begenilen sarkilara erisim.
- **Favorites**: Begenilen sarkilar listesi (Library'den de erisilebilir).
- **Profile**: Kullanici bilgileri, tema degistirme ve cikis yapma.

### Detay Ekranlari

- **PlaylistDetail**: Secilen calisma listesinin sarki listesi (`playlistId` path argumenti ile).
- **NowPlaying**: Tam ekran muzik oynatici — sarki bilgisi, gradient kapak, ilerleme cubugu, oynatma kontrolleri.
- **CreatePlaylist**: Yeni calisma listesi olusturma formu.

### Mini Player

Herhangi bir sarki caldikda ekranin altinda (bottom bar'in ustunde) gorunen kompakt oynatici. Tiklayinca NowPlaying ekranina gider. Play/pause kontrolu icerir.

## Veri Akisi

### Repository Pattern

Her feature icin bir `interface` ve bir `implementasyon` bulunur:

| Feature | Interface | Gercek Implementasyon | Mock |
|---------|----------|----------------------|------|
| Auth | `AuthRepository` | - | `FakeAuthRepository` |
| Home | `HomeRepository` | `RemoteHomeRepository` | `MockHomeRepository` |
| Playback | `PlaybackRepository` | `ExoPlayerPlaybackRepository` | `MockPlaybackRepository` |
| Theme | `ThemePreferenceRepository` | `DataStoreThemeRepository` | - |
| Search | `SearchRepository` | - | `MockSearchRepository` |
| Library | `LibraryRepository` | - | `MockLibraryRepository` |
| Liked Songs | `LikedSongsRepository` | - | `MockLikedSongsRepository` |
| Profile | `ProfileRepository` | - | `MockProfileRepository` |
| Playlist Detail | `PlaylistDetailRepository` | - | `MockPlaylistDetailRepository` |
| Create Playlist | `CreatePlaylistRepository` | - | `MockCreatePlaylistRepository` |

Backend API hazir olmayan feature'lar icin stub/mock repository'ler kullanilir. Gercek API geldiginde yalnizca implementasyon ve DI baglama degisir; ViewModel ve Contract etkilenmez.

### Paylasilan State

`PlaybackRepository` Singleton olarak tum uygulamada paylasilir. `PlayerViewModel` (mini player) ve `NowPlayingViewModel` ayni repository instance'ini inject eder — iki ViewModel birbirleriyle dogrudan haberlesemez, tek dogruluk kaynagi repository'dir.

## Navigasyon

Tek `NavHost` + tek dis `Scaffold` yaklasimi kullanilir.

### Rotalar

| Rota | Tanim | Bottom Bar |
|------|-------|-----------|
| `login` | Giris ekrani | Gizli |
| `register` | Kayit ekrani | Gizli |
| `home` | Ana sayfa | Gorunur |
| `search` | Arama | Gorunur |
| `library` | Kutuphane | Gorunur |
| `favorites` | Begenilen sarkilar | Gorunur |
| `profile` | Profil | Gorunur |
| `playlistdetail/{playlistId}` | Calisma listesi detayi | Gizli |
| `nowplaying` | Tam ekran oynatici | Gizli |
| `createplaylist` | Calisma listesi olusturma | Gizli |

### Navigasyon Kurallari

- Navigasyon ViewModel icinden **dogrudan tetiklenmez**. `Intent -> Effect` akisi ile gerceklesir.
- Route composable'i `LaunchedEffect` ile Effect'i tuketir ve `NavController` cagirisini yapar.
- Sekme gecisi: `popUpTo(Home) { saveState = true }` + `launchSingleTop` + `restoreState`.
- Auth sonrasi: Back stack tamamen temizlenir.

## Ses Oynatimi

### ExoPlayer Entegrasyonu

`ExoPlayerPlaybackRepository` su islevleri saglar:

- **playSong(song)**: Stream URL'i API'den alinir, ExoPlayer'a yuklenir ve calmaya baslar.
- **pause() / resume()**: Oynatimi durdurur veya devam ettirir.
- **next() / previous()**: Kuyrukta ileri/geri gecer. `previous()` pozisyon > 3 saniye ise basa sarar.
- **seekTo(progress)**: 0-1 arasi normalize deger ile belirli pozisyona atlar.
- **toggleShuffle() / toggleRepeat() / toggleLike()**: Durum degistirme islemleri.

### Ilerleme Takibi

500ms aralikla calisan coroutine ticker, `PlaybackState.progress` (0-1 float) ve `currentPositionLabel` ("M:SS" formati) degerlerini gunceller.

### Arka Plan Oynatimi

`PlaybackService` (`MediaSessionService`) foreground service olarak calisir:
- ExoPlayer `ForwardingPlayer` ile sarmalanarak `MediaSession` olusturulur.
- Bildirim kontrolleri (play/pause, onceki, sonraki) Media3 tarafindan otomatik yonetilir.
- Gradient album kapagi `MediaMetadata.artworkData` olarak set edilir (128x128 bitmap).
- Bildirimden gelen next/previous komutlari repository'nin kuyruk yonetimine delege edilir.

### Gerekli Izinler

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

`POST_NOTIFICATIONS` izni API 33+ icin `MainActivity`'de runtime olarak istenir.

## Dependency Injection

Hilt ile **KSP** (kapt degil) annotation processing kullanilir.

### Modul Yapisi

Tum moduller `@InstallIn(SingletonComponent::class)` ile singleton scope'a kurulur:

- **NetworkModule**: `Retrofit`, `OkHttpClient`, `Moshi`, `SongApiService` saglar (`@Provides`).
- **Feature Modulleri** (Auth, Home, Playback vb.): Repository interface'ini implementasyona baglar (`@Binds`).

### Onemli Notlar

- `LyraApplication`: `@HiltAndroidApp` ile isaretlenir.
- `MainActivity`: `@AndroidEntryPoint` ile isaretlenir.
- ViewModel'ler Compose'a `hiltViewModel()` ile alinir.
- `PlayerViewModel` NavHost seviyesinde olusturulur ve Activity scope boyunca yasayarak tum sekmelerde ayni instance'i kullanir.
- `gradle.properties` icinde `android.disallowKotlinSourceSets=false` zorunludur (AGP 9 + KSP uyumu).

## Tema Sistemi

Material Design 3 renk sistemi, karanlik ve aydinlik tema destegi ile uygulanir.

### Karanlik Tema (Varsayilan)

- Primary: `#FFB1C8` (pembe)
- Secondary: `#E3BDC6` (leylak)
- Tertiary: `#EFBD94` (turuncu)
- Surface: `#191114` (koyu)

### Aydinlik Tema

- Primary: `#8F4A5F` (koyu mor)
- Secondary: `#74565F` (koyu leylak)
- Tertiary: `#7C5635` (kahverengi)
- Surface: `#FFF8F8` (beyaz)

### Tema Kaliciligi

Kullanicinin tema tercihi `DataStore Preferences` ile saklanir. `MainActivity` tema Flow'unu `collectAsStateWithLifecycle` ile toplar. Profil ekranindaki tema degisikligi aninda tum uygulamaya yansir.

### Artwork (Kapak Gorseli)

CDN/gorsel servisi henuz mevcut olmadigi icin kapak gorselleri gradient renk ciftleri (`artworkStartColor` / `artworkEndColor`) olarak temsil edilir. UI bu degerleri kullanarak dinamik gradient olusturur.

## API

### Base URL

```
https://streaming-api.halitkalayci.com/
```

### Endpoint'ler

| Metot | Yol | Aciklama |
|-------|-----|---------|
| `GET` | `/api/v1/songs` | Sarki listesi (cursor tabanli sayfalama: `limit`, `cursor`) |
| `GET` | `/api/v1/songs/{id}/stream-url` | Sarki icin imzali stream URL'i (TTL=300s) |

### Veri Modelleri (DTO)

```kotlin
data class SongDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAt: String
)

data class SongsResponseDto(
    val data: List<SongDto>,
    val nextCursor: String?
)

data class StreamUrlResponseDto(
    val url: String,
    val expiresAt: String,
    val mimeType: String
)
```

## Gereksinimler

- **Android Studio**: Ladybug veya ustu (AGP 9.2.1 destegi)
- **JDK**: 11
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Kotlin**: 2.2.10

## Kurulum

1. Repoyu klonlayin:
   ```bash
   git clone <repo-url>
   cd kotlin-lyraapp-gygy5
   ```

2. Android Studio ile projeyi acin.

3. Gradle sync isleminin tamamlanmasini bekleyin.

4. Uygulamayi bir emulator veya fiziksel cihazda calistirin:
   ```bash
   ./gradlew installDebug
   ```

> **Not**: `gradle.properties` icinde `android.disallowKotlinSourceSets=false` ayari mevcuttur ve AGP 9 + KSP uyumu icin zorunludur.

## Dokumantasyon

Projedeki ek dokumantasyon dosyalari:

- [docs/decisions.md](docs/decisions.md) — Tum mimarisel ve teknik karar gecmisi
- [docs/architecture/mvi-overview.md](docs/architecture/mvi-overview.md) — MVI mimarisi genel bakis
- [docs/architecture/mvi-contracts.md](docs/architecture/mvi-contracts.md) — MVI sozlesme kurallari
- [docs/architecture/mvi-viewmodel-rules.md](docs/architecture/mvi-viewmodel-rules.md) — ViewModel kurallari
- [docs/design/00-color-system.md](docs/design/00-color-system.md) — Renk sistemi tasarimi
- [docs/api/openapi.json](docs/api/openapi.json) — API sema tanimi
