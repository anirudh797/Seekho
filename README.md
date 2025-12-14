# Seekho - Android Anime Application

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)

A modern Android application that showcases top anime using the Jikan API (unofficial MyAnimeList API). Built with clean architecture principles following MVVM pattern, featuring robust offline support, data synchronization, and graceful error handling.

## 📱 Features

### Home Screen
- List of top anime from MyAnimeList
- Display anime title, poster, episodes count, and rating
- Pull-to-refresh for manual data sync
- Smooth scrolling with RecyclerView
- Click any anime to view details

### Detail Screen
- Large poster/cover image with fallback placeholder
- Comprehensive anime information:
  - Title and alternative titles
  - Synopsis/description
  - Genres
  - Episodes count
  - Rating/Score
  - Type (TV, Movie, OVA, etc.)
- Main characters list with images
- Trailer information (when available)

### Offline Capabilities
- Room database caches all fetched anime data
- Works completely offline after initial data fetch
- Automatic cache updates when online
- Smart fallback to cached data on network errors

## 🏗️ Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture pattern:

```
┌─────────────────┐
│   UI Layer      │ Activities, Fragments, Adapters
│   (Views)       │ 
└────────┬────────┘
         │
┌────────▼────────┐
│   ViewModel     │ HomeViewModel, DetailViewModel
│                 │ (LiveData, Coroutines)
└────────┬────────┘
         │
┌────────▼────────┐
│   Repository    │ AnimeRepository
│                 │ (Offline-first strategy)
└────┬───────┬────┘
     │       │
┌────▼────┐ ┌▼─────────┐
│  Remote │ │  Local   │
│  (API)  │ │  (Room)  │
└─────────┘ └──────────┘
```

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM
- **Networking**: 
  - Retrofit 2.9.0
  - OkHttp 4.12.0
  - Gson Converter
- **Database**: Room 2.6.1 (for offline caching)
- **Image Loading**: Coil 2.5.0 (Kotlin-first image loading library)
- **Async Operations**: 
  - Kotlin Coroutines
  - LiveData
- **UI Components**:
  - Material Components
  - RecyclerView
  - SwipeRefreshLayout
  - ConstraintLayout

## 📁 Project Structure

```
app/
├── src/main/
│   ├── java/com/seekho/anime/
│   │   ├── data/
│   │   │   ├── api/          # Retrofit API services
│   │   │   ├── db/           # Room database components
│   │   │   ├── model/        # Data models
│   │   │   └── repository/   # Data repositories
│   │   ├── ui/
│   │   │   ├── home/         # Home screen
│   │   │   ├── detail/       # Detail screen
│   │   │   └── common/       # Shared UI components
│   │   └── utils/            # Helper utilities
│   └── res/                  # Resources (layouts, strings, etc.)
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or later
- JDK 17
- Android SDK with API level 34
- Gradle 8.2+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/anirudh797/Seekho.git
   cd Seekho
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned repository
   - Wait for Gradle sync to complete

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```
   Or simply click the "Run" button in Android Studio

## 🌐 API Integration

This app uses the [Jikan API](https://jikan.moe/) - a free, open-source REST API for MyAnimeList data.

**Endpoints Used**:
- `GET /v4/top/anime` - Fetch top anime list
- `GET /v4/anime/{id}/full` - Fetch detailed anime information
- `GET /v4/anime/{id}/characters` - Fetch anime characters

## 📊 Data Flow

1. **User Action**: Opens app or refreshes data
2. **ViewModel**: Requests data from Repository
3. **Repository**: Checks network connectivity
   - **Online**: Fetches from API → Caches in Room → Returns to ViewModel
   - **Offline**: Retrieves from Room → Returns cached data
4. **ViewModel**: Updates LiveData
5. **UI**: Observes LiveData and updates display

## 🎨 UI/UX Features

- **Loading States**: ProgressBar indicators during data fetch
- **Error Handling**: User-friendly error messages with Snackbar
- **Placeholder Images**: Graceful fallback when images unavailable
- **Pull-to-Refresh**: Manual sync gesture
- **Smooth Navigation**: Activity transitions with shared elements
- **Material Design**: Following Material Design guidelines

## 🔒 Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🧪 Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📦 Build Variants

- **Debug**: Development build with logging enabled
- **Release**: Production build with ProGuard/R8 optimization

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- [Jikan API](https://jikan.moe/) for providing free MyAnimeList data
- [MyAnimeList](https://myanimelist.net/) for the anime database
- Android community for the excellent libraries and tools

## 📧 Contact

For questions or feedback, please open an issue on GitHub.

---

**Note**: This app is built for educational purposes and uses publicly available APIs. All anime data and images are property of their respective owners.