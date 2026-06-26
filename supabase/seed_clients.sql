-- ============================================================
-- Harnie — Seed: Clientes ficticios
-- Toma automaticamente el primer usuario de profiles
-- Ejecutar despues de migration_clients.sql
-- ============================================================

INSERT INTO clients (user_id, name, last_name, country, phone, email, document_type, document_number)
SELECT
    p.id,
    c.name,
    c.last_name,
    c.country::country_code,
    c.phone,
    c.email,
    c.document_type,
    c.document_number
FROM (SELECT id FROM profiles LIMIT 1) p
CROSS JOIN (VALUES
    -- Peru
    ('Carlos', 'Mendoza Quispe', 'PERU', '+51 987654321', 'carlos.mendoza@gmail.com', 'DNI', '72345678'),
    ('Maria', 'Flores Gutierrez', 'PERU', '+51 912345678', 'maria.flores@hotmail.com', 'DNI', '45678912'),
    ('Jorge', 'Ramirez Torres', 'PERU', '+51 945678123', 'jorge.ramirez@outlook.com', 'DNI', '10234567'),
    ('Ana', 'Huaman Lopez', 'PERU', '+51 976543210', 'ana.huaman@gmail.com', 'Carnet de extranjeria', 'CE-202345'),
    ('Luis', 'Paredes Castillo', 'PERU', '+51 934567890', 'luis.paredes@yahoo.com', 'DNI', '08765432'),
    ('Rosa', 'Vargas Diaz', 'PERU', '+51 961234567', 'rosa.vargas@gmail.com', 'Pasaporte', 'PE12345678'),

    -- Ecuador
    ('Santiago', 'Morales Vera', 'ECUADOR', '+593 991234567', 'santiago.morales@gmail.com', 'Cédula', '1712345678'),
    ('Daniela', 'Cevallos Ponce', 'ECUADOR', '+593 984567123', 'daniela.cevallos@hotmail.com', 'Cédula', '0923456789'),
    ('Andres', 'Suarez Bravo', 'ECUADOR', '+593 978912345', 'andres.suarez@outlook.com', 'Carnet de extranjeria', 'EC-567890'),
    ('Valentina', 'Rojas Guerrero', 'ECUADOR', '+593 962345678', 'valentina.rojas@gmail.com', 'Cédula', '1301234567'),

    -- Rusia
    ('Dmitri', 'Ivanov', 'RUSSIA', '+7 9161234567', 'dmitri.ivanov@mail.ru', 'Pasaporte', 'RU45678901'),
    ('Anastasia', 'Petrova', 'RUSSIA', '+7 9037654321', 'anastasia.petrova@yandex.ru', 'DNI', '7712345678'),
    ('Alexei', 'Volkov', 'RUSSIA', '+7 9259876543', 'alexei.volkov@gmail.com', 'Pasaporte', 'RU98765432'),
    ('Natalia', 'Sokolova', 'RUSSIA', '+7 9054321987', 'natalia.sokolova@mail.ru', 'DNI', '5009876543')
) AS c(name, last_name, country, phone, email, document_type, document_number);
