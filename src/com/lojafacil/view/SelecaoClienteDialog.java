package com.lojafacil.view;

// Imports necessários
import com.lojafacil.model.Cliente; // <<< Modelo de Cliente
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * Janela de diálogo para permitir ao usuário selecionar um cliente quando a
 * busca retorna múltiplos resultados.
 */
public class SelecaoClienteDialog extends javax.swing.JDialog {

    // --- Variáveis de Instância ---
    private DefaultTableModel tableModel;
    private Cliente clienteSelecionado = null; // Guarda o cliente escolhido
    private List<Cliente> listaClientes;    // Guarda a lista original de clientes recebida

    /**
     * Creates new form SelecaoClienteDialog
     *
     * @param parent O frame pai (geralmente TelaRegistrarVenda)
     * @param modal Se o diálogo deve bloquear a janela pai
     * @param clientes A lista de clientes encontrada para exibir
     */
    public SelecaoClienteDialog(java.awt.Frame parent, boolean modal, List<Cliente> clientes) {
        super(parent, modal); // Chama o construtor JDialog
        this.listaClientes = clientes != null ? clientes : new ArrayList<>();
        initComponents();

        // Configurações pós-inicialização
        configurarTabela();
        popularTabela();
        configurarAcoesAdicionaisTabela();

        // Configurações finais do diálogo
        this.setTitle("Selecione um Cliente");
        this.setLocationRelativeTo(parent);
        this.clienteSelecionado = null;
        btnSelecionarCli.setEnabled(false); // Garante que começa desabilitado
    }

    /**
     * Configura o modelo, renderizadores e larguras da JTable
     * tblClientesEncontrados.
     */
    private void configurarTabela() {
        // <<< Colunas para ajudar a identificar o cliente >>>
        String[] colunas = {"ID", "Nome", "CPF", "Telefone"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class; // ID
                    case 1:
                        return String.class;  // Nome
                    case 2:
                        return String.class;  // CPF
                    case 3:
                        return String.class;  // Telefone
                    default:
                        return Object.class;
                }
            }
        };
        // Usa a variável da JTable criada pelo NetBeans
        tblClientesEncontrados.setModel(tableModel);
        tblClientesEncontrados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // <<< Ajuste de Larguras, conforme necessário) >>>
        TableColumnModel columnModelWidth = tblClientesEncontrados.getColumnModel();
        columnModelWidth.getColumn(0).setPreferredWidth(60);  // ID
        columnModelWidth.getColumn(1).setPreferredWidth(250); // Nome
        columnModelWidth.getColumn(2).setPreferredWidth(120); // CPF
        columnModelWidth.getColumn(3).setPreferredWidth(110); // Telefone
        tblClientesEncontrados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    /**
     * Preenche a tabela com os clientes da lista recebida no construtor.
     */
    private void popularTabela() {
        tableModel.setRowCount(0);
        if (this.listaClientes != null) {
            for (Cliente c : this.listaClientes) {
                Object[] rowData = {
                    c.getId(),
                    c.getNome(),
                    c.getCpf(),
                    c.getTelefone() // Mostra telefone para ajudar a diferenciar
                };
                tableModel.addRow(rowData);
            }
        }
    }

    /**
     * Configura listeners adicionais para a tabela (seleção e duplo clique).
     */
    private void configurarAcoesAdicionaisTabela() {
        // Listener para seleção na tabela -> Habilita/Desabilita botão Selecionar
        tblClientesEncontrados.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Usa o nome da variável do botão definido no Design
                btnSelecionarCli.setEnabled(tblClientesEncontrados.getSelectedRow() != -1);
            }
        });

        // Listener para duplo clique na tabela -> Seleciona cliente
        tblClientesEncontrados.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblClientesEncontrados.getSelectedRow() != -1) {
                    selecionarClienteLogica(); // Chama a lógica de seleção
                }
            }
        });
    }

    /**
     * Lógica interna para pegar o cliente selecionado e fechar o diálogo.
     */
    private void selecionarClienteLogica() {
        int selectedRowView = tblClientesEncontrados.getSelectedRow();
        if (selectedRowView != -1) {
            int modelRow = tblClientesEncontrados.convertRowIndexToModel(selectedRowView);
            if (modelRow >= 0 && modelRow < listaClientes.size()) {
                this.clienteSelecionado = listaClientes.get(modelRow); // Armazena
                this.dispose(); // Fecha
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao obter o cliente selecionado (índice inválido).",
                        "Erro Interno", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Retorna o cliente selecionado pelo usuário.
     *
     * @return O Cliente selecionado, ou null se cancelado.
     */
    public Cliente getClienteSelecionado() {
        return this.clienteSelecionado;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        btnSelecionarCli = new javax.swing.JButton();
        btnCancelarCli = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblClientesEncontrados = new javax.swing.JTable();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        btnSelecionarCli.setBackground(new java.awt.Color(0, 153, 51));
        btnSelecionarCli.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSelecionarCli.setForeground(new java.awt.Color(255, 255, 255));
        btnSelecionarCli.setText("Selecionar");
        btnSelecionarCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSelecionarCli.setEnabled(false);
        btnSelecionarCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelecionarCliActionPerformed(evt);
            }
        });

        btnCancelarCli.setBackground(new java.awt.Color(255, 0, 0));
        btnCancelarCli.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnCancelarCli.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelarCli.setText("Cancelar ");
        btnCancelarCli.setToolTipText("Excluir o produto selecionado na tabela");
        btnCancelarCli.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCancelarCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarCliActionPerformed(evt);
            }
        });

        tblClientesEncontrados.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblClientesEncontrados);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancelarCli, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSelecionarCli, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 547, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSelecionarCli)
                    .addComponent(btnCancelarCli))
                .addGap(16, 16, 16))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelecionarCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelecionarCliActionPerformed
        selecionarClienteLogica(); // Chama a lógica que seleciona e fecha
    }//GEN-LAST:event_btnSelecionarCliActionPerformed

    private void btnCancelarCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarCliActionPerformed
        selecionarClienteLogica(); // Chama a lógica que seleciona e fecha
    }//GEN-LAST:event_btnCancelarCliActionPerformed

    public static void main(String args[]) {

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancelarCli;
    private javax.swing.JButton btnSelecionarCli;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable tblClientesEncontrados;
    // End of variables declaration//GEN-END:variables
}
