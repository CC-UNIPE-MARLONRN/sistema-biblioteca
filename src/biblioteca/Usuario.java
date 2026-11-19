package biblioteca;

import java.util.Objects;

public final class Usuario {
    private final String identificador;
    private final String nome;
    private final String matricula;

    public Usuario(String identificador, String nome, String matricula) {
        this.identificador = validarTexto(identificador, "identificador");
        this.nome = validarTexto(nome, "nome");
        this.matricula = validarTexto(matricula, "matrícula");
    }

    private static String validarTexto(String valor, String campo) {
        Objects.requireNonNull(valor, campo + " não pode ser nulo");
        String texto = valor.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException(campo + " não pode ser vazio");
        }
        return texto;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getNome() {
        return nome;
    }

    public String getMatricula() {
        return matricula;
    }

    @Override
    public String toString() {
        return identificador + " - " + nome + " (" + matricula + ")";
    }
}
