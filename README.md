# FormTree

A native Android app that fetches a hierarchical JSON form definition, renders it as a
single scrollable tree, and caches it in a relational database so the app works offline.

Data source: [lumiform-android-test.json](https://gist.githubusercontent.com/aruana-lumiform/383e1291df3e0cc1d49aeb14d45f5b94/raw/lumiform-android-test.json)

## Features

- Displays the form as a single scrollable tree — pages, sections, and questions
  clearly nested inside color-coded blocks
- Text, image, and choice questions, each shown with its id
- Choice questions with modern selectable options — single or multiple, clearly
  distinguished, with a live selection counter
- Tapping an image opens a details screen showing it full-sized with its title
- Works fully offline — previously loaded content is always available
- Friendly feedback when a refresh fails: a retry screen, or a notice with a retry
  option when saved data is shown
- Follows the system light and dark theme
- Comfortable in both portrait and landscape

## Setup

1. Clone the repository
2. Open the project in **Android Studio** and run the `app` configuration

```bash
./gradlew :app:assembleDebug      # debug APK at app/build/outputs/apk/debug/
./gradlew :app:installDebug       # install to a connected device
./gradlew :app:installRelease     # R8-optimized release build
```

The release build is signed with the debug keystore so it can be installed for evaluation.
A real deployment would use a dedicated keystore and Play App Signing.

## Architecture

MVVM with unidirectional data flow, organized feature-first:

```
com.ban.formtree
├── core/            shared infrastructure (no feature dependencies)
│   ├── database/    Room database + DI
│   ├── navigation/  type-safe serializable routes
│   ├── network/     OkHttp/Retrofit + DI
│   └── ui/theme/    palette, typography, theme
├── form/            form feature
│   ├── domain/      pure Kotlin models (Page, FormItem tree)
│   ├── data/        API, DTOs, entities, DAO, mappers, repository
│   └── ui/          ViewModel, UiState, screen composables
└── imagedetails/    image details feature (route args → UiState)
```

Room is the single source of truth — the UI only ever reads the database:

```
Refresh path:  API ──► DTOs ──► entities ──► Room      (atomic replaceAll)
Observe path:  Room ──► domain tree ──► ViewModel ──► UiState ──► Compose
```

## Testing

JVM unit tests only:

```bash
./gradlew :app:testDebugUnitTest   # 17 tests
```
