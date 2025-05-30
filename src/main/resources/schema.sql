-- Enable uuid-ossp extension (for gen_random_uuid in PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create user_info table
CREATE TABLE user_info (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100),
    email VARCHAR(255),
    password VARCHAR(255),
    phone_number VARCHAR(15),
    address VARCHAR(255),
    created_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Create driver_data table
CREATE TABLE driver_data (
    driver_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(15) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    phone_number VARCHAR(15) NOT NULL UNIQUE,
    email VARCHAR(50) UNIQUE,
    vehicle_type VARCHAR(50) NOT NULL,
    vehicle_number VARCHAR(20) NOT NULL,
    address VARCHAR(150),
    registered_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true
);

-- Create ride_info table
CREATE TABLE ride_info (
    ride_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES driver_data(driver_id),
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL
);

-- Create requests table
CREATE TABLE requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES user_info(user_id),
    ride_id UUID NOT NULL REFERENCES ride_info(ride_id),
    goods_description VARCHAR(500) NOT NULL,
    weight DOUBLE PRECISION NOT NULL CHECK (weight > 0),
    volume DOUBLE PRECISION NOT NULL CHECK (volume > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'DECLINED', 'ASSIGNED', 'CANCELLED')),
    assigned_driver_id UUID REFERENCES driver_data(driver_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create goods_info table
CREATE TABLE goods_info (
    goods_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_info(user_id),
    ride_id UUID NOT NULL REFERENCES ride_info(ride_id),
    description VARCHAR(100) NOT NULL,
    weight REAL NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Create user_role_data table
CREATE TABLE user_role_data (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID
);