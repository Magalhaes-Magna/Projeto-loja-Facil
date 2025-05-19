package com.lojafacil.dao;

// Imports necessários
import com.lojafacil.model.Cliente;
import com.lojafacil.model.ItemVenda;
import com.lojafacil.model.Produto;
import com.lojafacil.model.Venda;
import com.lojafacil.util.ConexaoMySQL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO para a entidade Venda.
 * Responsável por registrar vendas completas (incluindo itens e baixa de estoque)
 * e por buscar vendas para relatórios.
 */
public class VendaDAO {

    // Dependência do ProdutoDAO para buscar produtos ao carregar vendas
    // e para chamar a atualização de estoque (embora a baixa seja feita na transação aqui)
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    // Dependência do ClienteDAO para buscar clientes ao carregar vendas
    private ClienteDAO clienteDAO = new ClienteDAO();

    /**
     * Registra uma venda completa no banco de dados, incluindo:
     * 1. Inserção na tabela VENDAS.
     * 2. Inserção de todos os itens na tabela ITENS_VENDA.
     * 3. Baixa no estoque dos produtos na tabela PRODUTOS.
     * Tudo isso é feito dentro de uma TRANSAÇÃO para garantir a atomicidade.
     *
     * @param venda O objeto Venda contendo o Cliente e a Lista de ItensVenda.
     * O ID da venda e dos itens será gerado pelo banco e atualizado no objeto.
     * @return true se a venda completa foi registrada com sucesso, false caso contrário.
     */
    public boolean registrarVendaCompleta(Venda venda) {
        Connection conn = null;
        boolean sucesso = false;

        // SQLs para as operações da transação
        String sqlVenda = "INSERT INTO VENDAS (id_cliente, data_venda) VALUES (?, ?)"; // Inclui data_venda explícita
        String sqlItem = "INSERT INTO ITENS_VENDA (id_venda, id_produto, quantidade, preco_venda_momento) VALUES (?, ?, ?, ?)";
        // A baixa de estoque será feita chamando o método do ProdutoDAO dentro da transação

        try {
            // 1. Obter Conexão e Iniciar Transação
            conn = ConexaoMySQL.conectar();
            conn.setAutoCommit(false); // Desabilita auto-commit para controlar a transação

            // 2. Inserir a Venda principal e obter o ID gerado
            try (PreparedStatement pstmtVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS)) {

                // Define o ID do cliente
                pstmtVenda.setInt(1, venda.getCliente().getId());
                // Define a data da venda (obtida do objeto Venda ou data atual)
                // java.util.Date precisa ser convertido para java.sql.Timestamp
                if (venda.getDataVenda() == null) {
                   venda.setDataVenda(new Date()); // Define data atual se for nula
                }
                pstmtVenda.setTimestamp(2, new java.sql.Timestamp(venda.getDataVenda().getTime()));

                pstmtVenda.executeUpdate(); // Executa o insert da venda

                // Recupera o ID gerado para a venda
                try (ResultSet generatedKeys = pstmtVenda.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        venda.setId(generatedKeys.getInt(1)); // Atualiza o objeto Venda com o ID
                    } else {
                        throw new SQLException("Falha ao obter o ID gerado para a Venda.");
                    }
                }
            } // pstmtVenda e generatedKeys são fechados

            // 3. Inserir os Itens da Venda (usando o ID da venda obtido)
            try (PreparedStatement pstmtItem = conn.prepareStatement(sqlItem)) { // Não precisa retornar keys dos itens aqui
                for (ItemVenda item : venda.getItensVenda()) {
                    pstmtItem.setInt(1, venda.getId()); // ID da venda recém-inserida
                    pstmtItem.setInt(2, item.getProduto().getId()); // ID do produto no item
                    pstmtItem.setInt(3, item.getQuantidade());
                    pstmtItem.setDouble(4, item.getPrecoVenda()); // Preço no momento da venda
                    pstmtItem.addBatch(); // Adiciona o comando ao lote para execução otimizada
                }
                pstmtItem.executeBatch(); // Executa todos os inserts de itens em lote
            } // pstmtItem é fechado

            // 4. Dar Baixa no Estoque para cada item vendido
            // Reutiliza a conexão da transação!
            ProdutoDAO produtoDAOTransacional = new ProdutoDAO(); // Usa a mesma lógica DAO, mas com a conexão da transação
            for (ItemVenda item : venda.getItensVenda()) {
                boolean baixou = produtoDAOTransacional.baixarEstoque(item.getProduto().getId(), item.getQuantidade(), conn);
                if (!baixou) {
                    // Se a baixa falhar (ex: estoque insuficiente verificado no UPDATE), lança exceção para dar rollback
                    // A exceção já é lançada pelo método baixarEstoque se linhasAfetadas == 0
                    // Apenas garantindo que a exceção será lançada e capturada abaixo
                    throw new SQLException("Não foi possível dar baixa no estoque para o produto ID: " + item.getProduto().getId());
                }
            }

            // 5. Se tudo correu bem, Comitar a Transação
            conn.commit();
            sucesso = true;
            System.out.println("Venda registrada com sucesso (ID: " + venda.getId() + ") e transação comitada.");

        } catch (SQLException e) {
            // 6. Se qualquer erro ocorrer, fazer Rollback
            System.err.println("Erro ao registrar venda completa (ID Venda provisório: " + venda.getId() + "). Iniciando rollback...");
            e.printStackTrace();
            if (conn != null) {
                try {
                    System.err.println("Tentando reverter transação...");
                    conn.rollback(); // Desfaz todas as operações feitas na transação
                    System.err.println("Rollback efetuado com sucesso.");
                } catch (SQLException ex) {
                    System.err.println("ERRO CRÍTICO ao tentar reverter a transação: " + ex.getMessage());
                     ex.printStackTrace();
                }
            }
            sucesso = false; // Garante que o retorno seja false
        } finally {
            // 7. Independentemente de sucesso ou falha, restaurar AutoCommit e fechar Conexão
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaura o modo padrão
                } catch (SQLException e) {
                    System.err.println("Erro ao restaurar auto-commit: " + e.getMessage());
                    e.printStackTrace();
                }
                ConexaoMySQL.desconectar(conn); // Fecha a conexão
            }
        }
        return sucesso;
    }


     /**
     * Busca vendas realizadas dentro de um período de datas (INCLUSIVO).
     * Compara apenas a parte da DATA, ignorando a hora.
     * Retorna uma lista de Vendas contendo informações básicas e o Cliente associado.
     * Os itens da venda NÃO são carregados por este método para otimização.
     *
     * @param dataInicial Data de início do período.
     * @param dataFinal Data de fim do período.
     * @return Lista de Vendas encontradas no período.
     */
    public List<Venda> buscarPorPeriodo(Date dataInicial, Date dataFinal) {
        List<Venda> vendas = new ArrayList<>();
        // Modifica o WHERE para usar a função DATE() do MySQL
        // Isso compara apenas a parte da data, ignorando a hora.
        String sql = "SELECT v.id_venda, v.data_venda, v.id_cliente, c.cpf, c.nome " +
                     "FROM VENDAS v " +
                     "JOIN CLIENTES c ON v.id_cliente = c.id_cliente " +
                     "WHERE DATE(v.data_venda) >= ? AND DATE(v.data_venda) <= ? " + 
                     "ORDER BY v.data_venda DESC";

        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Define as datas como java.sql.Date para o PreparedStatement
            // Isso passa apenas a informação de data, sem a hora, para o banco
            pstmt.setDate(1, new java.sql.Date(dataInicial.getTime())); 
            pstmt.setDate(2, new java.sql.Date(dataFinal.getTime()));   

            System.out.println("Executando busca por período: " + pstmt.toString()); // Log para depuração

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Venda venda = new Venda();
                    venda.setId(rs.getInt("id_venda"));
                    // Ainda pegamos o Timestamp completo para ter a hora exata da venda
                    venda.setDataVenda(new Date(rs.getTimestamp("data_venda").getTime()));

                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getInt("id_cliente"));
                    cliente.setCpf(rs.getString("cpf"));
                    cliente.setNome(rs.getString("nome"));
                    venda.setCliente(cliente);

                    venda.setItensVenda(new ArrayList<>()); // Lista de itens vazia

                    vendas.add(venda);
                }
            }
             System.out.println("Vendas encontradas no período: " + vendas.size()); // Log
        } catch (SQLException e) {
            System.err.println("Erro ao buscar vendas por período: " + e.getMessage());
            e.printStackTrace();
        }
        return vendas;
    }

    /**
     * Busca vendas realizadas para um cliente específico (pelo ID do cliente).
     * Retorna informações básicas da venda e do cliente. Itens não são carregados.
     *
     * @param idCliente O ID do cliente.
     * @return Lista de Vendas do cliente.
     */
     public List<Venda> buscarPorCliente(int idCliente) {
        List<Venda> vendas = new ArrayList<>();
        String sql = "SELECT v.id_venda, v.data_venda, v.id_cliente, c.cpf, c.nome " +
                     "FROM VENDAS v " +
                     "JOIN CLIENTES c ON v.id_cliente = c.id_cliente " +
                     "WHERE v.id_cliente = ? " +
                     "ORDER BY v.data_venda DESC";

        try (Connection conn = ConexaoMySQL.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Venda venda = new Venda();
                    venda.setId(rs.getInt("id_venda"));
                    venda.setDataVenda(new Date(rs.getTimestamp("data_venda").getTime()));

                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getInt("id_cliente"));
                    cliente.setCpf(rs.getString("cpf"));
                    cliente.setNome(rs.getString("nome"));
                    venda.setCliente(cliente);

                    venda.setItensVenda(new ArrayList<>()); // Lista de itens vazia

                    vendas.add(venda);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar vendas por cliente (ID=" + idCliente + "): " + e.getMessage());
            e.printStackTrace();
        }
        return vendas;
    }


    /**
     * Busca uma Venda completa pelo seu ID, incluindo o Cliente associado
     * e a lista de ItensVenda (com os Produtos correspondentes).
     *
     * @param idVenda O ID da venda a ser buscada.
     * @return A Venda completa, ou null se não encontrada ou erro.
     */
    public Venda buscarVendaCompletaPorId(int idVenda) {
        Venda venda = null;
        // Busca dados da Venda e Cliente
        String sqlVenda = "SELECT v.id_venda, v.data_venda, v.id_cliente, c.cpf, c.nome, c.endereco, c.telefone, c.email " +
                          "FROM VENDAS v JOIN CLIENTES c ON v.id_cliente = c.id_cliente " +
                          "WHERE v.id_venda = ?";
        // Busca dados dos Itens e Produtos associados
        String sqlItens = "SELECT iv.id_item_venda, iv.quantidade, iv.preco_venda_momento, " +
                          "p.id_produto, p.nome, p.descricao, p.quantidade_estoque, p.preco_custo, p.preco_venda, p.estoque_minimo " +
                          "FROM ITENS_VENDA iv JOIN PRODUTOS p ON iv.id_produto = p.id_produto " +
                          "WHERE iv.id_venda = ?";

        try (Connection conn = ConexaoMySQL.conectar()) { // Abre uma única conexão

            // 1. Buscar dados da Venda e Cliente
            try (PreparedStatement pstmtVenda = conn.prepareStatement(sqlVenda)) {
                pstmtVenda.setInt(1, idVenda);
                try (ResultSet rsVenda = pstmtVenda.executeQuery()) {
                    if (rsVenda.next()) {
                        venda = new Venda();
                        venda.setId(rsVenda.getInt("id_venda"));
                        venda.setDataVenda(new Date(rsVenda.getTimestamp("data_venda").getTime()));

                        Cliente cliente = new Cliente();
                        cliente.setId(rsVenda.getInt("id_cliente"));
                        cliente.setCpf(rsVenda.getString("cpf"));
                        cliente.setNome(rsVenda.getString("nome"));
                        cliente.setEndereco(rsVenda.getString("endereco"));
                        cliente.setTelefone(rsVenda.getString("telefone"));
                        cliente.setEmail(rsVenda.getString("email"));
                        venda.setCliente(cliente);
                        venda.setItensVenda(new ArrayList<>()); // Inicializa a lista de itens
                    } else {
                        return null; // Venda não encontrada
                    }
                }
            } // pstmtVenda e rsVenda fechados

            // 2. Se a venda foi encontrada, buscar os Itens e Produtos
            if (venda != null) {
                try (PreparedStatement pstmtItens = conn.prepareStatement(sqlItens)) {
                    pstmtItens.setInt(1, idVenda);
                    try (ResultSet rsItens = pstmtItens.executeQuery()) {
                        while (rsItens.next()) {
                            ItemVenda item = new ItemVenda();
                            item.setId(rsItens.getInt("id_item_venda")); // ID do item (auto_increment)
                            item.setQuantidade(rsItens.getInt("quantidade"));
                            item.setPrecoVenda(rsItens.getDouble("preco_venda_momento")); // Preço no momento

                            Produto produto = new Produto();
                            produto.setId(rsItens.getInt("id_produto"));
                            produto.setNome(rsItens.getString("nome"));
                            produto.setDescricao(rsItens.getString("descricao"));
                            produto.setQuantidadeEstoque(rsItens.getInt("quantidade_estoque")); // Estoque atual
                            produto.setPrecoCusto(rsItens.getDouble("preco_custo"));
                            produto.setPrecoVenda(rsItens.getDouble("preco_venda")); // Preço atual do produto
                            produto.setEstoqueMinimo(rsItens.getInt("estoque_minimo"));

                            item.setProduto(produto); // Associa o produto ao item
                            venda.getItensVenda().add(item); // Adiciona o item à lista da venda
                        }
                    }
                } // pstmtItens e rsItens fechados
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar venda completa por ID (" + idVenda + "): " + e.getMessage());
            e.printStackTrace();
            return null; // Retorna null em caso de erro
        }
        return venda; // Retorna a venda completa
    }

}
