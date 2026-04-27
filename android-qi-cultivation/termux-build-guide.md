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
# jeśli wcześniej ustawiałeś złe JAVA_HOME, wyczyść:
unset JAVA_HOME

# wykryj JAVA_HOME automatycznie po javac:
export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(which javac)")")")"
export PATH=$JAVA_HOME/bin:$PATH

echo "JAVA_HOME=$JAVA_HOME"
java -version
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

Jeśli zobaczysz błąd:
`Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required...`
to wykonaj najpierw:

```bash
git pull
```

i uruchom build ponownie.

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
