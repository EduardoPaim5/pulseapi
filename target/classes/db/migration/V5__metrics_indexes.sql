CREATE INDEX IF NOT EXISTS idx_check_runs_monitor_started ON check_runs(monitor_id, started_at);
CREATE INDEX IF NOT EXISTS idx_check_runs_monitor_latency ON check_runs(monitor_id, latency_ms);
CREATE INDEX IF NOT EXISTS idx_alerts_status_created ON alerts(status, created_at);
