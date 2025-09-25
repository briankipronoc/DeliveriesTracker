// Top-level settings file for your project.

pluginManagement {
    repositories {
        // This is where Android-specific plugins are located.
        google()
        // This is the default repository for most general Gradle plugins.
        mavenCentral()
        // Add other repositories here if needed for specific plugins.
    }
}

dependencyResolutionManagement {
    // Repositories are where Gradle looks for dependencies.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // This is the primary repository for Android-specific libraries from Google.
        google()
        // This is the default repository for most general Java libraries.
        mavenCentral()
    }
}

// Include your app module
include(":app")

// Set the root project name
rootProject.name = "MamaMbogaQRApp"