# LojaFacil - Sistema de Ponto de Venda

## Status do Projeto
⏳Em desenvolvimento

## Tecnologias Aplicadas
* **Linguagem:** Java
* **Interface Gráfica:** Java Swing 
* **Banco de Dados:** MySQL 
* **Conectividade com Banco de Dados:** JDBC

## Time de Desenvolvedores
* Magna Magalhães - Desenvolvedor Principal

## Objetivo do Software
O LojaFacil é um sistema de gerenciamento de vendas projetado para facilitar as operações de um pequeno estabelecimento comercial. Ele visa permitir o cadastro e gerenciamento de clientes e produtos, o registro de vendas de forma eficiente e o controle básico de estoque. [cite: 1, 16, 27, 48, 62, 164]

## Funcionalidades do Sistema (Requisitos)
* **Gerenciamento de Clientes:**
    * Cadastro de novos clientes 
    * Atualização dos dados de clientes existentes. 
    * Exclusão de clientes (com verificação para não excluir clientes com vendas associadas). 
    * Busca de clientes. 
    * Listagem de todos os clientes cadastrados.
* **Gerenciamento de Produtos:**
    * Cadastro de novos produtos.
    * Atualização dos dados de produtos existentes. 
    * Exclusão de produtos (com atenção para produtos associados a vendas). 
    * Busca de produtos por código (ID) ou nome. 
    * Listagem de todos os produtos cadastrados. 
* **Controle de Estoque:**
    * Baixa automática de estoque ao registrar uma venda. 
    * Adição manual de quantidade ao estoque de um produto. 
    * Verificação de estoque mínimo. 
    * Impedir venda de produto sem estoque.
* **Registro de Vendas:**
    * Criação de novas vendas, associando um cliente e a data da venda.
    * Adição de múltiplos produtos (itens de venda) a uma venda, especificando a quantidade.
    * Cálculo do subtotal por item de venda. 
    * Cálculo do valor total da venda.
* **Interface Gráfica:**
    * Janelas de diálogo para seleção de clientes durante a venda. 
    * Janelas de diálogo para seleção de produtos durante a venda, exibindo informações relevantes como preço e estoque. 
* **Persistência de Dados:**
    * Utilização de banco de dados MySQL para armazenar todas as informações do sistema. 

---

