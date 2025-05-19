package com.lojafacil.view;

// Imports necessários
import com.lojafacil.model.Cliente;
import com.lojafacil.model.ItemVenda;
import com.lojafacil.model.Venda;
import com.lojafacil.dao.VendaDAO;
import com.lojafacil.dao.ClienteDAO;

import java.awt.CardLayout;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale; // Para formatação de moeda.
import java.text.NumberFormat; // Para formatação de moeda

public class TelaRelatorios extends javax.swing.JFrame {

    private DefaultTableModel tableModelPeriodo; // Modelo para tblListarRelatorioPorPeriodo
    private DefaultTableModel tableModelCliente; // Adicionar modelo para a tabela de cliente
    private VendaDAO vendaDAO;     // DAO de Venda
    private ClienteDAO clienteDAO; // DAO de Cliente (para buscar por CPF/Nome)

    public TelaRelatorios() {
        initComponents();
        vendaDAO = new VendaDAO();       //  Inicializa
        clienteDAO = new ClienteDAO(); //  Inicializa
        configurarTabelaPeriodo();
        configurarTabelaCliente(); //  Configura a nova tabela
        lblTitulo.setText("Tela de Relatórios");
        // Mostra o card inicial
        CardLayout cl = (CardLayout) pnlCardContainer.getLayout();
        cl.show(pnlCardContainer, "Inicio"); // Usar o nome do painel inicial
    }

    private void configurarTabelaPeriodo() {
        String[] colunas = {"ID Venda", "Data", "CPF Cliente", "Nome Cliente", "Qtd. Itens", "Valor Total"};
        tableModelPeriodo = new DefaultTableModel(colunas, 0) {
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
                        return String.class; // Data já formatada no DAO/atualizarTabela
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Integer.class;
                    case 5:
                        return Double.class;  //  Importante: Mantém como Double
                    default:
                        return Object.class;
                }
            }
        };
        tblListarRelatorioPorPeriodo.setModel(tableModelPeriodo);

        // ***** FORMATAR A COLUNA DE VALOR *****
        try {
            // Índice da coluna "Valor Total" (começa em 0, então é a 6ª coluna -> índice 5)
            int valorTotalColIndex = 5;
            TableColumnModel columnModel = tblListarRelatorioPorPeriodo.getColumnModel();
            TableColumn valorColumn = columnModel.getColumn(valorTotalColIndex);
            // Aplica o renderizador personalizado
            valorColumn.setCellRenderer(new SplitCurrencyRenderer());
        } catch (Exception e) {
            System.err.println("Erro ao aplicar SplitCurrencyRenderer na tabela por período: " + e.getMessage());
            // Continuar mesmo se houver erro na formatação
        }

    }

    private void configurarTabelaCliente() {
        String[] colunas = {"Data da Compra", "Nome Cliente", "Qtd. Itens", "Valor Total"};
        tableModelCliente = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // <<< AJUSTE DOS ÍNDICES E TIPOS >>>
                switch (columnIndex) {
                    case 0:
                        return String.class;  // Data (formatada)
                    case 1:
                        return String.class;  // Nome Cliente
                    case 2:
                        return Integer.class; // Qtd. Itens
                    case 3:
                        return Double.class;  // Valor Total
                    default:
                        return Object.class;
                }
            }
        };
        tblListarRelatorioPorCliente.setModel(tableModelCliente);

        // Aplica o renderizador de moeda 
        try {
            int valorTotalColIndexCliente = 3;
            TableColumnModel columnModelR = tblListarRelatorioPorCliente.getColumnModel();
            TableColumn valorColumnCliente = columnModelR.getColumn(valorTotalColIndexCliente);
            valorColumnCliente.setCellRenderer(new SplitCurrencyRenderer());
        } catch (Exception e) {
            System.err.println("Erro ao aplicar SplitCurrencyRenderer na tabela por cliente: " + e.getMessage());
        }

        // ***** AJUSTE DAS LARGURAS DAS COLUNAS  *****
        TableColumnModel columnModel = tblListarRelatorioPorCliente.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150); // Data da Compra
        columnModel.getColumn(1).setPreferredWidth(300); // Nome Cliente (mais espaço)
        columnModel.getColumn(2).setPreferredWidth(100); // Qtd. Itens
        columnModel.getColumn(3).setPreferredWidth(130); // Valor Total
    }

    // --- Relatório por Período ---
    /**
     * Atualiza a tabela de vendas por período
     */
    private void atualizarTabelaPeriodo(List<Venda> vendasFiltradas) {
        tableModelPeriodo.setRowCount(0); // Limpa
        SimpleDateFormat sdfTabela = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (vendasFiltradas != null) {
            for (Venda v : vendasFiltradas) {

                Venda vendaCompleta = vendaDAO.buscarVendaCompletaPorId(v.getId());
                int qtdItensVenda = 0;
                double valorTotalVenda = 0;
                if (vendaCompleta != null && vendaCompleta.getItensVenda() != null) {
                    for (ItemVenda item : vendaCompleta.getItensVenda()) {
                        qtdItensVenda += item.getQuantidade();
                        valorTotalVenda += item.calcularSubtotal();
                    }
                }

                Object[] rowData = {
                    v.getId(),
                    sdfTabela.format(v.getDataVenda()),
                    v.getCliente() != null ? v.getCliente().getCpf() : "N/A", // Verifica se cliente não é nulo
                    v.getCliente() != null ? v.getCliente().getNome() : "N/A",
                    qtdItensVenda,
                    valorTotalVenda
                };
                tableModelPeriodo.addRow(rowData);
            }
        }
        // Limpa os totais se a lista for nula ou vazia
        if (vendasFiltradas == null || vendasFiltradas.isEmpty()) {
            calcularEAtualizarTotaisPeriodo(new ArrayList<>()); // Passa lista vazia para zerar totais
        }
    }

    /**
     * Calcula e exibe os totais para o relatório por período
     */
    private void calcularEAtualizarTotaisPeriodo(List<Venda> vendasFiltradas) {
        int totalVendas = 0;
        int totalItens = 0;
        double valorTotalPeriodo = 0.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (vendasFiltradas != null) {
            totalVendas = vendasFiltradas.size();
            for (Venda v : vendasFiltradas) {
                // Busca detalhes para somar corretamente
                Venda vendaCompleta = vendaDAO.buscarVendaCompletaPorId(v.getId());
                if (vendaCompleta != null && vendaCompleta.getItensVenda() != null) {
                    for (ItemVenda item : vendaCompleta.getItensVenda()) {
                        totalItens += item.getQuantidade();
                        valorTotalPeriodo += item.calcularSubtotal();
                    }
                }
            }
        }

        txtQuantTotalVendas.setText(String.valueOf(totalVendas));
        txtQuantTotalItens.setText(String.valueOf(totalItens));
        txtValorTotalVendas.setText(currencyFormat.format(valorTotalPeriodo)); // Formata para R$
    }

    // --- Relatório por Cliente ---
    /**
     * Atualiza a tabela de compras por cliente
     */
    private void atualizarTabelaCliente(List<Venda> vendasFiltradas) {
        tableModelCliente.setRowCount(0); // Limpa
        SimpleDateFormat sdfTabela = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        System.out.println("DEBUG (atualizarTabelaCliente): Recebeu " + (vendasFiltradas != null ? vendasFiltradas.size() : "null") + " vendas filtradas.");

        if (vendasFiltradas != null) {
            for (Venda v : vendasFiltradas) {
                System.out.println("DEBUG (atualizarTabelaCliente): Processando Venda ID: " + v.getId());

                Venda vendaCompleta = vendaDAO.buscarVendaCompletaPorId(v.getId());
                int qtdItensVenda = 0;
                double valorTotalVenda = 0;
                String nomeCliente = "N/A";

                if (vendaCompleta != null && vendaCompleta.getCliente() != null) {
                    nomeCliente = vendaCompleta.getCliente().getNome();
                    if (vendaCompleta.getItensVenda() != null) {
                        System.out.println("DEBUG (atualizarTabelaCliente): Venda completa encontrada. Itens: " + vendaCompleta.getItensVenda().size());
                        for (ItemVenda item : vendaCompleta.getItensVenda()) {
                            qtdItensVenda += item.getQuantidade();
                            valorTotalVenda += item.calcularSubtotal();
                        }
                    } else {
                        System.out.println("DEBUG (atualizarTabelaCliente): Venda completa encontrada, MAS lista getItensVenda() ESTÁ VAZIA para Venda ID: " + v.getId());
                    }
                } else if (vendaCompleta == null) {
                    System.out.println("DEBUG (atualizarTabelaCliente): buscarVendaCompletaPorId RETORNOU NULL para Venda ID: " + v.getId());
                    if (v.getCliente() != null) {
                        nomeCliente = v.getCliente().getNome();
                    }
                } else {
                    System.out.println("DEBUG (atualizarTabelaCliente): Venda completa encontrada, MAS getCliente() É NULL para Venda ID: " + v.getId());
                }

                // Definição das colunas:
                // {"Data da Compra", "Nome Cliente", "Qtd. Itens", "Valor Total"}
                // Índices:        0               1             2             3
                Object[] rowData = {
                    sdfTabela.format(v.getDataVenda()), // Coluna 0: Data
                    nomeCliente, // Coluna 1: Nome Cliente
                    qtdItensVenda, // Coluna 2: Qtd. Itens
                    valorTotalVenda // Coluna 3: Valor Total (Double)
                };

                System.out.println("DEBUG (atualizarTabelaCliente): Adicionando linha: Data=" + sdfTabela.format(v.getDataVenda()) + ", Cliente=" + nomeCliente + ", Itens=" + qtdItensVenda + ", Total=" + valorTotalVenda);
                tableModelCliente.addRow(rowData);
            }
        }
        if (vendasFiltradas == null || vendasFiltradas.isEmpty()) {
            calcularEExibirTotaisCliente(new ArrayList<>());
        }
    }

    /**
     * Calcula e exibe os totais para o relatório por cliente
     */
    private void calcularEExibirTotaisCliente(List<Venda> vendasFiltradas) {
        int quantidadeTotalCompras = 0;
        int quantidadeTotalItens = 0;
        double valorTotalCompras = 0.0;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        if (vendasFiltradas != null) {
            quantidadeTotalCompras = vendasFiltradas.size();
            for (Venda v : vendasFiltradas) {
                // Busca detalhes para somar
                Venda vendaCompleta = vendaDAO.buscarVendaCompletaPorId(v.getId());
                if (vendaCompleta != null && vendaCompleta.getItensVenda() != null) {
                    for (ItemVenda item : vendaCompleta.getItensVenda()) {
                        quantidadeTotalItens += item.getQuantidade();
                        valorTotalCompras += item.calcularSubtotal();
                    }
                }
            }
        }

        txtQuantDeCompras.setText(String.valueOf(quantidadeTotalCompras));
        txtQuantItensPorCliente.setText(String.valueOf(quantidadeTotalItens));
        txtValorTotalCompras.setText(currencyFormat.format(valorTotalCompras));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlRelatorio = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        pnlCardContainer = new javax.swing.JPanel();
        pnlCardComprasCliente = new javax.swing.JPanel();
        pnlCampoBuscaPorCliente = new javax.swing.JPanel();
        txtBuscaCliCpf = new javax.swing.JTextField();
        txtBuscaCliNome = new javax.swing.JTextField();
        btnGerarRelatorioCliente = new javax.swing.JButton();
        lblCpfBusca = new javax.swing.JLabel();
        lblNomeBusca = new javax.swing.JLabel();
        pnlListagemPorCliente = new javax.swing.JPanel();
        lblQuantCompras = new javax.swing.JLabel();
        txtQuantDeCompras = new javax.swing.JTextField();
        lblQuantItensPorCliente = new javax.swing.JLabel();
        txtQuantItensPorCliente = new javax.swing.JTextField();
        lblValorTotalVendasPorCliente = new javax.swing.JLabel();
        txtValorTotalCompras = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblListarRelatorioPorCliente = new javax.swing.JTable();
        btnLimparCliente = new javax.swing.JButton();
        pnlCardVendasPeriodo = new javax.swing.JPanel();
        pnlCampoBusca = new javax.swing.JPanel();
        btnGerarRelatorio = new javax.swing.JButton();
        jdcDataFinal = new com.toedter.calendar.JDateChooser();
        jdcDataInicial = new com.toedter.calendar.JDateChooser();
        lblDataInicial = new javax.swing.JLabel();
        lblDataFinal = new javax.swing.JLabel();
        pnlListagem = new javax.swing.JPanel();
        lblQuantTotalVendas = new javax.swing.JLabel();
        txtQuantTotalVendas = new javax.swing.JTextField();
        lblQuantTotalItens = new javax.swing.JLabel();
        txtQuantTotalItens = new javax.swing.JTextField();
        lblValorTotalVendas = new javax.swing.JLabel();
        txtValorTotalVendas = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblListarRelatorioPorPeriodo = new javax.swing.JTable();
        btnLimpar = new javax.swing.JButton();
        pnlCardInicial = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuRelatorios = new javax.swing.JMenu();
        menuItemRelatorioPeriodo = new javax.swing.JMenuItem();
        menuItemRelatorioCliente = new javax.swing.JMenuItem();
        menuItemVoltar = new javax.swing.JMenuItem();
        menuItemSair = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(1100, 750));

        lblTitulo.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        lblTitulo.setText("Tela de Relatórios");

        pnlCardContainer.setName(""); // NOI18N
        pnlCardContainer.setLayout(new java.awt.CardLayout());

        pnlCardComprasCliente.setName("ComprasClienteCard"); // NOI18N

        pnlCampoBuscaPorCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Campo de Busca", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N
        pnlCampoBuscaPorCliente.setPreferredSize(new java.awt.Dimension(980, 92));

        btnGerarRelatorioCliente.setBackground(new java.awt.Color(0, 153, 51));
        btnGerarRelatorioCliente.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnGerarRelatorioCliente.setForeground(new java.awt.Color(255, 255, 255));
        btnGerarRelatorioCliente.setText("Gerar Relatório");
        btnGerarRelatorioCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnGerarRelatorioCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerarRelatorioClienteActionPerformed(evt);
            }
        });

        lblCpfBusca.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblCpfBusca.setText("CPF: ");

        lblNomeBusca.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblNomeBusca.setText("Nome: ");

        javax.swing.GroupLayout pnlCampoBuscaPorClienteLayout = new javax.swing.GroupLayout(pnlCampoBuscaPorCliente);
        pnlCampoBuscaPorCliente.setLayout(pnlCampoBuscaPorClienteLayout);
        pnlCampoBuscaPorClienteLayout.setHorizontalGroup(
            pnlCampoBuscaPorClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoBuscaPorClienteLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCpfBusca)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtBuscaCliCpf, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(lblNomeBusca)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtBuscaCliNome, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(48, 48, 48)
                .addComponent(btnGerarRelatorioCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77))
        );
        pnlCampoBuscaPorClienteLayout.setVerticalGroup(
            pnlCampoBuscaPorClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoBuscaPorClienteLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlCampoBuscaPorClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCpfBusca)
                    .addComponent(txtBuscaCliCpf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblNomeBusca)
                    .addComponent(txtBuscaCliNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnGerarRelatorioCliente))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pnlListagemPorCliente.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Listagem", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N
        pnlListagemPorCliente.setPreferredSize(new java.awt.Dimension(1041, 446));

        lblQuantCompras.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblQuantCompras.setText("Quantidade  de Compras:");

        txtQuantDeCompras.setEditable(false);
        txtQuantDeCompras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantDeComprasActionPerformed(evt);
            }
        });

        lblQuantItensPorCliente.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblQuantItensPorCliente.setText("Quantidade Total de Itens:");

        txtQuantItensPorCliente.setEditable(false);

        lblValorTotalVendasPorCliente.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblValorTotalVendasPorCliente.setText("Valor Total  de Compras:");

        txtValorTotalCompras.setEditable(false);

        tblListarRelatorioPorCliente.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Data da Compra", "Nome do Cliente", "Quant. de Itens", "Total da Venda"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblListarRelatorioPorCliente);

        btnLimparCliente.setBackground(new java.awt.Color(246, 232, 44));
        btnLimparCliente.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnLimparCliente.setText("Limpar");
        btnLimparCliente.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnLimparCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparClienteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlListagemPorClienteLayout = new javax.swing.GroupLayout(pnlListagemPorCliente);
        pnlListagemPorCliente.setLayout(pnlListagemPorClienteLayout);
        pnlListagemPorClienteLayout.setHorizontalGroup(
            pnlListagemPorClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlListagemPorClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2)
                .addContainerGap())
            .addGroup(pnlListagemPorClienteLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(lblQuantCompras)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtQuantDeCompras, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblQuantItensPorCliente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtQuantItensPorCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(lblValorTotalVendasPorCliente)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtValorTotalCompras, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(67, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlListagemPorClienteLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLimparCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(95, 95, 95))
        );
        pnlListagemPorClienteLayout.setVerticalGroup(
            pnlListagemPorClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlListagemPorClienteLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(pnlListagemPorClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuantCompras)
                    .addComponent(lblQuantItensPorCliente)
                    .addComponent(txtQuantItensPorCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValorTotalVendasPorCliente)
                    .addComponent(txtValorTotalCompras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQuantDeCompras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnLimparCliente)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlCardComprasClienteLayout = new javax.swing.GroupLayout(pnlCardComprasCliente);
        pnlCardComprasCliente.setLayout(pnlCardComprasClienteLayout);
        pnlCardComprasClienteLayout.setHorizontalGroup(
            pnlCardComprasClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardComprasClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlCardComprasClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlListagemPorCliente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCampoBuscaPorCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 1041, Short.MAX_VALUE))
                .addContainerGap(1866, Short.MAX_VALUE))
        );
        pnlCardComprasClienteLayout.setVerticalGroup(
            pnlCardComprasClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardComprasClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlCampoBuscaPorCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlListagemPorCliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCardContainer.add(pnlCardComprasCliente, "CompraClienteCard");

        pnlCardVendasPeriodo.setName("VendasPeriodoCard"); // NOI18N

        pnlCampoBusca.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Campo de Busca", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        btnGerarRelatorio.setBackground(new java.awt.Color(0, 153, 51));
        btnGerarRelatorio.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnGerarRelatorio.setForeground(new java.awt.Color(255, 255, 255));
        btnGerarRelatorio.setText("Gerar Relatório");
        btnGerarRelatorio.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnGerarRelatorio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerarRelatorioActionPerformed(evt);
            }
        });

        lblDataInicial.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDataInicial.setText("Data Inicial:");

        lblDataFinal.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblDataFinal.setText("Data Final:");

        javax.swing.GroupLayout pnlCampoBuscaLayout = new javax.swing.GroupLayout(pnlCampoBusca);
        pnlCampoBusca.setLayout(pnlCampoBuscaLayout);
        pnlCampoBuscaLayout.setHorizontalGroup(
            pnlCampoBuscaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoBuscaLayout.createSequentialGroup()
                .addGap(92, 92, 92)
                .addComponent(lblDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jdcDataInicial, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(74, 74, 74)
                .addComponent(lblDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jdcDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnGerarRelatorio, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
        );
        pnlCampoBuscaLayout.setVerticalGroup(
            pnlCampoBuscaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCampoBuscaLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(pnlCampoBuscaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnGerarRelatorio)
                    .addGroup(pnlCampoBuscaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(lblDataInicial, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jdcDataInicial, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlCampoBuscaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(lblDataFinal)
                        .addComponent(jdcDataFinal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        pnlListagem.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Listagem", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 0, 10))); // NOI18N

        lblQuantTotalVendas.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblQuantTotalVendas.setText("Quantidade Total de Vendas:");

        txtQuantTotalVendas.setEditable(false);
        txtQuantTotalVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantTotalVendasActionPerformed(evt);
            }
        });

        lblQuantTotalItens.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblQuantTotalItens.setText("Quantidade Total de Itens:");

        txtQuantTotalItens.setEditable(false);

        lblValorTotalVendas.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
        lblValorTotalVendas.setText("Valor Total  das Vendas:");

        txtValorTotalVendas.setEditable(false);

        tblListarRelatorioPorPeriodo.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Código", "CPF", "Nome do Cliente", "Quant. de Itens", "Total da Venda"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblListarRelatorioPorPeriodo);

        btnLimpar.setBackground(new java.awt.Color(246, 232, 44));
        btnLimpar.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N
        btnLimpar.setText("Limpar");
        btnLimpar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlListagemLayout = new javax.swing.GroupLayout(pnlListagem);
        pnlListagem.setLayout(pnlListagemLayout);
        pnlListagemLayout.setHorizontalGroup(
            pnlListagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlListagemLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlListagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlListagemLayout.createSequentialGroup()
                        .addComponent(lblQuantTotalVendas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtQuantTotalVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(lblQuantTotalItens)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtQuantTotalItens, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(34, 34, 34)
                        .addComponent(lblValorTotalVendas)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtValorTotalVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 31, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlListagemLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(66, 66, 66))
        );
        pnlListagemLayout.setVerticalGroup(
            pnlListagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlListagemLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(pnlListagemLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuantTotalVendas)
                    .addComponent(lblQuantTotalItens)
                    .addComponent(txtQuantTotalItens, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblValorTotalVendas)
                    .addComponent(txtValorTotalVendas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQuantTotalVendas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(btnLimpar)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout pnlCardVendasPeriodoLayout = new javax.swing.GroupLayout(pnlCardVendasPeriodo);
        pnlCardVendasPeriodo.setLayout(pnlCardVendasPeriodoLayout);
        pnlCardVendasPeriodoLayout.setHorizontalGroup(
            pnlCardVendasPeriodoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardVendasPeriodoLayout.createSequentialGroup()
                .addGroup(pnlCardVendasPeriodoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlListagem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCampoBusca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(1872, Short.MAX_VALUE))
        );
        pnlCardVendasPeriodoLayout.setVerticalGroup(
            pnlCardVendasPeriodoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardVendasPeriodoLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(pnlCampoBusca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlListagem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlCardContainer.add(pnlCardVendasPeriodo, "VendasPeriodoCard");

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Selecione um tipo de relatório no menu acima.");

        javax.swing.GroupLayout pnlCardInicialLayout = new javax.swing.GroupLayout(pnlCardInicial);
        pnlCardInicial.setLayout(pnlCardInicialLayout);
        pnlCardInicialLayout.setHorizontalGroup(
            pnlCardInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardInicialLayout.createSequentialGroup()
                .addGap(383, 383, 383)
                .addComponent(jLabel1)
                .addContainerGap(2235, Short.MAX_VALUE))
        );
        pnlCardInicialLayout.setVerticalGroup(
            pnlCardInicialLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCardInicialLayout.createSequentialGroup()
                .addGap(234, 234, 234)
                .addComponent(jLabel1)
                .addContainerGap(941, Short.MAX_VALUE))
        );

        pnlCardContainer.add(pnlCardInicial, "Inicio");

        javax.swing.GroupLayout pnlRelatorioLayout = new javax.swing.GroupLayout(pnlRelatorio);
        pnlRelatorio.setLayout(pnlRelatorioLayout);
        pnlRelatorioLayout.setHorizontalGroup(
            pnlRelatorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRelatorioLayout.createSequentialGroup()
                .addGroup(pnlRelatorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlRelatorioLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlCardContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 2913, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlRelatorioLayout.createSequentialGroup()
                        .addGap(483, 483, 483)
                        .addComponent(lblTitulo)))
                .addContainerGap())
        );
        pnlRelatorioLayout.setVerticalGroup(
            pnlRelatorioLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRelatorioLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(lblTitulo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(pnlCardContainer, javax.swing.GroupLayout.PREFERRED_SIZE, 1195, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        menuRelatorios.setText("Relatórios");

        menuItemRelatorioPeriodo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemRelatorioPeriodo.setText("Relatório de Vendas por Período");
        menuItemRelatorioPeriodo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRelatorioPeriodoActionPerformed(evt);
            }
        });
        menuRelatorios.add(menuItemRelatorioPeriodo);

        menuItemRelatorioCliente.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemRelatorioCliente.setText("Relátorio de Compras por Cliente");
        menuItemRelatorioCliente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRelatorioClienteActionPerformed(evt);
            }
        });
        menuRelatorios.add(menuItemRelatorioCliente);

        menuItemVoltar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemVoltar.setText("Voltar");
        menuItemVoltar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemVoltarActionPerformed(evt);
            }
        });
        menuRelatorios.add(menuItemVoltar);

        menuItemSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menuItemSair.setText("Sair");
        menuItemSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSairActionPerformed(evt);
            }
        });
        menuRelatorios.add(menuItemSair);

        jMenuBar1.add(menuRelatorios);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlRelatorio, javax.swing.GroupLayout.PREFERRED_SIZE, 1400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlRelatorio, javax.swing.GroupLayout.PREFERRED_SIZE, 739, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void menuItemSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSairActionPerformed
        int resposta = JOptionPane.showConfirmDialog(this, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }//GEN-LAST:event_menuItemSairActionPerformed

    private void btnGerarRelatorioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerarRelatorioActionPerformed
        // 1. Obter Datas dos JDateChoosers
        Date dataInicial = null;
        Date dataFinal = null;

        try {
            // Obtém o objeto Date diretamente do componente usando getDate()
            dataInicial = jdcDataInicial.getDate(); // Usa getDate()
            dataFinal = jdcDataFinal.getDate();     // Usa getDate()

            // 2. Validar Datas (agora verifica se são nulas)
            if (dataInicial == null || dataFinal == null) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione a Data Inicial e a Data Final.", "Datas Requeridas", JOptionPane.WARNING_MESSAGE);
                return; // Interrompe se alguma data não foi selecionada
            }

            // Verifica se data inicial não é posterior à final
            if (dataInicial.after(dataFinal)) {
                JOptionPane.showMessageDialog(this, "A Data Inicial não pode ser posterior à Data Final.", "Datas Inválidas", JOptionPane.WARNING_MESSAGE);
                return;
            }

        } catch (Exception e) {
            // Captura genérica para qualquer erro inesperado ao obter as datas
            JOptionPane.showMessageDialog(this, "Erro ao obter as datas selecionadas.", "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        // 3. Chamar o DAO 
        List<Venda> vendasFiltradas = vendaDAO.buscarPorPeriodo(dataInicial, dataFinal);

        // 4. Atualizar a tabela e os totais 
        atualizarTabelaPeriodo(vendasFiltradas);
        calcularEAtualizarTotaisPeriodo(vendasFiltradas);

        // 5. Mensagem de Conclusão 
        if (vendasFiltradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma venda encontrada no período informado.", "Relatório Vazio", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Relatório gerado com sucesso!", "Relatório Concluído", JOptionPane.INFORMATION_MESSAGE);
        }

    }//GEN-LAST:event_btnGerarRelatorioActionPerformed

    private void txtQuantTotalVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantTotalVendasActionPerformed

    }//GEN-LAST:event_txtQuantTotalVendasActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        // Limpa os JDateChoosers definindo a data como null
        jdcDataInicial.setDate(null);
        jdcDataFinal.setDate(null);

        atualizarTabelaPeriodo(new ArrayList<>()); // Limpa tabela
        calcularEAtualizarTotaisPeriodo(new ArrayList<>()); // Zera totais
        jdcDataInicial.requestFocusInWindow(); // Foco no primeiro date chooser

    }//GEN-LAST:event_btnLimparActionPerformed

    private void menuItemRelatorioPeriodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRelatorioPeriodoActionPerformed
        CardLayout cl = (CardLayout) pnlCardContainer.getLayout();
        cl.show(pnlCardContainer, "VendasPeriodoCard"); // Usa o Nome do Card definido no Design
        lblTitulo.setText("Relatório de Vendas por Período");
        btnLimparActionPerformed(null); // Limpa campos e tabela ao trocar


    }//GEN-LAST:event_menuItemRelatorioPeriodoActionPerformed

    private void menuItemRelatorioClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRelatorioClienteActionPerformed
        CardLayout cl = (CardLayout) pnlCardContainer.getLayout();
        cl.show(pnlCardContainer, "CompraClienteCard"); // Usa o Nome do Card definido no Design
        lblTitulo.setText("Relatório de Compras por Cliente");
        btnLimparClienteActionPerformed(null); // Limpa campos e tabela ao trocar

    }//GEN-LAST:event_menuItemRelatorioClienteActionPerformed

    private void menuItemVoltarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemVoltarActionPerformed
        // Voltar para TelaPrincipal e fechar esta.
        TelaPrincipal telaPrincipal = new TelaPrincipal();
        telaPrincipal.setVisible(true);
        this.dispose();

    }//GEN-LAST:event_menuItemVoltarActionPerformed

    private void btnGerarRelatorioClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerarRelatorioClienteActionPerformed
        String cpf = txtBuscaCliCpf.getText().trim();
        String nome = txtBuscaCliNome.getText().trim();
        System.out.println("DEBUG: Iniciando Relatório por Cliente. CPF: [" + cpf + "], Nome: [" + nome + "]");

        if (cpf.isEmpty() && nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o CPF ou o Nome do cliente.", "Critério Necessário", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("DEBUG: Buscando cliente no DAO...");
        List<Cliente> clientesEncontrados = clienteDAO.buscarPorCpfOuNome(cpf, nome);
        Cliente clienteSelecionadoParaRelatorio = null; // Guarda o cliente final escolhido

        if (clientesEncontrados == null || clientesEncontrados.isEmpty()) {
            System.out.println("DEBUG: Nenhum cliente encontrado pelo DAO.");
            JOptionPane.showMessageDialog(this, "Nenhum cliente encontrado com os dados informados.", "Cliente Não Encontrado", JOptionPane.INFORMATION_MESSAGE);
            atualizarTabelaCliente(new ArrayList<>()); // Limpa tabela
            calcularEExibirTotaisCliente(new ArrayList<>()); // Zera totais
            return; // Interrompe aqui
        } else if (clientesEncontrados.size() == 1) {
            // Se encontrou exatamente um, usa ele diretamente
            clienteSelecionadoParaRelatorio = clientesEncontrados.get(0);
            System.out.println("DEBUG: Cliente único encontrado: ID=" + clienteSelecionadoParaRelatorio.getId() + ", Nome=" + clienteSelecionadoParaRelatorio.getNome());
        } else {
            // ***** MAIS DE UM CLIENTE ENCONTRADO -> CHAMA O DIÁLOGO *****
            System.out.println("DEBUG: Múltiplos clientes encontrados (" + clientesEncontrados.size() + "). Abrindo diálogo de seleção...");
            // Cria e exibe o diálogo modal, passando a lista de clientes encontrados
            SelecaoClienteDialog dialogCli = new SelecaoClienteDialog(this, true, clientesEncontrados);
            dialogCli.setVisible(true); // Pausa a execução aqui

            // Pega o cliente que foi selecionado no diálogo
            clienteSelecionadoParaRelatorio = dialogCli.getClienteSelecionado(); // Pode ser null se cancelou

            if (clienteSelecionadoParaRelatorio == null) {
                System.out.println("DEBUG: Diálogo de seleção de cliente cancelado.");
                atualizarTabelaCliente(new ArrayList<>()); // Limpa tabela
                calcularEExibirTotaisCliente(new ArrayList<>()); // Zera totais
                // Opcional: Mostrar mensagem "Seleção cancelada"
                JOptionPane.showMessageDialog(this, "Seleção de cliente cancelada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                return; // Interrompe aqui se cancelou
            } else {
                System.out.println("DEBUG: Cliente selecionado no diálogo: ID=" + clienteSelecionadoParaRelatorio.getId() + ", Nome=" + clienteSelecionadoParaRelatorio.getNome());
            }
            // **********************************************************
        }

        // --- Se chegou aqui, temos um clienteSelecionadoParaRelatorio definido ---
        int idClienteParaBuscar = clienteSelecionadoParaRelatorio.getId();

        // Busca as vendas para o ID do cliente selecionado
        System.out.println("DEBUG: Buscando vendas para o cliente ID: " + idClienteParaBuscar);
        List<Venda> vendasFiltradas = vendaDAO.buscarPorCliente(idClienteParaBuscar);
        System.out.println("DEBUG: Vendas encontradas para o cliente: " + (vendasFiltradas != null ? vendasFiltradas.size() : "null"));

        // Atualiza a tabela e os totais
        System.out.println("DEBUG: Atualizando tabela e totais...");
        atualizarTabelaCliente(vendasFiltradas);
        calcularEExibirTotaisCliente(vendasFiltradas);
        System.out.println("DEBUG: Tabela e totais atualizados.");

        // Mensagem de feedback final
        String nomeClienteInfo = clienteSelecionadoParaRelatorio.getNome(); // Pega o nome para a mensagem
        if (vendasFiltradas == null || vendasFiltradas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma compra encontrada para o cliente: " + nomeClienteInfo, "Relatório Vazio", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Relatório de compras para o cliente '" + nomeClienteInfo + "' gerado com sucesso!", "Relatório Concluído", JOptionPane.INFORMATION_MESSAGE);
        }


    }//GEN-LAST:event_btnGerarRelatorioClienteActionPerformed

    private void txtQuantDeComprasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantDeComprasActionPerformed

    }//GEN-LAST:event_txtQuantDeComprasActionPerformed

    private void btnLimparClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparClienteActionPerformed
        txtBuscaCliCpf.setText("");
        txtBuscaCliNome.setText("");
        atualizarTabelaCliente(new ArrayList<>()); // Limpa tabela
        calcularEExibirTotaisCliente(new ArrayList<>()); // Zera totais
        txtBuscaCliCpf.requestFocus();
    }//GEN-LAST:event_btnLimparClienteActionPerformed

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaRelatorios().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGerarRelatorio;
    private javax.swing.JButton btnGerarRelatorioCliente;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnLimparCliente;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private com.toedter.calendar.JDateChooser jdcDataFinal;
    private com.toedter.calendar.JDateChooser jdcDataInicial;
    private javax.swing.JLabel lblCpfBusca;
    private javax.swing.JLabel lblDataFinal;
    private javax.swing.JLabel lblDataInicial;
    private javax.swing.JLabel lblNomeBusca;
    private javax.swing.JLabel lblQuantCompras;
    private javax.swing.JLabel lblQuantItensPorCliente;
    private javax.swing.JLabel lblQuantTotalItens;
    private javax.swing.JLabel lblQuantTotalVendas;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblValorTotalVendas;
    private javax.swing.JLabel lblValorTotalVendasPorCliente;
    private javax.swing.JMenuItem menuItemRelatorioCliente;
    private javax.swing.JMenuItem menuItemRelatorioPeriodo;
    private javax.swing.JMenuItem menuItemSair;
    private javax.swing.JMenuItem menuItemVoltar;
    private javax.swing.JMenu menuRelatorios;
    private javax.swing.JPanel pnlCampoBusca;
    private javax.swing.JPanel pnlCampoBuscaPorCliente;
    private javax.swing.JPanel pnlCardComprasCliente;
    private javax.swing.JPanel pnlCardContainer;
    private javax.swing.JPanel pnlCardInicial;
    private javax.swing.JPanel pnlCardVendasPeriodo;
    private javax.swing.JPanel pnlListagem;
    private javax.swing.JPanel pnlListagemPorCliente;
    private javax.swing.JPanel pnlRelatorio;
    private javax.swing.JTable tblListarRelatorioPorCliente;
    private javax.swing.JTable tblListarRelatorioPorPeriodo;
    private javax.swing.JTextField txtBuscaCliCpf;
    private javax.swing.JTextField txtBuscaCliNome;
    private javax.swing.JTextField txtQuantDeCompras;
    private javax.swing.JTextField txtQuantItensPorCliente;
    private javax.swing.JTextField txtQuantTotalItens;
    private javax.swing.JTextField txtQuantTotalVendas;
    private javax.swing.JTextField txtValorTotalCompras;
    private javax.swing.JTextField txtValorTotalVendas;
    // End of variables declaration//GEN-END:variables
}
