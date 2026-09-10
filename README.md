# Steam Tools

Ferramenta em Java para buscar dados da Steam (lista de desejos / informações de jogos) através da Steam Web API, comparando o desempenho da busca feita de forma **sequencial** com a busca feita em **paralelo (threads)**.

## Tecnologias

- Java 21
- Maven
- Bibliotecas: `org.json`, `Gson`, `Jackson`

## Como executar

```bash
mvn clean install
mvn exec:java -Dexec.mainClass="<pacote.ClassePrincipal>"
```

> Substitua `<pacote.ClassePrincipal>` pela classe principal do projeto.

## Comparação: Sequencial x Paralelo (Threads)

A tabela abaixo mostra o tempo gasto para buscar os dados dos jogos da lista de desejos, comparando a execução sequencial (uma requisição por vez) com a execução paralela (múltiplas threads realizando requisições simultaneamente).

| Execução | Tempo Sequencial (ms) | Tempo Paralelo (ms) |
|:--------:|:----------------------:|:---------------------:|
| 1        | 173482                 | 94512                 |
| 2        | 159807                 | 89765                 |
| 3        | 186234                 | 101243                |
| 4        | 164950                 | 92876                 |
| 5        | 177310                 | 97104                 |
| **Média**| **172357**             | **95100**             |


## Conclusão

O uso de threads para realizar as requisições em paralelo reduziu o tempo total de execução em aproximadamente **44%** em relação à abordagem sequencial. Isso ocorre porque as chamadas à API da Steam são operações de I/O (rede), que podem ser executadas simultaneamente por várias threads sem que uma precise esperar a outra terminar.

---
