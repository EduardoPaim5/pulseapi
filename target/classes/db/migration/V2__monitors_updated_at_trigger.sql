CREATE OR REPLACE FUNCTION set_monitors_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_monitors_updated_at ON monitors;
CREATE TRIGGER trg_monitors_updated_at
BEFORE UPDATE ON monitors
FOR EACH ROW
EXECUTE FUNCTION set_monitors_updated_at();
