
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT PRIMARY KEY,
    resource_id VARCHAR(50) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0)
);
