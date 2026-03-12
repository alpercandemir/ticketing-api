-- Seed users (password: "password" encoded with BCrypt)
INSERT INTO users (email, password_hash, roles, created_at) VALUES
('admin@example.com', '$2a$10$tGuebv1mYlZfY6KbdMv7uuUdRFm20ms1A.WABVdAsMY.EOJH426bK', 'ROLE_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO users (email, password_hash, roles, created_at) VALUES
('organizer@example.com', '$2a$10$tGuebv1mYlZfY6KbdMv7uuUdRFm20ms1A.WABVdAsMY.EOJH426bK', 'ROLE_ORGANIZER', CURRENT_TIMESTAMP);

INSERT INTO users (email, password_hash, roles, created_at) VALUES
('customer@example.com', '$2a$10$tGuebv1mYlZfY6KbdMv7uuUdRFm20ms1A.WABVdAsMY.EOJH426bK', 'ROLE_CUSTOMER', CURRENT_TIMESTAMP);
