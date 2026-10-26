package med.voll.api.consulta;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import med.voll.api.medico.Especialidade;

import java.time.LocalDateTime;

// Mesmo formato enviado pelo front-end: { idMedico, idPaciente, data: "2026-10-27T14:00", especialidade }.
// O médico é opcional: sem ele, a API escolhe um médico livre da especialidade.
public record DadosAgendamentoConsulta(
        Long idMedico,

        @NotNull
        Long idPaciente,

        @NotNull
        @Future
        LocalDateTime data,

        Especialidade especialidade) {

}
