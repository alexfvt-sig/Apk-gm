# MonoHome — чёрно-белый лаунчер для Android

MonoHome показывает приложения сеткой, делает их значки монохромными и запускает выбранное приложение. Это минимальный домашний экран без рекламы и лишних разрешений.

## Важно

Android не разрешает обычному приложению перекрашивать значки внутри Samsung One UI Home. MonoHome работает как отдельный лаунчер: после выбора его главным домашним экраном установленные приложения будут отображаться чёрно-белыми внутри MonoHome. В меню настроек Samsung, списке последних приложений и других местах значки останутся исходными.

## Сборка APK

1. Установите Android Studio на компьютер.
2. Откройте в Android Studio папку `MonoHome`.
3. Дождитесь синхронизации Gradle и установки Android SDK 35, если Android Studio её предложит.
4. Выберите **Build → Build APK(s)**. Готовый файл появится в `app/build/outputs/apk/debug/app-debug.apk`.
5. Передайте APK на телефон и установите его. Если Android спросит, разрешите установку приложений из этого источника.
6. Откройте MonoHome и нажмите **Сделать главным экраном**. Подтвердите выбор лаунчера в системном окне Android.

Чтобы вернуть стандартный экран Samsung: **Настройки → Приложения → Приложения по умолчанию → Главный экран → One UI Home**.

## Требования проекта

- Android Studio с поддержкой Android Gradle Plugin 8.8.2
- Android SDK Platform 35
- Минимальная версия Android: 8.0 (API 26)

Проект не использует сторонние библиотеки и не запрашивает доступ к файлам, контактам или сети.

## GitHub Actions build

Workflow file: `.github/workflows/build-apk.yml`. After the project is pushed to the `main` branch, GitHub Actions builds a debug APK and stores it as the `MonoHome-debug-apk` workflow artifact for 30 days. A build can also be started manually from the repository's **Actions** tab.
