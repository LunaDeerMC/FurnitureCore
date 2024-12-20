plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.hangar-publish-plugin") version "0.1.2"
}


group = "cn.lunadeer"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks.processResources {
        outputs.upToDateWhen { false }
        // replace @version@ in plugin.yml with project version
        filesMatching("**/plugin.yml") {
            filter {
                it.replace("@version@", rootProject.version.toString())
            }
        }
    }

    tasks.shadowJar {
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())
        dependsOn(tasks.withType<ProcessResources>())
    }
}

dependencies {
    implementation(project(":core"))
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
}
