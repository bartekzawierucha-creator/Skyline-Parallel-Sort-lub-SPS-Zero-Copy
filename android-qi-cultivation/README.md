# Android: Kroniki Kultywacji Qi

Bardzo rozbudowany prototyp gry mobilnej (Jetpack Compose) inspirowanej xianxia.

## Najważniejsze systemy

- **System technik i ataków**:
  - techniki podzielone na kategorie (`ATTACK`, `MOVEMENT`, `DEFENSE`, `SOUL`, `BODY`, `DOMAIN`),
  - koszt qi, bazowa moc, poziom biegłości,
  - trening technik i wybór aktywnej techniki do walki.
- **Linia krwi**:
  - typy (`DRAGON`, `PHOENIX`, `TITAN`, `VOID`, `THUNDER_GOD`),
  - czystość linii krwi,
  - mnożniki wpływające na atak, ciało, ducha i przeznaczenie.
- **Duch i ciało**:
  - `SpiritCore` (moc ducha, obrona duszy, zrozumienie),
  - `BodyFoundation` (twardość, regeneracja, pojemność meridianów),
  - osobne akcje rozwoju ducha i hartowania ciała.
- **Konstytucja i przeznaczenie** (duży wpływ):
  - `constitution` wpływa na przeżywalność i skalowanie,
  - `Destiny` steruje szansą okazji, odpornością na nieszczęścia i modyfikatorem losu,
  - eventy świata uwzględniają przeznaczenie.
- **Pozostałe**:
  - przełomy krain (`Realm`) do poziomu `VOID_RETURN`,
  - system RPG lokacji (`LocationType`) i podróży między obszarami,
  - automatycznie generowane wydarzenia podczas kultywacji/eksploracji,
  - szansa powtórzenia tego samego wydarzenia ustawiona na **0.5%**,
  - sekty z bonusami,
  - sklep i ekwipunek,
  - quest dnia,
  - rozbudowany UI/UX (karty, postępy, log, feedback snackbar).
  - system immersji: pogoda, cykl dnia, tytuły postaci, NPC, kontrakty i kronika fabularna.


## Podgląd w tym środowisku (CLI)

Jeśli nie masz pełnego Android/Gradle w kontenerze, możesz uruchomić podgląd mechanik w terminalu:

```bash
python3 preview_cli.py
```

CLI zawiera: kultywację, walkę technikami, trening technik, linię krwi, ducha, ciało i przeznaczenie.

## Główne pliki

- `app/src/main/java/com/example/qicultivation/GameLogic.kt`
- `app/src/main/java/com/example/qicultivation/MainActivity.kt`
- `preview_cli.py`

## Jak uruchomić

1. Otwórz folder `android-qi-cultivation` w Android Studio.
2. Dodaj standardową konfigurację projektu Android (`settings.gradle.kts`, `build.gradle.kts`, `AndroidManifest.xml`) jeśli tworzysz od zera.
3. Ustaw `MainActivity` jako launcher activity i uruchom na emulatorze.

> Logika i UI są gotowe do osadzenia w istniejącym projekcie Android Compose.

## Build APK z terminala (automatycznie)

Po odblokowaniu internetu w środowisku (dostęp do `dl.google.com`, `services.gradle.org`, `repo1.maven.org`):

```bash
./scripts/bootstrap_android_sdk.sh
./scripts/build_apk.sh
```

Gotowy plik:

```bash
app/build/outputs/apk/debug/app-debug.apk
```

## Paczka do Termux

Możesz przygotować gotową paczkę projektu pod telefon/Termux:

```bash
./scripts/package_for_termux.sh
```

Archiwum będzie w katalogu `dist/`, np.:

```bash
dist/qi-cultivation-termux-YYYYMMDD-HHMMSS.tar.gz
```

Szczegółowa instrukcja budowy w Termux znajduje się w pliku:

```bash
termux-build-guide.md
```

> Uwaga dla Termuxa: jeśli build wywala `AAPT2 ... Daemon startup failed`, doinstaluj `aapt2` (`pkg install -y aapt2`) i uruchom `./scripts/build_apk.sh` ponownie. Skrypt automatycznie użyje lokalnej binarki `aapt2`.
