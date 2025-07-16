-- Add 'PENDING_APPROVAL' to shift_trade_status_enum for shift_trades table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'shift_trade_status_enum') THEN
        CREATE TYPE shift_trade_status_enum AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'POSTED_TO_EVERYONE', 'PENDING_APPROVAL');
    ELSE
        -- Add value if not exists
        IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'PENDING_APPROVAL' AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'shift_trade_status_enum')) THEN
            ALTER TYPE shift_trade_status_enum ADD VALUE 'PENDING_APPROVAL';
        END IF;
    END IF;
END$$;
