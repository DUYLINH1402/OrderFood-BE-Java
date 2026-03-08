-- Fix coupons với created_at NULL
-- Đặt created_at = start_date nếu start_date có giá trị, ngược lại đặt = NOW()

UPDATE coupons
SET created_at = COALESCE(start_date, NOW())
WHERE created_at IS NULL;

UPDATE coupons
SET updated_at = COALESCE(updated_at, created_at, NOW())
WHERE updated_at IS NULL;

