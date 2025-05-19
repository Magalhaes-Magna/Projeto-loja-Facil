package com.lojafacil.dao;

//Imports necessários
import com.lojafacil.model.Produto;
import com.lojafacil.util.ConexaoMySQL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade Produto.
 */
public class ProdutoDAO {

    /**
     * Insere um novo produto no banco de dados.
     * O ID é gerado automaticamente (AUTO_INCREMENT).
     * O ID gerado é definido de volta no objeto Produto.
     *
     * @param produto O objeto Produto a ser inserido (sem ID).
     * @return true se a inserção foi bem-sucedida, false caso contrário.
     */
    public boolean inserir(Produto produto) {
        String sql = "INSERT INTO PRODUTOS (nome, descricao, quantidade_estoque, preco_custo, preco_venda, estoque_minimo) VALUES (?, ?, ?, ?, ?, ?)";
        boolean sucesso = false;

        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, produto.getNome());
            pstmt.setString(2, produto.getDescricao());
            pstmt.setInt(3, produto.getQuantidadeEstoque());
            // Tratar possível NullPointerException se os valores double não forem inicializados
            pstmt.setDouble(4, produto.getPrecoCusto()); // Assumindo que nunca será null
            pstmt.setDouble(5, produto.getPrecoVenda()); // Assumindo que nunca será null
            pstmt.setInt(6, produto.getEstoqueMinimo());

            int linhasAfetadas = pstmt.executeUpdate();

            if (linhasAfetadas > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        produto.setId(generatedKeys.getInt(1)); // Define o ID gerado
                        sucesso = true;
                    } else {
                         System.err.println("Falha ao obter o ID gerado para o produto.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao inserir produto: " + e.getMessage());
            // Adicionar verificação de erro de duplicidade se houver constraints UNIQUE (ex: nome do produto?)
            e.printStackTrace();
        }
        return sucesso;
    }

    /**
     * Atualiza os dados de um produto existente.
     *
     * @param produto O objeto Produto com os dados atualizados (deve conter o ID).
     * @return true se a atualização foi bem-sucedida, false caso contrário.
     */
    public boolean atualizar(Produto produto) {
        String sql = "UPDATE PRODUTOS SET nome = ?, descricao = ?, quantidade_estoque = ?, preco_custo = ?, preco_venda = ?, estoque_minimo = ? WHERE id_produto = ?";
        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, produto.getNome());
            pstmt.setString(2, produto.getDescricao());
            pstmt.setInt(3, produto.getQuantidadeEstoque());
            pstmt.setDouble(4, produto.getPrecoCusto());
            pstmt.setDouble(5, produto.getPrecoVenda());
            pstmt.setInt(6, produto.getEstoqueMinimo());
            pstmt.setInt(7, produto.getId()); // ID no WHERE

            int linhasAfetadas = pstmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar produto (ID=" + produto.getId() + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exclui um produto pelo seu ID.
     * Atenção: Pode falhar se o produto estiver associado a itens de venda (Foreign Key).
     *
     * @param id O ID do produto a ser excluído.
     * @return true se a exclusão foi bem-sucedida, false caso contrário.
     */
    public boolean excluir(int id) {
        String sql = "DELETE FROM PRODUTOS WHERE id_produto = ?";
        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int linhasAfetadas = pstmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao excluir produto (ID=" + id + "): " + e.getMessage());
             if (e.getMessage().contains("foreign key constraint fails")) {
                 System.err.println("Não é possível excluir o produto (ID=" + id + ") pois ele está registrado em vendas.");
             }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca um produto pelo seu ID.
     *
     * @param id O ID do produto.
     * @return O objeto Produto encontrado, ou null se não existir ou ocorrer erro.
     */
    public Produto buscarPorId(int id) {
        String sql = "SELECT * FROM PRODUTOS WHERE id_produto = ?";
        Produto produto = null;
        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    produto = new Produto();
                    produto.setId(rs.getInt("id_produto"));
                    produto.setNome(rs.getString("nome"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
                    produto.setPrecoCusto(rs.getDouble("preco_custo"));
                    produto.setPrecoVenda(rs.getDouble("preco_venda"));
                    produto.setEstoqueMinimo(rs.getInt("estoque_minimo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produto por ID (" + id + "): " + e.getMessage());
            e.printStackTrace();
        }
        return produto;
    }

    /**
     * Lista todos los produtos cadastrados, ordenados por nome.
     *
     * @return Uma lista de Produtos (pode estar vazia).
     */
    public List<Produto> listarTodos() {
        List<Produto> listaProdutos = new ArrayList<>();
        String sql = "SELECT * FROM PRODUTOS ORDER BY nome";

        try (Connection conn = ConexaoMySQL.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id_produto"));
                produto.setNome(rs.getString("nome"));
                produto.setDescricao(rs.getString("descricao"));
                produto.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
                produto.setPrecoCusto(rs.getDouble("preco_custo"));
                produto.setPrecoVenda(rs.getDouble("preco_venda"));
                produto.setEstoqueMinimo(rs.getInt("estoque_minimo"));
                listaProdutos.add(produto);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os produtos: " + e.getMessage());
            e.printStackTrace();
        }
        return listaProdutos;
    }

    /**
     * Busca produtos pelo código (ID) ou por parte do nome.
     *
     * @param codigo O código (ID) do produto a buscar. Se <= 0, será ignorado.
     * @param nome Parte do nome do produto a buscar. Se null ou vazio, será ignorado.
     * @return Lista de produtos encontrados.
     */
    public List<Produto> buscarPorCodigoOuNome(int codigo, String nome) {
        List<Produto> produtosEncontrados = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM PRODUTOS WHERE 1=1");
        List<Object> params = new ArrayList<>();
        boolean hasCriteria = false;

        // Adiciona critério de Código (ID) se for válido
        if (codigo > 0) {
            sqlBuilder.append(" AND id_produto = ?");
            params.add(codigo);
            hasCriteria = true;
        }

        // Adiciona critério de Nome se fornecido
        if (nome != null && !nome.trim().isEmpty()) {
            sqlBuilder.append(" AND LOWER(nome) LIKE ?");
            params.add("%" + nome.trim().toLowerCase() + "%");
            hasCriteria = true;
        }

        if (!hasCriteria) {
            // return listarTodos(); // Ou retornar lista vazia se nenhum critério foi dado
             return produtosEncontrados;
        }

        sqlBuilder.append(" ORDER BY nome");

        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Produto produto = new Produto();
                    produto.setId(rs.getInt("id_produto"));
                    produto.setNome(rs.getString("nome"));
                    produto.setDescricao(rs.getString("descricao"));
                    produto.setQuantidadeEstoque(rs.getInt("quantidade_estoque"));
                    produto.setPrecoCusto(rs.getDouble("preco_custo"));
                    produto.setPrecoVenda(rs.getDouble("preco_venda"));
                    produto.setEstoqueMinimo(rs.getInt("estoque_minimo"));
                    produtosEncontrados.add(produto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar produtos por código/nome: " + e.getMessage());
            e.printStackTrace();
        }
        return produtosEncontrados;
    }

    /**
     * Atualiza o estoque de um produto específico.
     * Usado principalmente após uma venda ser finalizada.
     * Este método DEVE ser chamado dentro da mesma transação que registra a venda.
     *
     * @param idProduto O ID do produto a ter o estoque atualizado.
     * @param quantidadeVendida A quantidade que foi vendida (será subtraída do estoque).
     * @param conn A conexão ativa da transação (NÃO PODE ABRIR UMA NOVA CONEXÃO AQUI).
     * @return true se a atualização foi bem-sucedida, false caso contrário.
     * @throws SQLException Se ocorrer um erro durante a atualização ou se o estoque ficar negativo.
     */
    public boolean baixarEstoque(int idProduto, int quantidadeVendida, Connection conn) throws SQLException {
        // SQL para subtrair do estoque, com verificação para não negativar
        String sql = "UPDATE PRODUTOS SET quantidade_estoque = quantidade_estoque - ? WHERE id_produto = ? AND quantidade_estoque >= ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, quantidadeVendida);
            pstmt.setInt(2, idProduto);
            pstmt.setInt(3, quantidadeVendida); // Garante que estoque atual >= quantidade vendida

            int linhasAfetadas = pstmt.executeUpdate();

            // Se nenhuma linha foi afetada, significa que o estoque era insuficiente
            if (linhasAfetadas == 0) {
                 throw new SQLException("Estoque insuficiente para o produto ID " + idProduto + " ou produto não encontrado.");
            }
            return true; // Atualização bem-sucedida
        }
        // A SQLException será lançada para ser tratada pela transação no VendaDAO
    }

     /**
     * Adiciona uma quantidade ao estoque de um produto.
     * Pode ser usado para registrar entradas de mercadoria.
     *
     * @param idProduto O ID do produto.
     * @param quantidadeAdicionar A quantidade a ser adicionada.
     * @return true se a atualização foi bem-sucedida, false caso contrário.
     */
    public boolean adicionarEstoque(int idProduto, int quantidadeAdicionar) {
        if (quantidadeAdicionar <= 0) {
            return false; // Não faz sentido adicionar zero ou negativo
        }
        String sql = "UPDATE PRODUTOS SET quantidade_estoque = quantidade_estoque + ? WHERE id_produto = ?";
        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantidadeAdicionar);
            pstmt.setInt(2, idProduto);

            int linhasAfetadas = pstmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao adicionar estoque para produto ID " + idProduto + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}