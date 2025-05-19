package com.lojafacil.view;

// Imports necessários
import com.lojafacil.model.Produto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Janela de diálogo para permitir ao usuário selecionar um produto quando a
 * busca retorna múltiplos resultados.
 */
public class SelecaoProdutoDialog extends javax.swing.JDialog {

    // Variáveis de instância
    private DefaultTableModel tableModel;
    private Produto produtoSelecionado = null; // Guarda o produto que o usuário escolher
    private List<Produto> listaProdutos; // Guarda a lista original de produtos recebida

    /**
     * Creates new form SelecaoProdutoDialog
     *
     * @param parent O frame pai (geralmente TelaRegistrarVenda)
     * @param modal Se o diálogo deve bloquear a janela pai
     * @param produtos A lista de produtos encontrada para exibir
     */
    public SelecaoProdutoDialog(java.awt.Frame parent, boolean modal, List<Produto> produtos) {
        super(parent, modal); // Chama o construtor do JDialog
        this.listaProdutos = produtos != null ? produtos : new ArrayList<>(); // Garante que a lista não é nula

        initComponents();
        configurarTabela();   // Define o modelo, renderizador, larguras, etc.
        popularTabela();      // Preenche a tabela com os produtos recebidos
        configurarAcoes();    // Adiciona os listeners para eventos (cliques)

        this.setTitle("Selecione um Produto"); // Define o título da janela
        this.setLocationRelativeTo(parent);   // Centraliza o diálogo na tela pai
        this.produtoSelecionado = null;       // Garante que começa sem produto selecionado
        btnSelecionar.setEnabled(false);      // Botão selecionar começa desabilitado
    }

    //Configura o modelo, renderizadores e larguras da JTable tblProdutosEncontrados.
    private void configurarTabela() {
        String[] colunas = {"Código", "Nome", "Preço Venda", "Estoque"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            } // Não editável

            @Override
            public Class<?> getColumnClass(int columnIndex) { // Tipos para ordenação/renderização
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Double.class; // Preço
                    case 3:
                        return Integer.class; // Estoque
                    default:
                        return Object.class;
                }
            }
        };
        // Define o modelo na JTable criada pelo NetBeans
        tblProdutosEncontrados.setModel(tableModel);
        tblProdutosEncontrados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Aplica o renderizador de moeda na coluna de Preço (índice 2)
        try {
            TableColumnModel columnModel = tblProdutosEncontrados.getColumnModel();
            TableColumn priceColumn = columnModel.getColumn(2);
            priceColumn.setCellRenderer(new SplitCurrencyRenderer()); // Reutiliza o renderer
        } catch (Exception e) {
            System.err.println("Erro ao aplicar SplitCurrencyRenderer na tabela de seleção: " + e.getMessage());
        }

        // Define larguras preferenciais 
        TableColumnModel columnModelWidth = tblProdutosEncontrados.getColumnModel();
        columnModelWidth.getColumn(0).setPreferredWidth(80);
        columnModelWidth.getColumn(1).setPreferredWidth(280);
        columnModelWidth.getColumn(2).setPreferredWidth(100);
        columnModelWidth.getColumn(3).setPreferredWidth(80);
        tblProdutosEncontrados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Rolagem horizontal
    }

    /**
     * Preenche a tabela com os produtos da lista recebida no construtor.
     */
    private void popularTabela() {
        tableModel.setRowCount(0); // Limpa tabela antes de popular
        if (this.listaProdutos != null) {
            for (Produto p : this.listaProdutos) {
                Object[] rowData = {
                    p.getId(),
                    p.getNome(),
                    p.getPrecoVenda(),
                    p.getQuantidadeEstoque()
                };
                tableModel.addRow(rowData);
            }
        }
    }

    /**
     * Configura os listeners (eventos) para a tabela e os botões.
     */
    private void configurarAcoes() {
        // Listener para habilitar/desabilitar o botão Selecionar quando uma linha é clicada
        tblProdutosEncontrados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Executa apenas quando a seleção estabiliza
                btnSelecionar.setEnabled(tblProdutosEncontrados.getSelectedRow() != -1);
            }
        });

        // Listener para detectar duplo clique na tabela
        tblProdutosEncontrados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Verifica duplo clique e se uma linha está selecionada
                if (e.getClickCount() == 2 && tblProdutosEncontrados.getSelectedRow() != -1) {
                    selecionarProduto(); // Chama o mesmo método do botão Selecionar
                }
            }
        });
    }

    /**
     * Método interno chamado quando o usuário seleciona um produto (seja por
     * duplo clique ou pelo botão Selecionar). Define o produto escolhido e
     * fecha o diálogo.
     */
    private void selecionarProduto() {
        int selectedRow = tblProdutosEncontrados.getSelectedRow(); // Índice da linha na VISÃO
        if (selectedRow != -1) { // Verifica se algo está selecionado
            // Converte o índice da linha da visão para o índice do MODELO de dados
            // Isso é importante caso o usuário tenha ordenado a tabela clicando no cabeçalho
            int modelRow = tblProdutosEncontrados.convertRowIndexToModel(selectedRow);

            // Garante que o índice do modelo é válido para a nossa lista original
            if (modelRow >= 0 && modelRow < listaProdutos.size()) {
                Produto p = listaProdutos.get(modelRow); // Pega o produto correto da lista

                // Verifica se há estoque
                if (p.getQuantidadeEstoque() <= 0) {
                    JOptionPane.showMessageDialog(this, "Este produto ('" + p.getNome() + "') está sem estoque!",
                            "Estoque Esgotado", JOptionPane.WARNING_MESSAGE);
                    this.produtoSelecionado = null; // Não define como selecionado
                    // Não fecha o diálogo, permite escolher outro
                } else {
                    this.produtoSelecionado = p; // Armazena o produto escolhido
                    this.dispose(); // Fecha o diálogo
                }
            } else {
                // Erro inesperado (índice inválido após conversão)
                JOptionPane.showMessageDialog(this, "Erro ao obter o produto selecionado (índice inválido).",
                        "Erro Interno", JOptionPane.ERROR_MESSAGE);
                this.produtoSelecionado = null;
            }
        }
    }

    /**
     * Método público para a tela chamadora (TelaRegistrarVenda) obter o produto
     * que foi efetivamente selecionado pelo usuário.
     *
     * @return O Produto selecionado, ou null se o usuário cancelou ou fechou o
     * diálogo.
     */
    public Produto getProdutoSelecionado() {
        return this.produtoSelecionado;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblProdutosEncontrados = new javax.swing.JTable();
        btnSelecionar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tblProdutosEncontrados.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblProdutosEncontrados);

        btnSelecionar.setBackground(new java.awt.Color(0, 153, 51));
        btnSelecionar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSelecionar.setForeground(new java.awt.Color(255, 255, 255));
        btnSelecionar.setText("Selecionar");
        btnSelecionar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSelecionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecionarActionPerformed(evt);
            }
        });

        btnCancelar.setBackground(new java.awt.Color(255, 0, 0));
        btnCancelar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnCancelar.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelar.setText("Cancelar ");
        btnCancelar.setToolTipText("Excluir o produto selecionado na tabela");
        btnCancelar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSelecionar, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnSelecionar))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelecionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarActionPerformed
        selecionarProduto();  // Chama o método que contém a lógica de seleção

    }//GEN-LAST:event_btnSelecionarActionPerformed

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarActionPerformed
        this.produtoSelecionado = null; // Garante que nada foi selecionado
        this.dispose(); // Fecha a janela de diálogo

    }//GEN-LAST:event_btnCancelarActionPerformed

    public static void main(String args[]) {

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelar;
    private javax.swing.JButton btnSelecionar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblProdutosEncontrados;
    // End of variables declaration//GEN-END:variables
}
