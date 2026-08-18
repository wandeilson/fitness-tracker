ALTER TABLE goals
    ALTER COLUMN carbs_g TYPE DOUBLE PRECISION USING carbs_g::double precision,
    ALTER COLUMN protein_g TYPE DOUBLE PRECISION USING protein_g::double precision,
    ALTER COLUMN fat_g TYPE DOUBLE PRECISION USING fat_g::double precision;

ALTER TABLE goals
    ADD COLUMN IF NOT EXISTS carbs_percent NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS protein_percent NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS fat_percent NUMERIC(5,2),
    ADD COLUMN IF NOT EXISTS carbs_calories NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS protein_calories NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS fat_calories NUMERIC(10,2);

UPDATE goals
SET
    carbs_percent = COALESCE(carbs_percent, 50.00),
    protein_percent = COALESCE(protein_percent, 25.00),
    fat_percent = COALESCE(fat_percent, 25.00),
    carbs_calories = COALESCE(carbs_calories, ROUND(calories * 0.50, 2)),
    protein_calories = COALESCE(protein_calories, ROUND(calories * 0.25, 2)),
    fat_calories = COALESCE(fat_calories, ROUND(calories * 0.25, 2)),
    carbs_g = COALESCE(carbs_g, ROUND((calories * 0.50) / 4.0, 2)),
    protein_g = COALESCE(protein_g, ROUND((calories * 0.25) / 4.0, 2)),
    fat_g = COALESCE(fat_g, ROUND((calories * 0.25) / 9.0, 2));

ALTER TABLE goals
    ALTER COLUMN carbs_percent SET NOT NULL,
    ALTER COLUMN protein_percent SET NOT NULL,
    ALTER COLUMN fat_percent SET NOT NULL,
    ALTER COLUMN carbs_calories SET NOT NULL,
    ALTER COLUMN protein_calories SET NOT NULL,
    ALTER COLUMN fat_calories SET NOT NULL;
