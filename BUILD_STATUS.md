# Подтверждённая сборка APK

Дата: 2026-10-29.

**APK успешно собран и подписан.**

- Репозиторий: https://github.com/SHADrussed/kabachok-exe
- Проверенный commit: `5f655225a69e8c2ff7f241346d39c6c0ad962ec2`.
- Успешный запуск: https://github.com/SHADrussed/kabachok-exe/actions/runs/40904972594
- APK-артефакт: https://github.com/SHADrussed/kabachok-exe/actions/runs/40904972594/artifacts/12911146480
- Файл внутри архива: `KABACHOK-EXE-1.0-debug.apk`.
- Размер APK: 9 528 174 байта.
- SHA-256 APK: `bb76f628f60506d40c24625f8c65debb40d710ae20a70151bebaed9fe3aa70cd`.

## Выполненные проверки

GitHub Actions выполнил:

```sh
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug
```

Результат: `BUILD SUCCESSFUL in 2m 4s`.

- Kotlin/Compose скомпилирован, задача `assembleDebug` завершена успешно.
- JUnit: 6 тестов, 0 ошибок, 0 пропусков.
- Android Lint: 0 ошибок, 7 предупреждений. Отчёты доступны в артефакте `build-reports` того же запуска.
- `apksigner verify --verbose`: подпись APK проверена, APK Signature Scheme v2, один подписант.
- APK-артефакт скачан; SHA-256 ZIP совпала с digest GitHub. Целостность ZIP/APK проверена; присутствуют `AndroidManifest.xml` и `classes.dex`.

## Ограничения проверки

Приложение не запускалось на физическом Samsung или эмуляторе. Подтверждены сборка, модульные тесты, lint, подпись и целостность установочного файла; визуальная и аппаратная проверка остаётся для устройства.

Это debug-сборка для установки напрямую, не релиз Google Play. Минимальная версия — Android 8.0 (API 26). Подпись между повторными сборками на разных runner может меняться; для долгосрочных обновлений с сохранением прогресса следует настроить постоянный ключ.

## История исправления окружения

Локальная среда не смогла скачать Gradle и Android SDK из-за недоступного прокси. `build-attempt.log` — журнал этой первоначальной неудачной локальной попытки, а не результат финальной сборки.

Первый запуск GitHub Actions остановился на попытке установить удалённый из каталога SDK пакет `tools`. Workflow исправлен: выбран `platform-tools`, runner Ubuntu 24.04 и Java 17. Добавлен официальный Gradle Wrapper 8.9. После исправления сборка прошла полностью.
