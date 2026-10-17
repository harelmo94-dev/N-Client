# Krypton Client - Step 1 (Fabric 1.21.1, Java 21)

## Get the jar without installing anything
1. Create a free GitHub account, make a new repository, and upload all files from this folder
   (including the hidden .github folder).
2. Open the Actions tab, wait for the "build" run to finish (about 3-5 minutes).
3. Open the run, download the "krypton-client" artifact, unzip it, and take krypton-client-0.1.0.jar.
4. Put the jar in .minecraft/mods together with Fabric API 1.21.1, and launch the Fabric 1.21.1 profile.

## Or build locally (JDK 21)
./gradlew build    ->  build/libs/krypton-client-0.1.0.jar
./gradlew runClient ->  dev client

Right Shift opens the ClickGUI.

## Build with GitHub Actions
Push this folder to a GitHub repo. The workflow in `.github/workflows/build.yml` builds on every push;
download the jar from the repo's Actions tab -> latest run -> Artifacts -> `krypton-client`.
