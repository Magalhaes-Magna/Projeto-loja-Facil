package com.lojafacil.dao;

// Imports necessários
import com.lojafacil.model.Cliente;
import com.lojafacil.util.ConexaoMySQL;
import com.lojafacil.dao.ClienteComVendasException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO (Data Access Object) para a entidade Cliente. Contém métodos para
 * interagir com a tabela CLIENTES no banco de dados.
 */
public class ClienteDAO {

    /**
     * Insere um novo cliente no banco de dados. O ID do cliente é gerado
     * automaticamente pelo banco (AUTO_INCREMENT). O ID gerado é definido de
     * volta no objeto Cliente passado como parâmetro.
     *
     * @param cliente O objeto Cliente a ser inserido (sem ID pré-definido).
     * @return true se a inserção foi bem-sucedida e o ID foi recuperado, false
     * caso contrário.
     */
    public boolean inserir(Cliente cliente) {
        // SQL não inclui id_cliente, pois é AUTO_INCREMENT
        String sql = "INSERT INTO CLIENTES (cpf, nome, endereco, telefone, email) VALUES (?, ?, ?, ?, ?)";
        boolean sucesso = false;

        // Usar try-with-resources garante o fechamento da conexão e do PreparedStatement
        try (Connection conn = ConexaoMySQL.conectar(); // Solicita o retorno das chaves geradas (o ID)
                 PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Define os parâmetros do PreparedStatement
            pstmt.setString(1, cliente.getCpf());
            pstmt.setString(2, cliente.getNome());
            pstmt.setString(3, cliente.getEndereco()); // Permite nulo
            pstmt.setString(4, cliente.getTelefone()); // Permite nulo
            pstmt.setString(5, cliente.getEmail());    // Permite nulo

            // Executa o comando de inserção
            int linhasAfetadas = pstmt.executeUpdate();

            // Verifica se a inserção funcionou (1 linha afetada)
            if (linhasAfetadas > 0) {
                // Recupera as chaves (IDs) geradas pelo banco
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Define o ID gerado no objeto Cliente
                        cliente.setId(generatedKeys.getInt(1));
                        sucesso = true; // Marca como sucesso
                    } else {
                        // Log de erro se não conseguiu recuperar o ID
                        System.err.println("Falha ao obter o ID gerado para o cliente.");
                    }
                } // ResultSet generatedKeys é fechado aqui
            }
        } catch (SQLException e) {
            // Tratamento de erros SQL
            System.err.println("Erro ao inserir cliente: " + e.getMessage());
            // Verifica erro específico de duplicidade (ex: CPF já existe)
            if ("23000".equals(e.getSQLState()) || e.getMessage().contains("Duplicate entry")) {
                System.err.println("Erro de inserção: CPF duplicado ('" + cliente.getCpf() + "').");
                // Considerar lançar uma exceção personalizada para tratar na camada de visão
            }
            e.printStackTrace();
        } // Connection e PreparedStatement são fechados aqui
        return sucesso;
    }

    /**
     * Atualiza os dados de um cliente existente no banco de dados.
     *
     * @param cliente O objeto Cliente com os dados atualizados (deve conter o
     * ID existente).
     * @return true se a atualização foi bem-sucedida (1 ou mais linhas
     * afetadas), false caso contrário.
     */
    public boolean atualizar(Cliente cliente) {
        // SQL para atualizar o cliente baseado no id_cliente
        String sql = "UPDATE CLIENTES SET cpf = ?, nome = ?, endereco = ?, telefone = ?, email = ? WHERE id_cliente = ?";
        try (Connection conn = ConexaoMySQL.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Define os novos valores para os campos
            pstmt.setString(1, cliente.getCpf());
            pstmt.setString(2, cliente.getNome());
            pstmt.setString(3, cliente.getEndereco());
            pstmt.setString(4, cliente.getTelefone());
            pstmt.setString(5, cliente.getEmail());
            // Define o ID para a cláusula WHERE
            pstmt.setInt(6, cliente.getId());

            // Executa o comando de atualização
            int linhasAfetadas = pstmt.executeUpdate();

            // Retorna true se alguma linha foi afetada (atualização bem-sucedida)
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar cliente (ID=" + cliente.getId() + "): " + e.getMessage());
            if ("23000".equals(e.getSQLState()) || e.getMessage().contains("Duplicate entry")) {
                System.err.println("Erro de atualização: Possível CPF duplicado.");
            }
            e.printStackTrace();
            return false; // Retorna false em caso de erro
        }
    }

    /**
     * Exclui um cliente do banco de dados pelo seu ID.
     *
     * @param id O ID do cliente a ser excluído.
     * @return true se a exclusão foi bem-sucedida.
     * @throws ClienteComVendasException Se o cliente não puder ser excluído por
     * ter vendas associadas.
     * @throws SQLException Se ocorrer outro erro no banco de dados durante a
     * exclusão.
     */
    public boolean excluir(int id) throws SQLException, ClienteComVendasException {
        String sql = "DELETE FROM CLIENTES WHERE id_cliente = ?";
        try (Connection conn = ConexaoMySQL.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int linhasAfetadas = pstmt.executeUpdate();
            // Retorna true se exatamente uma linha foi afetada (excluída)
            return linhasAfetadas == 1;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir cliente (ID=" + id + "): " + e.getMessage());
            // Verifica se o erro é de violação de chave estrangeira (código pode variar um pouco entre DBs/versões)
            // Para MySQL, o SQLState '23000' é comum para erros de integridade,
            // e a mensagem geralmente contém 'foreign key constraint fails'.
            if ("23000".equals(e.getSQLState()) || (e.getMessage() != null && e.getMessage().contains("foreign key constraint fails"))) {
                // Lança a exceção personalizada para ser tratada na tela
                throw new ClienteComVendasException("Não é possível excluir o cliente (ID=" + id + ") pois ele possui vendas registradas.", e);
            } else {
                // Para outros erros SQL, apenas relança a SQLException original
                e.printStackTrace(); // Mantém o log no console
                throw e;
            }
        }
    }

    /**
     * Busca um cliente específico pelo seu ID.
     *
     * @param id O ID do cliente a ser buscado.
     * @return Um objeto Cliente se encontrado, ou null se não encontrado ou em
     * caso de erro.
     */
    public Cliente buscarPorId(int id) {
        String sql = "SELECT id_cliente, cpf, nome, endereco, telefone, email FROM CLIENTES WHERE id_cliente = ?";
        Cliente cliente = null;
        try (Connection conn = ConexaoMySQL.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id); // Define o parâmetro ID

            try (ResultSet rs = pstmt.executeQuery()) { // Executa a busca
                if (rs.next()) { // Se encontrou um resultado
                    // Cria e preenche o objeto Cliente
                    cliente = new Cliente();
                    cliente.setId(rs.getInt("id_cliente"));
                    cliente.setCpf(rs.getString("cpf"));
                    cliente.setNome(rs.getString("nome"));
                    cliente.setEndereco(rs.getString("endereco"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setEmail(rs.getString("email"));
                }
            } // ResultSet é fechado aqui
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cliente por ID (" + id + "): " + e.getMessage());
            e.printStackTrace();
        } // Connection e PreparedStatement são fechados aqui
        return cliente; // Retorna o cliente encontrado ou null
    }

    /**
     * Lista todos os clientes cadastrados no banco de dados, ordenados por
     * nome.
     *
     * @return Uma lista de objetos Cliente (pode estar vazia).
     */
    public List<Cliente> listarTodos() {
        List<Cliente> listaClientes = new ArrayList<>();
        String sql = "SELECT id_cliente, cpf, nome, endereco, telefone, email FROM CLIENTES ORDER BY nome";

        // Usa Statement simples pois não há parâmetros na consulta
        try (Connection conn = ConexaoMySQL.conectar(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            // Itera sobre o resultado
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id_cliente"));
                cliente.setCpf(rs.getString("cpf"));
                cliente.setNome(rs.getString("nome"));
                cliente.setEndereco(rs.getString("endereco"));
                cliente.setTelefone(rs.getString("telefone"));
                cliente.setEmail(rs.getString("email"));
                listaClientes.add(cliente); // Adiciona à lista
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar todos os clientes: " + e.getMessage());
            e.printStackTrace();
            // Considerar limpar a lista ou retornar null em caso de erro grave?
        }
        return listaClientes;
    }

    /**
     * Busca clientes por parte do CPF (ignorando formatação) ou parte do Nome.
     * A busca por nome é case-insensitive. Se ambos os critérios forem nulos ou
     * vazios, retorna uma lista vazia.
     *
     * @param cpf O CPF a ser buscado (pode conter máscara ou ser apenas
     * números).
     * @param nome O Nome (ou parte do nome) a ser buscado.
     * @return Uma lista de Clientes que correspondem aos critérios.
     */
    public List<Cliente> buscarPorCpfOuNome(String cpf, String nome) {
        List<Cliente> clientesEncontrados = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT id_cliente, cpf, nome, endereco, telefone, email FROM CLIENTES WHERE 1=1");
        List<Object> params = new ArrayList<>();
        boolean hasCriteria = false;

        // Adiciona critério de CPF se fornecido
        if (cpf != null && !cpf.trim().isEmpty()) {
            // Limpa o CPF de entrada para conter apenas dígitos
            String cpfNumeros = cpf.trim().replaceAll("[^0-9]", "");

            // Adiciona cláusula SQL que TAMBÉM limpa o CPF do banco para comparação
            // A função REPLACE do MySQL é usada para remover '.' e '-'
            if (!cpfNumeros.isEmpty()) {
                sqlBuilder.append(" AND REPLACE(REPLACE(cpf, '.', ''), '-', '') = ?");
                params.add(cpfNumeros); // Adiciona APENAS os números como parâmetro
                hasCriteria = true;
            }
        }

        // Adiciona critério de Nome se fornecido
        if (nome != null && !nome.trim().isEmpty()) {
            sqlBuilder.append(" AND LOWER(nome) LIKE ?"); // Busca case-insensitive por parte do nome
            params.add("%" + nome.trim().toLowerCase() + "%");
            hasCriteria = true;
        }

        // Se nenhum critério válido foi adicionado, retorna a lista vazia
        if (!hasCriteria) {
            return clientesEncontrados;
        }

        sqlBuilder.append(" ORDER BY nome"); // Ordena o resultado

        try (Connection conn = ConexaoMySQL.conectar(); PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            // Define os parâmetros coletados
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            System.out.println("Executando busca cliente: " + pstmt.toString()); // Log para depuração

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) { // Itera nos resultados
                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getInt("id_cliente"));
                    cliente.setCpf(rs.getString("cpf")); // Pega o CPF como está no banco (com máscara)
                    cliente.setNome(rs.getString("nome"));
                    cliente.setEndereco(rs.getString("endereco"));
                    cliente.setTelefone(rs.getString("telefone"));
                    cliente.setEmail(rs.getString("email"));
                    clientesEncontrados.add(cliente);
                }
            }
            System.out.println("Clientes encontrados: " + clientesEncontrados.size()); // Log para depuração
        } catch (SQLException e) {
            System.err.println("Erro ao buscar clientes por CPF/Nome: " + e.getMessage());
            e.printStackTrace();
        }
        return clientesEncontrados;
    }

}
