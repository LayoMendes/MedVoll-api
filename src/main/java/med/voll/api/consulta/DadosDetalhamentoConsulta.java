package med.voll.api.consulta;

import med.voll.api.medico.Especialidade;

import java.time.LocalDateTime;

public record DadosDetalhamentoConsulta(
        Long id,
        Long idMedico,
        String medicoNome,
        Long idPaciente,
        String pacienteNome,
        Especialidade especialidade,
        LocalDateTime data,
        Boolean ativo,
        MotivoCancelamento motivoCancelamento) {


    public DadosDetalhamentoConsulta(Consulta consulta) {
        this(consulta.getId(),
                consulta.getMedico().getId(), consulta.getMedico().getNome(),
                consulta.getPaciente().getId(), consulta.getPaciente().getNome(),
                consulta.getMedico().getEspecialidade(),
                consulta.getData(), consulta.getAtivo(), consulta.getMotivoCancelamento());
    }
}
