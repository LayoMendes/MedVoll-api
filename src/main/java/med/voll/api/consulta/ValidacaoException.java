package med.voll.api.consulta;

/** Regra de negócio violada; a API responde 400 com a mensagem. */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
