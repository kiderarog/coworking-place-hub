# 💻 Coworking Place Hub 
Приложение для краткосрочного или долгосрочного бронирования рабочего места в коворкингах.
**Находится на стадии разработки**.

Использованные техниологии: **Java, Spring Framework (Boot, Web, Data JPA, Security), Apache Kafka, Hibernate ORM, PostgreSQL, MongoDB, LiquiBase, ElasticSearch, Docker, Maven.**

## 🏛 Архитектура  
Архитектура приложения основана на **гибридном подходе**, где **основной сервис** отвечает за бизнес-логику, а его **обслуживающие микросервисы** обеспечивают масштабируемость и отказоустойчивость.  
✅ **Обмен данными между микросервисами реализован через Apache Kafka**  
✅ **Liquibase** управляет миграциями БД PostgreSQL, обеспечивая стабильность структуры данных  
✅ **Логирование событий приложения ведётся в ElasticSearch**, а их визуализация доступна через Kibana  

---

Некоторый функционал приложения доступен для использования только при соблюдении определенных условий, определенной бизнес-логикой приложения.

## 📋 **Описание реализованного функционала** на 12.04.2025:

  ### 💳 *Payment-microservice*
   * Интеграция платежной системы Stripe с использованием Stripe API и WebClient. (работает в тестовом режиме через тестовый API)
     Возможность пополнения баланса через личный кабинет пользователя.
     При инициации пополнения пользователю будет предложено ввести данные банковской карты в графический интерфейс платежной системы.
     При успешной финансовой транзакции, с помощью Apache Kafka в главный сервис отправляется событие на увеличение виртуального баланса пользователя.
   * Функционал для списания денежных средств с виртуального баланса пользователя;
   * ...

  ### 🔐 *Auth Service*
   * JWT-аутентификация пользователя для безопасного управления доступом с использованием токенов;
   * Процесс регистрации и авторизации пользователя.
       🔄 При регистрации пользователя отправляются в CRM Purys для дальнейшего администрирования (исп. Purys API и WebClient);     
   * Процесс сброса забытого пароля через одноразовый код, отправляемый на электронную почту клиента.
     Процесс инициации сброса реализован в auth-service, событие о котором с помощью Apache Kafka отправляется в Notification-microservice
     для дальнейшей отправки одноразового кода для смены забытого пароля на электронную почту пользователя, указанную при регистрации.
   * ...

  ### 🏢 *Admin Service*
   * Функционал добавления коворкинга в приложение;
   * Управление ценовой политикой коворкинга;
   * Временная заморозка коворкинга (коворкинг становится недоступным для новых бронирований);
   * Удаление коворкинга из приложения при отсутствии активных бронирований;
   * Получение статистики по каждому коворкингу;
   * Возможность блокировки и разблокировки пользователя;
   * ...
     
  ### 📅 *Booking Service*
   * Возможность бронирования места в коворкинге:
       * до конца дня;
       * на определенное количество дней;
       * на 30 календарных дней;
       * ...
         
  ### 👤 *Client Service*
   * Функционал отображения профиля пользователя;
   * Функционал для редактирования пользовательских данных (ограничен);
   * Удаление профиля (!) - при отсутствии активных бронирований;
   * Смена пароля для доступа в приложение;
   * ...
   * 

