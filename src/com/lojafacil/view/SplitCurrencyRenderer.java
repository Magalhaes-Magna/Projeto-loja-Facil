package com.lojafacil.view; 

//Imports necessarios
import javax.swing.*;
import javax.swing.table.TableCellRenderer; // Implementa diretamente a interface
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Color; // Para cores de seleção
import java.text.NumberFormat;
import java.awt.Font;
import java.util.Locale;
import java.text.DecimalFormat; // Usaremos DecimalFormat para controlar melhor

/**
 * Renderizador de célula de tabela que exibe o símbolo da moeda (R$) à esquerda
 * e o valor numérico à direita, dentro da mesma célula.
 */
public class SplitCurrencyRenderer implements TableCellRenderer { // Implementa a interface

    private final JPanel panel;
    private final JLabel symbolLabel;
    private final JLabel numberLabel;
    private static final Locale BRASIL = new Locale("pt", "BR");
    // Usaremos DecimalFormat para formatar só o número, sem o símbolo R$
    private static final DecimalFormat numberFormatter;

    static {
        // Configura o formatador para números brasileiros (vírgula decimal, ponto milhar)
        numberFormatter = (DecimalFormat) NumberFormat.getNumberInstance(BRASIL);
        numberFormatter.setMinimumFractionDigits(2); // Sempre mostra 2 casas decimais
        numberFormatter.setMaximumFractionDigits(2); // Limita a 2 casas decimais
        // Opcional: Agrupamento de milhares (ex: 1.234,56)
        // numberFormatter.setGroupingUsed(true);
    }

    public SplitCurrencyRenderer() {
        panel = new JPanel(new BorderLayout(2, 0)); // BorderLayout com pequeno espaço horizontal (2px)
        symbolLabel = new JLabel("R$ "); // Símbolo com espaço
        numberLabel = new JLabel();

        // Alinhamentos
        symbolLabel.setHorizontalAlignment(JLabel.LEFT);
        numberLabel.setHorizontalAlignment(JLabel.RIGHT);

        // Adiciona os labels ao painel
        panel.add(symbolLabel, BorderLayout.WEST);  // Símbolo na esquerda
        panel.add(numberLabel, BorderLayout.CENTER); // Número ocupa o restante (BorderLayout estica o centro)
                                                     // ou BorderLayout.EAST se preferir que não estique

        panel.setOpaque(true); // Importante para cores de fundo funcionarem
        numberLabel.setOpaque(true); // Labels também opacos
        symbolLabel.setOpaque(true);
    }

     @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        // Chama o método da superclasse? Não, pois implementamos a interface diretamente.
        // Nós configuramos nosso painel diretamente.

        // Define as cores baseado na seleção/foco
        Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
        Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();

        panel.setBackground(bg);
        symbolLabel.setBackground(bg); // Fundo dos labels igual ao do painel/tabela
        numberLabel.setBackground(bg);

        symbolLabel.setForeground(fg);
        numberLabel.setForeground(fg);

        // ***** Define a Fonte dos Labels para ser igual à da Tabela *****
        Font tableFont = table.getFont();
        symbolLabel.setFont(tableFont);
        numberLabel.setFont(tableFont);
        // ***************************************************************

        // Define a borda baseado no foco
        if (hasFocus) {
            panel.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
        } else {
            // Usa uma borda vazia para manter o espaçamento consistente
            panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        }


        // Formata e define o texto
        if (value instanceof Number) {
            symbolLabel.setText("R$ ");
            numberLabel.setText(numberFormatter.format(((Number) value).doubleValue()));
        } else {
            symbolLabel.setText("");
            numberLabel.setText("");
        }

        return panel; // Retorna o PAINEL
    }
}