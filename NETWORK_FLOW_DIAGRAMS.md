# Network Connectivity & Auto-Sync Flow

## Complete Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                          USER DEVICE                                 │
│                                                                      │
│  ┌──────────────┐        ┌──────────────┐        ┌──────────────┐ │
│  │              │        │              │        │              │ │
│  │   WiFi OFF   │───────▶│  Mobile OFF  │───────▶│  Ethernet -  │ │
│  │              │        │              │        │              │ │
│  └──────────────┘        └──────────────┘        └──────────────┘ │
│                                                                      │
└───────────────────────────────┬──────────────────────────────────────┘
                                │
                                │ Network State Change
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    NetworkConnectivityObserver                         │
│                                                                        │
│  • Registers with ConnectivityManager                                 │
│  • Listens for network callbacks                                     │
│  • Validates internet connectivity                                    │
│  • Emits Flow<Boolean>                                                │
│                                                                        │
│  callback.onAvailable()      ──▶  emit(true)                         │
│  callback.onLost()           ──▶  emit(false)                        │
│  callback.onCapabilitiesChanged() ──▶ emit(hasInternet)             │
└────────────────────────────┬───────────────────────────────────────────┘
                             │
                             │ Flow<Boolean>
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
┌─────────────────────────┐    ┌─────────────────────────┐
│     HomeViewModel       │    │   DetailViewModel       │
│                         │    │                         │
│ networkObserver.observe()│    │ networkObserver.observe()│
│   .onEach { isConnected }│    │   .onEach { isConnected }│
│                         │    │                         │
│ if (isConnected &&      │    │ if (isConnected &&      │
│     wasOffline &&       │    │     wasOffline &&       │
│     !isInitialLoad) {   │    │     currentAnimeId != null) {│
│                         │    │                         │
│   autoSyncData() ◀──────┤    │   autoSyncAnimeDetails() ◀──┤
│     │                   │    │     │                   │
│     ├─▶ repository      │    │     ├─▶ repository      │
│     │     .fetchTopAnime()│  │     │     .fetchAnimeById()│
│     │                   │    │     │                   │
│     └─▶ Cache in DB     │    │     ├─▶ Cache in DB     │
│         Update LiveData │    │     │   Update LiveData │
│ }                       │    │     │                   │
│                         │    │     └─▶ loadCharacters() │
│                         │    │ }                       │
└────────┬────────────────┘    └────────┬────────────────┘
         │                              │
         │ LiveData<Boolean>            │ LiveData<Boolean>
         │ (networkState)               │ (networkState)
         │                              │
         ▼                              ▼
┌────────────────────────┐    ┌────────────────────────┐
│    MainActivity        │    │   DetailActivity       │
│                        │    │                        │
│ Observes:              │    │ Observes:              │
│ • animeListLiveData    │    │ • anime (details)      │
│ • loadingState         │    │ • characters           │
│ • errorState           │    │ • loadingState         │
│ • networkState ◀───────┤    │ • networkState ◀───────┤
│                        │    │                        │
│ previousState != null  │    │ previousState != null  │
│   && wasOffline        │    │   && wasOffline        │
│   && isOnline now?     │    │   && isOnline now?     │
│                        │    │                        │
│ Show Snackbar:         │    │ Show Snackbar:         │
│ "Connected! Syncing    │    │ "Connected!            │
│  latest data..."       │    │  Refreshing data..."   │
│                        │    │                        │
│ Update RecyclerView    │    │ Update UI elements     │
│ with fresh data ◀──────┤    │ with fresh data ◀──────┤
└────────────────────────┘    └────────────────────────┘
```

## State Transition Diagram

```
                    ┌──────────────┐
                    │  APP LAUNCH  │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   ONLINE?    │
                    └──────┬───────┘
                           │
                ┌──────────┴──────────┐
                │                     │
                ▼                     ▼
        ┌──────────────┐      ┌──────────────┐
        │   ONLINE     │      │   OFFLINE    │
        │              │      │              │
        │ • Fetch API  │      │ • Load Cache │
        │ • Update DB  │      │ • Show Msg   │
        │ • No Notify  │      │ • "Offline"  │
        └──────┬───────┘      └──────┬───────┘
               │                     │
               │                     │
               │       User          │
               │    disconnects      │
               │    ◀────────────────┤
               │                     │
               ▼                     │
        ┌──────────────┐             │
        │ NOW OFFLINE  │             │
        │              │             │
        │ • Show Cache │             │
        │ • "Offline"  │             │
        │ • Set Flag   │             │
        └──────┬───────┘             │
               │                     │
               │       User          │
               │    reconnects       │
               │    ─────────────────▶
               │                     │
               ▼                     ▼
        ┌──────────────┐      ┌──────────────┐
        │ NOW ONLINE   │      │ NOW ONLINE   │
        │ (was offline)│      │ (stayed on)  │
        │              │      │              │
        │ ✅ AUTO-SYNC │      │ • Continue   │
        │ • Fetch API  │      │ • No Sync    │
        │ • Update DB  │      │ • Normal     │
        │ • "Connected"│      │              │
        └──────────────┘      └──────────────┘
```

## Sequence Diagram: Offline → Online Transition

```
User         MainActivity    HomeViewModel    NetworkObserver    Repository    Database    API
 │                │               │                 │               │            │         │
 │ [Offline]      │               │                 │               │            │         │
 │ Opens App      │               │                 │               │            │         │
 ├───────────────▶│               │                 │               │            │         │
 │                │ loadTopAnime()│                 │               │            │         │
 │                ├──────────────▶│                 │               │            │         │
 │                │               │ fetchTopAnime() │               │            │         │
 │                │               ├────────────────────────────────▶│            │         │
 │                │               │                 │               │ Check Net  │         │
 │                │               │                 │               ├─ ❌ Offline│         │
 │                │               │                 │               │            │         │
 │                │               │                 │               │ getCache() │         │
 │                │               │                 │               ├───────────▶│         │
 │                │               │                 │               │            │         │
 │                │               │     Return cached data ◀────────┤◀───────────┤         │
 │                │               │◀────────────────────────────────┤            │         │
 │                │◀──────────────┤                 │               │            │         │
 │                │               │                 │               │            │         │
 │ [Shows cached  │               │ observe()       │               │            │         │
 │  anime list]   │               ├────────────────▶│               │            │         │
 │◀───────────────┤               │                 │ emit(false)   │            │         │
 │                │               │◀────────────────┤               │            │         │
 │                │ Snackbar:     │                 │               │            │         │
 │ "Offline -     │ "Offline"     │                 │               │            │         │
 │  Showing       │◀──────────────┤                 │               │            │         │
 │  cached data"  │               │                 │               │            │         │
 │◀───────────────┤               │                 │               │            │         │
 │                │               │                 │               │            │         │
 │ [User enables  │               │                 │               │            │         │
 │  WiFi/Data]    │               │                 │               │            │         │
 │                │               │                 │ emit(true)    │            │         │
 │                │               │◀────────────────┤               │            │         │
 │                │               │                 │               │            │         │
 │                │               │ autoSyncData()  │               │            │         │
 │                │               ├──┐              │               │            │         │
 │                │               │  │ (if was      │               │            │         │
 │                │               │  │  offline)    │               │            │         │
 │                │               │◀─┘              │               │            │         │
 │                │               │                 │               │            │         │
 │                │               │ fetchTopAnime() │               │            │         │
 │                │               ├────────────────────────────────▶│            │         │
 │                │               │                 │               │ Check Net  │         │
 │                │               │                 │               ├─ ✅ Online │         │
 │                │               │                 │               │            │         │
 │                │               │                 │               │ API Call   │         │
 │                │               │                 │               ├────────────┬────────▶│
 │                │               │                 │               │            │         │
 │                │               │                 │               │◀───────────┴─────────┤
 │                │               │                 │               │ Fresh Data │         │
 │                │               │                 │               │            │         │
 │                │               │                 │               │ Cache Data │         │
 │                │               │                 │               ├───────────▶│         │
 │                │               │                 │               │            │         │
 │                │               │     Return fresh data ◀─────────┤            │         │
 │                │               │◀────────────────────────────────┤            │         │
 │                │ LiveData      │                 │               │            │         │
 │                │ updated       │                 │               │            │         │
 │                │◀──────────────┤                 │               │            │         │
 │                │               │                 │               │            │         │
 │                │ Snackbar:     │                 │               │            │         │
 │ "Connected!    │ "Syncing..."  │                 │               │            │         │
 │  Syncing       │◀──────────────┤                 │               │            │         │
 │  latest data"  │               │                 │               │            │         │
 │◀───────────────┤               │                 │               │            │         │
 │                │               │                 │               │            │         │
 │ [RecyclerView  │               │                 │               │            │         │
 │  updates with  │               │                 │               │            │         │
 │  fresh data]   │               │                 │               │            │         │
 │◀───────────────┤               │                 │               │            │         │
```

## Component Responsibilities

```
┌────────────────────────────────────────────────────────────┐
│         NetworkConnectivityObserver                        │
│ ─────────────────────────────────────────────────────────  │
│ ✓ Monitor network state changes                           │
│ ✓ Emit Boolean Flow (connected/disconnected)              │
│ ✓ Validate actual internet connectivity                   │
│ ✓ Handle callback registration/cleanup                    │
│ ✓ Prevent duplicate state emissions                       │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│         ViewModel (Home & Detail)                          │
│ ─────────────────────────────────────────────────────────  │
│ ✓ Observe network state flow                              │
│ ✓ Track previous network state                            │
│ ✓ Detect offline → online transitions                     │
│ ✓ Trigger auto-sync when conditions met                   │
│ ✓ Expose networkState LiveData to Activity                │
│ ✓ Handle sync errors gracefully                           │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│         Activity (Main & Detail)                           │
│ ─────────────────────────────────────────────────────────  │
│ ✓ Observe networkState from ViewModel                     │
│ ✓ Track previous state for transition detection           │
│ ✓ Show Snackbar notifications                             │
│ ✓ Update UI with synced data                              │
│ ✓ Provide manual refresh option (SwipeRefresh)            │
└────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────┐
│         Repository                                         │
│ ─────────────────────────────────────────────────────────  │
│ ✓ Check network availability                              │
│ ✓ Fetch from API if online                                │
│ ✓ Return cached data if offline                           │
│ ✓ Handle API errors                                        │
│ ✓ Cache fresh data in database                            │
│ ✓ Update timestamps                                        │
└────────────────────────────────────────────────────────────┘
```

## Data Flow on Auto-Sync

```
Network State Changes
         │
         ▼
NetworkConnectivityObserver
         │
         │ emit(true)  [Device came online]
         ▼
HomeViewModel / DetailViewModel
         │
         │ if (wasOffline && isOnline)
         ▼
autoSyncData() / autoSyncAnimeDetails()
         │
         │ viewModelScope.launch
         ▼
Repository.fetchTopAnime() / fetchAnimeById()
         │
         │ if (NetworkUtils.isNetworkAvailable)
         ▼
Retrofit API Call
         │
         │ response.isSuccessful
         ▼
Update timestamps
         │
         ▼
Cache in Room Database
         │
         │ animeDao.insertAllAnime() / insertAnime()
         ▼
LiveData automatically updates
         │
         ▼
Activity observes LiveData
         │
         ▼
Show Snackbar notification
         │
         ▼
RecyclerView / UI updates
         │
         ▼
User sees fresh data ✓
```

