package com.lojafacil.util;

// Imports necessários
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoMySQL {

    // --- Constantes para a Conexão ---
    // Dados do banco MySQL:
    private static final String DB_NAME = "LojaFacil"; // NOME DO BANCO 
    private static final String DB_HOST = "localhost"; // Geralmente 'localhost' 
    private static final String DB_PORT = "3306";      // Porta padrão do MySQL

    // URL de conexão JDBC para MySQL
    private static final String URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true";

    // Usuário do banco MySQL 
    private static final String USER = "root";
    // Senha do usuário do banco MySQL
    private static final String PASSWORD = "Magalhaes@1990";

    // Driver JDBC do MySQL 
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // Método estático para obter uma conexão
    public static Connection conectar() throws SQLException {
        Connection conn = null;
        try {
            // Carrega o driver JDBC (Necessário em algumas configurações ou versões mais antigas)
            Class.forName(JDBC_DRIVER);

            // Tenta estabelecer a conexão
            System.out.println("Tentando conectar ao MySQL: " + URL); // Log para depuração
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexão com o banco de dados MySQL estabelecida com sucesso!");

        } catch (ClassNotFoundException e) {
            System.err.println("Erro: Driver JDBC do MySQL não encontrado! Verifique se o JAR foi adicionado.");
            e.printStackTrace();
            throw new SQLException("Driver MySQL não encontrado", e);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados MySQL:");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("ErrorCode: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            // Re-lança a exceção para que a classe que chamou saiba do erro
            throw e;
        }
        return conn;
    }

    // Método para fechar a conexão
    public static void desconectar(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Conexão com o MySQL fechada.");
            } catch (SQLException e) {
                System.err.println("Erro ao fechar a conexão com o MySQL: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
