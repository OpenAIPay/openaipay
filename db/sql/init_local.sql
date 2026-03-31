-- Local minimal initialization entry (DDL + base config + admin)
-- Usage:
--   mysql -h127.0.0.1 -P3306 -u<user> -p portal < db/sql/init_local.sql

SOURCE db/sql/00_schema.sql;
SOURCE db/sql/10_base_dict_config.sql;
SOURCE db/sql/20_admin_seed.sql;
