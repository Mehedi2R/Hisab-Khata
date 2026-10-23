plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// Dynamic Versioning: Can be passed via -PappVersion=3.4.8 or defaults to current release version
val appVersionName = (project.findProperty("appVersion") ?: "3.4.8").toString()
val appVersionCode = (project.findProperty("appVersionCode")?.toString()?.toIntOrNull() ?: 348)

android {
    namespace = "com.hisabkhata.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hisabkhata2r.android"
        minSdk = 24
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        resourceConfigurations += listOf("en", "bn")

        buildConfigField("String", "VERSION_NAME", "\"$appVersionName\"")
        buildConfigField("int", "VERSION_CODE", "$appVersionCode")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "NONE"
            }
        }
    }

    applicationVariants.all {
        val variant = this
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = "HisabKhata-v${variant.versionName}-${variant.buildType.name}.apk"
        }

        // Permanent Non-Destructive Release Archive: NEVER replace or overwrite any existing release APK!
        if (variant.buildType.name == "release") {
            variant.assembleProvider.configure {
                doLast {
                    val releaseApkDir = file("${rootProject.rootDir}/release_apks")
                    releaseApkDir.mkdirs()
                    variant.outputs.forEach { out ->
                        val apkFile = out.outputFile
                        if (apkFile != null && apkFile.exists()) {
                            var dest = file("$releaseApkDir/HisabKhata-v${variant.versionName}.apk")
                            if (dest.exists()) {
                                val timestamp = System.currentTimeMillis()
                                dest = file("$releaseApkDir/HisabKhata-v${variant.versionName}_build_$timestamp.apk")
                            }
                            // Strict non-destructive copy: overwrite is false
                            apkFile.copyTo(dest, overwrite = false)
                            println(">>> Release APK PERMANENTLY PRESERVED: ${dest.absolutePath}")

                            // Generate/Update permanent catalog ledger
                            val catalogFile = file("$releaseApkDir/RELEASE_CATALOG.txt")
                            val allApks = releaseApkDir.listFiles { f -> f.extension == "apk" } ?: emptyArray()
                            val lines = StringBuilder()
                            lines.append("=== HISAB KHATA PERMANENT RELEASE ARCHIVE LEDGER ===\n")
                            lines.append("Total Preserved Releases: ${allApks.size}\n\n")
                            allApks.sortedBy { it.name }.forEach { apk ->
                                lines.append("- ${apk.name} | Size: ${apk.length()} bytes\n")
                            }
                            catalogFile.writeText(lines.toString())
                        }
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += listOf(
                "META-INF/**/LICENSE*",
                "META-INF/**/NOTICE*",
                "META-INF/**/AL2.0",
                "META-INF/**/LGPL2.1",
                "META-INF/**/*.version",
                "META-INF/**/*.txt",
                "META-INF/DEPENDENCIES",
                "META-INF/INDEX.LIST",
                "DebugProbesKt.bin",
                "**/*.kotlin_builtins",
                "kotlin-tooling-metadata.json",
                "META-INF/services/javax.annotation.processing.Processor",
                "META-INF/androidx.*",
                "META-INF/proguard/**"
            )
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Room Database (Offline First)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Google Play Services Auth & Google Drive API Client
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("com.google.api-client:google-api-client-android:2.7.0")
    implementation("com.google.http-client:google-http-client-gson:1.45.3")
    implementation("com.google.apis:google-api-services-drive:v3-rev20241027-2.0.0") {
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "com.google.guava", module = "listenablefuture")
    }

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
}
