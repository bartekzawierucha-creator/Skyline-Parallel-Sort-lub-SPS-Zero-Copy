# Build APK w Termux (Android)

Poniżej masz prostą procedurę, żeby zbudować APK na telefonie przez Termux.

## 1) Instalacja pakietów w Termux

```bash
pkg update -y
pkg upgrade -y
pkg install -y openjdk-17 gradle unzip tar curl
```

Sprawdź Java:

```bash
java -version
```

## 2) Skopiuj paczkę do telefonu i rozpakuj

Jeśli masz plik `qi-cultivation-termux-*.tar.gz`, to w Termux:

```bash
cd ~
mkdir -p qi-cultivation
cd qi-cultivation
tar -xzf /sdcard/Download/qi-cultivation-termux-*.tar.gz
```

## 3) Ustaw zmienne środowiskowe

```bash
cd ~/qi-cultivation/android-qi-cultivation
export JAVA_HOME=$PREFIX/opt/openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

## 4) Pobierz Android SDK i narzędzia

```bash
./scripts/bootstrap_android_sdk.sh
```

Jeśli to nie przejdzie, to problem jest po stronie sieci/proxy i trzeba odblokować ruch do:
- `dl.google.com`
- `maven.google.com`
- `repo1.maven.org`
- `plugins.gradle.org`
- `services.gradle.org`

## 5) Build APK

```bash
./scripts/build_apk.sh
```

Po sukcesie APK będzie tutaj:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

## 6) Instalacja APK na telefonie

Najprościej otwórz plik menedżerem plików i kliknij instalację.

Alternatywnie przez ADB (na innym urządzeniu/PC):

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
