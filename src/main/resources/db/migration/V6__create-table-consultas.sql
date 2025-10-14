CREATE TABLE consultas (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  medico_id BIGINT NOT NULL,
  paciente_id BIGINT NOT NULL,
  data_hora DATETIME NOT NULL,
  observacoes VARCHAR(500),
  status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
  CONSTRAINT fk_consulta_medico FOREIGN KEY (medico_id)
      REFERENCES medicos(id) ON DELETE CASCADE,
  CONSTRAINT fk_consulta_paciente FOREIGN KEY (paciente_id)
      REFERENCES pacientes(id) ON DELETE CASCADE,
  CONSTRAINT uk_consulta_medico_horario UNIQUE (medico_id, data_hora)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE medicos   ADD COLUMN senha VARCHAR(255) NOT NULL;
ALTER TABLE pacientes ADD COLUMN senha VARCHAR(255) NOT NULL;
