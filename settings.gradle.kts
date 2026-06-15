pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val hibernateOrmVersion: String by settings
    val graalvmNativeBuildToolsVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
        id("org.hibernate.orm") version hibernateOrmVersion
        id("org.graalvm.buildtools.native") version graalvmNativeBuildToolsVersion
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "notification-service"
