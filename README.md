#UnknownNetwork Bootstrap
Hacky code!

Use SpongePowered/Mixin and Mojang/LegacyLauncher to inject customized code to Paper-Server code.

#Build (First time only)
I. Run `gradlew build`

II. Gradle when throw error or build completed,
please check `.gradle/caches/paperweight/taskCache/reobfJar.log`

III. Grab a tiny-remapper jar file location.<br>
Format: `Command: {java} -Xmx1G -classpath {tiny-remapper-jar-location} {class-name} {in-file} {out-file} {tiny-mapping-file} mojang+yarn spigot {paper-clip-patcher} --threads=1`

IV. Open gathered file directory to open file explorer.

V. Copy and overwrite `tiny-remapper-0.7.0-fat.jar` to opened folder.

VI. Re-run `gradlew build`

VII. `build/libs/launchwrapper-1.0-SNAPSHOT.jar` is now usable.

**It is first time only, next time to build, only need to run `gradlew build`.**