CREATE TABLE IF NOT EXISTS vegan_locations (
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL,
    description text,
    address text NOT NULL,
    city varchar(100) NOT NULL,
    country varchar(100) NOT NULL,
    latitude double precision NOT NULL,
    longitude double precision NOT NULL,
    type varchar(50) NOT NULL,
    website_url varchar(255),
    phone_number varchar(20),
    opening_hours text,
    price_range varchar(20),
    user_id bigint REFERENCES users(id),
    is_verified boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

CREATE INDEX idx_vegan_locations_type ON vegan_locations (type);
CREATE INDEX idx_vegan_locations_city ON vegan_locations (city);
CREATE INDEX idx_vegan_locations_name ON vegan_locations (name);
CREATE INDEX idx_vegan_locations_user_id ON vegan_locations (user_id);