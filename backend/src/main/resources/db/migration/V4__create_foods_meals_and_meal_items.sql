CREATE TABLE IF NOT EXISTS foods (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(20) NOT NULL,
    source_code VARCHAR(50),
    name VARCHAR(255) NOT NULL,
    kcal_per_100g NUMERIC(10,2) NOT NULL,
    carbs_per_100g NUMERIC(10,2) NOT NULL,
    protein_per_100g NUMERIC(10,2) NOT NULL,
    fat_per_100g NUMERIC(10,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_foods_name ON foods (name);
CREATE INDEX IF NOT EXISTS idx_foods_source_code ON foods (source, source_code);

CREATE TABLE IF NOT EXISTS meals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(30) NOT NULL,
    consumed_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meals_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_meals_user_date ON meals (user_id, meal_date);

CREATE TABLE IF NOT EXISTS meal_items (
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    grams NUMERIC(10,2) NOT NULL,
    kcal_consumed NUMERIC(10,2) NOT NULL,
    carbs_consumed NUMERIC(10,2) NOT NULL,
    protein_consumed NUMERIC(10,2) NOT NULL,
    fat_consumed NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_items_meal FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_items_food FOREIGN KEY (food_id) REFERENCES foods(id),
    CONSTRAINT ck_meal_items_grams_positive CHECK (grams > 0)
);

CREATE INDEX IF NOT EXISTS idx_meal_items_meal ON meal_items (meal_id);
