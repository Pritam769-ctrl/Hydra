# Hydra Vault 🛡️✨

**Hydra** is a sleek, premium, and highly secure media vault application for Android. Built with a stunning custom **Glassmorphism UI**, Hydra allows users to safely hide, organize, and manage their personal photos and videos away from the public system gallery.

## ✨ Key Features

*   **Secure App Lock:** A robust 6-digit PIN lock screen that secures the app on boot. Includes a custom security question recovery system in case the PIN is forgotten.
*   **Stunning Glassmorphism UI:** Built from the ground up with a custom aesthetic featuring translucent glass panels, rounded geometries, and dynamic animated backgrounds.
*   **Stealth Storage:** Automatically generates `.nomedia` files in linked directories to ensure hidden photos and videos never accidentally appear in the public Android Gallery.
*   **Smart Timeline & Folders:** View media chronologically in a master timeline or organize them into custom albums. Includes dynamic multi-select, sharing, and deletion capabilities.
*   **Recycle Bin:** Accidentally deleted a memory? Hydra includes a 30-day Recycle Bin to safely restore or permanently shred media.
*   **Full-Screen Media Engine:** A custom-built media viewer supporting seamless swipe-to-navigate, multi-touch pinch-to-zoom, panning, and native video playback.
*   **Local Backup & Restore:** Export your entire album structure to a secure JSON file and restore your vault structure seamlessly on a new device.

## 📸 Screenshots

*(Tip: Add your screenshots to a `docs/` folder in your repo and update these links!)*

| Lock Screen | Timeline View | Folder Organization | Full Screen Viewer |
| :---: | :---: | :---: | :---: |
| <img src="https://via.placeholder.com/250x500.png?text=Lock+Screen" width="200"/> | <img src="https://via.placeholder.com/250x500.png?text=Timeline" width="200"/> | <img src="https://via.placeholder.com/250x500.png?text=Folders" width="200"/> | <img src="https://via.placeholder.com/250x500.png?text=Media+Viewer" width="200"/> |

## 🛠️ Tech Stack & Architecture

Hydra is built natively for Android, focusing on performance, smooth animations, and local data privacy (no cloud servers).

*   **Language:** Java
*   **Architecture:** MVC / Android Lifecycle Components
*   **UI Components:** 
    *   `RecyclerView` with custom `GridLayoutManager` span lookups.
    *   Custom XML Drawables (Shape, Stroke, Corners) for the Glass effect.
    *   `ScaleGestureDetector` and `GestureDetector` for custom photo manipulation.
*   **Image Loading:** [Glide](https://github.com/bumptech/glide) for optimized, memory-safe thumbnail and full-image rendering.
*   **Storage:** 
    *   Android `SharedPreferences` (Security & App State).
    *   SQLite / `SQLiteOpenHelper` (Media linking & metadata).
    *   Android Storage Access Framework (SAF) via `DocumentFile` API.

## 📂 Project Structure

A quick look at the core package structure:

```text
com.pritam.mybottom.hydra
├── adapters/          # RecyclerView Adapters (Timeline, Folders, Media)
├── database/          # SQLite DatabaseHelper & Data Models (Folder, MediaItem)
├── ui/                # Main Fragments (Timeline, Folders, Notifications)
├── AppLockActivity    # The Front Door: Handles PIN setup and unlocking
├── MainActivity       # The Core Shell: Bottom Navigation and Fragment hosting
├── FullScreenImageActivity # The Viewer: Multi-touch zoom, pan, and video playback
└── RecycleBinActivity # The Trash: Handles restoration and permanent deletion
