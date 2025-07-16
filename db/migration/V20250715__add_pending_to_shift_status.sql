-- Add PENDING to allowed shift status values
ALTER TABLE shifts DROP CONSTRAINT IF EXISTS shifts_status_check;
ALTER TABLE shifts ADD CONSTRAINT shifts_status_check CHECK (
    status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'AVAILABLE_FOR_PICKUP', 'PENDING')
);
