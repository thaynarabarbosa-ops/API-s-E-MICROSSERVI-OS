public class Produto {
    private String nome;
    private double preco;
    private String categoria;
    public Produto(String nome, double preco, String categoria) {
        this.nome = nome;
        this.preco = preco;
        this.categoria = categoria;
    }
    public String getNome() {
        return nome;
    }
    public double getPreco() {
        return preco;
    }
    public String getCategoria() {
        return categoria;
    }
    @Override
    public String toString() {
        return "Produto{" +
                "nome='" + nome + '\'' +
                ", preco=" + preco +
                ", categoria='" + categoria + '\'' +
                '}';
    }
}

import java.util.*;
import java.util.stream.Collectors;
public class TesteProduto {
    public static void main(String[] args) {
        List<Produto> produtos = new ArrayList<>();
        produtos.add(new Produto("Notebook", 3500.0, "Eletrônicos"));
        produtos.add(new Produto("Smartphone", 2200.0, "Eletrônicos"));
        produtos.add(new Produto("Fone de Ouvido", 300.0, "Eletrônicos"));
        produtos.add(new Produto("Livro Java", 150.0, "Livros"));
        produtos.add(new Produto("Livro Python", 200.0, "Livros"));
        produtos.add(new Produto("TV 50", 2800.0, "Eletrônicos"));
        produtos.add(new Produto("Geladeira", 4000.0, "Eletrônicos"));
        produtos.add(new Produto("Livro Banco de Dados", 180.0, "Livros"));
        // a) forEach + if
        produtos.forEach(p -> {
            if (p.getCategoria().equals("Eletrônicos")) {
                System.out.println(p.getNome());
            }
        });
        // a) Stream + filter
        produtos.stream()
                .filter(p -> p.getCategoria().equals("Eletrônicos"))
                .forEach(p -> System.out.println(p.getNome()));
        // b) Preços maiores que 500
        List<Double> precosMaioresQue500 = produtos.stream()
                .filter(p -> p.getPreco() > 500.0)
                .map(Produto::getPreco)
                .collect(Collectors.toList());
        // c) Soma dos livros
        double totalLivros = produtos.stream()
                .filter(p -> p.getCategoria().equals("Livros"))
                .mapToDouble(Produto::getPreco)
                .sum();
        // d) Buscar produto
        Optional<Produto> produtoEncontrado = buscarProdutoPorNome(produtos, "Notebook");
        produtoEncontrado.ifPresent(p ->
                System.out.println("Produto encontrado: " + p)
        );
        Produto produtoNaoExiste = buscarProdutoPorNome(produtos, "Tablet")
                .orElseThrow(() -> new RuntimeException("Produto não encontrado!"));
        // f) Lista de nomes (lambda)
        List<String> nomesLambda = produtos.stream()
                .map(p -> p.getNome())
                .collect(Collectors.toList());
        // f) Lista de nomes (referência de método)
        List<String> nomesMetodo = produtos.stream()
                .map(Produto::getNome)
                .collect(Collectors.toList());
    }
    public static Optional<Produto> buscarProdutoPorNome(List<Produto> produtos, String nome) {
        return produtos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }
}