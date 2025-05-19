package com.lojafacil.view;

// Imports necessarios
import com.lojafacil.model.Cliente;
import com.lojafacil.dao.ClienteDAO;
import com.lojafacil.dao.ClienteComVendasException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class TelaGerenciarClientes extends javax.swing.JFrame {

    // Modelo da tabela para exibir os clientes
    private DefaultTableModel tableModelClientes;
    // ID do cliente sendo editado
    private int idClienteEditando = -1; // Usar -1 ou 0 para indicar nenhum cliente sendo editado
    private ClienteDAO clienteDAO; //  Instância do DAO

    // Construtor 
    public TelaGerenciarClientes() {
        initComponents();
        try {
            // --- Máscara para CPF ---
            javax.swing.text.MaskFormatter cpfMask = new javax.swing.text.MaskFormatter("###.###.###-##");
            cpfMask.setPlaceholderCharacter('_'); // Caractere que aparece nos espaços vazios
            // Remove a máscara se o campo não estiver preenchido (opcional)
            // cpfMask.setValueContainsLiteralCharacters(false); // Se quiser pegar só números com getValue()
            ftxtCpf.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(cpfMask));

            // --- Máscara para Telefone Celular (com 9 dígitos + DDD) ---
            javax.swing.text.MaskFormatter phoneMask = new javax.swing.text.MaskFormatter("(##) # ####-####");
            phoneMask.setPlaceholderCharacter('_');
            // phoneMask.setValueContainsLiteralCharacters(false); // Se quiser pegar só números com getValue()
            ftxtTelefone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(phoneMask));

        } catch (java.text.ParseException ex) {
            // Trata erro na criação das máscaras (não deveria acontecer com máscaras fixas)
            JOptionPane.showMessageDialog(this, "Erro ao criar máscaras de formatação:\n" + ex.getMessage(),
                    "Erro de Formatação", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        clienteDAO = new ClienteDAO(); //  Inicializa o DAO
        configurarTabelaClientes();         // Chama o método para definir coluna
        adicionarListenerSelecaoTabela();   // Chama o método para habilitar/desabilitar botões
        habilitarBotoesEdicaoExclusao(false); // Garante que Editar/Excluir comecem desabilitados
        carregarTabelaClientes(); //  Carrega dados do banco ao iniciar
        limparCamposClientes(); // Garante estado inicial limpo

        //Componentes focaveis utilizando a tecla enter
        setupEnterKeyFocusTransfer(ftxtCpf);      // Do CPF para o próximo...
        setupEnterKeyFocusTransfer(txtNome);      // Do Nome para o próximo...
        setupEnterKeyFocusTransfer(txtEndereco);  // Do Endereço para o próximo...
        setupEnterKeyFocusTransfer(txtEmail);     // Do Email para o próximo...
        setupEnterKeyFocusTransfer(ftxtTelefone); // Do Telefone para o próximo...

    }

    private void configurarTabelaClientes() {
        // Define os nomes das colunas que aparecerão na JTable
        String[] colunas = {"Código", "CPF", "Nome", "Telefone", "Email"};
        // Cria o modelo de tabela SEMPRE iniciando sem linhas (0)
        // Também define que as células não serão editáveis diretamente na tabela
        tableModelClientes = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            // Define tipos de coluna para melhor ordenação/renderização

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return Integer.class; // <<< Código (ID)
                    case 1:
                        return String.class;  // <<< CPF
                    case 2:
                        return String.class;  // <<< Nome 
                    case 3:
                        return String.class;  // <<< Telefone 
                    case 4:
                        return String.class;  // <<< Email 
                    default:
                        return Object.class;
                }
            }
        };
        tblClientes.setModel(tableModelClientes);

        // Definir largura preferencial para as colunas
        tblClientes.getColumnModel().getColumn(0).setPreferredWidth(20); // Código
        tblClientes.getColumnModel().getColumn(1).setPreferredWidth(100); // CPF
        tblClientes.getColumnModel().getColumn(2).setPreferredWidth(250); // Nome
        tblClientes.getColumnModel().getColumn(4).setPreferredWidth(200); // Email

    }

    private void atualizarTabelaClientes(List<Cliente> clientesParaExibir) {
        tableModelClientes.setRowCount(0);
        if (clientesParaExibir != null) {
            for (Cliente c : clientesParaExibir) {
                Object[] rowData = {
                    c.getId(),
                    c.getCpf(),
                    c.getNome(),
                    c.getTelefone(),
                    c.getEmail()
                };
                tableModelClientes.addRow(rowData);
            }
        }
        habilitarBotoesEdicaoExclusao(false);
    }

    /**
     * Limpa a tabela e a recarrega com dados do banco de dados.
     */
    private void carregarTabelaClientes() {
        // Busca todos os clientes do banco de dados usando o DAO
        List<Cliente> clientesDoBanco = clienteDAO.listarTodos();
        // Passa a lista obtida para o método que atualiza a JTable
        atualizarTabelaClientes(clientesDoBanco);
        // Limpa a seleção após recarregar
        tblClientes.clearSelection();
    }

    private void adicionarListenerSelecaoTabela() {
        // Adiciona um listener que "ouve" mudanças na seleção de linhas da tabela
        tblClientes.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                // event.getValueIsAdjusting() evita disparos múltiplos durante a seleção
                if (!event.getValueIsAdjusting()) {
                    // Verifica se exatamente UMA linha está selecionada
                    habilitarBotoesEdicaoExclusao(tblClientes.getSelectedRowCount() == 1);
                }
            }
        });
    }

    private void habilitarBotoesEdicaoExclusao(boolean habilitar) {
        // Habilita ou desabilita os botões com base no parâmetro
        btnEditar.setEnabled(habilitar);
        btnExcluir.setEnabled(habilitar);
    }

    private void limparCamposClientes() {
        // Limpa campos do formulário de dados do cliente
        txtCodigo.setText("");
        ftxtCpf.setText("");
        txtNome.setText("");
        txtEndereco.setText("");
        txtEmail.setText("");
        ftxtTelefone.setText("");
        txtPesquisarCpf.setText("");
        txtPesquisarNome.setText("");

        // Garante que o campo código esteja habilitado 
        this.idClienteEditando = -1; // Indica que não está editando
        txtCodigo.setEnabled(false); // <<< Desabilita campo código (será AUTO_INCREMENT)
        // Ou mantenha habilitado se quiser *exibir* o ID gerado após salvar

        // Coloca o foco no primeiro campo editável do formulário 
        ftxtCpf.requestFocus();
    }

    /**
     * Valida um endereço de e-mail usando uma expressão regular simples.
     *
     * @param email O email a ser validado.
     * @return true se o formato for válido, false caso contrário.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return true; // Considera email vazio como válido (não obrigatório)
        }
        // Expressão regular simples para validação de email
        // (Pode ser ajustada para ser mais ou menos restritiva)
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        java.util.regex.Pattern pat = java.util.regex.Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    /**
     * Configura um componente para transferir o foco para o próximo componente
     * na ordem de foco quando a tecla Enter for pressionada.
     *
     * @param component O JComponent (JTextField, JFormattedTextField, etc.)
     */
    private void setupEnterKeyFocusTransfer(JComponent component) {
        if (component == null) {
            return; // Verifica se o componente existe
        }
        // Define o KeyStroke que representa a tecla Enter sendo pressionada
        KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        // Define um nome (chave) para a nossa ação de transferir foco
        String actionKey = "transferFocus";

        // Pega o InputMap do componente que é usado QUANDO o componente TEM o foco
        InputMap inputMap = component.getInputMap(JComponent.WHEN_FOCUSED);
        // Associa a tecla Enter (enterKeyStroke) com o nome da nossa ação (actionKey)
        // Quando Enter for pressionado neste componente, o sistema procurará por "transferFocus" no ActionMap
        inputMap.put(enterKeyStroke, actionKey);

        // Pega o ActionMap do componente
        ActionMap actionMap = component.getActionMap();
        // Cria a Ação que será executada
        Action transferFocusAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // O código dentro desta ação é executado quando Enter é pressionado
                // Pega o componente que disparou o evento e chama transferFocus() nele
                ((JComponent) e.getSource()).transferFocus();
            }
        };
        // Associa o nome da nossa ação ("transferFocus") com a Ação que acabamos de criar
        actionMap.put(actionKey, transferFocusAction);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlClientes = new javax.swing.JPanel();
        pnlCampoPesquisa = new javax.swing.JPanel();
        txtPesquisarCpf = new javax.swing.JTextField();
        txtPesquisarNome = new javax.swing.JTextField();
        btnExecutarPesquisa = new javax.swing.JButton();
        lblPesquisarCpf = new javax.swing.JLabel();
        lblPesquisarNome = new javax.swing.JLabel();
        pnlListaClientes = new javax.swing.JPanel();
        btnEditar = new javax.swing.JButton();
        btnExcluir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        pnlNovoCliente = new javax.swing.JPanel();
        ftxtCpf = new javax.swing.JFormattedTextField();
        txtNome = new javax.swing.JTextField();
        txtEndereco = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        ftxtTelefone = new javax.swing.JFormattedTextField();
        lblCodigo = new javax.swing.JLabel();
        lblCpf = new javax.swing.JLabel();
        lblNome = new javax.swing.JLabel();
        lblEndereco = new javax.swing.JLabel();
        lblTelefone = new javax.swing.JLabel();
        lblEmail = new javax.swing.JLabel();
        txtCodigo = new javax.swing.JTextField();
        btnLimpar = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        MenuCliente = new javax.swing.JMenu();
        MenuItemNovo = new javax.swing.JMenuItem();
        MenuItemVoltar = new javax.swing.JMenuItem();
        MenuItemSair = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(976, 764));

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
        btnExecutarPesquisa.setPreferredSize(new java.awt.Dimension(46, 26));
        btnExecutarPesquisa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExecutarPesquisaActionPerformed(evt);
            }
        });

        lblPesquisarCpf.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblPesquisarCpf.setText("CPF:");

        lblPesquisarNome.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblPesquisarNome.setText("Nome:");

        javax.swing.GroupLayout pnlCampoPesquisaLayout = new javax.swing.GroupLayout(pnlCampoPesquisa);
        pnlCampoPesquisa.setLayout(pnlCampoPesquisaLayout);
        pnlCampoPesquisaLayout.setHorizontalGroup(
            pnlCampoPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoPesquisaLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblPesquisarCpf)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesquisarCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblPesquisarNome)
                .addGap(18, 18, 18)
                .addComponent(txtPesquisarNome, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExecutarPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(34, Short.MAX_VALUE))
        );
        pnlCampoPesquisaLayout.setVerticalGroup(
            pnlCampoPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoPesquisaLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(pnlCampoPesquisaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPesquisarCpf)
                    .addComponent(txtPesquisarCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPesquisarNome)
                    .addComponent(txtPesquisarNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnExecutarPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pnlListaClientes.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lista de Clientes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N
        pnlListaClientes.setForeground(new java.awt.Color(153, 153, 153));

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
        btnExcluir.setPreferredSize(new java.awt.Dimension(46, 26));
        btnExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExcluirActionPerformed(evt);
            }
        });

        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "CPF", "Nome", "Telefone", "Email"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblClientes);

        javax.swing.GroupLayout pnlListaClientesLayout = new javax.swing.GroupLayout(pnlListaClientes);
        pnlListaClientes.setLayout(pnlListaClientesLayout);
        pnlListaClientesLayout.setHorizontalGroup(
            pnlListaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlListaClientesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEditar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
            .addGroup(pnlListaClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 914, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlListaClientesLayout.setVerticalGroup(
            pnlListaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlListaClientesLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlListaClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditar)
                    .addComponent(btnExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlNovoCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cadastar Novo Cliente", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N
        pnlNovoCliente.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pnlNovoClienteFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                pnlNovoClienteFocusLost(evt);
            }
        });

        lblCodigo.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblCodigo.setText("Código:");

        lblCpf.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblCpf.setText("CPF:");

        lblNome.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblNome.setText("Nome:");

        lblEndereco.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblEndereco.setText("Endereço:");

        lblTelefone.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblTelefone.setText("Telefone:");

        lblEmail.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblEmail.setText("Email:");

        btnLimpar.setBackground(new java.awt.Color(246, 232, 44));
        btnLimpar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnLimpar.setText("Limpar");
        btnLimpar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
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

        javax.swing.GroupLayout pnlNovoClienteLayout = new javax.swing.GroupLayout(pnlNovoCliente);
        pnlNovoCliente.setLayout(pnlNovoClienteLayout);
        pnlNovoClienteLayout.setHorizontalGroup(
            pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                        .addComponent(lblTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ftxtTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34))
                    .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                        .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                                .addComponent(lblEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE))
                            .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                                .addComponent(lblCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(88, 88, 88)
                                .addComponent(lblCpf)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ftxtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                                .addComponent(lblNome, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtNome))
                            .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                                .addComponent(lblEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtEndereco)))
                        .addContainerGap(239, Short.MAX_VALUE))))
        );
        pnlNovoClienteLayout.setVerticalGroup(
            pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlNovoClienteLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCodigo)
                            .addComponent(txtCodigo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlNovoClienteLayout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblCpf)
                            .addComponent(ftxtCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(18, 18, 18)
                .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNome)
                    .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEndereco)
                    .addComponent(txtEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmail)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlNovoClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnLimpar)
                    .addComponent(btnSalvar)
                    .addComponent(lblTelefone)
                    .addComponent(ftxtTelefone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlClientesLayout = new javax.swing.GroupLayout(pnlClientes);
        pnlClientes.setLayout(pnlClientesLayout);
        pnlClientesLayout.setHorizontalGroup(
            pnlClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlListaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCampoPesquisa, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlNovoCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        pnlClientesLayout.setVerticalGroup(
            pnlClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlNovoCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlCampoPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlListaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        MenuCliente.setText("Clientes");

        MenuItemNovo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuItemNovo.setText("Novo Cliente");
        MenuItemNovo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItemNovoActionPerformed(evt);
            }
        });
        MenuCliente.add(MenuItemNovo);

        MenuItemVoltar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuItemVoltar.setText("Voltar");
        MenuItemVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItemVoltarActionPerformed(evt);
            }
        });
        MenuCliente.add(MenuItemVoltar);

        MenuItemSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        MenuItemSair.setText("Sair");
        MenuItemSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuItemSairActionPerformed(evt);
            }
        });
        MenuCliente.add(MenuItemSair);

        jMenuBar1.add(MenuCliente);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtPesquisarNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesquisarNomeActionPerformed

    }//GEN-LAST:event_txtPesquisarNomeActionPerformed

    private void btnExecutarPesquisaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExecutarPesquisaActionPerformed
        String cpfPesquisa = txtPesquisarCpf.getText().trim(); // Campo de pesquisa de CPF
        String nomePesquisa = txtPesquisarNome.getText().trim(); // Campo de pesquisa de Nome
        List<Cliente> resultados;

        // VERIFICA SE AMBOS OS CAMPOS ESTÃO VAZIOS
        if (cpfPesquisa.isEmpty() && nomePesquisa.isEmpty()) {
            // Se vazios, carrega todos os clientes
            System.out.println("DEBUG: Campos de pesquisa de cliente vazios. Listando todos...");
            resultados = clienteDAO.listarTodos();
            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum cliente cadastrado.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Mensagem opcional, pois a tabela será preenchida
                // JOptionPane.showMessageDialog(this, "Exibindo todos os " + resultados.size() + " cliente(s).", "Listagem Completa", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            // Se pelo menos um campo tem algo, faz a busca filtrada
            System.out.println("DEBUG: Pesquisando Cliente por CPF: [" + cpfPesquisa + "], Nome: [" + nomePesquisa + "]"); // Log
            resultados = clienteDAO.buscarPorCpfOuNome(cpfPesquisa, nomePesquisa);

            if (resultados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com os critérios informados.", "Pesquisa Sem Resultados", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, resultados.size() + " cliente(s) encontrado(s).", "Pesquisa Concluída", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // Atualiza a tabela com os resultados (sejam todos ou filtrados)
        atualizarTabelaClientes(resultados);

    }//GEN-LAST:event_btnExecutarPesquisaActionPerformed

    private void btnEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditarActionPerformed
        int linhaSelecionada = tblClientes.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para editar.", "Nenhuma Seleção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obter o ID do cliente da linha selecionada (Coluna 0)
        Object idObj = tableModelClientes.getValueAt(linhaSelecionada, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Erro ao obter ID do cliente selecionado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int idParaEditar = (Integer) idObj;

            // Buscar o cliente completo no banco usando o DAO
            Cliente clienteParaEditar = clienteDAO.buscarPorId(idParaEditar);

            if (clienteParaEditar != null) {
                // Preencher os campos da tela
                txtCodigo.setText(String.valueOf(clienteParaEditar.getId())); // Exibe o ID
                ftxtCpf.setText(clienteParaEditar.getCpf());
                txtNome.setText(clienteParaEditar.getNome());
                txtEndereco.setText(clienteParaEditar.getEndereco());
                txtEmail.setText(clienteParaEditar.getEmail());
                ftxtTelefone.setText(clienteParaEditar.getTelefone());

                // Armazena o ID do cliente que está sendo editado
                this.idClienteEditando = idParaEditar;
                txtCodigo.setEnabled(false); // Desabilita edição do código
                ftxtCpf.requestFocus(); // Foco no CPF
            } else {
                JOptionPane.showMessageDialog(this, "Cliente não encontrado no banco de dados (ID: " + idParaEditar + ").", "Erro", JOptionPane.ERROR_MESSAGE);
                // Limpar seleção e recarregar tabela 
                tblClientes.clearSelection();
                carregarTabelaClientes();
            }
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Erro interno ao converter ID do cliente.", "Erro de Tipo", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEditarActionPerformed

    private void btnExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcluirActionPerformed
        int linhaSelecionada = tblClientes.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela para excluir.", "Nenhuma Seleção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = tableModelClientes.getValueAt(linhaSelecionada, 0);
        Object nomeObj = tableModelClientes.getValueAt(linhaSelecionada, 1); // Pega nome da coluna correta
        if (idObj == null || nomeObj == null) {
            JOptionPane.showMessageDialog(this, "Erro ao obter dados do cliente selecionado.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int idParaExcluir = (Integer) idObj;
            String nomeParaExcluir = (String) nomeObj;

            int resposta = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja excluir o cliente:\nID: " + idParaExcluir + "\nNome: " + nomeParaExcluir,
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (resposta == JOptionPane.YES_OPTION) {
                // --- Bloco Try-Catch para a exclusão ---
                try {
                    // Tenta excluir usando o DAO
                    boolean sucesso = clienteDAO.excluir(idParaExcluir);

                    if (sucesso) {
                        JOptionPane.showMessageDialog(this, "Cliente excluído com sucesso!", "Exclusão Concluída", JOptionPane.INFORMATION_MESSAGE);
                        carregarTabelaClientes(); // Recarrega a tabela apenas se excluiu
                    } else {
                        // Isso não deveria acontecer se o DAO lançar exceção em caso de erro,
                        // mas mantemos uma mensagem genérica por segurança.
                        JOptionPane.showMessageDialog(this, "A exclusão falhou por um motivo inesperado (nenhuma linha afetada).", "Falha na Exclusão", JOptionPane.WARNING_MESSAGE);
                    }
                    // Captura a exceção específica de cliente com vendas
                } catch (ClienteComVendasException exCVE) {
                    System.err.println("Capturado ClienteComVendasException: " + exCVE.getMessage()); // Log interno
                    // Mostra mensagem específica e amigável para o usuário
                    JOptionPane.showMessageDialog(this,
                            "Não é possível excluir este cliente, pois ele possui vendas registradas no sistema.",
                            "Exclusão Não Permitida",
                            JOptionPane.WARNING_MESSAGE); // Aviso para o usuário
                    // Captura outros erros SQL que podem ocorrer
                } catch (SQLException exSQL) {
                    System.err.println("Capturado SQLException ao excluir: " + exSQL.getMessage()); // Log interno
                    exSQL.printStackTrace(); // Imprime detalhes técnicos no console
                    // Mostra mensagem genérica de erro de banco de dados
                    JOptionPane.showMessageDialog(this,
                            "Ocorreu um erro no banco de dados ao tentar excluir o cliente.\nDetalhe: " + exSQL.getMessage(),
                            "Erro no Banco de Dados",
                            JOptionPane.ERROR_MESSAGE);
                    // Captura qualquer outro erro inesperado
                } catch (Exception ex) {
                    System.err.println("Capturado Exception inesperada ao excluir: " + ex.getMessage()); // Log interno
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Ocorreu um erro inesperado ao tentar excluir o cliente.",
                            "Erro Inesperado",
                            JOptionPane.ERROR_MESSAGE);
                }

                // Limpar campos do formulário após tentativa de exclusão (mesmo se falhar)
                limparCamposClientes();
            }
        } catch (ClassCastException e) {
            JOptionPane.showMessageDialog(this, "Erro interno ao obter dados do cliente.", "Erro de Tipo", JOptionPane.ERROR_MESSAGE);
        } finally {
            // Garante que o estado de edição seja resetado após a tentativa
            // e que os botões sejam atualizados se a seleção for perdida.
            if (tblClientes.getSelectedRowCount() != 1) {
                habilitarBotoesEdicaoExclusao(false);
                // Se a exclusão foi bem sucedida e limpou a seleção,
                // ou se houve erro e a seleção foi perdida,
                // garante que os botões fiquem desabilitados.
                // Se a exclusão falhou mas a seleção continua, os botões devem permanecer como estão.
            }
        }

    }//GEN-LAST:event_btnExcluirActionPerformed

    private void MenuItemSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItemSairActionPerformed
        //  Pede confirmação ao usuário
        int resposta = JOptionPane.showConfirmDialog(this,
                "Deseja realmente sair da aplicação?",
                "Confirmar Saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        //  Verifica se o usuário clicou em "Sim"
        if (resposta == JOptionPane.YES_OPTION) {
            //  Encerra toda a aplicação Java
            System.exit(0);
        }
        // Se clicar em "Não", a caixa de diálogo fecha e nada mais acontece.
    }//GEN-LAST:event_MenuItemSairActionPerformed

    private void MenuItemNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItemNovoActionPerformed
        limparCamposClientes();
        tblClientes.clearSelection(); // Limpa seleção da tabela
        habilitarBotoesEdicaoExclusao(false); // Garante botões desabilitados
    }//GEN-LAST:event_MenuItemNovoActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        // --- Bloco 1: Obter dados da tela ---
        // (código para ler ftxtCpf, txtNome, ftxtTelefone, txtEndereco, txtEmail)
        String cpf = (ftxtCpf != null) ? ftxtCpf.getText() : ftxtCpf.getText();
        String nome = txtNome.getText().trim();
        String telefone = (ftxtTelefone != null) ? ftxtTelefone.getText() : ftxtTelefone.getText();
        String endereco = txtEndereco.getText().trim();
        String email = txtEmail.getText().trim();

        // --- Bloco 2: Validações ---
        // (Validação de email, CPF/Nome obrigatórios, formato CPF, etc., como implementado antes)
        if (!email.isEmpty() && !isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "O formato do E-mail informado é inválido.", "E-mail Inválido", JOptionPane.WARNING_MESSAGE);
            txtEmail.requestFocus();
            return;
        }
        String cpfSemMascara = (String) ftxtCpf.getValue(); // Ou use getText().replaceAll(...) se necessário
        if (cpfSemMascara == null || cpfSemMascara.trim().replace("_", "").length() == 0 || nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Os campos CPF e Nome são obrigatórios.", "Erro de Validação", JOptionPane.WARNING_MESSAGE);
            if (nome.isEmpty()) {
                txtNome.requestFocus();
            } else {
                ftxtCpf.requestFocus();
            }
            return;
        }
        String cpfNumeros = cpf.replaceAll("[^0-9]", "");
        if (cpfNumeros.length() != 11) {
            JOptionPane.showMessageDialog(this, "O CPF informado não contém 11 dígitos.", "CPF Inválido", JOptionPane.WARNING_MESSAGE);
            ftxtCpf.requestFocus();
            return;
        }
        // --- Fim Validações ---

        // --- Bloco 3: Criar Objeto Cliente ---
        Cliente cliente = new Cliente();
        cliente.setCpf(cpf); // Salva com máscara 
        cliente.setNome(nome);
        cliente.setEndereco(endereco);
        cliente.setEmail(email);
        cliente.setTelefone(telefone); // Salva com máscara

        // --- Bloco 4: Tentar Salvar (Inserir ou Atualizar) ---
        boolean sucesso = false;
        String operacao = ""; // Para saber qual operação foi feita

        if (idClienteEditando == -1) { // Modo INSERÇÃO
            operacao = "inserido";
            sucesso = clienteDAO.inserir(cliente); // O ID será definido no objeto 'cliente' se sucesso
        } else { // Modo ATUALIZAÇÃO
            operacao = "atualizado";
            cliente.setId(idClienteEditando); // Define o ID para o WHERE na atualização
            sucesso = clienteDAO.atualizar(cliente);
        }

        // --- Bloco 5: Feedback e Ações Pós-Sucesso ---
        if (sucesso) {
            String mensagemSucesso = "Cliente '" + cliente.getNome() + "' (ID: " + cliente.getId() + ") " + operacao + " com sucesso!";
            JOptionPane.showMessageDialog(this, mensagemSucesso, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            limparCamposClientes();
            carregarTabelaClientes();
        } else {
            // Se o DAO retornou false, significa que houve um erro (já logado no console pelo DAO)
            // Mostra uma mensagem genérica para o usuário.
            if (operacao.equals("inserido")) {
                JOptionPane.showMessageDialog(this, "Erro ao inserir cliente. Verifique os dados (CPF pode já existir) ou o console.", "Falha na Inserção", JOptionPane.ERROR_MESSAGE);
            } else { // atualizado
                JOptionPane.showMessageDialog(this, "Erro ao atualizar cliente. Verifique os dados ou o console.", "Falha na Atualização", JOptionPane.ERROR_MESSAGE);
            }
        }

    }//GEN-LAST:event_btnSalvarActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        // ... limpar outros campos ...
        txtCodigo.setText("");
        txtNome.setText("");
        txtEndereco.setText("");
        txtEmail.setText("");

        // Limpa os campos JFormattedTextField 
        if (ftxtCpf != null) {
            ftxtCpf.setValue(null); // Define o valor como null para limpar
        }
        if (ftxtTelefone != null) {
            ftxtTelefone.setValue(null); // Define o valor como null para limpar
        }
        // Limpa campos da área de pesquisa
        txtPesquisarCpf.setText("");
        txtPesquisarNome.setText("");

        // Reseta o controle de edição
        this.idClienteEditando = -1;
        txtCodigo.setEnabled(false);

        // Limpa a tabela
        tableModelClientes.setRowCount(0);

        // Limpa a seleção e desabilita botões
        tblClientes.clearSelection();
        habilitarBotoesEdicaoExclusao(false);

        // Foco no primeiro campo útil 
        if (ftxtCpf != null)
            ftxtCpf.requestFocusInWindow(); // Foca no campo correto
    }//GEN-LAST:event_btnLimparActionPerformed

    private void MenuItemVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuItemVoltarActionPerformed
        // Cria uma NOVA instância da Tela Principal
        TelaPrincipal telaPrincipal = new TelaPrincipal();

        //  Torna a Tela Principal visível
        telaPrincipal.setVisible(true);

        //FECHA a janela ATUAL (TelaGerenciarClientes)
        this.dispose();
    }//GEN-LAST:event_MenuItemVoltarActionPerformed

    private void pnlNovoClienteFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pnlNovoClienteFocusGained

    }//GEN-LAST:event_pnlNovoClienteFocusGained

    private void pnlNovoClienteFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pnlNovoClienteFocusLost

    }//GEN-LAST:event_pnlNovoClienteFocusLost

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaGerenciarClientes().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu MenuCliente;
    private javax.swing.JMenuItem MenuItemNovo;
    private javax.swing.JMenuItem MenuItemSair;
    private javax.swing.JMenuItem MenuItemVoltar;
    private javax.swing.JButton btnEditar;
    private javax.swing.JButton btnExcluir;
    private javax.swing.JButton btnExecutarPesquisa;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JFormattedTextField ftxtCpf;
    private javax.swing.JFormattedTextField ftxtTelefone;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblCodigo;
    private javax.swing.JLabel lblCpf;
    private javax.swing.JLabel lblEmail;
    private javax.swing.JLabel lblEndereco;
    private javax.swing.JLabel lblNome;
    private javax.swing.JLabel lblPesquisarCpf;
    private javax.swing.JLabel lblPesquisarNome;
    private javax.swing.JLabel lblTelefone;
    private javax.swing.JPanel pnlCampoPesquisa;
    private javax.swing.JPanel pnlClientes;
    private javax.swing.JPanel pnlListaClientes;
    private javax.swing.JPanel pnlNovoCliente;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtCodigo;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtEndereco;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtPesquisarCpf;
    private javax.swing.JTextField txtPesquisarNome;
    // End of variables declaration//GEN-END:variables
}
