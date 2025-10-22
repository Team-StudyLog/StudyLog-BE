INSERT INTO user_monthly_stat (user_id, year, month, record_count)
SELECT
    r.user_id,
    EXTRACT(YEAR FROM r.create_date)::INT AS year,
    EXTRACT(MONTH FROM r.create_date)::INT AS month,
    COUNT(*) AS record_count
FROM study_record r
GROUP BY r.user_id, year, month
ON CONFLICT (user_id, year, month)
    DO UPDATE SET record_count = EXCLUDED.record_count;
