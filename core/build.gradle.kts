plugins {
    id("java")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// utf-8
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}


dependencies {
    implementation(project(":api"))
    implementation("net.kyori:adventure-platform-bukkit:4.3.3")
    implementation("com.alibaba:fastjson:2.0.31")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
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
