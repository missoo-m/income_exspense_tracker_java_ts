-- V2__Add_unique_constraints.sql

-- проверка на дубликаты 

DELETE FROM categories c1
USING categories c2
WHERE c1.id > c2.id 
  AND c1.user_id = c2.user_id 
  AND c1.name = c2.name;

DELETE FROM budgets b1
USING budgets b2
WHERE b1.id > b2.id 
  AND b1.user_id = b2.user_id 
  AND b1.month = b2.month 
  AND b1.general_category = b2.general_category;

-- добавление индексов 

-- поиск расходов по дате
CREATE INDEX IF NOT EXISTS idx_expense_date ON expense(date);

-- поиск доходов по дате
CREATE INDEX IF NOT EXISTS idx_income_date ON income(date);

-- поиск уведомлений по пользователю и статусу прочтения
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read);

-- поиса бюджетов по месяцу
CREATE INDEX IF NOT EXISTS idx_budgets_month ON budgets(month);

