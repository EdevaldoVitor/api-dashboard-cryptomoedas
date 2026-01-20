# 📊 API Dashboard de Criptomoedas

API REST desenvolvida em **Java + Spring Boot** para coleta, gerenciamento e análise de criptomoedas, com autenticação via **JWT** e integração com **OpenAI** para geração de resumos inteligentes sobre o mercado cripto.

---

## 🚀 Funcionalidades

- 🔍 **Consulta de criptomoedas**
  - Integração com a API pública do **CoinGecko**
  - Busca de informações atualizadas de mercado

- ⭐ **Gerenciamento de moedas favoritas**
  - Associação de moedas por usuário autenticado
  - Listagem de favoritos

- 🤖 **Resumo inteligente com IA**
  - Integração com **OpenAI (Spring AI)**
  - Geração de resumos contextuais sobre criptomoedas
  - Prompt especializado em mercado cripto

- 🔐 **Autenticação e Segurança**
  - Autenticação baseada em **JWT**
  - Controle de acesso por usuário
  - Rotas protegidas com Spring Security

- 📈 **Controle e auditoria de uso da IA**
  - Registro de chamadas à OpenAI
  - Armazenamento de custo estimado e tokens utilizados
  - Prevenção de uso indevido da API

---

## 🧠 Exemplo de Prompt de IA

> Você é um especialista em criptomoedas e mercado financeiro.  
> Gere um resumo claro, objetivo e informativo sobre as seguintes moedas,  
> considerando preço, tendência e relevância no mercado.

---

## 🛠️ Tecnologias Utilizadas

- Java 21  
- Spring Boot 3.5.6  
- Spring Web  
- Spring Security  
- Spring Data JPA  
- JWT (Auth0)  
- Spring AI  
- OpenAI API  
- H2 Database  
- Maven  

---

## 🔑 Configuração da OpenAI (Segurança)

A chave da OpenAI **não é versionada** no repositório.

Ela deve ser configurada como **variável de ambiente**:

```bash
export SPRING_AI_OPENAI_API_KEY=sk-xxxx
