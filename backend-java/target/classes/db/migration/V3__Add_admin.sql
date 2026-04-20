-- V3__Add_admin.sql

-- Пароль: 123456 

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@expensetracker.com') THEN
        INSERT INTO users (full_name, email, password, role, profile_image_url, created_at, updated_at
        ) VALUES (
            'System Admin',                          
            'admin@expensetracker.com',              
            '"$2a$10$UgqGyu5REJFAlZtCURfuj.153KYS/KQ.bgtMMO.hcJXlG.uQ67Hum"', 
            'ADMIN',                                  
            NULL,                                     
            NOW(),                                   
            NOW()                                     
        );
        
        RAISE NOTICE 'Администратор admin@expensetracker.com успешно создан (пароль: 123456)';
    ELSE
        RAISE NOTICE 'Администратор admin@expensetracker.com уже существует';
    END IF;
END $$;