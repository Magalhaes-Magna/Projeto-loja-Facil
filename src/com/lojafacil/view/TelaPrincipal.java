package com.lojafacil.view;

import javax.swing.JOptionPane;

public class TelaPrincipal extends javax.swing.JFrame {

  
    public TelaPrincipal() {
        initComponents();
    }

 
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMenuPrincipal = new javax.swing.JPanel();
        btnRegistarVendas = new javax.swing.JButton();
        btnGerenciarProdutos = new javax.swing.JButton();
        btnGerenciarClientes = new javax.swing.JButton();
        btnRelatorios = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        lblLogo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(976, 764));

        pnlMenuPrincipal.setBackground(new java.awt.Color(228, 240, 245));
        pnlMenuPrincipal.setPreferredSize(new java.awt.Dimension(976, 764));

        btnRegistarVendas.setBackground(new java.awt.Color(0, 51, 102));
        btnRegistarVendas.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRegistarVendas.setForeground(new java.awt.Color(255, 255, 255));
        btnRegistarVendas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/registrar_vendas.png"))); // NOI18N
        btnRegistarVendas.setText("   Registrar Vendas        ");
        btnRegistarVendas.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnRegistarVendas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistarVendasActionPerformed(evt);
            }
        });

        btnGerenciarProdutos.setBackground(new java.awt.Color(0, 51, 102));
        btnGerenciarProdutos.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnGerenciarProdutos.setForeground(new java.awt.Color(255, 255, 255));
        btnGerenciarProdutos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/gerenciarProdutos.png"))); // NOI18N
        btnGerenciarProdutos.setText("    Gerenciar Produtos  ");
        btnGerenciarProdutos.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnGerenciarProdutos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerenciarProdutosActionPerformed(evt);
            }
        });

        btnGerenciarClientes.setBackground(new java.awt.Color(0, 51, 102));
        btnGerenciarClientes.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnGerenciarClientes.setForeground(new java.awt.Color(255, 255, 255));
        btnGerenciarClientes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/registrar_vendas.png"))); // NOI18N
        btnGerenciarClientes.setText("    Gerenciar Clientes     ");
        btnGerenciarClientes.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnGerenciarClientes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerenciarClientesActionPerformed(evt);
            }
        });

        btnRelatorios.setBackground(new java.awt.Color(0, 51, 102));
        btnRelatorios.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnRelatorios.setForeground(new java.awt.Color(255, 255, 255));
        btnRelatorios.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/relatório.png"))); // NOI18N
        btnRelatorios.setText("   Relatórios                  ");
        btnRelatorios.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnRelatorios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRelatoriosActionPerformed(evt);
            }
        });

        btnSair.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnSair.setForeground(new java.awt.Color(0, 25, 51));
        btnSair.setText("Sair");
        btnSair.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/dimensões_1.png"))); // NOI18N

        javax.swing.GroupLayout pnlMenuPrincipalLayout = new javax.swing.GroupLayout(pnlMenuPrincipal);
        pnlMenuPrincipal.setLayout(pnlMenuPrincipalLayout);
        pnlMenuPrincipalLayout.setHorizontalGroup(
            pnlMenuPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMenuPrincipalLayout.createSequentialGroup()
                .addGap(32, 398, Short.MAX_VALUE)
                .addGroup(pnlMenuPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMenuPrincipalLayout.createSequentialGroup()
                        .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMenuPrincipalLayout.createSequentialGroup()
                        .addGroup(pnlMenuPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlMenuPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnGerenciarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnRegistarVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnGerenciarProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(296, 296, 296))))
            .addGroup(pnlMenuPrincipalLayout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        pnlMenuPrincipalLayout.setVerticalGroup(
            pnlMenuPrincipalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMenuPrincipalLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRegistarVendas, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(btnGerenciarProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(btnGerenciarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnRelatorios, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36)
                .addComponent(btnSair, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pnlMenuPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 1017, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMenuPrincipal, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnGerenciarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerenciarClientesActionPerformed
         TelaGerenciarClientes telaCli = new TelaGerenciarClientes();
    telaCli.setVisible(true);
    }//GEN-LAST:event_btnGerenciarClientesActionPerformed

    private void btnGerenciarProdutosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerenciarProdutosActionPerformed
         // Cria uma instância da tela de Gerenciar Produtos e a torna visível
    TelaGerenciarProdutos telaProd = new TelaGerenciarProdutos();
    telaProd.setVisible(true);
    // Opcional: Você pode querer esconder ou fechar a TelaPrincipal aqui
    // this.setVisible(false); // Esconde a principal
    // this.dispose(); // Fecha a principal (cuidado se precisar voltar)
    }//GEN-LAST:event_btnGerenciarProdutosActionPerformed

    private void btnRegistarVendasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistarVendasActionPerformed
        TelaRegistrarVenda telaVenda = new TelaRegistrarVenda();
    telaVenda.setVisible(true);
    }//GEN-LAST:event_btnRegistarVendasActionPerformed

    private void btnRelatoriosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRelatoriosActionPerformed
        TelaRelatorios telaRel = new TelaRelatorios();
    telaRel.setVisible(true);
    }//GEN-LAST:event_btnRelatoriosActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
         // Pode adicionar uma confirmação se quiser:
     int R = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja sair?", "Sair", JOptionPane.YES_NO_OPTION);
     if (R == JOptionPane.YES_OPTION) {
         System.exit(0);
     }
    System.exit(0); // Simplesmente fecha a aplicação

    }//GEN-LAST:event_btnSairActionPerformed

  
    public static void main(String args[]) {
     
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGerenciarClientes;
    private javax.swing.JButton btnGerenciarProdutos;
    private javax.swing.JButton btnRegistarVendas;
    private javax.swing.JButton btnRelatorios;
    private javax.swing.JButton btnSair;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JPanel pnlMenuPrincipal;
    // End of variables declaration//GEN-END:variables
}
