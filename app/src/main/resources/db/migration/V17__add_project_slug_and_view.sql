-- Add a virtual generated column for the slug
ALTER TABLE projects
ADD COLUMN slug VARCHAR(255) GENERATED ALWAYS AS (
    LOWER(
        REGEXP_REPLACE(
            REGEXP_REPLACE(name, '[^a-zA-Z0-9[:space:]]', ''),
            '[[:space:]]+',
            '-'
        )
    )
) VIRTUAL;

-- Add an index on jurisdiction_id and slug for fast lookups
CREATE INDEX idx_projects_jurisdiction_slug ON projects (jurisdiction_id, slug);

-- Create a view for open projects
CREATE OR REPLACE VIEW open_projects AS
SELECT *
FROM projects
WHERE closed_date IS NULL AND end_date >= CURRENT_TIMESTAMP;
