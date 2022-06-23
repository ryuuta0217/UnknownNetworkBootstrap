# UnknownNetwork Bootstrap
"UnknownNetworkBootstrap" is a project that uses the Mixin created by SpongePowered and LegacyLauncher created by Mojang to modify the code of Paper at the bytecode level and customize it specifically for Unknown Network.

This project is using(contains code) Mojang's Launchwrapper(LegacyLauncher). Repository is a [here](https://github.com/Mojang/LegacyLauncher).

# License
(c) 2022 Unknown Network Developer Team and contributors. All rights reserved.

(EN) No copying of source code or secondary distribution of compiled binaries in this repository is permitted.

(JP) 本リポジトリ内におけるソースコードのコピー、コンパイル済みのバイナリの二次頒布は一切許可しません。

## How works?
* LegacyLauncher (Launcher)
* Load paper.jar
* Load libraries/* jar
* Hook & Inject Mixins
* Call main class (e.g. org.bukkit.craftbukkit.main.Main)

LegacyLauncher is used ClassLoading, Class transforming (adapt to mixin).

### Thanks to:
<table>
    <tr>
        <td align="center" width="50%">
            <a href="https://www.jetbrains.com/idea/">
                <img src="https://resources.jetbrains.com/storage/products/intellij-idea/img/meta/intellij-idea_1280x800.png" alt="Jetbrains IntelliJ IDEA">
            </a>
            <p><strong>JetBrainsによる<br/>プロ開発者のためのJava統合開発環境</strong></p>            
        </td>
        <td align="center" width="50%">
            <a href="https://github.com/SpongePowered/Mixin">
                <img src="https://github.com/SpongePowered/Mixin/blob/master/docs/javadoc/resources/logo.png?raw=true" alt="SpongePowered Mixin">
            </a>
            <p><strong>SpongePoweredによる<br/>MojangのLegacyLauncherシステムを利用したtrait/mixinフレームワーク</strong></p>
        </td>
    </tr>
</table>