ALTER TABLE messages
ALTER COLUMN datetime SET DEFAULT NOW(),
ALTER COLUMN datetime SET NOT NULL;
