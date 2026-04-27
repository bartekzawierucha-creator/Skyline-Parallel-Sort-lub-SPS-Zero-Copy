# Build APK w Termux (Android)

Poniżej masz prostą procedurę, żeby zbudować APK na telefonie przez Termux.

## 1) Instalacja pakietów w Termux

```bash
pkg update -y
pkg upgrade -y
pkg install -y openjdk-17 gradle unzip tar curl aapt2
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
which aapt2
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

Jeśli zobaczysz błąd `AAPT2 ... Daemon startup failed`, to znaczy że Gradle próbuje użyć binarki AAPT2 dla Linux x86_64.
`build_apk.sh` automatycznie wymusi lokalne `aapt2` z Termuxa (`android.aapt2FromMavenOverride`), ale musisz mieć pakiet:

```bash
pkg install -y aapt2
```

I uruchamiaj build przez skrypt (nie `./gradlew` bez parametrów):

```bash
./scripts/build_apk.sh
```

W logu powinno pojawić się:
`Using local aapt2 override: /data/data/com.termux/files/usr/bin/aapt2`

Jeśli zobaczysz błąd:
`Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required...`
to wykonaj:

```bash
git pull
```

Jeśli nadal pojawia się `aapt2-...-linux` (np. `Syntax error: "(" unexpected`), to znaczy że AGP i tak uruchamia binarkę x86_64 niezgodną z Termux/ARM. Wtedy użyj builda w GitHub Actions (x64 runner), plik workflow:

```bash
.github/workflows/android-debug-apk.yml
```

Uruchom workflow ręcznie (tab **Actions** -> **Android Debug APK** -> **Run workflow**) i pobierz artefakt `qi-cultivation-debug-apk`.

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
