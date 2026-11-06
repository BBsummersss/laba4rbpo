-- Выполнить от имени администратора PostgreSQL один раз.
CREATE USER shelter_app WITH PASSWORD 'replace_with_a_local_password';
CREATE DATABASE shelter_db OWNER shelter_app;
GRANT CONNECT ON DATABASE shelter_db TO shelter_app;
-- После подключения к shelter_db выполнить:
-- GRANT USAGE, CREATE ON SCHEMA public TO shelter_app;
-- Для обычной работы приложению нужны права на таблицы и последовательности,
-- которые Hibernate создаёт от имени shelter_app при первом запуске.
