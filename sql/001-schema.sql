SET NAMES utf8mb4;

CREATE TABLE activity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  total_stock INT NOT NULL,
  available_stock INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE',
  start_at DATETIME NOT NULL,
  end_at DATETIME NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CHECK (available_stock >= 0)
);

CREATE TABLE ticket_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  request_id VARCHAR(64) NOT NULL,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_request_id (request_id),
  UNIQUE KEY uk_user_activity (user_id, activity_id),
  CONSTRAINT fk_order_activity FOREIGN KEY (activity_id) REFERENCES activity(id)
);

INSERT INTO activity (name, total_stock, available_stock, status, start_at, end_at)
VALUES ('2026 校园文化节开幕演出', 1000, 1000, 'ON_SALE', '2026-09-15 19:00:00', '2026-09-15 22:00:00');
