package med.voll.api.consulta;

import med.voll.api.medico.Medico;
import med.voll.api.medico.MedicoRepository;
import med.voll.api.pacientes.Paciente;
import med.voll.api.pacientes.PacienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Regras de agendamento da clínica (as mesmas que o front-end já confere):
 * segunda a sábado, das 07:00 às 19:00 (última consulta às 18:00), consultas de 1 hora,
 * 30 minutos de antecedência para agendar e 24 horas para cancelar.
 */
@Service
public class AgendaDeConsultas {

    private static final int ABERTURA = 7;
    private static final int ULTIMO_HORARIO = 18;
    private static final long DURACAO_MINUTOS = 60;
    private static final long ANTECEDENCIA_MINUTOS = 30;
    private static final long ANTECEDENCIA_CANCELAMENTO_HORAS = 24;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;


    public DadosDetalhamentoConsulta agendar(DadosAgendamentoConsulta dados) {
        var paciente = pacienteRepository.findById(dados.idPaciente())
                .orElseThrow(() -> new ValidacaoException("Paciente não encontrado."));

        validarHorario(dados.data());
        validarPaciente(paciente, dados.data());

        var medico = escolherMedico(dados);
        validarMedico(medico, dados.data());

        var consulta = new Consulta(medico, paciente, dados.data());
        consultaRepository.save(consulta);
        return new DadosDetalhamentoConsulta(consulta);
    }

    public void cancelar(DadosCancelamentoConsulta dados) {
        var consulta = consultaRepository.findById(dados.idConsulta())
                .orElseThrow(() -> new ValidacaoException("Consulta não encontrada."));

        if (!consulta.getAtivo()) {
            throw new ValidacaoException("Esta consulta já foi cancelada.");
        }
        var horasAteConsulta = Duration.between(LocalDateTime.now(), consulta.getData()).toHours();
        if (horasAteConsulta < ANTECEDENCIA_CANCELAMENTO_HORAS) {
            throw new ValidacaoException("Cancelamentos exigem antecedência mínima de 24 horas.");
        }

        consulta.cancelar(dados.motivo());
    }


    private void validarHorario(LocalDateTime data) {
        if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ValidacaoException("A clínica não funciona aos domingos.");
        }
        if (data.getHour() < ABERTURA || data.getHour() > ULTIMO_HORARIO) {
            throw new ValidacaoException("Horário fora do funcionamento da clínica (07:00 às 19:00).");
        }
        if (Duration.between(LocalDateTime.now(), data).toMinutes() < ANTECEDENCIA_MINUTOS) {
            throw new ValidacaoException("A consulta deve ser agendada com no mínimo 30 minutos de antecedência.");
        }
    }

    private void validarPaciente(Paciente paciente, LocalDateTime data) {
        if (!paciente.getAtivo()) {
            throw new ValidacaoException("Paciente inativo não pode ter consultas agendadas.");
        }
        var inicioDoDia = data.toLocalDate().atStartOfDay();
        var fimDoDia = inicioDoDia.plusDays(1).minusNanos(1);
        if (consultaRepository.existsByPacienteIdAndAtivoTrueAndDataBetween(paciente.getId(), inicioDoDia, fimDoDia)) {
            throw new ValidacaoException("Este paciente já possui uma consulta neste dia.");
        }
    }

    private void validarMedico(Medico medico, LocalDateTime data) {
        if (!medico.getAtivo()) {
            throw new ValidacaoException("Médico inativo não pode receber consultas.");
        }
        if (consultaRepository.existsByMedicoIdAndAtivoTrueAndDataBetween(medico.getId(), inicioDoConflito(data), fimDoConflito(data))) {
            throw new ValidacaoException("O médico já possui outra consulta neste horário.");
        }
    }

    /** Usa o médico informado ou sorteia um livre da especialidade. */
    private Medico escolherMedico(DadosAgendamentoConsulta dados) {
        if (dados.idMedico() != null) {
            return medicoRepository.findById(dados.idMedico())
                    .orElseThrow(() -> new ValidacaoException("Médico não encontrado."));
        }
        if (dados.especialidade() == null) {
            throw new ValidacaoException("Informe o médico ou a especialidade.");
        }

        var ocupados = new HashSet<>(consultaRepository.idsDeMedicosOcupados(inicioDoConflito(dados.data()), fimDoConflito(dados.data())));
        List<Medico> livres = medicoRepository.findAll().stream()
                .filter(m -> m.getAtivo() && m.getEspecialidade() == dados.especialidade() && !ocupados.contains(m.getId()))
                .toList();
        if (livres.isEmpty()) {
            throw new ValidacaoException("Nenhum médico desta especialidade está livre neste horário.");
        }
        return livres.get(ThreadLocalRandom.current().nextInt(livres.size()));
    }

    // Duas consultas conflitam quando começam a menos de 1 hora uma da outra.
    private LocalDateTime inicioDoConflito(LocalDateTime data) {
        return data.minusMinutes(DURACAO_MINUTOS).plusSeconds(1);
    }

    private LocalDateTime fimDoConflito(LocalDateTime data) {
        return data.plusMinutes(DURACAO_MINUTOS).minusSeconds(1);
    }
}
