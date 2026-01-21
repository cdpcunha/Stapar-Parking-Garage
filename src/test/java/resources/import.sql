-- Insere Setores Iniciais
INSERT INTO sector (code, base_price, max_capacity) VALUES ('A', 10.00, 100);
INSERT INTO sector (code, base_price, max_capacity) VALUES ('B', 15.00, 50);

-- Insere Algumas Vagas para teste manual
INSERT INTO spot (id, sector_code, lat, lng) VALUES (1, 'A', -23.561684, -46.655981);