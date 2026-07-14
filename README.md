# Arka Launcher

Arka Launcher is a custom Android home-screen application inspired by the Konark Sun Temple. Named after the Sanskrit word for "sun," it features a unique design language based on dark sandstone and copper tones, centered around a signature "Arka Wheel" clock widget.

## Architecture
- **Pattern**: MVVM (Model-View-ViewModel)
- **Framework**: Jetpack Compose for a fully declarative and modern UI.
- **Dependency Injection**: Hilt for robust and scalable DI.
- **Local Persistence**: 
  - **Room**: Caches the list of installed applications for fast cold starts.
  - **DataStore**: Persists user preferences and dock customizations.
- **Image Loading**: Coil with a custom `Fetcher` implementation for high-performance app icon loading and memory caching.

## Key Technical Features
- **Arka Wheel Physics**: Implemented using Compose `Animatable` and `pointerInput`. The wheel supports 1:1 drag tracking with `atan2` trigonometry and realistic momentum-based rotation using exponential decay friction.
- **Performance Optimization**: 
  - **Recomposition Isolation**: The live clock updates are isolated into a dedicated Composable, ensuring the complex Canvas-drawn wheel and background silhouettes don't redraw every second.
  - **Icon Caching**: Custom Coil integration prevents redundant package manager queries and bitmap decoding during app drawer scrolling.
- **System Integration**: Uses `PackageManager` for app discovery and a `BroadcastReceiver` for real-time synchronization of the app database when packages are installed, updated, or removed.
- **Prabha Mode**: A focused "Digital Wellbeing" mode that hides distractions (dock/search) and replaces the main UI with a calming, multi-layered mountain scene drawn entirely on a custom Canvas.

## Setup Instructions
1. Clone the repository.
2. Open the project in the latest version of **Android Studio**.
3. Sync the project with Gradle files.
4. Run the application on a physical device or emulator (Android 8.0+).
5. Set Arka as your default home app in the system settings to enable full launcher functionality.
