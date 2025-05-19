package com.lojafacil.view;

// Imports necessarios
import com.lojafacil.model.Cliente;
import com.lojafacil.model.Produto;
import com.lojafacil.model.ItemVenda;
import com.lojafacil.model.Venda;
import com.lojafacil.dao.ClienteDAO;
import com.lojafacil.dao.ProdutoDAO;
import com.lojafacil.dao.VendaDAO;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Date; // Para a data da venda
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class TelaRegistrarVenda extends javax.swing.JFrame {

    // --- Variáveis de Estado da Venda Atual ---
    private Cliente clienteSelecionado = null;    // Guarda o cliente selecionado para esta venda
    private Produto produtoSelecionado = null;    // Guarda o último produto buscado/selecionado
    private List<ItemVenda> itensDaVendaAtual = new ArrayList<>(); // Lista os itens adicionados à venda
    private DefaultTableModel tableModelItensVenda; // Modelo da JTable tblItensVenda

    // --- DAOs ---
    private ClienteDAO clienteDAO;
    private ProdutoDAO produtoDAO;
    private VendaDAO vendaDAO;

    // Construtor
    public TelaRegistrarVenda() {
        initComponents(); // Executa o código do NetBeans para criar os componentes visuais

        // Inicializa os DAOs
        clienteDAO = new ClienteDAO();
        produtoDAO = new ProdutoDAO();
        vendaDAO = new VendaDAO();

        configurarTabelaItensVenda();
        // A tabela começa vazia, então não precisa carregar nada aqui inicialmente
        atualizarTabelaItensVenda(); // Apenas para garantir formatação e atualizar totais (serão 0)
        configurarListenersBusca(); // Agrupa configuração dos listeners
        limparTelaVenda(); // Garante estado inicial limpo e botões desabilitados
        setupEnterKeyFocusTransfer(jSpinner1);
    }

    private void configurarTabelaItensVenda() {
        String[] colunas = {"Código", "Produto", "Quantidade", "Preço Unitário", "Subtotal"};
        tableModelItensVenda = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Integer.class;
                    case 3:
                        return Double.class;
                    case 4:
                        return Double.class;
                    default:
                        return Object.class;
                }
            }
        };
        tblItensVenda.setModel(tableModelItensVenda);

        // ***** FORMATAR AS COLUNAS DE MOEDA *****
        try {
            // Cria uma única instância do renderer para reutilizar
            SplitCurrencyRenderer currencyRenderer = new SplitCurrencyRenderer();

            // Aplica ao "Preço Unitário" (Índice 3)
            TableColumnModel columnModel = tblItensVenda.getColumnModel();
            TableColumn precoUnitColumn = columnModel.getColumn(3);
            precoUnitColumn.setCellRenderer(currencyRenderer);

            // Aplica ao "Subtotal" (Índice 4)
            TableColumn subtotalColumn = columnModel.getColumn(4);
            subtotalColumn.setCellRenderer(currencyRenderer);

            // Opcional: Ajustar larguras das colunas de moeda aqui também
            precoUnitColumn.setPreferredWidth(130); //Preço unitario
            subtotalColumn.setPreferredWidth(130); // Subtotal
            columnModel.getColumn(0).setPreferredWidth(140); // Código
            columnModel.getColumn(1).setPreferredWidth(420); // Produto
            columnModel.getColumn(2).setPreferredWidth(120);  // Quantidade
            tblItensVenda.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Se quiser rolagem

        } catch (Exception e) {
            System.err.println("Erro ao aplicar SplitCurrencyRenderer na tabela de itens de venda: " + e.getMessage());
        }
    }

    private void configurarListenersBusca() {
        // Listener para buscar cliente
        lblBuscarCliente.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                buscarCliente();
            }
        });
        // Listener para buscar produto
        lblBuscarProduto.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (lblBuscarProduto.isEnabled()) {
                    buscarProduto();
                }
            }
        });
    }

    /**
     * Atualiza a JTable com os itens da lista temporária itensDaVendaAtual.
     */
    private void atualizarTabelaItensVenda() {
        tableModelItensVenda.setRowCount(0); // Limpa
        if (itensDaVendaAtual != null) {
            for (ItemVenda item : itensDaVendaAtual) {
                // Garante que temos o produto para exibir nome e ID
                Produto prod = item.getProduto();
                if (prod != null) {
                    double subtotal = item.calcularSubtotal();
                    Object[] rowData = {
                        prod.getId(),
                        prod.getNome(),
                        item.getQuantidade(),
                        item.getPrecoVenda(), // Preço unitário no momento da adição
                        subtotal
                    };
                    tableModelItensVenda.addRow(rowData);
                }
            }
        }
        atualizarTotais(); // Recalcula totais sempre que a tabela muda
        // Habilita finalizar venda apenas se houver itens
        btnFinalizarVenda.setEnabled(!itensDaVendaAtual.isEmpty());
    }

    /**
     * Atualiza os campos de totais (itens e valor) com base na lista
     * itensDaVendaAtual.
     */
    private void atualizarTotais() {
        int totalItens = 0;
        double valorTotalVenda = 0.0;

        if (itensDaVendaAtual != null) {
            for (ItemVenda item : itensDaVendaAtual) {
                totalItens += item.getQuantidade();
                valorTotalVenda += item.calcularSubtotal();
            }
        }

        // Atualiza o campo de total de itens (sem alteração)
        txtTotalItens.setText(String.valueOf(totalItens));
        // Formata valor total para Reais (R$) 
        // Cria um formatador para a moeda Brasileira (pt-BR)
        Locale localBrasil = new Locale("pt", "BR");
        NumberFormat formatadorMoeda = NumberFormat.getCurrencyInstance(localBrasil);

        // Formata o valor calculado como moeda (ex: "R$ 1.234,56")
        String valorFormatado = formatadorMoeda.format(valorTotalVenda);

        // Define o texto formatado no JTextField
        txtValorTotal.setText(valorFormatado);
        // ------------------------------------

        // Opcional: Alinhar o texto à direita no JTextField (melhora aparência de números)
        txtValorTotal.setHorizontalAlignment(JTextField.RIGHT); // Adicione esta linha se desejar
    }

    // --- Limpeza e Habilitação de Campos ---
    private void limparSelecaoCliente() {
        this.clienteSelecionado = null;
        txtVendaCliCpf.setText("");
        txtVendaCliNome.setText("");
        txtVendaCliEndereco.setText("");
        txtVendaCliTelefone.setText("");
        txtVendaCliEmail.setText("");
        // Desabilita seção de produto se nenhum cliente estiver selecionado
        habilitarSecaoProduto(false);
    }

    private void limparSelecaoProduto() {
        this.produtoSelecionado = null;
        txtVendaProdCodigo.setText("");
        txtVendaProdNome.setText("");
        jSpinner1.setValue(1); // Reset spinner
        btnAdicionarItem.setEnabled(false); // Desabilita botão adicionar
    }

    /**
     * Limpa toda a tela de venda, incluindo seleções e itens.
     */
    private void limparTelaVenda() {
        limparSelecaoCliente(); // Limpa cliente e desabilita produtos
        limparSelecaoProduto(); // Limpa seleção de produto
        this.itensDaVendaAtual.clear(); // Esvazia a lista de itens da venda ATUAL
        atualizarTabelaItensVenda(); // Limpa a JTable e atualiza totais (serão zero)
        // btnFinalizarVenda já será desabilitado por atualizarTabelaItensVenda
        // Foco inicial pode ir para o campo de busca de cliente ou similar
        lblBuscarCliente.requestFocus(); // Ou outro componente inicial
    }

    /**
     * Habilita/desabilita campos da seção de adicionar produto
     */
    private void habilitarSecaoProduto(boolean habilitar) {
        lblVendaProdCodigo.setEnabled(habilitar);
        txtVendaProdCodigo.setEnabled(false); // Código do produto nunca é editável aqui
        lblVendaProdNome.setEnabled(habilitar);
        txtVendaProdNome.setEnabled(false); // Nome do produto nunca é editável aqui
        lblBuscarProduto.setEnabled(habilitar);
        lblVendaProdQuantidade.setEnabled(habilitar);
        jSpinner1.setEnabled(habilitar);
        // Botão Adicionar SÓ habilita DEPOIS que um produto é buscado/selecionado
        btnAdicionarItem.setEnabled(false);

        if (!habilitar) {
            limparSelecaoProduto(); // Limpa campos se desabilitar
        }
    }

    // --- Lógica de Busca ---
    private void buscarCliente() {
        String criterio = JOptionPane.showInputDialog(this,
                "Digite o CPF ou Nome do Cliente:",
                "Buscar Cliente", JOptionPane.QUESTION_MESSAGE);

        if (criterio != null && !criterio.trim().isEmpty()) {
            criterio = criterio.trim();
            List<Cliente> encontrados = null;

            String criterioNumerico = criterio.replaceAll("[^0-9]", "");
            boolean pareceCpf = !criterioNumerico.isEmpty() && criterioNumerico.length() == 11 && criterio.matches(".*\\d.*");

            if (pareceCpf) {
                System.out.println("Buscando cliente por CPF: " + criterio);
                encontrados = clienteDAO.buscarPorCpfOuNome(criterio, null);
            } else {
                System.out.println("Buscando cliente por Nome: " + criterio);
                encontrados = clienteDAO.buscarPorCpfOuNome(null, criterio);
            }

            if (encontrados == null || encontrados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado.", "Não Encontrado", JOptionPane.WARNING_MESSAGE);
                limparSelecaoCliente();
            } else if (encontrados.size() == 1) {
                // Comportamento original se achar só um
                this.clienteSelecionado = encontrados.get(0);
                preencherCamposCliente();
                habilitarSecaoProduto(true);
                JOptionPane.showMessageDialog(this, "Cliente '" + clienteSelecionado.getNome() + "' selecionado.", "Cliente Encontrado", JOptionPane.INFORMATION_MESSAGE);
                lblBuscarProduto.requestFocus();
            } else {
                // ***** COMPORTAMENTO PARA MÚLTIPLOS CLIENTES *****
                System.out.println("DEBUG: Múltiplos clientes encontrados. Abrindo diálogo de seleção...");
                // Cria e exibe o diálogo modal, passando a lista encontrada
                SelecaoClienteDialog dialogCli = new SelecaoClienteDialog(this, true, encontrados); // Chama o novo diálogo
                dialogCli.setVisible(true); // Pausa a execução aqui até o diálogo fechar

                // Pega o cliente que foi selecionado no diálogo
                Cliente clienteEscolhido = dialogCli.getClienteSelecionado();

                if (clienteEscolhido != null) {
                    // Se um cliente foi escolhido
                    this.clienteSelecionado = clienteEscolhido;
                    preencherCamposCliente();
                    habilitarSecaoProduto(true); // Habilita busca de produto
                    JOptionPane.showMessageDialog(this, "Cliente '" + clienteSelecionado.getNome() + "' selecionado.", "Cliente Selecionado", JOptionPane.INFORMATION_MESSAGE);
                    lblBuscarProduto.requestFocus(); // Move foco para busca de produto
                } else {
                    // Se o usuário cancelou o diálogo de seleção de cliente
                    System.out.println("DEBUG: Diálogo de seleção de cliente cancelado.");
                    limparSelecaoCliente(); // Limpa a seleção anterior
                }
            }
        }
    }

    private void preencherCamposCliente() {
        if (this.clienteSelecionado != null) {
            txtVendaCliCpf.setText(clienteSelecionado.getCpf());
            txtVendaCliNome.setText(clienteSelecionado.getNome());
            txtVendaCliEndereco.setText(clienteSelecionado.getEndereco());
            txtVendaCliTelefone.setText(clienteSelecionado.getTelefone());
            txtVendaCliEmail.setText(clienteSelecionado.getEmail());
        } else {
            limparSelecaoCliente(); // Garante limpeza se objeto for nulo
        }
    }

    private void buscarProduto() {
        String criterio = JOptionPane.showInputDialog(this,
                "Digite o Código ou Nome do Produto:",
                "Buscar Produto", JOptionPane.QUESTION_MESSAGE);

        if (criterio != null && !criterio.trim().isEmpty()) {
            criterio = criterio.trim();
            List<Produto> encontrados = null;
            int codigoBusca = -1;
            boolean buscaPorCodigo = false;

            try {
                codigoBusca = Integer.parseInt(criterio);
                if (codigoBusca > 0) {
                    buscaPorCodigo = true;
                } else {
                    codigoBusca = -1;
                }
            } catch (NumberFormatException e) {
                /* Ignora */ }

            if (buscaPorCodigo) {
                encontrados = produtoDAO.buscarPorCodigoOuNome(codigoBusca, null);
            } else {
                encontrados = produtoDAO.buscarPorCodigoOuNome(-1, criterio);
            }

            if (encontrados == null || encontrados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum produto encontrado.", "Não Encontrado", JOptionPane.WARNING_MESSAGE);
                limparSelecaoProduto();
            } else if (encontrados.size() == 1) {
                // Comportamento original para resultado único
                this.produtoSelecionado = encontrados.get(0);
                if (produtoSelecionado.getQuantidadeEstoque() <= 0) {
                    JOptionPane.showMessageDialog(this, "Produto '" + produtoSelecionado.getNome() + "' sem estoque!", "Estoque Esgotado", JOptionPane.WARNING_MESSAGE);
                    limparSelecaoProduto();
                } else {
                    preencherCamposProduto();
                    btnAdicionarItem.setEnabled(true);
                    jSpinner1.requestFocus();
                    JOptionPane.showMessageDialog(this, "Produto '" + produtoSelecionado.getNome() + "' selecionado (Estoque: " + produtoSelecionado.getQuantidadeEstoque() + ").", "Produto Encontrado", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {

                System.out.println("DEBUG: Múltiplos produtos encontrados. Abrindo diálogo de seleção...");
                // Cria e exibe o diálogo modal, passando a lista encontrada
                SelecaoProdutoDialog dialog = new SelecaoProdutoDialog(this, true, encontrados); // Passa true para modal
                dialog.setVisible(true); // Pausa a execução aqui até o diálogo fechar

                // Pega o produto que foi selecionado no diálogo
                Produto produtoEscolhido = dialog.getProdutoSelecionado();

                if (produtoEscolhido != null) {
                    // Se um produto foi escolhido (e não cancelado, e tinha estoque)
                    this.produtoSelecionado = produtoEscolhido;
                    preencherCamposProduto();
                    btnAdicionarItem.setEnabled(true);
                    jSpinner1.requestFocus();
                    JOptionPane.showMessageDialog(this, "Produto '" + produtoSelecionado.getNome() + "' selecionado (Estoque: " + produtoSelecionado.getQuantidadeEstoque() + ").", "Produto Selecionado", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Se o usuário cancelou o diálogo ou o produto escolhido estava sem estoque
                    System.out.println("DEBUG: Diálogo de seleção cancelado ou produto sem estoque.");
                    limparSelecaoProduto(); // Limpa a seleção
                }
            }
        }
    }

    private void preencherCamposProduto() {
        if (this.produtoSelecionado != null) {
            txtVendaProdCodigo.setText(String.valueOf(produtoSelecionado.getId()));
            txtVendaProdNome.setText(produtoSelecionado.getNome());
        } else {
            limparSelecaoProduto();
        }
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

        pnlVendas = new javax.swing.JPanel();
        pnlDadosCliente = new javax.swing.JPanel();
        txtVendaCliCpf = new javax.swing.JTextField();
        txtVendaCliNome = new javax.swing.JTextField();
        txtVendaCliEndereco = new javax.swing.JTextField();
        txtVendaCliTelefone = new javax.swing.JTextField();
        txtVendaCliEmail = new javax.swing.JTextField();
        lblVendaCliCpf = new javax.swing.JLabel();
        lblVendaCliNome = new javax.swing.JLabel();
        lblBuscarCliente = new javax.swing.JLabel();
        lblVendaCliEndereco = new javax.swing.JLabel();
        lblVendaCliTelefone = new javax.swing.JLabel();
        lblVendaCliEmail = new javax.swing.JLabel();
        pnlAdicionarProduto = new javax.swing.JPanel();
        txtVendaProdCodigo = new javax.swing.JTextField();
        txtVendaProdNome = new javax.swing.JTextField();
        jSpinner1 = new javax.swing.JSpinner();
        btnAdicionarItem = new javax.swing.JButton();
        lblVendaProdCodigo = new javax.swing.JLabel();
        lblVendaProdNome = new javax.swing.JLabel();
        lblBuscarProduto = new javax.swing.JLabel();
        lblVendaProdQuantidade = new javax.swing.JLabel();
        pnlItensVenda = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblItensVenda = new javax.swing.JTable();
        lblTotalItens = new javax.swing.JLabel();
        txtTotalItens = new javax.swing.JTextField();
        lblValorTotal = new javax.swing.JLabel();
        txtValorTotal = new javax.swing.JTextField();
        btnFinalizarVenda = new javax.swing.JButton();
        btnCancelarVenda = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuVendas = new javax.swing.JMenu();
        MenuItemVoltar = new javax.swing.JMenuItem();
        MenuItemSair = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlDadosCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Dados de Cliente", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        txtVendaCliCpf.setEditable(false);

        txtVendaCliNome.setEditable(false);

        txtVendaCliEndereco.setEditable(false);

        txtVendaCliTelefone.setEditable(false);

        txtVendaCliEmail.setEditable(false);

        lblVendaCliCpf.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaCliCpf.setText("CPF: ");

        lblVendaCliNome.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaCliNome.setText("Nome: ");

        lblBuscarCliente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/lupa32x32.png"))); // NOI18N
        lblBuscarCliente.setText("jLabel3");

        lblVendaCliEndereco.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaCliEndereco.setText("Endereço: ");

        lblVendaCliTelefone.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaCliTelefone.setText("Telefone: ");

        lblVendaCliEmail.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaCliEmail.setText("Email: ");

        javax.swing.GroupLayout pnlDadosClienteLayout = new javax.swing.GroupLayout(pnlDadosCliente);
        pnlDadosCliente.setLayout(pnlDadosClienteLayout);
        pnlDadosClienteLayout.setHorizontalGroup(
            pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDadosClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlDadosClienteLayout.createSequentialGroup()
                        .addComponent(lblVendaCliTelefone)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtVendaCliTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblVendaCliEmail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVendaCliEmail))
                    .addGroup(pnlDadosClienteLayout.createSequentialGroup()
                        .addGroup(pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblVendaCliEndereco)
                            .addComponent(lblVendaCliCpf))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlDadosClienteLayout.createSequentialGroup()
                                .addComponent(txtVendaCliCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblVendaCliNome)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtVendaCliNome, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(lblBuscarCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtVendaCliEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 833, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        pnlDadosClienteLayout.setVerticalGroup(
            pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDadosClienteLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVendaCliCpf)
                    .addComponent(txtVendaCliCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVendaCliNome)
                    .addComponent(txtVendaCliNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBuscarCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVendaCliEndereco)
                    .addComponent(txtVendaCliEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlDadosClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVendaCliTelefone)
                    .addComponent(txtVendaCliTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVendaCliEmail)
                    .addComponent(txtVendaCliEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pnlAdicionarProduto.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Adicionar Produto", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        txtVendaProdCodigo.setEditable(false);

        txtVendaProdNome.setEditable(false);

        btnAdicionarItem.setBackground(new java.awt.Color(0, 51, 102));
        btnAdicionarItem.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnAdicionarItem.setForeground(new java.awt.Color(255, 255, 255));
        btnAdicionarItem.setText("Adicionar");
        btnAdicionarItem.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnAdicionarItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarItemActionPerformed(evt);
            }
        });

        lblVendaProdCodigo.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaProdCodigo.setText("Código: ");

        lblVendaProdNome.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaProdNome.setText("Produto: ");

        lblBuscarProduto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/lupa32x32.png"))); // NOI18N
        lblBuscarProduto.setText("jLabel3");

        lblVendaProdQuantidade.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblVendaProdQuantidade.setText("Quantidade: ");

        javax.swing.GroupLayout pnlAdicionarProdutoLayout = new javax.swing.GroupLayout(pnlAdicionarProduto);
        pnlAdicionarProduto.setLayout(pnlAdicionarProdutoLayout);
        pnlAdicionarProdutoLayout.setHorizontalGroup(
            pnlAdicionarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdicionarProdutoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAdicionarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAdicionarProdutoLayout.createSequentialGroup()
                        .addComponent(lblVendaProdCodigo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVendaProdCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblVendaProdNome)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtVendaProdNome, javax.swing.GroupLayout.PREFERRED_SIZE, 517, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlAdicionarProdutoLayout.createSequentialGroup()
                        .addComponent(lblVendaProdQuantidade)
                        .addGap(18, 18, 18)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28)
                        .addComponent(btnAdicionarItem, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28)
                .addComponent(lblBuscarProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlAdicionarProdutoLayout.setVerticalGroup(
            pnlAdicionarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAdicionarProdutoLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlAdicionarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVendaProdCodigo)
                    .addComponent(txtVendaProdCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblVendaProdNome)
                    .addComponent(txtVendaProdNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblBuscarProduto))
                .addGap(18, 18, 18)
                .addGroup(pnlAdicionarProdutoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVendaProdQuantidade)
                    .addComponent(btnAdicionarItem)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pnlItensVenda.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Itens Venda", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        tblItensVenda.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "Produto", "Quantidade", "Preço Unitário", "Subtotal"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblItensVenda);

        lblTotalItens.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblTotalItens.setText("Total de Itens: ");

        txtTotalItens.setEditable(false);

        lblValorTotal.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblValorTotal.setText("Valor Total R$:");

        txtValorTotal.setEditable(false);

        javax.swing.GroupLayout pnlItensVendaLayout = new javax.swing.GroupLayout(pnlItensVenda);
        pnlItensVenda.setLayout(pnlItensVendaLayout);
        pnlItensVendaLayout.setHorizontalGroup(
            pnlItensVendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlItensVendaLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlItensVendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(pnlItensVendaLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblTotalItens)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTotalItens, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblValorTotal)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)))
                .addContainerGap())
        );
        pnlItensVendaLayout.setVerticalGroup(
            pnlItensVendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlItensVendaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addGroup(pnlItensVendaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTotalItens)
                    .addComponent(txtTotalItens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValorTotal)
                    .addComponent(txtValorTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnFinalizarVenda.setBackground(new java.awt.Color(0, 153, 51));
        btnFinalizarVenda.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnFinalizarVenda.setForeground(new java.awt.Color(255, 255, 255));
        btnFinalizarVenda.setText("Finalizar Venda");
        btnFinalizarVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnFinalizarVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFinalizarVendaActionPerformed(evt);
            }
        });

        btnCancelarVenda.setBackground(new java.awt.Color(255, 0, 0));
        btnCancelarVenda.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnCancelarVenda.setForeground(new java.awt.Color(255, 255, 255));
        btnCancelarVenda.setText("Cancelar Venda");
        btnCancelarVenda.setToolTipText("Excluir o produto selecionado na tabela");
        btnCancelarVenda.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCancelarVenda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarVendaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlVendasLayout = new javax.swing.GroupLayout(pnlVendas);
        pnlVendas.setLayout(pnlVendasLayout);
        pnlVendasLayout.setHorizontalGroup(
            pnlVendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVendasLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlVendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlDadosCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlAdicionarProduto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlItensVenda, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlVendasLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancelarVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnFinalizarVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
        );
        pnlVendasLayout.setVerticalGroup(
            pnlVendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlVendasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlDadosCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlAdicionarProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlItensVenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(pnlVendasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnFinalizarVenda)
                    .addComponent(btnCancelarVenda))
                .addContainerGap(23, Short.MAX_VALUE))
        );

        menuVendas.setMnemonic('v');
        menuVendas.setText("Venda");

        MenuItemVoltar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuItemVoltar.setMnemonic('o');
        MenuItemVoltar.setText("Voltar");
        MenuItemVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItemVoltarActionPerformed(evt);
            }
        });
        menuVendas.add(MenuItemVoltar);

        MenuItemSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuItemSair.setText("Sair");
        MenuItemSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItemSairActionPerformed(evt);
            }
        });
        menuVendas.add(MenuItemSair);

        jMenuBar1.add(menuVendas);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlVendas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlVendas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAdicionarItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarItemActionPerformed
        // Pré-requisitos já verificados (cliente selecionado, produto selecionado com estoque)
        if (this.produtoSelecionado == null || this.clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione o cliente e o produto primeiro.", "Erro", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade;
        try {
            quantidade = (Integer) jSpinner1.getValue();
            if (quantidade <= 0) {
                JOptionPane.showMessageDialog(this, "A quantidade deve ser maior que zero.", "Quantidade Inválida", JOptionPane.WARNING_MESSAGE);
                jSpinner1.requestFocus();
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Valor inválido para quantidade.", "Erro de Quantidade", JOptionPane.ERROR_MESSAGE);
            jSpinner1.requestFocus();
            return;
        }

        // Verifica estoque novamente (segurança)
        if (produtoSelecionado.getQuantidadeEstoque() < quantidade) {
            JOptionPane.showMessageDialog(this,
                    "Estoque insuficiente para '" + produtoSelecionado.getNome() + "'.\nDisponível: " + produtoSelecionado.getQuantidadeEstoque(),
                    "Erro de Estoque", JOptionPane.WARNING_MESSAGE);
            jSpinner1.requestFocus();
            return;
        }

        // Cria o ItemVenda (não precisamos de ID aqui, ele será gerado no banco)
        ItemVenda novoItem = new ItemVenda();
        novoItem.setProduto(this.produtoSelecionado); // Associa o produto selecionado
        novoItem.setQuantidade(quantidade);
        novoItem.setPrecoVenda(this.produtoSelecionado.getPrecoVenda()); // Guarda o preço do momento

        // Adiciona à lista temporária da venda atual
        itensDaVendaAtual.add(novoItem);

        // Atualiza a JTable e os totais
        atualizarTabelaItensVenda();

        // Limpa a seleção de produto para o próximo item
        limparSelecaoProduto();

        // Foco pode voltar para busca de produto
        lblBuscarProduto.requestFocus();

    }//GEN-LAST:event_btnAdicionarItemActionPerformed

    private void btnFinalizarVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFinalizarVendaActionPerformed
        // Verifica se há itens e cliente
        if (itensDaVendaAtual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione itens à venda antes de finalizar.", "Venda Vazia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (clienteSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente para finalizar a venda.", "Cliente Ausente", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirmação
        double valorTotal = calcularValorTotalAtual(); // Método auxiliar para pegar o total
        int resposta = JOptionPane.showConfirmDialog(this,
                "Total da Venda: R$ " + String.format("%.2f", valorTotal) + "\nConfirmar e finalizar esta venda?",
                "Finalizar Venda", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resposta == JOptionPane.YES_OPTION) {
            // Cria o objeto Venda
            Venda vendaParaRegistrar = new Venda();
            vendaParaRegistrar.setCliente(this.clienteSelecionado);
            // IMPORTANTE: Passar uma CÓPIA da lista de itens, não a referência original!
            vendaParaRegistrar.setItensVenda(new ArrayList<>(this.itensDaVendaAtual));
            vendaParaRegistrar.setDataVenda(new Date()); // Define data/hora atual

            // Chama o DAO para registrar a venda completa (com transação)
            boolean sucesso = vendaDAO.registrarVendaCompleta(vendaParaRegistrar);

            if (sucesso) {
                // Venda registrada com sucesso (incluindo baixa de estoque)
                JOptionPane.showMessageDialog(this,
                        "Venda (ID: " + vendaParaRegistrar.getId() + ") finalizada com sucesso!", // Mostra o ID gerado
                        "Venda Concluída", JOptionPane.INFORMATION_MESSAGE);
                limparTelaVenda(); // Limpa tudo para a próxima venda
            } else {
                // O DAO já deve ter tratado o rollback e mostrado/logado o erro.
                // Apenas informa o usuário que a finalização falhou.
                JOptionPane.showMessageDialog(this,
                        "Erro ao finalizar a venda. A operação foi cancelada.\nVerifique o console para detalhes do erro ou se o estoque era suficiente.",
                        "Erro na Finalização", JOptionPane.ERROR_MESSAGE);
                // Não limpa a tela neste caso, para o usuário poder tentar corrigir ou cancelar
            }
        }
    }

    // Método auxiliar para pegar o valor total atual
    private double calcularValorTotalAtual() {
        double valorTotal = 0.0;
        if (itensDaVendaAtual != null) {
            for (ItemVenda item : itensDaVendaAtual) {
                valorTotal += item.calcularSubtotal();
            }
        }
        return valorTotal;

    }//GEN-LAST:event_btnFinalizarVendaActionPerformed

    private void btnCancelarVendaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarVendaActionPerformed
        // Cancela a venda em andamento
        if (!itensDaVendaAtual.isEmpty() || clienteSelecionado != null) {
            int resposta = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja cancelar esta venda em andamento?\nTodos os itens adicionados serão perdidos.",
                    "Cancelar Venda", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (resposta == JOptionPane.YES_OPTION) {
                limparTelaVenda(); // Limpa tudo
                JOptionPane.showMessageDialog(this, "Venda cancelada!", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Nenhuma venda em andamento para cancelar.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_btnCancelarVendaActionPerformed

    private void MenuItemVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItemVoltarActionPerformed
        //  Cria uma NOVA instância da Tela Principal
        TelaPrincipal telaPrincipal = new TelaPrincipal();
        //  Torna a nova instância da Tela Principal visível
        telaPrincipal.setVisible(true);
        // FECHA a janela ATUAL (TelaGerenciarProdutos)
        //  this.dispose() libera os recursos desta janela específica.
        this.dispose();

    }//GEN-LAST:event_MenuItemVoltarActionPerformed

    private void MenuItemSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItemSairActionPerformed
        // Adicionar um diálogo de confirmação
        int confirma = JOptionPane.showConfirmDialog(this, "Deseja realmente sair da aplicação?", "Confirmar Saída", JOptionPane.YES_NO_OPTION);
        if (confirma == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
        System.exit(0); // Fecha a aplicação
    }//GEN-LAST:event_MenuItemSairActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaRegistrarVenda().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem MenuItemSair;
    private javax.swing.JMenuItem MenuItemVoltar;
    private javax.swing.JButton btnAdicionarItem;
    private javax.swing.JButton btnCancelarVenda;
    private javax.swing.JButton btnFinalizarVenda;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JLabel lblBuscarCliente;
    private javax.swing.JLabel lblBuscarProduto;
    private javax.swing.JLabel lblTotalItens;
    private javax.swing.JLabel lblValorTotal;
    private javax.swing.JLabel lblVendaCliCpf;
    private javax.swing.JLabel lblVendaCliEmail;
    private javax.swing.JLabel lblVendaCliEndereco;
    private javax.swing.JLabel lblVendaCliNome;
    private javax.swing.JLabel lblVendaCliTelefone;
    private javax.swing.JLabel lblVendaProdCodigo;
    private javax.swing.JLabel lblVendaProdNome;
    private javax.swing.JLabel lblVendaProdQuantidade;
    private javax.swing.JMenu menuVendas;
    private javax.swing.JPanel pnlAdicionarProduto;
    private javax.swing.JPanel pnlDadosCliente;
    private javax.swing.JPanel pnlItensVenda;
    private javax.swing.JPanel pnlVendas;
    private javax.swing.JTable tblItensVenda;
    private javax.swing.JTextField txtTotalItens;
    private javax.swing.JTextField txtValorTotal;
    private javax.swing.JTextField txtVendaCliCpf;
    private javax.swing.JTextField txtVendaCliEmail;
    private javax.swing.JTextField txtVendaCliEndereco;
    private javax.swing.JTextField txtVendaCliNome;
    private javax.swing.JTextField txtVendaCliTelefone;
    private javax.swing.JTextField txtVendaProdCodigo;
    private javax.swing.JTextField txtVendaProdNome;
    // End of variables declaration//GEN-END:variables
}
