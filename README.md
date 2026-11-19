# 📚 Sistema de Gerenciamento de Biblioteca

[![Java](https://img.shields.io/badge/Java-8%2B-orange?logo=openjdk)](https://openjdk.org/)
[![Build](https://img.shields.io/badge/Build-javac-blue)]()
[![License](https://img.shields.io/badge/License-Educational-green)]()
[![Status](https://img.shields.io/badge/Status-Complete-brightgreen)]()

> Implementação em **Java** da atividade prática de **Orientação a Objetos e Programação Funcional** — Sistema de gerenciamento de biblioteca universitária com empréstimos, multas, relatórios e estatísticas. Código 100% em memória, sem dependências externas, sem framework.

---

## 🎯 Objetivo

Desenvolver um sistema que integre **Programação Orientada a Objetos (POO)** e **Programação Funcional (PF)** aplicando:

| Conceito POO | Conceito PF |
|--------------|-------------|
| Encapsulamento | Funções puras |
| Relacionamentos entre objetos | Imutabilidade |
| Distribuição de responsabilidades | Transparência referencial |
| | Funções de alta ordem (`map`, `filter`, `reduce`) |

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                         Main                                    │
│  (Demonstração + Verificações Automáticas)                     │
└──────────────────────────┬──────────────────────────────────────┘
                           │
         ┌─────────────────┼─────────────────┐
         ▼                 ▼                 ▼
┌────────────────┐ ┌───────────────┐ ┌──────────────┐
│   Biblioteca   │ │   Relatorios  │ │  Entidades   │
│  (Estado +     │ │  (Funções     │ │ (Imutáveis)  │
│   Regras Neg.) │ │   Puras)      │ │              │
└───────┬────────┘ └───────┬───────┘ └──────┬───────┘
        │                  │                │
        ▼                  ▼                ▼
┌────────────────┐ ┌───────────────┐ ┌──────────────┐
│  Map<String,   │ │  filter, map, │ │  Livro       │
│  Livro>        │ │  reduce       │ │  Usuario     │
│  Map<String,   │ │               │ │  Emprestimo  │
│  Usuario>      │ │  Estatísticas │ │              │
│  Map<String,   │ │  com reduce   │ │  DadosBiblio │
│  Emprestimo>   │ │               │ │  Multa       │
└────────────────┘ └───────────────┘ │  RelatorioM. │
                                     │  Estatisticas│
                                     └──────────────┘
```

### Separação de Responsabilidades

| Camada | Classe | Responsabilidade | Paradigma |
|--------|--------|------------------|-----------|
| **Entidades** | `Livro`, `Usuario`, `Emprestimo` | Dados imutáveis, validação no construtor | POO |
| **Gerenciamento** | `Biblioteca` | Estado mutável, regras de negócio, concorrência | POO (impuro) |
| **Consultas/Relatórios** | `Relatorios` | Funções puras, `filter`/`map`/`reduce`, imutabilidade | PF |
| **Demonstração** | `Main` | Setup, execução, verificações, teste concorrente | — |

---

## 📦 Estrutura do Projeto

```
.
├── src/
│   └── biblioteca/
│       ├── Livro.java              # Entidade Livro (imutável)
│       ├── Usuario.java            # Entidade Usuario (imutável)
│       ├── Emprestimo.java         # Entidade Emprestimo (imutável)
│       ├── Biblioteca.java         # Gerenciamento + estado + sync
│       ├── Relatorios.java         # Funções puras + estatísticas
│       └── Main.java               # Demo + testes automatizados
├── out/                            # Compilados (gitignored)
├── DIAGRAMAS_MANUSCRITOS.html      # 4 páginas A4 para referência manuscrita
├── README.md                       # Este arquivo
└── atividade_biblioteca.pdf        # Enunciado original
```

---

## 🚀 Como Compilar e Executar

### Pré-requisitos
- **JDK 8 ou superior** (testado com Eclipse Temurin 21)
- Terminal na raiz do projeto

### Windows (PowerShell)

```powershell
# A partir da raiz do projeto
javac -encoding UTF-8 -d out src\biblioteca\*.java
java -cp out biblioteca.Main
```

### Linux / macOS (bash/zsh)

```bash
# A partir da raiz
javac -encoding UTF-8 -d out src/biblioteca/*.java
java -cp out biblioteca.Main
```

### Verificação de compilação JDK 8 (compatibilidade)

```bash
javac --release 8 -encoding UTF-8 -d out src/biblioteca/*.java
java -cp out biblioteca.Main
```

---

## 📋 Saída Esperada

```
=== Sistema de Biblioteca: verificações concluídas ===
Livros da categoria Literatura: 2
Empréstimos ativos: 3
Empréstimos ativos atrasados: 1
Total de multas: R$ 6.00
Multa E1 | Livro: Dom Casmurro | Usuário: Joana Lima | Dias: 3 | Valor: R$ 6.00
Livros por categoria: {Ciência=1, Literatura=2}
Empréstimos por usuário: {U1=2, U2=2}
Comparação imperativa/funcional: resultados iguais
Livro devolvido e novamente emprestado: verificado
Identificador duplicado com espaços: bloqueado
Dois empréstimos concorrentes do mesmo livro: somente um foi aceito
```

---

## 🔧 Funcionalidades Implementadas

### 1. Cadastro e Consulta
| Operação | Método | Descrição |
|----------|--------|-----------|
| Adicionar livro | `Biblioteca.adicionarLivro(Livro)` | Valida unicidade do identificador |
| Cadastrar usuário | `Biblioteca.cadastrarUsuario(Usuario)` | Valida unicidade do identificador |
| Consultar livro | `Biblioteca.consultarLivro(String id)` | Retorna `Livro` ou lança exceção |
| Consultar usuário | `Biblioteca.consultarUsuario(String id)` | Retorna `Usuario` ou lança exceção |
| Listar empréstimos | `Biblioteca.consultarEmprestimos()` | Histórico completo (imutável) |

### 2. Empréstimos e Devoluções
| Operação | Método | Regras |
|----------|--------|--------|
| Emprestar | `Biblioteca.emprestar(idEmp, idLiv, idUsu, dtEmp, dtPrev)` | Bloqueia se livro já tem empréstimo ativo; `synchronized` |
| Devolver | `Biblioteca.devolver(idEmp, dtDev)` | Cria novo `Emprestimo` imutável com data; libera livro |

### 3. Relatórios Funcionais (Classe `Relatorios`)

#### Filtragem de Livros (5.1)
```java
// Pura — não altera a lista original
List<Livro> filtrarLivrosPorCategoria(List<Livro> livros, String categoria)
```
- Usa `filter` + `equalsIgnoreCase`
- Retorna `Collections.unmodifiableList`

#### Cálculo de Multa — Função Pura (5.2)
```java
// Pura — mesma entrada = mesma saída, sem efeitos colaterais
BigDecimal calcularMulta(long diasAtraso, BigDecimal multaDiaria)
```
- `diasAtraso × multaDiaria`
- Valida entradas não-negativas

#### Empréstimos Atrasados (5.3)
```java
// Versão funcional
List<Emprestimo> emprestimosAtrasados(List<Emprestimo> emprestimos, LocalDate ref)

// Versão imperativa (para comparação — item 6)
List<Emprestimo> emprestimosAtrasadosImperativo(List<Emprestimo> emprestimos, LocalDate ref)
```
- `filter(Emprestimo::isAtivo)` → `filter(e -> e.isAtrasado(ref))`
- Ambas retornam listas imutáveis

#### Relatório de Multas — **filter → map → reduce obrigatórios** (5.4)
```java
RelatorioMultas relatorioMultas(List<Emprestimo> emprestimos, LocalDate ref)
```

```java
// Pipeline funcional:
emprestimosAtrasados(emprestimos, ref).stream()           // filter
    .map(emp -> new Multa(                                 // map
        emp.getIdentificador(),
        emp.getLivro().getTitulo(),
        emp.getUsuario().getNome(),
        emp.diasAtraso(ref),
        calcularMulta(emp.diasAtraso(ref), MULTA_DIARIA)
    ))
    .collect(Collectors.collectingAndThen(
        Collectors.toList(), Collections::unmodifiableList
    ))

// Reduce para total:
multas.stream()
    .map(Multa::getValor)
    .reduce(BigDecimal.ZERO, BigDecimal::add)
```

**Retorna:** `RelatorioMultas` contendo `List<Multa>` + `BigDecimal total`

#### Estatísticas — **pelo menos uma com `reduce`** (5.5)
```java
Estatisticas estatisticas(List<Livro> livros, List<Emprestimo> emprestimos, LocalDate ref)
```

| Estatística | Implementação |
|-------------|---------------|
| Livros por categoria | `groupingBy(Livro::getCategoria, TreeMap::new, counting())` |
| Empréstimos por usuário | `groupingBy(emp -> emp.getUsuario().getIdentificador(), TreeMap::new, counting())` |
| Empréstimos ativos | `filter(Emprestimo::isAtivo).map(e -> 1L).reduce(0L, Long::sum)` ⭐ **usa reduce** |
| Empréstimos atrasados | `emprestimosAtrasados(...).map(e -> 1L).reduce(0L, Long::sum)` ⭐ **usa reduce** |
| Total de multas | `totalMultas(emprestimos, ref)` (internamente usa `reduce`) |

---

## 🧪 Testes e Verificações Automáticas

O `Main.main()` executa uma suíte que valida todos os requisitos:

| Verificação | Requisito Atendido |
|-------------|-------------------|
| Consulta de livros/usuários | 3.1, 3.2, 3.4 |
| Bloqueio de livro ocupado | Regra 4.2 |
| Normalização de ID | Encapsulamento |
| Devolução e novo empréstimo | Regra 4.4 |
| Imutabilidade do filtro | 5.6 |
| Equivalência imperativa/funcional | Item 6 |
| Cálculo de multa (3 dias = R$ 6,00) | Regra 4.6, 5.2 |
| Relatório exclui devolvidos | Regra 4.7, 5.4 |
| Data prevista = referência → sem atraso | Regra 4.5 |
| Estatísticas com reduce | 5.5 |
| Concorrência (2 threads → 1 aceita) | Item 10, sincronização |

---

## 🔒 Concorrência

```java
public synchronized Emprestimo emprestar(...) { ... }
```

- **Problema**: *Race condition* — duas threads verificam disponibilidade antes de qualquer uma registrar
- **Solução**: `synchronized` garante atomicidade do *check-then-act*
- **Teste**: `Main.verificarConcorrencia()` usa `CountDownLatch` + `ExecutorService` para disparar 2 tentativas simultâneas

---

## ✅ Conformidade com Requisitos Técnicos (Seção 7)

| Requisito | Status | Onde |
|-----------|--------|------|
| 7.1 Quatro classes + relacionamentos | ✅ | `Livro`, `Usuario`, `Emprestimo`, `Biblioteca` |
| 7.2 Encapsulamento + responsabilidades | ✅ | Campos `private final`, getters, construtores validados |
| 7.3 ≥3 funções puras | ✅ | 6 em `Relatorios`: filtrar, calcularMulta, emprestimosAtrasados, relatorioMultas, totalMultas, estatisticas |
| 7.4 `map`, `filter`, `reduce` obrigatórios | ✅ | Pipeline em `relatorioMultas` + estatísticas |
| 7.5 `reduce` no relatório + ≥1 estatística | ✅ | `relatorioMultas` + `emprestimosAtivos` + `emprestimosAtrasados` |
| 7.6 Imutabilidade nos processamentos | ✅ | Entidades `final`, `unmodifiableList`, `devolver()` cria novo |
| 7.7 Separação pura/impura | ✅ | `Relatorios` (puro) vs `Biblioteca` (impuro) |
| 7.8 Execução + demonstração | ✅ | `Main` com verificações automáticas |
| 7.9 Nomes claros, **sem comentários no código** | ✅ | Verificado |
| 7.10 Documentação no README | ✅ | Este arquivo |

---

## 📐 Diagramas Manuscritos (Obrigatórios — Seção 8)

> **Cada aluno produz individualmente à mão**, em papel A4 branco sem pautas/quadriculados, identificado com nome completo.

### 8.1 Diagrama de Classes (UML)
- Classes, principais atributos e métodos
- Relacionamentos e multiplicidades
- Responsabilidades de cada classe

### 8.2 Diagrama de Fluxo de Processamento Funcional
- Fluxo do relatório de multas: obtenção → filtragem → transformação → agregação → exibição
- Cada etapa em bloco com setas: **nome • dados recebidos • resultado • pura/impura • operação (map/filter/reduce)**

**Arquivo de referência para cópia:** `DIAGRAMAS_MANUSCRITOS.html` (4 páginas A4 prontas para imprimir)

---

## ❓ Questões para Análise — Manuscritas (Seção 9)

Cada aluno responde à mão em papel A4 branco sem pauta:

| Questão | Tema |
|---------|------|
| **A** | Transparência referencial — escolha uma função pura e explique |
| **B** | Operação impura — identifique e explique por que não é pura |
| **C** | Imutabilidade — como foi aplicada e problemas que evita |
| **D** | Comparação imperativa vs funcional — legibilidade, manutenção, mutabilidade, efeitos colaterais |
| **E** | Concorrência — race condition; puras/imutabilidade bastam? Justifique |

**Respostas de referência:** `DIAGRAMAS_MANUSCRITOS.html` (páginas 3 e 4)

---

## 👥 Integrantes

| Nome | RGM |
|------|-----|
| Marlon Rufino do Nascimento | 35879718 |

> Em grupos: adicione todos os integrantes. **Cada um entrega seus próprios manuscritos e apresenta individualmente.**

---

## 📄 Licença

Uso educacional — atividade acadêmica de Linguagens de Programação (POO + PF).

---

## 🔗 Links

- [Enunciado Original (PDF)](atividade_biblioteca.pdf)
- [Diagramas e Respostas para Manuscrito (HTML)](DIAGRAMAS_MANUSCRITOS.html)