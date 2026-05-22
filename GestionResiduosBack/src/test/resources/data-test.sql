-- Datos mínimos requeridos por DataInitializer para pruebas de integración
-- City
INSERT INTO citys (cityid, name, postalcode) VALUES (1, 'Bogotá', '110111');

-- District
INSERT INTO districts (districtid, name, code, cityid) VALUES (1, 'Kennedy', 'KEN', 1);

-- Neighborhood
INSERT INTO neighborhoods (neighborhoodid, name, districtid) VALUES (1, 'Barrio Test', 1);

