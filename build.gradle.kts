// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    // ✅ 여기에 Firebase 구글 서비스 플러그인을 등록합니다.
    id("com.google.gms.google-services") version "4.4.0" apply false
}
