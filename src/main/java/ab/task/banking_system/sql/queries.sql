-- Получить всех пользователей, у которых баланс на всех счетах больше 10 000.
SELECT u.* FROM users u WHERE EXISTS (SELECT 1 FROM accounts a0 WHERE a0.user_id = u.id)
    AND NOT EXISTS (SELECT 1 FROM accounts a WHERE a.user_id = u.id AND a.balance <= 10000);

-- Найти пользователя по email.
SELECT * FROM users WHERE email = 'adilet@example.com';

-- Получить сумму всех средств в системе.
SELECT SUM(balance) AS total_money FROM accounts;