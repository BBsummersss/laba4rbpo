-- Выполнить администратором после первого запуска приложения, подключившись к shelter_db.
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO shelter_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO shelter_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO shelter_app;
ALTER DEFAULT PRIVILEGES FOR USER shelter_app IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO shelter_app;
ALTER DEFAULT PRIVILEGES FOR USER shelter_app IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO shelter_app;
