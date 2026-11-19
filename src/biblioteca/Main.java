package biblioteca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) throws Exception {
        LocalDate referencia = LocalDate.of(2026, 11, 8);
        Biblioteca biblioteca = new Biblioteca();

        Livro livro1 = new Livro("L1", "Dom Casmurro", "Machado de Assis", "Literatura");
        Livro livro2 = new Livro("L2", "Memórias Póstumas", "Machado de Assis", "Literatura");
        Livro livro3 = new Livro("L3", "Introdução à Ciência", "Ana Silva", "Ciência");
        biblioteca.adicionarLivro(livro1);
        biblioteca.adicionarLivro(livro2);
        biblioteca.adicionarLivro(livro3);
        verificar(biblioteca.consultarLivro("L1").getTitulo().equals("Dom Casmurro"),
                "A biblioteca deve permitir consultar livros cadastrados");

        Usuario usuario1 = new Usuario("U1", "Joana Lima", "2026001");
        Usuario usuario2 = new Usuario("U2", "Pedro Alves", "2026002");
        biblioteca.cadastrarUsuario(usuario1);
        biblioteca.cadastrarUsuario(usuario2);
        verificar(biblioteca.consultarUsuario("U1").getMatricula().equals("2026001"),
                "A biblioteca deve permitir consultar usuários cadastrados");

        biblioteca.emprestar(
                "E1", "L1", "U1", referencia.minusDays(10), referencia.minusDays(3));
        biblioteca.emprestar(
                "E2", "L2", "U2", referencia.minusDays(1), referencia);
        biblioteca.emprestar(
                "E3", "L3", "U1", referencia.minusDays(10), referencia.minusDays(5));
        biblioteca.devolver("E3", referencia.minusDays(1));
        biblioteca.emprestar(
                "E4", "L3", "U2", referencia, referencia.plusDays(2));

        boolean bloqueouEmprestimoDuplicado = false;
        try {
            biblioteca.emprestar(
                    "E5", "L1", "U2", referencia, referencia.plusDays(7));
        } catch (IllegalStateException esperado) {
            bloqueouEmprestimoDuplicado = true;
        }
        verificar(bloqueouEmprestimoDuplicado, "Um livro já emprestado não pode ser emprestado de novo");

        boolean bloqueouIdComEspacos = false;
        try {
            biblioteca.emprestar(
                    " E1 ", "L2", "U2", referencia, referencia.plusDays(7));
        } catch (IllegalArgumentException esperado) {
            bloqueouIdComEspacos = true;
        }
        verificar(bloqueouIdComEspacos, "Espaços não podem contornar a unicidade do identificador");
        verificar(biblioteca.consultarEmprestimos().size() == 4,
                "Um identificador repetido não pode substituir um registro existente");

        Biblioteca.DadosBiblioteca dados = biblioteca.obterDados();
        verificar(biblioteca.consultarEmprestimos().size() == 4,
                "A biblioteca deve permitir consultar o histórico de empréstimos");
        List<String> livrosAntes = dados.getLivros().stream()
                .map(Livro::getIdentificador)
                .collect(Collectors.toList());
        List<Livro> livrosLiteratura =
                Relatorios.filtrarLivrosPorCategoria(dados.getLivros(), "literatura");
        verificar(livrosLiteratura.size() == 2, "O filtro por categoria deve encontrar dois livros");
        verificar(livrosAntes.equals(dados.getLivros().stream()
                .map(Livro::getIdentificador)
                .collect(Collectors.toList())), "O filtro não deve alterar a coleção original");

        List<Emprestimo> atrasadosFuncional =
                Relatorios.emprestimosAtrasados(dados.getEmprestimos(), referencia);
        List<Emprestimo> atrasadosImperativo =
                Relatorios.emprestimosAtrasadosImperativo(dados.getEmprestimos(), referencia);
        verificar(identificadores(atrasadosFuncional).equals(identificadores(atrasadosImperativo)),
                "As versões funcional e imperativa devem produzir os mesmos empréstimos");
        verificar(atrasadosFuncional.size() == 1, "Somente um empréstimo ativo deve estar atrasado");

        BigDecimal multa = Relatorios.calcularMulta(3, Relatorios.MULTA_DIARIA);
        verificar(multa.compareTo(new BigDecimal("6.00")) == 0, "Três dias de atraso devem custar R$ 6,00");
        Relatorios.RelatorioMultas relatorio =
                Relatorios.relatorioMultas(dados.getEmprestimos(), referencia);
        List<Relatorios.Multa> multas = relatorio.getMultas();
        verificar(multas.size() == 1, "O relatório não deve incluir empréstimos devolvidos");
        verificar(relatorio.getTotal().compareTo(new BigDecimal("6.00")) == 0,
                "A soma das multas deve ser R$ 6,00");
        verificar(Relatorios.calcularMulta(0, Relatorios.MULTA_DIARIA)
                .compareTo(BigDecimal.ZERO) == 0, "Empréstimos sem atraso não geram multa");
        verificar(dados.getEmprestimos().stream()
                .filter(emprestimo -> emprestimo.getIdentificador().equals("E2"))
                .noneMatch(emprestimo -> emprestimo.isAtrasado(referencia)),
                "A data prevista igual à data de referência ainda não configura atraso");

        Relatorios.Estatisticas estatisticas =
                Relatorios.estatisticas(dados.getLivros(), dados.getEmprestimos(), referencia);
        verificar(estatisticas.getEmprestimosAtivos() == 3, "Devem existir três empréstimos ativos");
        verificar(estatisticas.getEmprestimosAtrasados() == 1, "Deve existir um empréstimo atrasado");
        verificar(estatisticas.getLivrosPorCategoria().get("Literatura") == 2,
                "A estatística deve contar os livros por categoria");
        verificar(estatisticas.getEmprestimosPorUsuario().get("U1") == 2,
                "A estatística deve contar empréstimos por identificador de usuário");
        verificarConcorrencia(referencia);

        System.out.println("=== Sistema de Biblioteca: verificações concluídas ===");
        System.out.println("Livros da categoria Literatura: " + livrosLiteratura.size());
        System.out.println("Empréstimos ativos: " + estatisticas.getEmprestimosAtivos());
        System.out.println("Empréstimos ativos atrasados: " + estatisticas.getEmprestimosAtrasados());
        System.out.println("Total de multas: R$ " + relatorio.getTotal());
        for (Relatorios.Multa item : multas) {
            System.out.println("Multa " + item.getIdentificadorEmprestimo()
                    + " | Livro: " + item.getTituloLivro()
                    + " | Usuário: " + item.getNomeUsuario()
                    + " | Dias: " + item.getDiasAtraso()
                    + " | Valor: R$ " + item.getValor());
        }
        System.out.println("Livros por categoria: " + estatisticas.getLivrosPorCategoria());
        System.out.println("Empréstimos por usuário: " + estatisticas.getEmprestimosPorUsuario());
        System.out.println("Comparação imperativa/funcional: resultados iguais");
        System.out.println("Livro devolvido e novamente emprestado: verificado");
        System.out.println("Identificador duplicado com espaços: bloqueado");
        System.out.println("Dois empréstimos concorrentes do mesmo livro: somente um foi aceito");
    }

    private static void verificarConcorrencia(LocalDate referencia) throws Exception {
        Biblioteca biblioteca = new Biblioteca();
        biblioteca.adicionarLivro(new Livro("LC", "Livro Concorrido", "Autoria", "Teste"));
        biblioteca.cadastrarUsuario(new Usuario("UC1", "Usuário Um", "M1"));
        biblioteca.cadastrarUsuario(new Usuario("UC2", "Usuário Dois", "M2"));

        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> primeiro = executor.submit(() -> {
                inicio.await();
                return tentarEmprestimoConcorrente(biblioteca, "EC1", "UC1", referencia);
            });
            Future<Boolean> segundo = executor.submit(() -> {
                inicio.await();
                return tentarEmprestimoConcorrente(biblioteca, "EC2", "UC2", referencia);
            });
            inicio.countDown();
            int aceitos = (primeiro.get() ? 1 : 0) + (segundo.get() ? 1 : 0);
            verificar(aceitos == 1, "Exatamente um empréstimo concorrente deve ser aceito");
            verificar(Relatorios.estatisticas(
                    biblioteca.obterDados().getLivros(),
                    biblioteca.obterDados().getEmprestimos(),
                    referencia).getEmprestimosAtivos() == 1,
                    "A concorrência não pode criar mais de um empréstimo ativo para o livro");
        } finally {
            executor.shutdownNow();
        }
    }

    private static boolean tentarEmprestimoConcorrente(
            Biblioteca biblioteca, String idEmprestimo, String idUsuario, LocalDate referencia) {
        try {
            biblioteca.emprestar(
                    idEmprestimo, "LC", idUsuario, referencia, referencia.plusDays(7));
            return true;
        } catch (IllegalStateException esperado) {
            return false;
        }
    }

    private static List<String> identificadores(List<Emprestimo> emprestimos) {
        return emprestimos.stream()
                .map(Emprestimo::getIdentificador)
                .sorted()
                .collect(Collectors.toList());
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }
}
