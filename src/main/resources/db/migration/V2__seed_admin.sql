INSERT INTO users (email, password, role, created_at)
VALUES (
           'admin@pit.local',
           -- bcrypt "admin" d'exemple: change-le si besoin
           '$2a$10$yCc1gx4rsQ1JaBtaEM6GDO5t3PNUTT.yPH6YKuYzvEPUp3PMq5x1q',
           'ADMIN',
           CURRENT_TIMESTAMP
       );
