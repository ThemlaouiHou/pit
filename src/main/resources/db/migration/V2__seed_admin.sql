INSERT INTO users (email, password, role, created_at)
VALUES (
           'admin@pit.local',
           -- Example bcrypt hash for "admin"; replace it for production deployments.
           '$2a$10$yCc1gx4rsQ1JaBtaEM6GDO5t3PNUTT.yPH6YKuYzvEPUp3PMq5x1q',
           'ADMIN',
           CURRENT_TIMESTAMP
       );
