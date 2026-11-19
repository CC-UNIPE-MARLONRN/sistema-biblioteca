package biblioteca;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class Emprestimo {
    private final String identificador;
    private final Livro livro;
    private final Usuario usuario;
    private final LocalDate dataEmprestimo;
    private final LocalDate dataPrevistaDevolucao;
    private final LocalDate dataDevolucao;

    public Emprestimo(
            String identificador,
            Livro livro,
            Usuario usuario,
            LocalDate dataEmprestimo,
            LocalDate dataPrevistaDevolucao,
            LocalDate dataDevolucao) {
        this.identificador = validarTexto(identificador, "identificador");
        this.livro = Objects.requireNonNull(livro, "livro não pode ser nulo");
        this.usuario = Objects.requireNonNull(usuario, "usuário não pode ser nulo");
        this.dataEmprestimo = Objects.requireNonNull(dataEmprestimo, "data do empréstimo não pode ser nula");
        this.dataPrevistaDevolucao =
                Objects.requireNonNull(dataPrevistaDevolucao, "data prevista não pode ser nula");
        if (dataPrevistaDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A devolução prevista não pode anteceder o empréstimo");
        }
        if (dataDevolucao != null && dataDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A devolução não pode anteceder o empréstimo");
        }
        this.dataDevolucao = dataDevolucao;
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

    public Livro getLivro() {
        return livro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public boolean isAtivo() {
        return dataDevolucao == null;
    }

    public long diasAtraso(LocalDate dataReferencia) {
        Objects.requireNonNull(dataReferencia, "data de referência não pode ser nula");
        if (!isAtivo() || !dataPrevistaDevolucao.isBefore(dataReferencia)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(dataPrevistaDevolucao, dataReferencia);
    }

    public boolean isAtrasado(LocalDate dataReferencia) {
        return diasAtraso(dataReferencia) > 0;
    }

    public Emprestimo devolver(LocalDate dataDevolucao) {
        Objects.requireNonNull(dataDevolucao, "data de devolução não pode ser nula");
        if (!isAtivo()) {
            throw new IllegalStateException("O empréstimo já foi devolvido");
        }
        if (dataDevolucao.isBefore(dataEmprestimo)) {
            throw new IllegalArgumentException("A devolução não pode anteceder o empréstimo");
        }
        return new Emprestimo(
                identificador, livro, usuario, dataEmprestimo, dataPrevistaDevolucao, dataDevolucao);
    }
}
