package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class Relatorios {
    public static final BigDecimal MULTA_DIARIA = new BigDecimal("2.00");

    private Relatorios() {
    }

    public static List<Livro> filtrarLivrosPorCategoria(List<Livro> livros, String categoria) {
        Objects.requireNonNull(livros, "livros não pode ser nulo");
        Objects.requireNonNull(categoria, "categoria não pode ser nula");
        String categoriaNormalizada = categoria.trim();
        return Collections.unmodifiableList(livros.stream()
                .filter(livro -> livro.getCategoria().equalsIgnoreCase(categoriaNormalizada))
                .collect(Collectors.toList()));
    }

    public static BigDecimal calcularMulta(long diasAtraso, BigDecimal multaDiaria) {
        Objects.requireNonNull(multaDiaria, "multa diária não pode ser nula");
        if (diasAtraso < 0 || multaDiaria.signum() < 0) {
            throw new IllegalArgumentException("Dias e valor da multa não podem ser negativos");
        }
        return multaDiaria.multiply(BigDecimal.valueOf(diasAtraso));
    }

    public static List<Emprestimo> emprestimosAtrasados(
            List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        Objects.requireNonNull(emprestimos, "empréstimos não pode ser nulo");
        Objects.requireNonNull(dataReferencia, "data de referência não pode ser nula");
        return Collections.unmodifiableList(emprestimos.stream()
                .filter(Emprestimo::isAtivo)
                .filter(emprestimo -> emprestimo.isAtrasado(dataReferencia))
                .collect(Collectors.toList()));
    }

    public static List<Emprestimo> emprestimosAtrasadosImperativo(
            List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        Objects.requireNonNull(emprestimos, "empréstimos não pode ser nulo");
        Objects.requireNonNull(dataReferencia, "data de referência não pode ser nula");
        List<Emprestimo> atrasados = new ArrayList<>();
        for (Emprestimo emprestimo : emprestimos) {
            if (emprestimo.isAtivo() && emprestimo.isAtrasado(dataReferencia)) {
                atrasados.add(emprestimo);
            }
        }
        return Collections.unmodifiableList(atrasados);
    }

    public static RelatorioMultas relatorioMultas(
            List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        List<Multa> multas = emprestimosAtrasados(emprestimos, dataReferencia).stream()
                .map(emprestimo -> new Multa(
                        emprestimo.getIdentificador(),
                        emprestimo.getLivro().getTitulo(),
                        emprestimo.getUsuario().getNome(),
                        emprestimo.diasAtraso(dataReferencia),
                        calcularMulta(emprestimo.diasAtraso(dataReferencia), MULTA_DIARIA)))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(), Collections::unmodifiableList));
        BigDecimal total = multas.stream()
                .map(Multa::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RelatorioMultas(multas, total);
    }

    public static BigDecimal totalMultas(
            List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        return relatorioMultas(emprestimos, dataReferencia).getTotal();
    }

    public static Estatisticas estatisticas(
            List<Livro> livros, List<Emprestimo> emprestimos, LocalDate dataReferencia) {
        Objects.requireNonNull(livros, "livros não pode ser nulo");
        Objects.requireNonNull(emprestimos, "empréstimos não pode ser nulo");
        Objects.requireNonNull(dataReferencia, "data de referência não pode ser nula");

        Map<String, Long> livrosPorCategoria = livros.stream()
                .collect(Collectors.groupingBy(
                        Livro::getCategoria, TreeMap::new, Collectors.counting()));
        Map<String, Long> emprestimosPorUsuario = emprestimos.stream()
                .collect(Collectors.groupingBy(
                        emprestimo -> emprestimo.getUsuario().getIdentificador(),
                        TreeMap::new,
                        Collectors.counting()));
        long emprestimosAtivos = emprestimos.stream()
                .filter(Emprestimo::isAtivo)
                .map(emprestimo -> 1L)
                .reduce(0L, Long::sum);
        long emprestimosAtrasados = emprestimosAtrasados(emprestimos, dataReferencia).stream()
                .map(emprestimo -> 1L)
                .reduce(0L, Long::sum);
        BigDecimal multas = totalMultas(emprestimos, dataReferencia);

        return new Estatisticas(
                livrosPorCategoria, emprestimosPorUsuario,
                emprestimosAtivos, emprestimosAtrasados, multas);
    }

    public static final class Multa {
        private final String identificadorEmprestimo;
        private final String tituloLivro;
        private final String nomeUsuario;
        private final long diasAtraso;
        private final BigDecimal valor;

        private Multa(
                String identificadorEmprestimo,
                String tituloLivro,
                String nomeUsuario,
                long diasAtraso,
                BigDecimal valor) {
            this.identificadorEmprestimo = identificadorEmprestimo;
            this.tituloLivro = tituloLivro;
            this.nomeUsuario = nomeUsuario;
            this.diasAtraso = diasAtraso;
            this.valor = valor;
        }

        public String getIdentificadorEmprestimo() {
            return identificadorEmprestimo;
        }

        public String getTituloLivro() {
            return tituloLivro;
        }

        public String getNomeUsuario() {
            return nomeUsuario;
        }

        public long getDiasAtraso() {
            return diasAtraso;
        }

        public BigDecimal getValor() {
            return valor;
        }
    }

    public static final class RelatorioMultas {
        private final List<Multa> multas;
        private final BigDecimal total;

        private RelatorioMultas(List<Multa> multas, BigDecimal total) {
            this.multas = Collections.unmodifiableList(new ArrayList<>(multas));
            this.total = total;
        }

        public List<Multa> getMultas() {
            return multas;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    public static final class Estatisticas {
        private final Map<String, Long> livrosPorCategoria;
        private final Map<String, Long> emprestimosPorUsuario;
        private final long emprestimosAtivos;
        private final long emprestimosAtrasados;
        private final BigDecimal totalMultas;

        private Estatisticas(
                Map<String, Long> livrosPorCategoria,
                Map<String, Long> emprestimosPorUsuario,
                long emprestimosAtivos,
                long emprestimosAtrasados,
                BigDecimal totalMultas) {
            this.livrosPorCategoria = Collections.unmodifiableMap(new TreeMap<>(livrosPorCategoria));
            this.emprestimosPorUsuario =
                    Collections.unmodifiableMap(new TreeMap<>(emprestimosPorUsuario));
            this.emprestimosAtivos = emprestimosAtivos;
            this.emprestimosAtrasados = emprestimosAtrasados;
            this.totalMultas = totalMultas;
        }

        public Map<String, Long> getLivrosPorCategoria() {
            return livrosPorCategoria;
        }

        public Map<String, Long> getEmprestimosPorUsuario() {
            return emprestimosPorUsuario;
        }

        public long getEmprestimosAtivos() {
            return emprestimosAtivos;
        }

        public long getEmprestimosAtrasados() {
            return emprestimosAtrasados;
        }

        public BigDecimal getTotalMultas() {
            return totalMultas;
        }
    }
}
