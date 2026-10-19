package med.voll.api.medico;

public record DadosListagemMedico(Long id, String nome, String email, String crm, String telefone, Especialidade especialidade, Boolean ativo) {


    public DadosListagemMedico(Medico medico){
        this(medico.getId(), medico.getNome(), medico.getEmail(), medico.getCrm(), medico.getTelefone(), medico.getEspecialidade(), medico.getAtivo());
    }


}
