-- Add policy content columns to jurisdictions table
-- These columns store markdown content for Terms of Use and Privacy Policy
-- Part of feature: 001-policy-acknowledgment

ALTER TABLE jurisdictions
    ADD COLUMN terms_of_use_content TEXT,
    ADD COLUMN privacy_policy_content TEXT;
