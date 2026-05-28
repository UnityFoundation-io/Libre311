-- Add standalone index so the jurisdiction_id FK retains index support
-- when we drop the compound index from V17
ALTER TABLE projects ADD INDEX idx_projects_jurisdiction_id (jurisdiction_id);

-- Drop the V17 compound index, then the virtual column
DROP INDEX idx_projects_jurisdiction_slug ON projects;
ALTER TABLE projects DROP COLUMN slug;

-- Add a real slug column, populated from name + id for uniqueness and stability
ALTER TABLE projects ADD COLUMN slug VARCHAR(255);

UPDATE projects SET slug = CONCAT(
    id,
    '-',
    LOWER(REGEXP_REPLACE(REGEXP_REPLACE(name, '[^a-zA-Z0-9 ]', ''), ' +', '-'))
);

ALTER TABLE projects MODIFY COLUMN slug VARCHAR(255) NOT NULL;

-- uk_projects_jurisdiction_slug covers jurisdiction_id as its leftmost prefix,
-- so the temporary standalone index is no longer needed
ALTER TABLE projects ADD UNIQUE KEY uk_projects_jurisdiction_slug (jurisdiction_id, slug);
DROP INDEX idx_projects_jurisdiction_id ON projects;
