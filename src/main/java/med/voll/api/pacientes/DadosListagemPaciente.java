package med.voll.api.pacientes;

public record DadosListagemPaciente(Long id, String nome, String email, String telefone, Boolean ativo) {


    public DadosListagemPaciente(Paciente paciente){
        this(paciente.getId(), paciente.getNome(), paciente.getEmail(), paciente.getTelefone(), paciente.getAtivo());
    }


}
