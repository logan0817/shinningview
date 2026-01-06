# agp incompatibility with IntelliJ IDEA

Opening this template in IDEA might result in an error like this:

```
The project is using an incompatible version (AGP 8.0.0) of the Android Gradle plugin. Latest supported version is AGP 7.4.0
```

This is caused by IDEA support for AGP lagging behind releases of AGP and is tracked by bugs like https://youtrack.jetbrains.com/issue/IDEA-317997.

To get this working in the mean time, you can downgrade the version of `agp` in `libs.versions.toml` to a supported version. 

# Use correct JVM version

This template requires you to have the correct JDK version in your path. Check which version you have by running `java --version` in a terminal. This should match the version specified in the Gradle build files e.g.:

```
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
```
