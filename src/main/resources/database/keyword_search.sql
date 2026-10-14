CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_chunks_law_id
    ON chunks (law_id);
CREATE INDEX IF NOT EXISTS idx_laws_name
    ON laws (name);
CREATE INDEX IF NOT EXISTS idx_chunks_title_trgm
    ON chunks USING gin (title gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_chunks_content_trgm
    ON chunks USING gin (content gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_prec_chunks_precedent_id
    ON prec_chunks (precedent_id);
CREATE INDEX IF NOT EXISTS idx_prec_chunks_title_lower_trgm
    ON prec_chunks USING gin (lower(title) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_prec_chunks_content_lower_trgm
    ON prec_chunks USING gin (lower(content) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_precedent_issue_lower_trgm
    ON precedent USING gin (lower(issue) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_precedent_judgment_summary_lower_trgm
    ON precedent USING gin (lower(judgment_summary) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_precedent_reference_articles_lower_trgm
    ON precedent USING gin (lower(reference_articles) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_precedent_reference_cases_lower_trgm
    ON precedent USING gin (lower(reference_cases) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_cases_case_number_lower_trgm
    ON cases USING gin (lower(case_number) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_cases_case_name_lower_trgm
    ON cases USING gin (lower(case_name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_cases_court_name_lower_trgm
    ON cases USING gin (lower(court_name) gin_trgm_ops);
