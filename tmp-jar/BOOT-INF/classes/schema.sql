CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT
);

CREATE TABLE rides (
    id BIGINT PRIMARY KEY AUTO_INCREMENT
);

CREATE TABLE drivers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT
);

CREATE TABLE requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    ride_id BIGINT NOT NULL,
    goods_description VARCHAR(500) NOT NULL,
    weight DECIMAL(10,2) NOT NULL CHECK (weight > 0),
    volume DECIMAL(10,2) NOT NULL CHECK (volume > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'ASSIGNED', 'CANCELLED')),
    assigned_driver_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (ride_id) REFERENCES rides(id),
    FOREIGN KEY (assigned_driver_id) REFERENCES drivers(id)
);