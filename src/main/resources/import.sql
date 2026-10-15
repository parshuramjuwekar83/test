-- Create table for member tokens
CREATE TABLE IF NOT EXISTS member_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  member_id VARCHAR(200) NOT NULL UNIQUE,
  token TEXT,
  expires_at TIMESTAMP WITH TIME ZONE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Create table for config entries
CREATE TABLE IF NOT EXISTS app_config (
  id VARCHAR(100) PRIMARY KEY,
  username VARCHAR(200),
  password VARCHAR(200),
  client_secret TEXT,
  endpoint VARCHAR(1000),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
