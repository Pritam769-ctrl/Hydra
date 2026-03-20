package com.pritam.mybottom.hydra;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
// --- RESTORE FIX VARIABLES ---
    private com.pritam.mybottom.hydra.database.DatabaseHelper dbHelper;
    private java.util.List<com.pritam.mybottom.hydra.database.Folder> brokenFolders = new java.util.ArrayList<>();
    private int currentFixIndex = 0;

    // The launcher to ask the user to re-select the folder
    private final androidx.activity.result.ActivityResultLauncher<android.content.Intent> folderRelinkLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri treeUri = result.getData().getData();
                    if (treeUri != null) {
                        // 1. Tell Android to permanently remember this permission again!
                        getContentResolver().takePersistableUriPermission(treeUri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        com.pritam.mybottom.hydra.database.Folder fixedFolder = brokenFolders.get(currentFixIndex);
                        
                        // 2. Update the folder in the database with the fresh URI
                        android.content.ContentValues values = new android.content.ContentValues();
                        values.put("folderUri", treeUri.toString());
                        dbHelper.getWritableDatabase().update("folders", values, "folderId = ?", new String[]{String.valueOf(fixedFolder.folderId)});

                        // --- THE MAGIC RESYNC ENGINE ---
                        // 3. Delete all the old, broken photo links for this folder from the database
                        dbHelper.getWritableDatabase().delete("media_items", "parentFolderId = ?", new String[]{String.valueOf(fixedFolder.folderId)});

                        // 4. Open the physical folder and create fresh, working links for every photo inside it!
                        androidx.documentfile.provider.DocumentFile dir = androidx.documentfile.provider.DocumentFile.fromTreeUri(MainActivity.this, treeUri);
                        if (dir != null && dir.isDirectory()) {
                            for (androidx.documentfile.provider.DocumentFile file : dir.listFiles()) {
                                
                                // --- THE FIX: Make sure it is a file AND its name is not ".nomedia" ---
                                if (file.isFile() && file.getName() != null && !file.getName().equals(".nomedia")) { 
                                    
                                    // Found a valid media file! Save its brand-new URI to the database
                                    dbHelper.insertMediaItem(fixedFolder.folderId, file.getUri().toString());
                                }
                            }
                        }

                        // 5. Move to the next broken folder (if they have multiple)
                        currentFixIndex++;
                        promptNextBrokenFolder();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ==========================================
        // 1. NAVIGATION SETUP
        // ==========================================
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_timeline, R.id.navigation_folders)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        
        // Connect our new custom Glass Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.top_toolbar);
        if (toolbar != null) {
            NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        }
        
        // Connect the Bottom Tabs
        if (navView != null) {
            NavigationUI.setupWithNavController(navView, navController);
        }

        // ==========================================
        // 2. GLASSMORPHISM ANIMATION SETUP
        // ==========================================
        ConstraintLayout container = findViewById(R.id.container);
        
        // SAFETY CHECK: Ensure the background actually exists and is an animation before starting it
        if (container != null && container.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) container.getBackground();
            animationDrawable.setEnterFadeDuration(2000);
            animationDrawable.setExitFadeDuration(4000);
            animationDrawable.start();
        }
        // Initialize DB
        dbHelper = new com.pritam.mybottom.hydra.database.DatabaseHelper(this);
        
        // Check for broken restored permissions
        verifyStoragePermissions();
    }
    // --- THE RESTORE DETECTION ENGINE ---
    private void verifyStoragePermissions() {
        brokenFolders.clear();
        currentFixIndex = 0;

        // 1. Get all folders the app thinks it has
        java.util.List<com.pritam.mybottom.hydra.database.Folder> allFolders = dbHelper.getAllFolders();
        if (allFolders.isEmpty()) return; // No folders? Nothing to fix.

        // 2. Ask Android what permissions we ACTUALLY have right now
        java.util.List<android.content.UriPermission> activePermissions = getContentResolver().getPersistedUriPermissions();
        java.util.HashSet<String> activeUris = new java.util.HashSet<>();
        for (android.content.UriPermission p : activePermissions) {
            activeUris.add(p.getUri().toString());
        }

        // 3. Find the folders that lost their permissions
        for (com.pritam.mybottom.hydra.database.Folder folder : allFolders) {
            if (!activeUris.contains(folder.folderUri)) {
                brokenFolders.add(folder);
            }
        }

        // 4. If we found broken folders, start the fix process!
        if (!brokenFolders.isEmpty()) {
            promptNextBrokenFolder();
        }
    }

    private void promptNextBrokenFolder() {
        if (currentFixIndex >= brokenFolders.size()) {
            android.widget.Toast.makeText(this, "All albums successfully restored!", android.widget.Toast.LENGTH_LONG).show();
            // Optional: Restart the activity to instantly redraw the thumbnails
            recreate(); 
            return;
        }

        com.pritam.mybottom.hydra.database.Folder brokenFolder = brokenFolders.get(currentFixIndex);

        new android.app.AlertDialog.Builder(this)
                .setTitle("Restore Access")
                .setCancelable(false)
                .setMessage("To restore your album '" + brokenFolder.folderName + "', please select its folder on your device again so Hydra can re-link it safely.")
                .setPositiveButton("Select Folder", (dialog, which) -> {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT_TREE);
                    folderRelinkLauncher.launch(intent);
                })
                .show();
    }

}