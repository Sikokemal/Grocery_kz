buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")


    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.1" apply false

}
val smileyratingVersion by extra("1.0.0")
val smileyratingVersion1 by extra(smileyratingVersion)
