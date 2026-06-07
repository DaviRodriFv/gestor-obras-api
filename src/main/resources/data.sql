-- ============================================================
-- Dados de exemplo — Obras
-- ============================================================
-- Para rodar automaticamente ao iniciar, adicione ao application.properties:
--   spring.sql.init.mode=always
-- Requer que o admin (admgestor@gmail.com) já exista (criado pelo DataInitializer).
-- Execute manualmente via psql ou client SQL caso prefira.
-- ============================================================

INSERT INTO obra (id, usuario_id, nome, endereco, cliente, data_inicio, prazo_conclusao, status, criado_em)
SELECT gen_random_uuid(), f.id,
       'Residência Jardim Paulista',
       'Rua das Flores, 100 — São Paulo/SP',
       'Carlos Mendes',
       '2025-01-15', '2025-12-31',
       'EM_ANDAMENTO', NOW()
FROM funcionarios f WHERE f.email = 'admgestor@gmail.com' LIMIT 1;

INSERT INTO obra (id, usuario_id, nome, endereco, cliente, data_inicio, prazo_conclusao, status, criado_em)
SELECT gen_random_uuid(), f.id,
       'Condomínio Verde Vale',
       'Av. Paulista, 2000 — São Paulo/SP',
       'Construtora Alfa Ltda',
       '2025-03-01', '2026-06-30',
       'EM_ANDAMENTO', NOW()
FROM funcionarios f WHERE f.email = 'admgestor@gmail.com' LIMIT 1;

INSERT INTO obra (id, usuario_id, nome, endereco, cliente, data_inicio, prazo_conclusao, status, criado_em)
SELECT gen_random_uuid(), f.id,
       'Loja Comercial Centro',
       'Rua XV de Novembro, 55 — Curitiba/PR',
       'Marcos Oliveira',
       '2024-06-01', '2025-02-28',
       'CONCLUIDA', NOW()
FROM funcionarios f WHERE f.email = 'admgestor@gmail.com' LIMIT 1;

INSERT INTO obra (id, usuario_id, nome, endereco, cliente, data_inicio, prazo_conclusao, status, criado_em)
SELECT gen_random_uuid(), f.id,
       'Galpão Industrial Norte',
       'Rod. Anhanguera km 32 — Guarulhos/SP',
       'Indústrias Beta S.A.',
       '2025-05-10', '2026-03-15',
       'PAUSADA', NOW()
FROM funcionarios f WHERE f.email = 'admgestor@gmail.com' LIMIT 1;

INSERT INTO obra (id, usuario_id, nome, endereco, cliente, data_inicio, prazo_conclusao, status, criado_em)
SELECT gen_random_uuid(), f.id,
       'Edifício Residencial Horizonte',
       'Av. Brasil, 500 — Rio de Janeiro/RJ',
       'Incorporadora Gama',
       '2024-01-01', '2025-12-01',
       'CANCELADA', NOW()
FROM funcionarios f WHERE f.email = 'admgestor@gmail.com' LIMIT 1;
