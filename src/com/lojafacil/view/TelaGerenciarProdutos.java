package com.lojafacil.view;

// Imports necessarios
import com.lojafacil.model.Produto;
import com.lojafacil.dao.ProdutoDAO;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
//import com.lojafacil.view.CurrencyRenderer;

public class TelaGerenciarProdutos extends javax.swing.JFrame {

    // Modelo para controlar os dados da JTable
    private DefaultTableModel tableModel;
    private int idProdutoEditando = -1;
    private ProdutoDAO produtoDAO; //  Instância do DAO

    // Construtor da classe 
    public TelaGerenciarProdutos() {
        initComponents();
        produtoDAO = new ProdutoDAO(); // Inicializa DAO
        configurarTabela(); // método para configurar a tabela
        adicionarListenerSelecaoTabela();
        habilitarBotoesEdicaoExclusao(false);
        carregarTabelaProdutos(); // Carrega dados do banco
        limparCampos(); // Garante estado inicial limpo

        // Componentes focaveis
        setupEnterKeyFocusTransfer(txtProduto);       // Do Nome do Produto vai para...
        setupEnterKeyFocusTransfer(txtDescricao);     // Da Descrição vai para...
        setupEnterKeyFocusTransfer(txtQuantidade);    // Da Quantidade vai para...
        setupEnterKeyFocusTransfer(txtPrecoCusto);    // Do Preço Custo vai para...
        setupEnterKeyFocusTransfer(txtPrecoVenda);    // Do Preço Venda vai para...
        setupEnterKeyFocusTransfer(txtQuantMinima); // Da Quant. Mínima vai para... (próximo na ordem Tab)
    }

    private void configurarTabela() {
        String[] colunas = {"Código", "Produto", "Preço Venda", "Quantidade"};
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class; // Código
                    case 1:
                        return String.class;  // Produto
                    case 2:
                        return Double.class;  // Preço Venda
                    case 3:
                        return Integer.class; // Quantidade
                    default:
                        return Object.class;
                }
            }
        };
        tblProdutos.setModel(tableModel);

        // *****FORMATAR A COLUNA DE PREÇO *****
        try {
            // Índice da coluna "Preço Venda" (3ª coluna -> índice 2)
            int precoVendaColIndex = 2;
            TableColumnModel columnModel = tblProdutos.getColumnModel();
            // Obtém o objeto TableColumn para a coluna de preço
            TableColumn precoColumn = columnModel.getColumn(precoVendaColIndex);

            // Aplica o renderizador personalizado à coluna correta
            precoColumn.setCellRenderer(new SplitCurrencyRenderer());

        } catch (Exception e) {
            System.err.println("Erro ao aplicar SplitCurrencyRenderer na tabela de produtos: " + e.getMessage());
            // O programa continua mesmo se a formatação falhar
        }
        // ***** AJUSTE DAS LARGURAS DAS COLUNAS *****
        TableColumnModel columnModel = tblProdutos.getColumnModel();
        // Coluna 0: Código
        columnModel.getColumn(0).setPreferredWidth(80);
        columnModel.getColumn(0).setMaxWidth(100); // Opcional: Largura máxima
        columnModel.getColumn(0).setMinWidth(60);  // Opcional: Largura mínima
        // Coluna 1: Produto (Nome) - Geralmente a maior
        columnModel.getColumn(1).setPreferredWidth(350);
        columnModel.getColumn(1).setMinWidth(150);
        // Coluna 2: Preço Venda
        columnModel.getColumn(2).setPreferredWidth(120);
        columnModel.getColumn(2).setMaxWidth(150);
        columnModel.getColumn(2).setMinWidth(90);
        // Coluna 3: Quantidade
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(3).setMaxWidth(120);
        columnModel.getColumn(3).setMinWidth(70);
    }

    /**
     * Limpa a tabela e a recarrega com dados do banco de dados.
     */
    private void carregarTabelaProdutos() {
        List<Produto> produtosDoBanco = produtoDAO.listarTodos();
        atualizarTabela(produtosDoBanco);
        tblProdutos.clearSelection();
    }

    /**
     * Atualiza a JTable com a lista de produtos fornecida.
     *
     * @param produtosParaExibir A lista de produtos a ser exibida.
     */
    private void atualizarTabela(List<Produto> produtosParaExibir) {
        tableModel.setRowCount(0); // Limpa
        if (produtosParaExibir != null) {
            for (Produto p : produtosParaExibir) {
                Object[] rowData = {
                    p.getId(),
                    p.getNome(),
                    p.getPrecoVenda(),
                    p.getQuantidadeEstoque()
                };
                tableModel.addRow(rowData);
            }
        }
        habilitarBotoesEdicaoExclusao(false); // Desabilita botões após recarregar
    }

    private void adicionarListenerSelecaoTabela() {
        tblProdutos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (!event.getValueIsAdjusting()) {
                    habilitarBotoesEdicaoExclusao(tblProdutos.getSelectedRowCount() == 1);
                }
            }
        });
    }

    private void habilitarBotoesEdicaoExclusao(boolean habilitar) {
        btnEditar.setEnabled(habilitar);
        btnExcluir.setEnabled(habilitar);
    }

    private void limparCampos() {
        txtCodigo.setText("");
        txtProduto.setText("");
        txtDescricao.setText("");
        txtQuantidade.setText("");
        txtPrecoCusto.setText("");
        txtPrecoVenda.setText("");
        txtQuantMinima.setText("");
        txtPesquisarCodigo.setText("");
        txtPesquisarNome.setText("");

        this.idProdutoEditando = -1; // Reseta controle de edição
        txtCodigo.setEnabled(false);  // <<< Desabilita campo código (AUTO_INCREMENT)

        txtProduto.requestFocus(); // Foco no nome do produto
    }

    /**
     * Configura um componente para transferir o foco com Enter.
     */
    private void setupEnterKeyFocusTransfer(JComponent component) {
        if (component == null) {
            return;
        }
        String actionKey = "transferFocus";
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        inputMap.put(enterKeyStroke, actionKey);
        Action transferAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() instanceof JComponent) {
                    ((JComponent) e.getSource()).transferFocus();
                }
            }
        };
        component.getActionMap().put(actionKey, transferAction);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlProdutos = new javax.swing.JPanel();
        pnlArea = new javax.swing.JPanel();
        pnlbotao = new javax.swing.JPanel();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProdutos = new javax.swing.JTable();
        pnlCampoPesquisa = new javax.swing.JPanel();
        txtPesquisarCodigo = new javax.swing.JTextField();
        txtPesquisarNome = new javax.swing.JTextField();
        btnExecutarPesquisa = new javax.swing.JButton();
        lblPesquisarCodigo = new javax.swing.JLabel();
        lblPesquisarNome = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        txtCodigo = new javax.swing.JTextField();
        txtProduto = new javax.swing.JTextField();
        txtDescricao = new javax.swing.JTextField();
        txtQuantidade = new javax.swing.JTextField();
        txtPrecoCusto = new javax.swing.JTextField();
        txtPrecoVenda = new javax.swing.JTextField();
        txtQuantMinima = new javax.swing.JTextField();
        btnSalvar = new javax.swing.JButton();
        lblQuantMinima = new javax.swing.JLabel();
        lblPrecoVenda = new javax.swing.JLabel();
        lblPrecoCusto = new javax.swing.JLabel();
        lblQuantidade = new javax.swing.JLabel();
        lblDescricao = new javax.swing.JLabel();
        lblCodigo = new javax.swing.JLabel();
        lblProduto = new javax.swing.JLabel();
        btnLimpar1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuProduto = new javax.swing.JMenu();
        menuItemNovo = new javax.swing.JMenuItem();
        menuItemVoltar = new javax.swing.JMenuItem();
        menuItemSair = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(976, 764));

        pnlProdutos.setForeground(new java.awt.Color(0, 25, 51));

        pnlbotao.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lista de Produtos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N
        pnlbotao.setForeground(new java.awt.Color(242, 242, 242));
        pnlbotao.setToolTipText("");

        btnEditar.setBackground(new java.awt.Color(204, 204, 204));
        btnEditar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnEditar.setText("Editar");
        btnEditar.setToolTipText("Editar o produto selecionado na tabela");
        btnEditar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditarActionPerformed(evt);
            }
        });

        btnExcluir.setBackground(new java.awt.Color(255, 0, 0));
        btnExcluir.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnExcluir.setForeground(new java.awt.Color(255, 255, 255));
        btnExcluir.setText("Excluir");
        btnExcluir.setToolTipText("Excluir o produto selecionado na tabela");
        btnExcluir.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        tblProdutos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Código", "Produto", "Preço Venda", "Quantidade"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Double.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblProdutos);

        javax.swing.GroupLayout pnlbotaoLayout = new javax.swing.GroupLayout(pnlbotao);
        pnlbotao.setLayout(pnlbotaoLayout);
        pnlbotaoLayout.setHorizontalGroup(
            pnlbotaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlbotaoLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
            .addGroup(pnlbotaoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        pnlbotaoLayout.setVerticalGroup(
            pnlbotaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlbotaoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlbotaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditar)
                    .addComponent(btnExcluir))
                .addContainerGap())
        );

        pnlCampoPesquisa.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Campo de Pesquisa", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        txtPesquisarNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesquisarNomeActionPerformed(evt);
            }
        });

        btnExecutarPesquisa.setBackground(new java.awt.Color(204, 204, 204));
        btnExecutarPesquisa.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnExecutarPesquisa.setText("Buscar");
        btnExecutarPesquisa.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnExecutarPesquisa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecutarPesquisaActionPerformed(evt);
            }
        });

        lblPesquisarCodigo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblPesquisarCodigo.setText("Código:");

        lblPesquisarNome.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblPesquisarNome.setText("Produto:");

        javax.swing.GroupLayout pnlCampoPesquisaLayout = new javax.swing.GroupLayout(pnlCampoPesquisa);
        pnlCampoPesquisa.setLayout(pnlCampoPesquisaLayout);
        pnlCampoPesquisaLayout.setHorizontalGroup(
            pnlCampoPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoPesquisaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPesquisarCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPesquisarCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblPesquisarNome, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPesquisarNome, javax.swing.GroupLayout.PREFERRED_SIZE, 389, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(85, 85, 85)
                .addComponent(btnExecutarPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );
        pnlCampoPesquisaLayout.setVerticalGroup(
            pnlCampoPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoPesquisaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlCampoPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPesquisarCodigo)
                    .addComponent(txtPesquisarCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPesquisarNome)
                    .addComponent(txtPesquisarNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExecutarPesquisa))
                .addContainerGap(29, Short.MAX_VALUE))
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cadastrar Novo Porduto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        txtCodigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCodigoActionPerformed(evt);
            }
        });

        txtProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProdutoActionPerformed(evt);
            }
        });

        btnSalvar.setBackground(new java.awt.Color(0, 153, 51));
        btnSalvar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnSalvar.setForeground(new java.awt.Color(255, 255, 255));
        btnSalvar.setText("Salvar");
        btnSalvar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        lblQuantMinima.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblQuantMinima.setText("Quant. Mínima:");

        lblPrecoVenda.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblPrecoVenda.setText("Preço de Venda:");

        lblPrecoCusto.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblPrecoCusto.setText("Preço de Custo:");

        lblQuantidade.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblQuantidade.setText("Quantidade:");

        lblDescricao.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblDescricao.setText("Descrição:");

        lblCodigo.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblCodigo.setText("Código:");

        lblProduto.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
        lblProduto.setText("Produto:");

        btnLimpar1.setBackground(new java.awt.Color(246, 232, 44));
        btnLimpar1.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnLimpar1.setText("Limpar");
        btnLimpar1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnLimpar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimpar1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblProduto)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 584, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblDescricao)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtDescricao)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblQuantidade)
                            .addComponent(txtQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblPrecoCusto, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(lblPrecoVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(txtPrecoCusto, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(62, 62, 62)
                                .addComponent(txtPrecoVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(txtQuantMinima, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblQuantMinima, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLimpar1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCodigo)
                    .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblProduto)
                    .addComponent(txtProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDescricao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDescricao))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuantidade)
                    .addComponent(lblPrecoCusto)
                    .addComponent(lblPrecoVenda)
                    .addComponent(lblQuantMinima))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtQuantidade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecoCusto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrecoVenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQuantMinima, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSalvar)
                    .addComponent(btnLimpar1))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlAreaLayout = new javax.swing.GroupLayout(pnlArea);
        pnlArea.setLayout(pnlAreaLayout);
        pnlAreaLayout.setHorizontalGroup(
            pnlAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAreaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlbotao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCampoPesquisa, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlAreaLayout.setVerticalGroup(
            pnlAreaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAreaLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlCampoPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(pnlbotao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlProdutosLayout = new javax.swing.GroupLayout(pnlProdutos);
        pnlProdutos.setLayout(pnlProdutosLayout);
        pnlProdutosLayout.setHorizontalGroup(
            pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlProdutosLayout.setVerticalGroup(
            pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlArea, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        menuProduto.setText("Produto");
        menuProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuProdutoActionPerformed(evt);
            }
        });

        menuItemNovo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemNovo.setText("Novo Produto");
        menuItemNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemNovoActionPerformed(evt);
            }
        });
        menuProduto.add(menuItemNovo);

        menuItemVoltar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemVoltar.setText("Voltar");
        menuItemVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemVoltarActionPerformed(evt);
            }
        });
        menuProduto.add(menuItemVoltar);

        menuItemSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemSair.setText("Sair");
        menuItemSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSairActionPerformed(evt);
            }
        });
        menuProduto.add(menuItemSair);

        jMenuBar1.add(menuProduto);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlProdutos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        // 1. Obter dados da tela
        String nome = txtProduto.getText().trim();
        String descricao = txtDescricao.getText().trim();
        String quantidadeStr = txtQuantidade.getText().trim();
        String precoCustoStr = txtPrecoCusto.getText().trim();
        String precoVendaStr = txtPrecoVenda.getText().trim();
        String quantMinimaStr = txtQuantMinima.getText().trim();

        // 2. Validar campos obrigatórios e numéricos
        if (nome.isEmpty() || precoVendaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Os campos Produto e Preço de Venda são obrigatórios.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade;
        double precoCusto;
        double precoVenda;
        int quantMinima;

        try {
            quantidade = quantidadeStr.isEmpty() ? 0 : Integer.parseInt(quantidadeStr);
            precoCusto = precoCustoStr.isEmpty() ? 0.0 : Double.parseDouble(precoCustoStr);
            precoVenda = Double.parseDouble(precoVendaStr); // Obrigatório
            quantMinima = quantMinimaStr.isEmpty() ? 0 : Integer.parseInt(quantMinimaStr);

            if (quantidade < 0 || precoCusto < 0 || precoVenda < 0 || quantMinima < 0) {
                throw new NumberFormatException("Valores numéricos não podem ser negativos.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Erro ao converter valores numéricos: " + e.getMessage() + "\nUse ponto (.) como separador decimal.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. Criar objeto Produto
        Produto produto = new Produto();
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setQuantidadeEstoque(quantidade);
        produto.setPrecoCusto(precoCusto);
        produto.setPrecoVenda(precoVenda);
        produto.setEstoqueMinimo(quantMinima);

        boolean sucesso;
        String mensagemSucesso;

        // 4. Verificar INSERÇÃO ou ATUALIZAÇÃO
        if (idProdutoEditando == -1) { // Inserir
            sucesso = produtoDAO.inserir(produto);
            if (sucesso) {
                mensagemSucesso = "Produto '" + produto.getNome() + "' (ID: " + produto.getId() + ") adicionado com sucesso!";
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao inserir produto.", "Erro no Banco de Dados", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else { // Atualizar
            produto.setId(idProdutoEditando);
            sucesso = produtoDAO.atualizar(produto);
            if (sucesso) {
                mensagemSucesso = "Produto '" + produto.getNome() + "' (ID: " + produto.getId() + ") atualizado com sucesso!";
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar produto.", "Erro no Banco de Dados", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // 5. Se sucesso, limpar e recarregar
        if (sucesso) {
            JOptionPane.showMessageDialog(this, mensagemSucesso, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCampos();
            carregarTabelaProdutos();
        }

    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        int linhaSelecionada = tblProdutos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na tabela para editar.", "Nenhuma Seleção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object idObj = tableModel.getValueAt(linhaSelecionada, 0);
        if (idObj == null) {
            return; // Segurança
        }
        try {
            int idParaEditar = (Integer) idObj;
            Produto produtoParaEditar = produtoDAO.buscarPorId(idParaEditar);

            if (produtoParaEditar != null) {
                // Preencher campos
                txtCodigo.setText(String.valueOf(produtoParaEditar.getId()));
                txtProduto.setText(produtoParaEditar.getNome());
                txtDescricao.setText(produtoParaEditar.getDescricao());
                txtQuantidade.setText(String.valueOf(produtoParaEditar.getQuantidadeEstoque()));
                txtPrecoCusto.setText(String.valueOf(produtoParaEditar.getPrecoCusto()));
                txtPrecoVenda.setText(String.valueOf(produtoParaEditar.getPrecoVenda()));
                txtQuantMinima.setText(String.valueOf(produtoParaEditar.getEstoqueMinimo()));

                this.idProdutoEditando = idParaEditar; // Guarda ID
                txtCodigo.setEnabled(false); // Desabilita campo código
                txtProduto.requestFocus();
            } else {
                JOptionPane.showMessageDialog(this, "Produto não encontrado no banco (ID: " + idParaEditar + ").", "Erro", JOptionPane.ERROR_MESSAGE);
                tblProdutos.clearSelection();
                carregarTabelaProdutos();
            }
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Erro interno ao obter ID do produto.", "Erro de Tipo", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnLimpar1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimpar1ActionPerformed
        limparCampos();
        if (!txtPesquisarCodigo.getText().isEmpty() || !txtPesquisarNome.getText().isEmpty()) {
            txtPesquisarCodigo.setText("");
            txtPesquisarNome.setText("");
            carregarTabelaProdutos();
        } else {
            tblProdutos.clearSelection();
        }
        habilitarBotoesEdicaoExclusao(false);
    }//GEN-LAST:event_btnLimpar1ActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed

        int linhaSelecionada = tblProdutos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para excluir.", "Nenhuma Seleção", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object idObj = tableModel.getValueAt(linhaSelecionada, 0);
        Object nomeObj = tableModel.getValueAt(linhaSelecionada, 1);
        if (idObj == null || nomeObj == null) {
            return;
        }

        try {
            int idParaExcluir = (Integer) idObj;
            String nomeParaExcluir = (String) nomeObj;

            int resposta = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o produto:\nID: " + idParaExcluir + "\nNome: " + nomeParaExcluir,
                    "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (resposta == JOptionPane.YES_OPTION) {
                boolean sucesso = produtoDAO.excluir(idParaExcluir);
                if (sucesso) {
                    JOptionPane.showMessageDialog(this, "Produto excluído com sucesso!", "Exclusão Concluída", JOptionPane.INFORMATION_MESSAGE);
                    carregarTabelaProdutos();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao excluir produto. Verifique se ele está registrado em alguma venda.", "Erro no Banco de Dados", JOptionPane.ERROR_MESSAGE);
                }
                limparCampos();
            }
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Erro interno ao obter dados do produto.", "Erro de Tipo", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_btnExcluirActionPerformed

    private void btnExecutarPesquisaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecutarPesquisaActionPerformed
        String codigoStr = txtPesquisarCodigo.getText().trim();
        String nomePesquisa = txtPesquisarNome.getText().trim();
        List<Produto> resultados; // Lista para guardar os produtos encontrados

        // Verifica se AMBOS os campos de pesquisa estão vazios
        if (codigoStr.isEmpty() && nomePesquisa.isEmpty()) {
            // Se ambos vazios, lista todos os produtos
            System.out.println("DEBUG: Campos de pesquisa vazios. Listando todos os produtos..."); // Log
            resultados = produtoDAO.listarTodos();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum produto cadastrado.", "Banco Vazio", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Exibindo todos os " + resultados.size() + " produto(s) cadastrado(s).", "Listagem Completa", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Se pelo menos um campo foi preenchido, filtra a busca
            System.out.println("DEBUG: Pesquisando por Código: [" + codigoStr + "], Nome: [" + nomePesquisa + "]"); // Log
            int codigoPesquisa = -1; // Valor inválido para indicar que não foi fornecido ou é inválido

            if (!codigoStr.isEmpty()) {
                try {
                    codigoPesquisa = Integer.parseInt(codigoStr);
                    if (codigoPesquisa <= 0) {
                        JOptionPane.showMessageDialog(this, "Código de pesquisa deve ser um número positivo.", "Código Inválido", JOptionPane.WARNING_MESSAGE);
                        // Mesmo com código inválido, pode haver busca por nome, então não retornamos aqui
                        // Apenas resetamos para que a busca por código não seja considerada
                        codigoPesquisa = -1;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Código de pesquisa inválido. Insira apenas números.", "Formato Inválido", JOptionPane.WARNING_MESSAGE);
                    // Se o código for inválido e o nome também estiver vazio, não faz sentido prosseguir
                    if (nomePesquisa.isEmpty()) {
                        atualizarTabela(new ArrayList<>()); // Limpa a tabela
                        return;
                    }
                    codigoPesquisa = -1; // Garante que não buscará por código inválido
                }
            }

            // Chama o DAO com os critérios (codigoPesquisa será -1 se não for um código válido)
            resultados = produtoDAO.buscarPorCodigoOuNome(codigoPesquisa, nomePesquisa);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum produto encontrado com os critérios informados.", "Pesquisa Sem Resultados", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, resultados.size() + " produto(s) encontrado(s).", "Pesquisa Concluída", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // Atualiza a tabela com os resultados (sejam todos os produtos ou os filtrados)
        atualizarTabela(resultados);

    }//GEN-LAST:event_btnExecutarPesquisaActionPerformed

    private void menuProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuProdutoActionPerformed

    }//GEN-LAST:event_menuProdutoActionPerformed

    private void menuItemNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemNovoActionPerformed
        limparCampos();
        tblProdutos.clearSelection();
        habilitarBotoesEdicaoExclusao(false);

    }//GEN-LAST:event_menuItemNovoActionPerformed

    private void menuItemVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemVoltarActionPerformed
        // Cria uma NOVA instância da Tela Principal
        TelaPrincipal telaPrincipal = new TelaPrincipal();
        //  Torna a nova instância da Tela Principal visível
        telaPrincipal.setVisible(true);
        // FECHA a janela ATUAL (TelaGerenciarProdutos)
        //    this.dispose() libera os recursos desta janela específica.
        this.dispose();

    }//GEN-LAST:event_menuItemVoltarActionPerformed

    private void menuItemSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSairActionPerformed
        // Adicionar um diálogo de confirmação
        int confirma = JOptionPane.showConfirmDialog(this, "Deseja realmente sair da aplicação?", "Confirmar Saída", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        System.exit(0); // Fecha a aplicação
    }//GEN-LAST:event_menuItemSairActionPerformed

    private void txtCodigoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCodigoActionPerformed

    }//GEN-LAST:event_txtCodigoActionPerformed

    private void txtPesquisarNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesquisarNomeActionPerformed

    }//GEN-LAST:event_txtPesquisarNomeActionPerformed

    private void txtProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProdutoActionPerformed

    }//GEN-LAST:event_txtProdutoActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaGerenciarProdutos().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnExecutarPesquisa;
    private javax.swing.JButton btnLimpar1;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblDescricao;
    private javax.swing.JLabel lblPesquisarCodigo;
    private javax.swing.JLabel lblPesquisarNome;
    private javax.swing.JLabel lblPrecoCusto;
    private javax.swing.JLabel lblPrecoVenda;
    private javax.swing.JLabel lblProduto;
    private javax.swing.JLabel lblQuantMinima;
    private javax.swing.JLabel lblQuantidade;
    private javax.swing.JMenuItem menuItemNovo;
    private javax.swing.JMenuItem menuItemSair;
    private javax.swing.JMenuItem menuItemVoltar;
    private javax.swing.JMenu menuProduto;
    private javax.swing.JPanel pnlArea;
    private javax.swing.JPanel pnlCampoPesquisa;
    private javax.swing.JPanel pnlProdutos;
    private javax.swing.JPanel pnlbotao;
    private javax.swing.JTable tblProdutos;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtDescricao;
    private javax.swing.JTextField txtPesquisarCodigo;
    private javax.swing.JTextField txtPesquisarNome;
    private javax.swing.JTextField txtPrecoCusto;
    private javax.swing.JTextField txtPrecoVenda;
    private javax.swing.JTextField txtProduto;
    private javax.swing.JTextField txtQuantMinima;
    private javax.swing.JTextField txtQuantidade;
    // End of variables declaration//GEN-END:variables
}
