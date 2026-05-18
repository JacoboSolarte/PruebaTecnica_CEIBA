INSERT INTO bicicleta (codigo, tipo, estado, fecha_creacion)
SELECT 'BIC-001', 'URBANA', 'DISPONIBLE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM bicicleta WHERE codigo = 'BIC-001');

INSERT INTO bicicleta (codigo, tipo, estado, fecha_creacion)
SELECT 'BIC-002', 'MONTAÑA', 'DISPONIBLE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM bicicleta WHERE codigo = 'BIC-002');

INSERT INTO bicicleta (codigo, tipo, estado, fecha_creacion)
SELECT 'BIC-003', 'ELÉCTRICA', 'DISPONIBLE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM bicicleta WHERE codigo = 'BIC-003');

INSERT INTO bicicleta (codigo, tipo, estado, fecha_creacion)
SELECT 'BIC-004', 'MONTAÑA', 'EN_MANTENIMIENTO', NOW()
WHERE NOT EXISTS (SELECT 1 FROM bicicleta WHERE codigo = 'BIC-004');

INSERT INTO bicicleta (codigo, tipo, estado, fecha_creacion)
SELECT 'BIC-005', 'URBANA', 'DISPONIBLE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM bicicleta WHERE codigo = 'BIC-005');
