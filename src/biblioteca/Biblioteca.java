package biblioteca;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Biblioteca {
    private final Map<String, Livro> livros = new LinkedHashMap<>();
    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final Map<String, Emprestimo> emprestimos = new LinkedHashMap<>();

    public synchronized void adicionarLivro(Livro livro) {
        Objects.requireNonNull(livro, "livro não pode ser nulo");
        if (livros.putIfAbsent(livro.getIdentificador(), livro) != null) {
            throw new IllegalArgumentException("Já existe um livro com esse identificador");
        }
    }

    public synchronized void cadastrarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario, "usuário não pode ser nulo");
        if (usuarios.putIfAbsent(usuario.getIdentificador(), usuario) != null) {
            throw new IllegalArgumentException("Já existe um usuário com esse identificador");
        }
    }

    public synchronized Livro consultarLivro(String identificadorLivro) {
        Livro livro = livros.get(identificadorLivro);
        if (livro == null) {
            throw new IllegalArgumentException("Livro não encontrado");
        }
        return livro;
    }

    public synchronized Usuario consultarUsuario(String identificadorUsuario) {
        Usuario usuario = usuarios.get(identificadorUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        return usuario;
    }

    public synchronized List<Emprestimo> consultarEmprestimos() {
        return Collections.unmodifiableList(new ArrayList<>(emprestimos.values()));
    }

    public synchronized Emprestimo emprestar(
            String identificadorEmprestimo,
            String identificadorLivro,
            String identificadorUsuario,
            LocalDate dataEmprestimo,
            LocalDate dataPrevistaDevolucao) {
        Objects.requireNonNull(identificadorEmprestimo, "identificador do empréstimo não pode ser nulo");
        String idEmprestimoNormalizado = identificadorEmprestimo.trim();
        if (idEmprestimoNormalizado.isEmpty()) {
            throw new IllegalArgumentException("Identificador do empréstimo não pode ser vazio");
        }
        Livro livro = livros.get(identificadorLivro);
        Usuario usuario = usuarios.get(identificadorUsuario);
        if (livro == null) {
            throw new IllegalArgumentException("Livro não encontrado");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        if (emprestimos.containsKey(idEmprestimoNormalizado)) {
            throw new IllegalArgumentException("Já existe um empréstimo com esse identificador");
        }
        boolean livroEmprestado = emprestimos.values().stream()
                .anyMatch(emprestimo -> emprestimo.isAtivo()
                        && emprestimo.getLivro().getIdentificador().equals(identificadorLivro));
        if (livroEmprestado) {
            throw new IllegalStateException("O livro já possui um empréstimo ativo");
        }
        Emprestimo emprestimo = new Emprestimo(
                idEmprestimoNormalizado,
                livro,
                usuario,
                dataEmprestimo,
                dataPrevistaDevolucao,
                null);
        emprestimos.put(emprestimo.getIdentificador(), emprestimo);
        return emprestimo;
    }

    public synchronized Emprestimo devolver(String identificadorEmprestimo, LocalDate dataDevolucao) {
        Emprestimo emprestimo = emprestimos.get(identificadorEmprestimo);
        if (emprestimo == null) {
            throw new IllegalArgumentException("Empréstimo não encontrado");
        }
        Emprestimo devolvido = emprestimo.devolver(dataDevolucao);
        emprestimos.put(identificadorEmprestimo, devolvido);
        return devolvido;
    }

    public synchronized DadosBiblioteca obterDados() {
        return new DadosBiblioteca(
                new ArrayList<>(livros.values()),
                new ArrayList<>(usuarios.values()),
                new ArrayList<>(emprestimos.values()));
    }

    public static final class DadosBiblioteca {
        private final List<Livro> livros;
        private final List<Usuario> usuarios;
        private final List<Emprestimo> emprestimos;

        private DadosBiblioteca(
                List<Livro> livros, List<Usuario> usuarios, List<Emprestimo> emprestimos) {
            this.livros = Collections.unmodifiableList(new ArrayList<>(livros));
            this.usuarios = Collections.unmodifiableList(new ArrayList<>(usuarios));
            this.emprestimos = Collections.unmodifiableList(new ArrayList<>(emprestimos));
        }

        public List<Livro> getLivros() {
            return livros;
        }

        public List<Usuario> getUsuarios() {
            return usuarios;
        }

        public List<Emprestimo> getEmprestimos() {
            return emprestimos;
        }
    }
}
