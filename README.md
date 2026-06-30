# LyraApp

Jetpack Compose ile gelistirilmis, modern Android muzik streaming uygulamasi. MVI (Model-View-Intent) mimarisi, Hilt dependency injection, ExoPlayer (Media3) ses oynatimi, Room ile offline indirme ve Material Design 3 tasarim sistemi uzerine insa edilmistir.

## Icindekiler

- [Ozellikler](#ozellikler)
- [Mimari](#mimari)
- [Teknoloji Yigini](#teknoloji-yigini)
- [Proje Yapisi](#proje-yapisi)
- [Ekranlar ve Navigasyon](#ekranlar-ve-navigasyon)
- [Veri Akisi](#veri-akisi)
- [Kimlik Dogrulama](#kimlik-dogrulama)
- [Ses Oynatimi](#ses-oynatimi)
- [Reklam Gating (Ad-Gated Playback)](#reklam-gating-ad-gated-playback)
- [Offline Indirme](#offline-indirme)
- [Premium Uyelik ve Odeme](#premium-uyelik-ve-odeme)
- [Dependency Injection](#dependency-injection)
- [Tema Sistemi](#tema-sistemi)
- [API](#api)
- [Gereksinimler](#gereksinimler)
- [Kurulum](#kurulum)
- [Uygulamayi Calistirma](#uygulamayi-calistirma)
- [Dokumantasyon](#dokumantasyon)

## Ozellikler

- Telefon numarasi + OTP ile sifresiz kimlik dogrulama, ilk girişte profil tamamlama
- Ana sayfa: kisisellestirilmis karsilama, sizin icin oneriler, son calananlar, oneriler, indirilenler bolumu
- Muzik arama
- Kutuphane yonetimi (calisma listeleri, sanatcilar, albumler) — gercek API ile paylasilan playlist state'i
- Calisma listesi olusturma, sarki ekleme/cikarma, silme
- Begenilen sarkilar listesi (Favoriler sekmesi)
- Tam ekran muzik oynatici (NowPlaying) ve mini player
- Arka plan ses oynatimi (MediaSessionService ile foreground service), bildirim ve kilit ekrani kontrolleri
- Sunucu-otoriter reklam gating: free kullanicilar periyodik olarak reklam dinler, premium kullanicilar dinlemez
- Sarki indirme ve offline calma (Room veritabani)
- Premium uyelik: plan secimi, kart ile odeme, uyelik bitis uyarisi ve yenileme akisi
- Karanlik / Aydinlik tema destegi (DataStore ile kalici)
- Profil yonetimi

## Mimari

Proje **MVI (Model-View-Intent)** mimarisini takip eder. Her ekran uc temel bilesenden olusur:

| Bilesen | Sorumluluk |
|---------|-----------|
| **State** | Ekranin anlik durumunu temsil eden immutable data class |
| **Intent** | Kullanicinin UI uzerindeki eylemlerini temsil eden sealed class/interface |
| **Effect** | Navigasyon, snackbar gibi tek seferlik olaylari temsil eden sealed class/interface |

### Tek Yonlu Veri Akisi

```
Kullanici Etkilesimi
       |  onIntent(Intent)
       v
  ViewModel  ──(viewModelScope)──>  Repository (suspend / Flow)
       |                                  |
       |  _uiState.update { ... }         |  Result<...> / StateFlow
       v                                  v
  StateFlow<UiState>           Channel<Effect> (one-shot)
       |  collectAsStateWithLifecycle()   |  receiveAsFlow()
       v                                  v
  Screen (durumsuz composable)     Route -> LaunchedEffect
```

### Katman Sorumluluklari

| Katman | Yapabilecegi | Yapamayacagi |
|--------|-------------|-------------|
| **UI (Screen)** | Durumsuz cizim, Intent yayma | Is mantigi, state sahipligi |
| **UI (Route)** | ViewModel alma, state toplama, Effect tuketme | Is mantigi |
| **ViewModel** | Intent isleme, state uretme, Effect gonderme | Android/Context bagimliligi |
| **Repository** | Veri kaynagi soyutlamasi (ag/yerel) | UI/ViewModel bagimliligi |
| **DI Module** | Bagimlilik baglama (`@Binds`/`@Provides`) | Is mantigi |

Detayli MVI kurallari icin: [docs/architecture/mvi-overview.md](docs/architecture/mvi-overview.md). Referans implementasyon Login ekranidir (`ui/auth/login/`).

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
| Yerel Veritabani | Room (offline indirme) | 2.7.1 |
| Tercih Saklama | DataStore Preferences | 1.1.7 |
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
|   |   |-- login/                   # Telefon numarasi girisi
|   |   |-- otp/                     # OTP dogrulama
|   |   +-- completeprofile/         # Profil tamamlama (yalniz ilk girişte)
|   |-- home/                        # Ana sayfa
|   |-- search/                      # Arama ekrani
|   |-- library/                     # Kutuphane ekrani
|   |-- likedsongs/                  # Begenilen sarkilar (Favoriler sekmesi)
|   |-- profile/                     # Profil ekrani
|   |-- nowplaying/                  # Tam ekran oynatici
|   |-- playlistdetail/              # Calisma listesi detayi
|   |-- createplaylist/              # Calisma listesi olusturma
|   |-- premium/                     # Premium tanitim + PremiumSuccess
|   |-- payment/                     # Odeme ekrani
|   |-- player/                      # MiniPlayer + PlayerViewModel
|   |-- navigation/
|   |   |-- LyraNavHost.kt           # NavHost + Scaffold (tek giris noktasi)
|   |   |-- LyraDestination.kt       # Rota tanimlari (enum + route helper'lari)
|   |   +-- LyraBottomBar.kt         # Alt gezinme cubugu
|   |-- theme/                       # Material 3 tema (Color, Theme, Type)
|   +-- icons/                       # Ozel ikon seti
|
|-- data/                            # Veri katmani
|   |-- auth/                        # AuthRepository, RealAuthRepository, FakeAuthRepository,
|   |   |                              AuthTokenManager, UserSessionManager
|   |-- home/                        # HomeRepository + RemoteHomeRepository + MockHomeRepository
|   |-- search/                      # SearchRepository + MockSearchRepository
|   |-- library/                     # LibraryModels (UI-ozel model donusumleri)
|   |-- likedsongs/                  # LikedSongsRepository + MockLikedSongsRepository
|   |-- profile/                     # ProfileRepository + RealProfileRepository + MockProfileRepository
|   |-- playback/                    # PlaybackRepository + ExoPlayerPlaybackRepository
|   |   |                              + PlaybackService (MediaSessionService) + MockPlaybackRepository
|   |-- playlist/                    # PlaylistRepository (Singleton) + RealPlaylistRepository
|   |   |                              + MockPlaylistRepository — Library/CreatePlaylist/PlaylistDetail
|   |   |                              ekranlari tarafindan paylasilir
|   |-- playlistdetail/              # PlaylistDetailModels (UI-ozel model donusumleri)
|   |-- createplaylist/              # CreatePlaylistModels (UI-ozel model donusumleri)
|   |-- membership/                  # MembershipRepository + RealMembershipRepository
|   |-- download/                    # DownloadRepository + RealDownloadRepository, Room entity/DAO/DB
|   |-- preferences/                 # ThemePreferenceRepository + DataStore impl.
|   +-- remote/
|       |-- SongApiService.kt        # Sarki katalogu
|       |-- HomeApiService.kt        # Ana sayfa veri kaynaklari
|       |-- AuthApiService.kt        # OTP + profil + me
|       |-- MembershipApiService.kt  # Uyelik planlari + odeme
|       |-- PlaybackApiService.kt    # playback/next + ad-complete
|       |-- PlaylistApiService.kt    # Calisma listesi CRUD
|       |-- AuthInterceptor.kt       # Bearer token enjeksiyonu (OkHttp interceptor)
|       +-- dto/                     # Tum API DTO'lari (Song, Home, Auth, Membership, Playback, Playlist)
|
+-- di/                              # Hilt DI modulleri
    |-- NetworkModule.kt             # Retrofit, OkHttp, Moshi, tum ApiService'ler
    |-- AuthModule.kt
    |-- HomeModule.kt
    |-- PlaybackModule.kt
    |-- PlaylistModule.kt
    |-- MembershipModule.kt
    |-- DownloadModule.kt
    |-- DatabaseModule.kt            # Room database + DAO
    |-- ProfileModule.kt
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
  |-- <Feature>Models.kt        # Domain/UI modelleri
  |-- <Feature>Repository.kt    # Interface
  +-- Real/Mock<Feature>Repository.kt  # Implementasyon(lar)

di/
  +-- <Feature>Module.kt        # @Module @InstallIn(SingletonComponent)
```

## Ekranlar ve Navigasyon

Tek `NavHost` + tek dis `Scaffold` yaklasimi kullanilir (`ui/navigation/LyraNavHost.kt`).

| Rota | Tanim | Bottom Bar |
|------|-------|-----------|
| `login` | Telefon numarasi girisi (baslangic destinasyonu) | Gizli |
| `otp?phoneNumber={..}&firstTime={..}` | OTP dogrulama | Gizli |
| `completeprofile?phoneNumber={..}` | Profil tamamlama (yalniz ilk girişte) | Gizli |
| `home` | Ana sayfa | Gorunur |
| `search` | Arama | Gorunur |
| `library` | Kutuphane | Gorunur |
| `favorites` | Begenilen sarkilar | Gorunur |
| `profile` | Profil | Gorunur |
| `playlistdetail/{playlistId}` | Calisma listesi detayi | Gizli |
| `nowplaying` | Tam ekran oynatici | Gizli |
| `createplaylist` | Calisma listesi olusturma | Gizli |
| `premium` | Premium tanitim ve plan secimi | Gizli |
| `payment?planType={..}` | Odeme ekrani | Gizli |
| `premiumsuccess?durationDays={..}` | Odeme basarisi | Gizli |

### Navigasyon Kurallari

- Navigasyon ViewModel icinden **dogrudan tetiklenmez**. `Intent -> Effect` akisi ile gerceklesir.
- Route composable'i `LaunchedEffect` ile Effect'i tuketir ve `NavController` cagrisini yapar.
- Sekme gecisi: `popUpTo(Home) { saveState = true }` + `launchSingleTop` + `restoreState`.
- Kimlik dogrulama sonrasi: Back stack `popUpTo(Login) { inclusive = true }` ile temizlenir.

### Ekran Ozetleri

**Kimlik Dogrulama**
- **Login**: Telefon numarasi girisi, OTP gonderimi tetiklenir.
- **Otp**: 6 haneli OTP dogrulama. Basarili dogrulamada `firstTime` true ise CompleteProfile'a, degilse Home'a yonlendirilir.
- **CompleteProfile**: Ad, soyad ve dogum tarihi ile profil tamamlama (yalniz ilk kayitta).

**Ana Ekranlar (Bottom Navigation)**
- **Home**: Gercek API ile beslenen "Sizin Icin", "Son Calananlar", "Oneriler" bolumleri; yerel Room'dan okunan "Indirilenler" bolumu; premium bitis uyari popup'i.
- **Search**: Arama cubugu ve sonuc listesi.
- **Library**: Calisma listeleri / Sanatcilar / Albumler sekmeleri, yeni calisma listesi olusturma ve begenilen sarkilara erisim.
- **Favorites**: Begenilen sarkilar listesi.
- **Profile**: Kullanici bilgileri, premium durum karti, tema degistirme ve cikis yapma.

**Detay Ekranlari**
- **PlaylistDetail**: Secilen calisma listesinin sarki listesi; sahiplik kontrolu ile "Cikar"/"Sarki ekle" aksiyonlari.
- **NowPlaying**: Tam ekran muzik oynatici — sarki bilgisi, gradient kapak, ilerleme cubugu, oynatma kontrolleri, indirme butonu, reklam modu.
- **CreatePlaylist**: Yeni calisma listesi olusturma formu.
- **Premium**: API'den dinamik yuklenen plan listesi, plan secimi.
- **Payment**: Kart bilgisi formu (canli maskeli onizleme), odeme tamamlama.
- **PremiumSuccess**: Odeme sonrasi basari ekrani.

**Mini Player**: Herhangi bir sarki caldikda ekranin altinda (bottom bar'in ustunde) gorunen kompakt oynatici. Tiklayinca NowPlaying ekranina gider.

## Veri Akisi

### Repository Pattern

Her feature icin bir `interface` ve bir veya birden fazla implementasyon bulunur:

| Feature | Interface | Gercek Implementasyon | Mock |
|---------|----------|----------------------|------|
| Auth | `AuthRepository` | `RealAuthRepository` | `FakeAuthRepository` |
| Home | `HomeRepository` | `RemoteHomeRepository` | `MockHomeRepository` |
| Playback | `PlaybackRepository` | `ExoPlayerPlaybackRepository` | `MockPlaybackRepository` |
| Playlist | `PlaylistRepository` | `RealPlaylistRepository` | `MockPlaylistRepository` |
| Membership | `MembershipRepository` | `RealMembershipRepository` | - |
| Download | `DownloadRepository` | `RealDownloadRepository` | - |
| Profile | `ProfileRepository` | `RealProfileRepository` | `MockProfileRepository` |
| Theme | `ThemePreferenceRepository` | `DataStoreThemeRepository` | - |
| Search | `SearchRepository` | - | `MockSearchRepository` |
| Liked Songs | `LikedSongsRepository` | - | `MockLikedSongsRepository` |

Backend API hazir olmayan feature'lar icin stub/mock repository'ler kullanilir. Gercek API geldiginde yalnizca implementasyon ve DI baglama degisir; ViewModel ve Contract etkilenmez.

### Paylasilan State (Singleton Repository'ler)

Asagidaki repository'ler Singleton scope'tadir ve birden fazla ViewModel tarafindan dogrudan inject edilerek paylasilir; ViewModel'lar birbirleriyle hicbir zaman dogrudan haberlesmez:

- **PlaybackRepository**: `PlayerViewModel` (mini player) ve `NowPlayingViewModel`.
- **PlaylistRepository**: `LibraryViewModel`, `CreatePlaylistViewModel` ve `PlaylistDetailViewModel`. Olusturma/silme sonrasi `StateFlow<List<Playlist>>` otomatik yayilir; manuel refetch gerekmez.
- **ThemePreferenceRepository**: `MainActivity` ve `ProfileViewModel`.

## Kimlik Dogrulama

Sifre tabanli akis kaldirilmistir; yerine **telefon numarasi + OTP** tabanli sifresiz akis kullanilir.

1. **Login**: Telefon numarasi girilir, `POST /api/v1/auth/otp/request` cagrilir.
2. **Otp**: 6 haneli kod `POST /api/v1/auth/otp/verify` ile dogrulanir; access/refresh token `AuthTokenManager` (DataStore) ile saklanir.
3. **CompleteProfile**: Yalniz ilk kez giris yapan kullanicilar icin (`firstTime=true`) ad, soyad ve dogum tarihi `POST /api/v1/me/update-informations` ile gonderilir.

`AuthInterceptor` (OkHttp) her istekte `AuthTokenManager`'dan token okuyup `Authorization: Bearer <token>` header'i ekler; token yoksa header eklenmez.

## Ses Oynatimi

### ExoPlayer Entegrasyonu

`ExoPlayerPlaybackRepository` su islevleri saglar:

- **playSong(song) / loadAndPlay()**: Once yerel indirilmis dosya kontrol edilir; varsa yerelden, yoksa `playback/next` uzerinden alinan stream URL'i ile calinir.
- **pause() / resume()**: Oynatimi durdurur veya devam ettirir.
- **next() / previous()**: Kuyrukta ileri/geri gecer. `previous()` pozisyon > 3 saniye ise basa sarar.
- **seekTo(progress)**: 0-1 arasi normalize deger ile belirli pozisyona atlar.
- **toggleShuffle() / toggleRepeat() / toggleLike()**: Durum degistirme islemleri.

### Ilerleme Takibi

500ms araliklarla calisan coroutine ticker, `PlaybackState.progress` (0-1 float) ve `currentPositionLabel` ("M:SS" formati) degerlerini gunceller.

### Arka Plan Oynatimi

`PlaybackService` (`MediaSessionService`) foreground service olarak calisir:
- ExoPlayer `ForwardingPlayer` ile sarmalanarak `MediaSession` olusturulur.
- Bildirim, Media3'un dahili kontrolcusu yerine `Notification.MediaStyle` ile dogrudan yayinlanir (servis-player ayni anda baslatildiginda ilk playback event'lerinin kacirilmasini onlemek icin); oynatma basladiginda `ServiceCompat.startForeground()` cagirilir.
- Onceki/oynat-duraklat/sonraki/begeni aksiyonlari explicit `PendingIntent`'ler ile repository komutlarina delege edilir.
- Gradient album kapagi `MediaMetadata.artworkData` olarak set edilir (128x128 bitmap).
- `onTaskRemoved()`: Kullanici uygulamayi son uygulamalar ekranindan kapattiginda oynatma durdurulur, bildirim kaldirilir, servis sonlandirilir.

### Gerekli Izinler

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

`POST_NOTIFICATIONS` izni API 33+ icin `MainActivity`'de runtime olarak istenir.

## Reklam Gating (Ad-Gated Playback)

Sarki calma/gecis akisi `POST /api/v1/me/playback/next` endpoint'i uzerinden yurutulur; reklam gating tamamen sunucu tarafinda yonetilir.

- **Free kullanicilar**: Periyodik araliklarla (sayac sunucu tarafinda tutulur) `type: "ad"` yaniti alir.
- **Premium kullanicilar**: Daima `type: "song"` alir.
- **Offline calma**: Yerel dosyadan calindiginda reklam uygulanmaz.

Reklam akisi: `playback/next` -> `type: "ad"` ise `adStream.url` ile reklam calinir -> bitiminde `POST /api/v1/me/playback/ad-complete` cagrilir -> ardindan asil sarki `stream.url` ile calinir. Reklam sirasinda `next()`, `previous()`, `seekTo()` ve `playSong()` devre disidir (no-op); yalnizca play/pause aktif kalir. Mini player'da "Reklam" etiketi, NowPlaying'de "Reklam" overlay'i ve `AdInfo` (reklam basligi + reklam veren) gosterilir.

## Offline Indirme

Room veritabani + OkHttp coroutine indirici ile offline calma destegi saglanir.

- **DownloadedSongEntity**: Room entity (songId PK, title, artist, localPath, mimeType, fileSize, downloadedAt).
- **RealDownloadRepository**: Stream URL mint edilir -> OkHttp ile bayt bayt indirilir (ilerleme bildirilir) -> app-specific `filesDir/downloads/` altina kaydedilir -> Room'a yazilir. Yarim kalan indirmeler `.tmp` uzantisiyla yazilir, tamamlaninca rename edilir.
- **NowPlayingScreen**: 4 durumlu indirme butonu (ikon / `CircularProgressIndicator` / onay ikonu / hata ikonu).
- **Home**: "Indirilenler" bolumu, Room'dan reaktif `Flow` ile okunur; silinmis dosyalarin Room kayitlari otomatik temizlenir.
- **ExoPlayerPlaybackRepository**: Calma oncesi yerel dosya kontrolu yapar; dosya varsa yerel URI'den, yoksa stream URL'den calar.

Dosyalar app-specific storage'a kaydedildigi icin ek depolama izni gerekmez.

## Premium Uyelik ve Odeme

Premium tanitim, plan secimi, odeme ve uyelik bitis uyarisi uctan uca akis olarak uygulanmistir.

- **Premium**: Plan listesi `GET /api/v1/memberships/plans` ile dinamik yuklenir (hardcode fiyat yoktur). Secili plan radio ile vurgulanir.
- **Payment**: Kart bilgisi formu (16 haneli numara, isim, AA/YY, CVC validasyonu) ve canli maskeli kart onizlemesi. `POST /api/v1/memberships/checkout` cagrilir. Basarida `PremiumSuccess` ekranina, 402 (odeme reddi) durumunda hata mesaji ile ayni ekranda kalinir.
- **Profile**: Gradient premium kart — free kullanicida "Premium'a gec", premium kullanicida "N gun kaldi" gosterir.
- **Home**: Uyelik durumu `GET /api/v1/me` ile kontrol edilir; kalan sure <= 3 gun ve bugun gosterilmediyse `PremiumExpiryDialog` popup'i tetiklenir (gosterim kontrolu SharedPreferences ile gun bazli yapilir).

## Dependency Injection

Hilt ile **KSP** (kapt degil) annotation processing kullanilir.

### Modul Yapisi

Tum moduller `@InstallIn(SingletonComponent::class)` ile singleton scope'a kurulur:

- **NetworkModule**: `Retrofit`, `OkHttpClient`, `Moshi` ve tum `ApiService` arayuzlerini saglar (`@Provides`).
- **DatabaseModule**: Room `LyraDatabase` ve `DownloadedSongDao` saglar.
- **Feature Modulleri** (Auth, Home, Playback, Playlist, Membership, Download, Profile vb.): Repository interface'ini implementasyona baglar (`@Binds`).

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

CDN/gorsel servisi henuz mevcut olmadigi icin kapak gorselleri gradient renk ciftleri (`artworkStartColor` / `artworkEndColor`) olarak temsil edilir; sarki ID'sinin hash'i sabit bir renk paletine modüler bolunerek deterministik atanir. UI bu degerleri kullanarak dinamik gradient olusturur.

## API

### Base URL

```
https://streaming-api.halitkalayci.com/
```

### Endpoint'ler

| Servis | Metot | Yol | Aciklama |
|--------|-------|-----|---------|
| Song | `GET` | `/api/v1/songs` | Sarki listesi (cursor tabanli sayfalama: `limit`, `cursor`) |
| Song | `GET` | `/api/v1/songs/{id}/stream-url` | Sarki icin imzali stream URL'i (premium-only; free tier 403) |
| Auth | `POST` | `/api/v1/auth/otp/request` | OTP kodu gonderimi |
| Auth | `POST` | `/api/v1/auth/otp/verify` | OTP dogrulama, token donusu |
| Auth | `POST` | `/api/v1/me/update-informations` | Profil tamamlama (ad/soyad/dogum tarihi) |
| Auth | `GET` | `/api/v1/me` | Mevcut kullanici + uyelik durumu |
| Home | `GET` | `/api/v1/me/recently-played` | Son calinan sarkilar (`limit`) |
| Home | `GET` | `/api/v1/me/for-you` | Sizin icin oneriler (`limit`) |
| Home | `GET` | `/api/v1/me/recommendations` | Genel oneriler (`limit`) |
| Home | `POST` | `/api/v1/me/plays` | Calma kaydi (playback/next aktif oldugunda kullanilmaz) |
| Playback | `POST` | `/api/v1/me/playback/next` | Siradaki sarki/reklam — sunucu-otoriter reklam gating |
| Playback | `POST` | `/api/v1/me/playback/ad-complete` | Reklam tamamlanma bildirimi |
| Membership | `GET` | `/api/v1/memberships/plans` | Uyelik plan listesi (public) |
| Membership | `POST` | `/api/v1/memberships/checkout` | Kart ile odeme |
| Playlist | `GET` | `/api/v1/me/playlists` | Kullanicinin calisma listeleri |
| Playlist | `POST` | `/api/v1/me/playlists` | Yeni calisma listesi olusturma |
| Playlist | `DELETE` | `/api/v1/me/playlists/{id}` | Calisma listesi silme |
| Playlist | `POST` | `/api/v1/me/playlists/{id}/tracks` | Calisma listesine sarki ekleme |
| Playlist | `DELETE` | `/api/v1/me/playlists/{id}/tracks/{songId}` | Calisma listesinden sarki cikarma |
| Playlist | `GET` | `/api/v1/playlists/{id}` | Calisma listesi detayi + sarkilar (public) |

Tum kimlik gerektiren istekler `AuthInterceptor` tarafindan eklenen `Authorization: Bearer <token>` header'i ile yapilir. Tam sema icin [docs/api/openapi.json](docs/api/openapi.json) referans alinmalidir.

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

## Uygulamayi Calistirma

Uygulama, yerel bir backend kurulumu gerektirmez; dogrudan canli test ortamina (`https://streaming-api.halitkalayci.com/`) baglanir. Cihazda/emulatorde internet erisimi yeterlidir.

### 1) Ilk Acilis ve Giris

1. Uygulama ilk acildiginda **Login** ekrani gelir. Herhangi bir telefon numarasi girilebilir (orn. `+905551112233`) — backend OTP sistemi mock'tur, gercek SMS gonderilmez.
2. **Otp** ekraninda asagidaki kodlardan biri her zaman gecerlidir; baska bir kod girilirse dogrulama 401 ile reddedilir:

   ```
   280600   260702   250506
   101000   346134   123456
   ```

3. Telefon numarasi backend'de daha once kayitli degilse veya profili eksikse (`firstTime: true`) **CompleteProfile** ekranina yonlendirilir; ad, soyad ve dogum tarihi girilerek **Home**'a gecilir. Aksi halde dogrudan Home'a gecilir.
4. Android 13 (API 33) ve uzerinde ilk acilista bildirim izni (`POST_NOTIFICATIONS`) istenir; muzik calma bildirimlerinin gorunmesi icin onaylanmalidir.

### 2) Muzik Dinleme

- Home, Search veya Library ekranlarindan bir sarkiya dokunarak calma baslatilir; ekranin altinda mini player belirir.
- Free (premium olmayan) hesaplarda backend her 3 sarkida bir reklam dondurur (server-authoritative gating); reklam sirasinda yalniz oynat/duraklat kullanilabilir.
- NowPlaying ekranindaki indirme butonu ile sarki yerel depolamaya indirilip offline calinabilir.

### 3) Premium Akisini Deneme

1. **Profile** ekranindaki premium kartindan veya Home'daki uyelik bitis uyarisindan **Premium** ekranina gidilir, bir plan secilir.
2. **Payment** ekraninda kart bilgileri girilir. Backend odeme sistemi de mock'tur ve sonuc tamamen kart numarasina gore belirlenir:

   | Kart Numarasi | Sonuc |
   |----------------|-------|
   | `4242 4242 4242 4242` | Onaylanir, uyelik aktif olur |
   | `4000 0000 0000 0002` | Reddedilir (402 hatasi) |

   Listelenenler disindaki her kart numarasi da reddedilir. Isim, son kullanma tarihi (AA/YY) ve CVC alanlari formati gecerli olacak sekilde herhangi bir deger ile doldurulabilir.
3. Basarili odeme sonrasi **PremiumSuccess** ekranina, ardindan Home'a yonlendirilir.

> **Not**: Yukaridaki OTP kodlari ve test kart numaralari yalniz bu test/gelistirme backend'ine ozeldir; kaynagi [docs/api/openapi.json](docs/api/openapi.json) dosyasidir.

## Dokumantasyon

Projedeki ek dokumantasyon dosyalari:

- [agents.md](agents.md) — Projede calisan herkesin uymak zorunda oldugu genel kurallar
- [docs/decisions.md](docs/decisions.md) — Tum mimarisel ve teknik karar gecmisi
- [docs/architecture/mvi-overview.md](docs/architecture/mvi-overview.md) — MVI mimarisi genel bakis
- [docs/architecture/mvi-contracts.md](docs/architecture/mvi-contracts.md) — MVI sozlesme kurallari
- [docs/architecture/mvi-viewmodel-rules.md](docs/architecture/mvi-viewmodel-rules.md) — ViewModel kurallari
- [docs/design/00-color-system.md](docs/design/00-color-system.md) — Renk sistemi tasarimi
- [docs/api/openapi.json](docs/api/openapi.json) — API sema tanimi
