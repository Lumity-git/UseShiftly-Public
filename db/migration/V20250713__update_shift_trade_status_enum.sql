-- Migration to update allowed status values for shift_trades.status
-- Add POSTED_TO_EVERYONE to the allowed values

ALTER TABLE shift_trades
    DROP CONSTRAINT IF EXISTS shift_trades_status_check,
    ADD CONSTRAINT shift_trades_status_check
    CHECK (status IN (
        'PENDING',
        'PICKED_UP',
        'CANCELLED',
        'APPROVED',
        'REJECTED',
        'POSTED_TO_EVERYONE'
    ));
