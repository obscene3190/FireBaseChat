# FireBaseChat

[![Build Status](https://travis-ci.org/obscene3190/FireBaseChat.svg?branch=master)](https://travis-ci.org/obscene3190/FireBaseChat)

## Ссылка на скачиваение последней версии(постоянно обновляется). Текущая версия приложения 1.1.1:

https://yadi.sk/d/inyRPy0MFmRqAQ

## Интерфейс/реализация отдельных компонентов

- [x] реализация получения ключа с сервера
- [x] нормальная регистрация
- [x] кнопка выхода с нормальным singout
- [x] сменить название "Лоховской чат" на более презентабельное
- [x] проблема с форматом публичного RSA ключа - передача ключей в формате String на сервер, тк сейчас он только в виде класса PublicKey ```неактуально```
- [x] ??? добавить нормальный holder для чата, чтобы сообщения автоматически прокручивались(если буду успевать) ```есть более или менее приличная прокрутка и сообщения показываются нормально```
- [ ] мейби сделать для адмена локальную модерацию --> удаление сообщений(???)(если успею)
- [x] Дизайн приложения, чтобы более приятно смотрелось
- [x] Добавить фичи, типа кнопочек на экране регистрации, подписи и тд, чтобы все ок было```добавлены подписи при регистрации```
- [x] Снова объединить Админа и Юзера в один клиент, чтобы можно было быть Админом на любом устройстве
- [x] Хранение ключей админа в коде, но в зашифрованном виде, будут расшифровываться только после того, как Админ введет пароль от аккаунта своего
- [x] Перейти на RecyclerView
- [ ] Уведомления

## Передача ключей через Диффи-Хеллмана:

- [x] реализовать алгоритм передачи сессиооных ключей через ДХ
- [x] встроить эту конструкцию в приложение
- [x] на тестовом запуске Админ/Юзер в одном приложении добиться успеха
- [x] постричься
- [x] добиться более или менее нормальной скорости при создании ключа  - примерно 20 секунд
- [x] выполнить разделение клиента Админа и Юзера
- [x] постараться реализовать обмен ключами на стадии регистрации и создавать на сервере ячейку пользователя с этими ключами(да, они у всех по итогу одинаковые, но пользователь будет получать доступ через свой индивидуальный номер)```неактуально```
- [x] реализовать хранение приватного и публичного ключа на устройстве череез SharedReference(причем на одном устройстве могут спокойно регаться несколько пользователей, так как для каждого создается отдельная папка)
- [x] Избежать хранения ключей на сервере, теперь ключи хранятся у пользователя на устройстве


