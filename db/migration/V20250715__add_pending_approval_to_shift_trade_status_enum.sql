-- Add PENDING_APPROVAL to allowed values for shift_trades.status
ALTER TABLE shift_trades
    DROP CONSTRAINT IF EXISTS shift_trades_status_check;

ALTER TABLE shift_trades
    ADD CONSTRAINT shift_trades_status_check
    CHECK (status IN (
        'PENDING', 'PENDING_APPROVAL', 'PICKED_UP', 'CANCELLED', 'APPROVED', 'REJECTED', 'POSTED_TO_EVERYONE'
    ));
    