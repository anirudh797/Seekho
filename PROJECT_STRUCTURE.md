# Seekho - Android Anime App

A modern Android application that displays top anime using the Jikan API (unofficial MyAnimeList API). Built with MVVM architecture, featuring offline support, data synchronization, and graceful error handling.

## Features

### Core Features
- ✅ **Home Screen**: Displays a list of top anime with:
  - Title
  - Episodes count
  - Rating/Score
  - Poster image
  - Type (TV, Movie, OVA, etc.)

- ✅ **Detail Screen**: Shows comprehensive anime information:
  - Large poster/cover image
  - Trailer information (if available)
  - Full synopsis
  - Genres list
  - Main characters with images
  - Episode count and rating
  - Type and status

### Technical Features
- ✅ **Offline Mode**: Room database caches all anime data for offline viewing
- ✅ **Data Sync**: Pull-to-refresh functionality syncs latest data from API
- ✅ **Error Handling**: 
  - Network errors display user-friendly messages
  - Graceful degradation to cached data when offline
  - Snackbar notifications for errors with cached data available
- ✅ **Adaptive UI**:
  - Placeholder images when posters are unavailable
  - Handles missing data fields (episodes, ratings, etc.)
  - Responsive layouts

## Architecture

### MVVM Pattern
```
├── UI Layer (Views + ViewModels)
│   ├── MainActivity + HomeViewModel
│   └── DetailActivity + DetailViewModel
│
├── Data Layer
│   ├── Repository (AnimeRepository)
│   ├── Local Data Source (Room Database)
│   └── Remote Data Source (Retrofit API)
│
└── Domain Layer (Models)
```

### Technology Stack
- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Local Database**: Room 2.6.1
- **Image Loading**: Coil 2.5.0
- **Reactive Programming**: Kotlin Coroutines + LiveData
- **DI**: Manual dependency injection (easily upgradable to Hilt/Koin)

## Project Structure

```
app/src/main/
├── java/com/seekho/anime/
│   ├── data/
│   │   ├── api/
│   │   │   ├── JikanApiService.kt       # Retrofit API interface
│   │   │   └── RetrofitClient.kt        # Retrofit singleton
│   │   ├── db/
│   │   │   ├── AnimeDao.kt              # Room DAO
│   │   │   ├── AnimeDatabase.kt         # Room database
│   │   │   └── Converters.kt            # Type converters for Room
│   │   ├── model/
│   │   │   ├── Anime.kt                 # Main anime data model
│   │   │   ├── AnimeResponse.kt         # API response wrappers
│   │   │   └── Character.kt             # Character models
│   │   └── repository/
│   │       └── AnimeRepository.kt       # Central data repository
│   ├── ui/
│   │   ├── home/
│   │   │   ├── MainActivity.kt          # Home screen
│   │   │   ├── HomeViewModel.kt         # Home screen logic
│   │   │   └── AnimeAdapter.kt          # RecyclerView adapter
│   │   └── detail/
│   │       ├── DetailActivity.kt        # Detail screen
│   │       ├── DetailViewModel.kt       # Detail screen logic
│   │       └── CharacterAdapter.kt      # Characters adapter
│   └── utils/
│       ├── NetworkResult.kt             # Sealed class for network states
│       └── NetworkUtils.kt              # Network connectivity helper
├── res/
│   ├── layout/
│   │   ├── activity_main.xml            # Home screen layout
│   │   ├── activity_detail.xml          # Detail screen layout
│   │   ├── item_anime.xml               # Anime list item
│   │   └── item_character.xml           # Character list item
│   ├── values/
│   │   ├── strings.xml                  # String resources
│   │   ├── colors.xml                   # Color definitions
│   │   └── themes.xml                   # App theme
│   └── drawable/
│       └── ic_placeholder.xml           # Placeholder image
└── AndroidManifest.xml
```

## API Integration

### Jikan API v4 Endpoints Used
1. **GET /top/anime**: Fetches top anime list
2. **GET /anime/{id}/full**: Fetches detailed anime information
3. **GET /anime/{id}/characters**: Fetches anime characters

### Data Flow
1. User opens app → ViewModel requests data from Repository
2. Repository checks network connectivity
3. If online:
   - Fetches from API
   - Caches in Room database
   - Returns data to ViewModel
4. If offline:
   - Retrieves from Room database
   - Returns cached data to ViewModel
5. ViewModel updates UI via LiveData

## Database Schema

### Anime Table
```kotlin
@Entity(tableName = "anime")
data class Anime(
    @PrimaryKey val malId: Int,
    val title: String,
    val episodes: Int?,
    val score: Double?,
    val synopsis: String?,
    val images: AnimeImages?,
    val trailer: TrailerInfo?,
    val genres: List<Genre>?,
    val type: String?,
    val lastUpdated: Long,
    val isFavorite: Boolean
)
```

## Error Handling Strategy

1. **Network Errors**:
   - Try to fetch from API
   - On failure, fallback to cached data
   - Show appropriate error message

2. **Missing Data**:
   - Use default values ("N/A", "Unknown")
   - Show placeholder images
   - Hide optional sections if data unavailable

3. **Loading States**:
   - ProgressBar during initial load
   - SwipeRefreshLayout for manual refresh
   - Error TextView for critical failures

## Building the Project

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 34

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

### Network Configuration
The app requires internet permission to fetch data from Jikan API:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Future Enhancements
- [ ] Implement search functionality
- [ ] Add favorites/bookmarks feature
- [ ] Support for multiple anime types (movies, OVA, etc.)
- [ ] Pagination for infinite scrolling
- [ ] Detail page trailer playback
- [ ] Share anime functionality
- [ ] Dark mode support
- [ ] Unit and integration tests

## License
This project uses the Jikan API which is a free, open-source REST API for MyAnimeList data.

## API Attribution
- API: [Jikan API](https://jikan.moe/)
- Data Source: [MyAnimeList](https://myanimelist.net/)
