-- 인증 서버로부터 받은 refresh_token을 저장할 컬럼 생성
ALTER TABLE users
  ADD COLUMN IF NOT EXISTS refresh_token VARCHAR(1000);