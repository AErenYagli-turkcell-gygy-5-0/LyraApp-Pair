# decisions.md

> Projede verilen bütün mimarisel-teknik kararları ve karar geçmişini içeren dökümantasyondur.

---

### Dependency Injection Kütüphanesi

- Seçim\*: **Hilt**

- Son Güncelleme Tarihi\*: 04.06.2026

- Alternatifler: **Koin**

- Sebep: **Opsiyonel**

### Navigasyon

- Seçim: **Compose Navigation**

- Son Güncelleme Tarihi: 09.06.2026

- Bağımlılık: `androidx.navigation:navigation-compose` **2.9.5** (version catalog: `navigationCompose`).

- Uygulama: Tek `NavHost` (`ui/navigation/LyraNavHost.kt`) Auth grafiğini barındırır (başlangıç
  hedefi Login). Navigasyon MVI ile uyumlu kurulur: ViewModel'de navigasyon API'si yoktur
  (bkz. [architecture/mvi-viewmodel-rules.md](architecture/mvi-viewmodel-rules.md) §6); navigasyon
  `Intent → Effect` üzerinden akar, `Route` Effect'i tüketip `NavHost`'tan gelen lambda'ları çağırır.

### Sunum Katmanı Mimarisi

- Seçim: **MVI (Model-View-Intent)**

- Son Güncelleme Tarihi: 09.06.2026

- Kapsam: Her ekran State + Intent + Effect sözleşmesiyle yazılır. Detaylı kurallar ve
  referans implementasyon (Login) için bkz. [architecture/mvi-overview.md](architecture/mvi-overview.md).

- Sebep: Tek yönlü veri akışı, durumsuz UI, test edilebilirlik.

### Hilt Annotation Processing

- Seçim: **KSP** (kapt değil)

- Son Güncelleme Tarihi: 09.06.2026

- Sürümler: Hilt **2.59.2**, KSP **2.2.10-2.0.2** (Kotlin 2.2.10 ile birebir uyumlu).

- Compose'da ViewModel: `androidx.hilt:hilt-lifecycle-viewmodel-compose` (`hiltViewModel()`).
  Compose Navigation henüz kurulmadığından navigation-compose bağımlılığı eklenmemiştir.

- Sebep: KSP, kapt'a göre belirgin biçimde hızlıdır ve Kotlin 2.2 ile uyumludur.

### AGP 9 Built-in Kotlin + KSP Uyumu

- Karar: `gradle.properties` içinde **`android.disallowKotlinSourceSets=false`** zorunludur.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: AGP 9 built-in Kotlin kullanır; KSP'nin ürettiği kaynak dizinlerini eklemesi bu bayrak
  olmadan derlemeyi kırar. Bayrak deneysel (experimental) olarak işaretlidir ancak gereklidir.

### Alt Gezinme Çubuğu (Bottom Navigation Bar)

- Seçim: **Material 3 `NavigationBar`** — tek `NavHost` + iskelet seviyesinde tek dış `Scaffold`.

- Son Güncelleme Tarihi: 11.06.2026

- Uygulama: `ui/navigation/LyraBottomBar.kt` (bileşen + `LyraBottomBarTab` sekme tanımları) ve
  `ui/navigation/LyraNavHost.kt` (Scaffold `bottomBar` entegrasyonu). Çubuk yalnızca üst düzey
  sekme rotalarında görünür (Auth ekranlarında gizli); böylece her ana sayfanın altında otomatik
  yer alır. Sekme geçişi standart desenle yapılır: `popUpTo(Home) { saveState = true }` +
  `launchSingleTop` + `restoreState`. Dış Scaffold'ın `contentWindowInsets`'i sıfırdır; sistem
  çubuğu boşluklarını ekranlar kendisi yönetir, içerik dolgusu yalnızca alt çubuk yüksekliğini taşır.

- MVI kapsamı: BNB navigasyon iskeletidir (chrome), feature ekranı değildir; State/Intent/Effect
  sözleşmesi yoktur. Seçili sekme `currentBackStackEntryAsState()` ile nav back stack'ten türetilir
  (tek doğruluk kaynağı back stack'tir). Sekme ekranları MVI ile yazıldığında yalnızca
  `LyraNavHost` içindeki geçici placeholder rotaları gerçek `Route`'lara bağlanacaktır.

- Sebep: Tek doğruluk kaynağı (back stack) ile durum tekrarına yer bırakmaz; sekme başına ayrı
  `NavHost`/ViewModel karmaşıklığından kaçınılır; mevcut Auth grafiği değişmeden korunur.

### Backend Hazır Değilken Veri Katmanı

- Karar: **Stub repository** deseni — Repository interface + `Fake<X>Repository` implementasyonu.

- Son Güncelleme Tarihi: 09.06.2026

- Sebep: Backend REST API sözleşmesi tanımlı değil (`agents.md` §2.2 uydurmak yasak). Gerçek API
  geldiğinde yalnızca implementasyon ve DI bağlaması değişir; ViewModel/Contract etkilenmez.

### Arama Ekranı Veri Katmanı

- Karar: `data/search/SearchRepository` + `MockSearchRepository`. Tür listesi 8 öğe; gradyan renk
  çifti ile CDN URL yer tutucusu yapısı `data/home/HomeModels.kt` ile tutarlıdır.

- Son Güncelleme Tarihi: 13.06.2026

- Sebep: Arama sorgusu bu iterasyonda yalnızca yerel state'te tutulur (backend yok); ağ araması
  ileriki iterasyona bırakılır.

### Kütüphane Ekranı Veri Katmanı

- Karar: `data/library/LibraryRepository` + `MockLibraryRepository`. `LibraryPlaylist.isLikedSongs`
  bayrağı özel kapak renderlama (kalp ikonu + pembe gradient) için gereklidir; ayrı model yaratmak
  yerine flag tercih edildi çünkü tüm alanlar ortaktır.

- Son Güncelleme Tarihi: 13.06.2026

- Sebep: Sanatçılar ve Albümler sekmeleri bu iterasyonda backend gerektirmez; tab seçimi yalnızca
  yerel state değişikliği olarak uygulanır.

### Beğenilen Şarkılar Ekranı — Favoriler Sekmesi Olarak

- Karar: `LyraDestination.Favorites` rotası `LikedSongsRoute(onBack = {})` composable'ını render eder.
  Kütüphane'den "Beğenilen Şarkılar" tıklaması `LibraryEffect.NavigateToLikedSongs` üretir; NavHost
  bunu tüketerek `navigateToTab(Favorites)` çağırır.

- Son Güncelleme Tarihi: 13.06.2026

- Sebep: Tasarım, Favoriler sekmesi seçiliyken alt gezinme çubuğunun görünür olmasını ve Beğenilen
  Şarkılar içeriğinin gösterilmesini gerektiriyor. Ayrı `LikedSongs` rotası eklenmesi gereksiz bir
  back stack katmanı yaratırdı; `Favorites = LikedSongs` bağlamında tek doğruluk kaynağı korunur.

  `onBack = {}` no-op: Favoriler üst düzey sekmede geri navigasyona gerek yoktur. İleride detay
  ekranına taşınırsa bu bağlama özel bir `LikedSongs("liked_songs")` rotası eklenebilir.

### Tema Tercihi Kalıcı Saklama

- Karar: **DataStore Preferences** (`androidx.datastore:datastore-preferences:1.1.7`).
  `DataStoreThemeRepository` → `ThemePreferenceRepository` (Singleton) → `isDarkTheme: Flow<Boolean>`.

- Son Güncelleme Tarihi: 13.06.2026

- Uygulama: `MainActivity` DataStore Flow'unu `@Inject` + `collectAsStateWithLifecycle(initialValue = false)`
  ile toplar ve `LyraAppTheme(darkTheme = isDark)` çağırır. `ProfileViewModel` aynı Singleton'dan
  okur, `ProfileIntent.ThemeChanged` geldiğinde `setTheme()` yazar; Flow yayılmasıyla her iki
  tüketici de anında güncellenir.

- Sebep: SharedPreferences yerine DataStore seçildi; SharedPreferences bloklu IO, thread-unsafe ve
  Flow desteği yoktur. DataStore coroutine-native, Flow tabanlı ve null-safe'dir. `ViewModel`'de
  DataStore Session tutmak yerine Activity katmanında toplanmasının nedeni: tema uygulama geneli bir
  shell kararı olup UI katmanlarının ötesinde Activity lifecycle'a bağlıdır.

### Profil Ekranı Veri Katmanı

- Karar: `data/profile/ProfileRepository` + `MockProfileRepository`. `SettingItem.id` string anahtar
  ile ayar ikonu `ProfileScreen` içindeki `settingIconFor()` fonksiyonu tarafından çözülür; ikon
  bilgisi veri katmanına taşınmaz (UI kararı).

- Son Güncelleme Tarihi: 13.06.2026

- Sebep: Ayar navigasyonu bu iterasyonda kapsam dışı; `SettingClicked` Intent kaydedilmiş ancak
  Effect üretmiyor. Gerçek API geldiğinde navigasyon hedefleri Intent dallarına eklenir.

### Paylaşılan Oynatma State'i (Shared Playback State)

- Karar: `data/playback/PlaybackRepository` (Singleton) + `MockPlaybackRepository`. Repository
  `StateFlow<PlaybackState>` expose eder; `NowPlayingViewModel` ve `PlayerViewModel` (mini player)
  aynı Singleton instance'ı inject eder.

- Son Güncelleme Tarihi: 16.06.2026

- Uygulama: `ThemePreferenceRepository` Singleton pattern'i ile birebir aynı yaklaşım. `PlayerViewModel`
  `LyraNavHost` seviyesinde `hiltViewModel()` ile oluşturulur ve Activity scope boyunca yaşar; böylece
  tüm sekmelerde aynı instance kullanılır. `NowPlayingViewModel` ise kendi back stack entry'sine bağlıdır
  ancak aynı Singleton repository'den okur — iki ViewModel hiçbir zaman birbiriyle doğrudan haberleşmez.

- Sebep: `agents.md §2.2` gereği uydurma yasak; tek doğruluk kaynağı repository katmanında tutulur.
  ViewModel'lar arası doğrudan iletişim MVI sözleşmesini bozar.

### Mini Player Entegrasyonu

- Karar: `MiniPlayer` composable'ı `LyraNavHost` içindeki dış `Scaffold.bottomBar` bloğuna, `LyraBottomBar`'ın
  hemen üstüne yerleştirilir. `playerUiState.currentSong != null` koşuluna göre görünür/gizli olur;
  gizlendiğinde yer kaplamaz.

- Son Güncelleme Tarihi: 16.06.2026

- Uygulama: `bottomBar` içinde `Column { MiniPlayer(...); LyraBottomBar(...) }`. Mini player'a
  tıklanması `PlayerIntent.ExpandClicked` üretir; `PlayerEffect.OpenNowPlaying` NavHost'ta `LaunchedEffect`
  ile tüketilerek `NowPlaying` rotasına navigate edilir. Collapse (NowPlaying'deki aşağı ok) `popBackStack()`
  çağırır — mini player otomatik olarak yeniden görünür.

- Sebep: Mini player bir chrome bileşeni olup feature ekranı değildir; kendi MVI State/Intent/Effect
  sözleşmesi `PlayerContract.kt`'de tanımlıdır ancak bottom bar ile aynı layout katmanında yönetilir.

### Ag Katmani — Retrofit + Moshi

- Karar: **Retrofit 2.11.0** + **Moshi 1.15.2** (reflection adapter) + **OkHttp 4.12.0**.

- Son Güncelleme Tarihi: 18.06.2026

- Uygulama: `di/NetworkModule.kt` tek bir `@Singleton` `Retrofit` instance'ı sağlar; base URL
  `https://streaming-api.halitkalayci.com/`. `SongApiService` Retrofit arayüzü `GET api/v1/songs`
  endpoint'ini cursor tabanlı sayfalama ile tanımlar. JSON parse için `MoshiConverterFactory`
  kullanılır; Moshi `KotlinJsonAdapterFactory` ile Kotlin data class'larını reflection üzerinden
  çözer (KSP kod üretimi gerekmez). Log interceptor yalnızca debug amacıyla eklendi.

- DTO: `data/remote/dto/SongDto.kt` — API Song şeması ile birebir eşleşir. Artwork alanı API'da
  yoktur; `RemoteHomeRepository` her song ID'sinin hash'ini sabit bir renk paletine modüler
  bölerek deterministik gradient çifti atar (recompose'dan etkilenmez).

- Sebep: Retrofit ekosistemi Android'de olgun ve Hilt ile sorunsuz entegre olur. Moshi,
  Kotlin null-safety'yi Gson'dan daha iyi yönetir; codegen gerektirmez.

### Ses Oynatımı — ExoPlayer (Media3)

- Karar: **androidx.media3:media3-exoplayer 1.5.1** — `MockPlaybackRepository` yerine `ExoPlayerPlaybackRepository`.

- Son Güncelleme Tarihi: 18.06.2026

- Uygulama: `ExoPlayerPlaybackRepository` (`data/playback/`) `PlaybackRepository` arayüzünü implement eder;
  `PlaybackModule` bağlaması değiştirildi. `ExoPlayer` `Looper.getMainLooper()` ile oluşturulur (tüm
  player çağrıları main thread üzerinde yapılır, `withContext(Dispatchers.Main)` ile sarmalanır).
  Stream URL'i `GET /api/v1/songs/{id}/stream-url` endpoint'inden TTL=300s imzalı olarak alınır
  (her `playSong` çağrısında taze URL mint edilir, önbelleğe alınmaz). İlerleme durumu 500ms
  periyodik coroutine (`startProgressTicker`) ile `PlaybackState.progress` ve
  `currentPositionLabel` olarak yayınlanır. Kuyruğun sonuna gelince sıradaki şarkıya geçilir;
  repeat açıksa mevcut şarkı başa sarılır. `previous()` pozisyon > 3 saniye ise başa sarma,
  ≤ 3 saniye ise önceki şarkıya geçme davranışı sergiler.

- Bağımlılıklar: `media3-exoplayer:1.5.1`, `media3-common:1.5.1`.

- Sebep: `agents.md §2.2` gereği uydurma yasak; gerçek ses oynatımı için platform standardı olan
  Media3/ExoPlayer seçildi. Arayüz sabit kaldığından ViewModel ve UI katmanları etkilenmedi.

### Arka Plan Ses Oynatimi — MediaSessionService

- Karar: **Media3 MediaSessionService** ile foreground service + bildirim entegrasyonu.

- Son Guncelleme Tarihi: 24.06.2026

- Uygulama: `PlaybackService` (`data/playback/`) `MediaSessionService` extends eder ve `ExoPlayer`
  instance'inin sahibidir. Player servisin `onCreate()` metodunda olusturulur, repository'ye
  baglanir ve `onDestroy()` sirasinda repository'den ayrilarak serbest birakilir.
  `ExoPlayerPlaybackRepository`, stream URL ve `MediaMetadata` hazir olduktan sonra servisi
  baslatir; servis-player baglantisi hazir olana kadar sinirli sure bekler. `ForwardingPlayer`,
  bildirimden gelen next/previous komutlarini repository'nin kuyruk yonetimine delege eder.
  Bildirim Media3 tarafindan otomatik yonetilir (play/pause, onceki, sonraki kontrolleri).
  Begeni butonu `SessionCommand` ve `setMediaButtonPreferences()` ile bildirime eklenir; like
  state degistikce ikon guncellenir. Gradient album kapagi `MediaMetadata.artworkData` olarak
  set edilir (128x128 bitmap, repository tarafindan uretilir). `POST_NOTIFICATIONS` runtime
  izni `MainActivity` icinde istenir (API 33+).

- Bildirim guvenilirligi: Media3 1.5.1'in dahili bildirim controller'i, servis ile player ayni
  anda baslatildiginda ilk playback event'lerini kacirabildigi icin servis foreground'a
  gecemeyebilir. `PlaybackService`, platform `Notification.MediaStyle` bildirimini MediaSession
  token'i ile dogrudan yayinlar ve oynatma basladiginda `ServiceCompat.startForeground()` cagirir.
  Onceki, oynat/duraklat, sonraki ve begeni aksiyonlari explicit service `PendingIntent`'leri ile
  ayni repository komutlarina delege edilir. Boylece sistem mini player'i MediaSession metadata
  ve ilerleme bilgisini kullanmaya devam ederken foreground servis zaman asimi engellenir.

- Task kapatma davranisi: Kullanici LyraApp'i son uygulamalar ekranindan yukari kaydirarak
  kaldirdiginda `PlaybackService.onTaskRemoved()` oynaticiyi durdurur, media item'lari temizler,
  foreground bildirimini kaldirir ve servisi sonlandirir. Home ile uygulamayi yalnizca arka plana
  gondermek task'i kaldirmadigi icin oynatma ve bildirim devam eder.

- Bagimliliklar: `media3-session:1.5.1` (yeni), `FOREGROUND_SERVICE`,
  `FOREGROUND_SERVICE_MEDIA_PLAYBACK`, `POST_NOTIFICATIONS` izinleri.

- Sebep: Media3 `MediaSessionService` Android'in resmi arka plan ses oynatimi cozumudur; foreground
  service, bildirim ve kilit ekrani kontrollerini tek bir API ile yonetir. Player ve MediaSession'in
  ayni servis yasam dongusunde tutulmasi, arka plan oynatiminin Activity ve repository yasam
  dongulerinden bagimsiz olmasini ve kaynaklarin deterministik olarak serbest birakilmasini saglar.
  ViewModel ve UI katmanlari etkilenmedi.

### Yeni Ekranlar ve Rota Argumentleri

- Karar: `PlaylistDetail`, `NowPlaying`, `CreatePlaylist` rotaları `LyraDestination` enum'una eklendi.
  `PlaylistDetail` rotası `{playlistId}` path argument taşır; `NowPlaying` taşımaz (state PlaybackRepository'dedir).

- Son Güncelleme Tarihi: 16.06.2026

- Uygulama:
  - `"playlistdetail/{playlistId}"` → `navArgument("playlistId") { type = NavType.StringType }` →
    `PlaylistDetailViewModel.savedStateHandle["playlistId"]`.
  - `"nowplaying"` → PlaybackRepository'de şarkı zaten set edilmiş olur (navigate öncesi `playSong()` çağrılır).
  - `"createplaylist"` → Modal tam ekran, `popBackStack()` ile kapatılır.
  - Yardımcı fonksiyon: `fun playlistDetailRoute(playlistId: String) = "playlistdetail/$playlistId"`.

- Sebep: NowPlaying için argument gereksizdir çünkü PlaybackRepository Singleton'ı navigasyondan önce
  güncellenir; deep link gereksinimi bu iterasyonda kapsam dışıdır.

### Ana Sayfa — Gercek API Entegrasyonu

- Karar: Home ekrani 3 gercek endpoint ile beslenir: `GET /me/recently-played`, `GET /me/for-you`,
  `GET /me/recommendations`. Eski mock-only yapisi kaldirildi; `RemoteHomeRepository` 3 endpoint'i
  `coroutineScope { async {} }` ile paralel cagirir.

- Son Guncelleme Tarihi: 23.06.2026

- Uygulama:
  - `AuthInterceptor` (`data/remote/`) OkHttp interceptor'u olarak eklendi; `AuthTokenManager`'dan
    token okur ve `Authorization: Bearer <token>` header'i ekler. Token yoksa header eklenmez.
  - `HomeApiService` (`data/remote/`) Retrofit interface'i — 3 `/me/*` endpoint tanimlar.
  - `SongListResponseDto` (`data/remote/dto/HomeDto.kt`) — `{ data: List<SongDto> }` wrapper (cursor yok).
  - `HomeModels.kt` sadeleştirildi: `QuickPick`, `RecentlyPlayed`, `PlaylistForYou` → tek `HomeSong`
    domain modeli. 3 endpoint ayni Song semasini dondurdugu icin ayri model gereksizdi.
  - `HomeFeed` alanlari: `forYouSongs`, `recentlyPlayedSongs`, `recommendationSongs`. `userInitials`
    cikarildi (bu endpoint'lerden gelmiyor).
  - `HomeContract` guncellendi: `errorMessage: String?` (inline hata state'i), `SongClicked(song)` Intent.
  - `HomeScreen` loading/empty/error state composable'lari eklendi.
  - `MockHomeRepository` yeni model yapisina uygun mock verilerle korundu (preview/test icin).

- Sebep: Backend API (`docs/api/openapi.json`) hazir; `/me/*` endpoint'leri JWT Bearer token gerektirir.
  Tek domain modeli (`HomeSong`) API'nin 3 endpoint icin ayni Song semasini kullanmasini yansitir.

### Kimlik Dogrulama — Telefon + OTP Akisi

- Karar: Sifre tabanli login/register akisi kaldirildi; yerine **telefon numarasi + OTP** tabanli
  passwordless akis getirildi. 3 adimli flow: (1) Telefon numarasi girisi, (2) OTP dogrulama,
  (3) Bilgileri tamamla (yalnizca `firstTime` kullanicilari icin).

- Son Guncelleme Tarihi: 23.06.2026

- Uygulama:
  - `AuthRepository` interface'i yeniden tanimlandi: `requestOtp(phone)`, `verifyOtp(phone, code)`,
    `completeProfile(firstName, lastName, birthDate)`. Eski `login(phone, password)` ve `register(...)`
    metotlari kaldirildi.
  - `FakeAuthRepository` mock implementasyonu gecerli OTP kodlarini (`280600`, `260702`, `250506`,
    `101000`, `346134`, `123456`) ve `firstTime` mantigini taklit eder.
  - `AuthTokenManager` DataStore Preferences ile access + refresh token saklama/okuma saglar
    (`lyra_auth` DataStore dosyasi).
  - Eski `ui/auth/register/` paketi (RegisterContract, RegisterViewModel, RegisterScreen) silindi.
  - Yeni ekranlar: `ui/auth/otp/` (OtpContract, OtpViewModel, OtpScreen) ve
    `ui/auth/completeprofile/` (CompleteProfileContract, CompleteProfileViewModel, CompleteProfileScreen).
  - `LyraDestination` enum'undan `Register` kaldirildi; `Otp` ve `CompleteProfile` eklendi.
  - Navigasyon: Login -> OTP (phoneNumber + firstTime query param) -> (firstTime ise) CompleteProfile -> Home.

- Sebep: Backend API sozlesmesi (`docs/api/openapi.json`) telefon + OTP akisi gerektirir; sifre tabanli
  akis backend ile uyumsuzdu. Token yonetimi DataStore ile saglanir (mevcut tema tercihi pattern'i ile
  tutarli).
