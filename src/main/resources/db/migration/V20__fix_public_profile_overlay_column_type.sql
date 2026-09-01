ALTER TABLE tenants
    ALTER COLUMN public_background_overlay_percentage TYPE INTEGER
    USING public_background_overlay_percentage::INTEGER;

