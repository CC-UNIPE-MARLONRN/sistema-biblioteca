package biblioteca;

import java.util.Objects;

public final class Livro {
    private final String identificador;
    private final String titulo;
    private final String autor;
    private final String categoria;

    public Livro(String identificador, String titulo, String autor, String categoria) {
        this.identificador = validarTexto(identificador, "identificador");
        this.titulo = validarTexto(titulo, "título");
        this.autor = validarTexto(autor, "autor");
        this.categoria = validarTexto(categoria, "categoria");
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

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return identificador + " - " + titulo + " (" + categoria + ")";
    }
}
