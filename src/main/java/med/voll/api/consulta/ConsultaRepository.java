package med.voll.api.consulta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByPacienteIdAndAtivoTrueAndDataBetween(Long idPaciente, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByMedicoIdAndAtivoTrueAndDataBetween(Long idMedico, LocalDateTime inicio, LocalDateTime fim);

    // Busca médico e paciente junto, para a listagem não fazer uma consulta por linha.
    @Query(value = "select c from Consulta c join fetch c.medico join fetch c.paciente",
            countQuery = "select count(c) from Consulta c")
    Page<Consulta> listarComMedicoEPaciente(Pageable paginacao);

    @Query("""
            select c.medico.id from Consulta c
            where c.ativo = true
            and c.data between :inicio and :fim
            """)
    List<Long> idsDeMedicosOcupados(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
