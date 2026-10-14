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

## Escalonamento: FCFS (First-Come First-Served)

O pacote `org.escalonamento` escalona as tarefas da aplicação usando o algoritmo **FCFS**. Cada tarefa é tratada como um processo que entra na **Fila de Prontos (Ready Queue)**: o processo que chega primeiro é o primeiro a usar a CPU e executa até terminar (algoritmo **não-preemptivo**), só então o próximo é despachado.

Os processos escalonados são as quatro tarefas do projeto, que chegam na fila nesta ordem:

| Ordem | Processo | O que faz |
|:-----:|----------|-----------|
| 1 | Wishlist | Atualiza a lista de desejos e notifica os jogos em promoção |
| 2 | Biblioteca | Compara as bibliotecas de jogos de dois usuários |
| 3 | Jogadores | Consulta quantos jogadores estão online em cada jogo monitorado |
| 4 | DLC | Carrega os jogos e DLCs a partir do JSON local |

| Classe | Responsabilidade |
|--------|------------------|
| `Processo` | Representa um processo: nome, tarefa executada e os tempos de chegada, início e fim. Calcula tempo de espera, turnaround e pico de CPU. |
| `EscalonadorFCFS` | Mantém a Ready Queue (fila FIFO), despacha os processos em ordem de chegada e gera o relatório com tempo médio de espera, turnaround médio e throughput. |
| `SimuladorFCFS` | Classe principal: coloca as tarefas da Steam na fila e executa o escalonador. |

### Como executar

```bash
mvn compile exec:java -Dexec.mainClass="org.escalonamento.SimuladorFCFS"
```

Ao final, o programa imprime um relatório com a ordem de execução e, para cada processo, os tempos de chegada, início, fim, pico de CPU, espera e turnaround, além do tempo médio de espera, do turnaround médio e do throughput.

A tarefa Biblioteca pode usar dados mock ou a API real, conforme a constante `USE_MOCK` em `LibraryAPI`. Com `USE_MOCK = true`, ela lê os arquivos de `resources/mock` e não é preciso ter o `config.properties`. Com `USE_MOCK = false`, ela consulta a Steam Web API e precisa da chave em `config.properties`.

### Relatório de execução

O escalonador foi executado **5 vezes em cada cenário**: com a Biblioteca usando mock e com a Biblioteca usando a API real. Em todas as execuções a ordem foi a mesma: **Wishlist → Biblioteca → Jogadores → DLC**. Todos os tempos estão em milissegundos.

Origem dos dados de cada processo:

| Processo | Com mock (`USE_MOCK = true`) | Com API real (`USE_MOCK = false`) |
|----------|------------------------------|-----------------------------------|
| Wishlist | Steam Web API | Steam Web API |
| Biblioteca | Arquivos mock locais | Steam Web API |
| Jogadores | Steam Web API | Steam Web API |
| DLC | JSON local (`biblioteca_jogos.json`) | JSON local (`biblioteca_jogos.json`) |

#### Comparação entre os cenários (médias)

| Métrica | Com mock | Com API real |
|---------|---------:|-------------:|
| Pico de CPU da Biblioteca | 10,8 | 396,0 |
| Tempo médio de espera | 8185,80 | 7981,40 |
| Turnaround médio | 10943,35 | 10702,10 |
| Tempo total | 11066,20 | 10917,80 |
| Throughput | 0,36 processos/s | 0,37 processos/s |
| TME se as tarefas curtas chegassem primeiro | ~54,6 | ~191,4 |

Com a API real, a Biblioteca ficou cerca de **37 vezes mais lenta** (de 10,8 para 396,0 ms), mas o resultado geral praticamente não mudou: a Wishlist continua dominando o tempo de CPU. A pequena queda no tempo médio de espera vem da variação do tempo de resposta da API na Wishlist (10,8 s com mock contra 10,3 s com API real), não da Biblioteca.

### Cenário 1: Biblioteca com mock (`USE_MOCK = true`)

#### Médias por processo

| Processo | Pico de CPU | Tempo de espera | Turnaround | % do tempo de CPU |
|----------|------------:|----------------:|-----------:|------------------:|
| Wishlist | 10832,0 | 27,4 | 10859,4 | 98,2% |
| Biblioteca | 10,8 | 10838,2 | 10849,0 | 0,1% |
| Jogadores | 182,8 | 10848,4 | 11031,2 | 1,7% |
| DLC | 4,6 | 11029,2 | 11033,8 | < 0,1% |

#### Resultado de cada execução

| Execução | Tempo médio de espera | Turnaround médio | Tempo total | Throughput |
|:--------:|----------------------:|-----------------:|------------:|-----------:|
| 1 | 8382,75 | 11202,00 | 11311 | 0,35 processos/s |
| 2 | 7902,25 | 10573,75 | 10718 | 0,37 processos/s |
| 3 | 8505,25 | 11364,75 | 11472 | 0,35 processos/s |
| 4 | 8124,00 | 10868,75 | 11017 | 0,36 processos/s |
| 5 | 8014,75 | 10707,50 | 10813 | 0,37 processos/s |
| **Média** | **8185,80** | **10943,35** | **11066,20** | **0,36 processos/s** |

#### Linha do tempo (gráfico de Gantt, médias)

```
|                    Wishlist                    | Biblioteca | Jogadores | DLC |
0                                                10832        10843       11026 11030 ms
```

> Fora de escala: em proporção real, Biblioteca, Jogadores e DLC juntos ocupam menos de 2% da linha do tempo.

#### E se as tarefas curtas chegassem primeiro?

Usando os picos de CPU médios medidos, se a ordem de chegada fosse **DLC → Biblioteca → Jogadores → Wishlist** (da tarefa mais curta para a mais longa), os tempos de espera seriam 0; 4,6; 15,4 e 198,2 ms. O tempo médio de espera cairia de **8185,8 ms** para cerca de **54,6 ms**, ou seja, **cerca de 150 vezes menor**, com as mesmas tarefas e a mesma CPU.

### Cenário 2: Biblioteca com API real (`USE_MOCK = false`)

#### Médias por processo

| Processo | Pico de CPU | Tempo de espera | Turnaround | % do tempo de CPU |
|----------|------------:|----------------:|-----------:|------------------:|
| Wishlist | 10303,8 | 28,0 | 10331,8 | 94,7% |
| Biblioteca | 396,0 | 10310,4 | 10706,4 | 3,6% |
| Jogadores | 179,4 | 10705,8 | 10885,2 | 1,7% |
| DLC | 3,6 | 10881,4 | 10885,0 | < 0,1% |

#### Resultado de cada execução

| Execução | Tempo médio de espera | Turnaround médio | Tempo total | Throughput |
|:--------:|----------------------:|-----------------:|------------:|-----------:|
| 1 | 7828,50 | 10483,50 | 10656 | 0,38 processos/s |
| 2 | 8181,25 | 11003,00 | 11322 | 0,35 processos/s |
| 3 | 8044,25 | 10792,50 | 11026 | 0,36 processos/s |
| 4 | 7837,25 | 10498,00 | 10679 | 0,37 processos/s |
| 5 | 8015,75 | 10733,50 | 10906 | 0,37 processos/s |
| **Média** | **7981,40** | **10702,10** | **10917,80** | **0,37 processos/s** |

#### Linha do tempo (gráfico de Gantt, médias)

```
|                    Wishlist                    | Biblioteca | Jogadores | DLC |
0                                                10304        10700       10879 10883 ms
```

> Fora de escala: em proporção real, Biblioteca, Jogadores e DLC juntos ocupam cerca de 5% da linha do tempo.

#### E se as tarefas curtas chegassem primeiro?

Com a API real, a Biblioteca passa a ser mais lenta que os Jogadores, então a ordem da tarefa mais curta para a mais longa seria **DLC → Jogadores → Biblioteca → Wishlist**. Os tempos de espera seriam 0; 3,6; 183,0 e 579,0 ms, e o tempo médio de espera cairia de **7981,4 ms** para cerca de **191,4 ms**, ou seja, **cerca de 42 vezes menor**.

### Conclusões

- **Os processos são executados na ordem de chegada:** nas 10 execuções (5 com mock e 5 com API real) a ordem foi sempre Wishlist, Biblioteca, Jogadores e DLC. Cada tarefa só começa depois que a anterior termina, independentemente de quanto tempo ela leva.
- **Efeito comboio:** como o FCFS é não-preemptivo, a Wishlist, que chega primeiro e ocupa de **94,7% a 98,2%** do tempo de CPU (cerca de 10,3 a 10,8 s, por fazer várias requisições à API), faz todas as outras esperarem. Mesmo com a Biblioteca usando a API real, as outras três tarefas somam menos de 600 ms, mas cada uma espera **mais de 10 s** na fila.
- **A ordem de chegada define o desempenho:** o tempo médio de espera ficou em torno de **8 s** nos dois cenários. Se as tarefas curtas fossem despachadas primeiro, ele cairia para cerca de **54,6 ms** (com mock) ou **191,4 ms** (com API real). O FCFS não olha a duração das tarefas, então o resultado depende só da ordem em que elas chegam.
- **Mock x API real:** trocar o mock pela API real deixou a Biblioteca cerca de 37 vezes mais lenta, mas quase não mudou as métricas gerais, porque o gargalo do FCFS nesse projeto é a tarefa longa no início da fila.
- **Baixo custo de despacho:** a primeira tarefa esperou em média menos de 30 ms para começar, e a troca entre uma tarefa e a próxima levou de 0 a 2 ms. Quase todo o tempo de espera vem das tarefas que estão na frente na fila, não do escalonador.
- **Simplicidade:** o FCFS é o algoritmo mais simples de implementar (basta uma fila FIFO) e nenhum processo sofre *starvation*, mas o tempo médio de espera raramente é o mínimo. O Round Robin, por alternar entre os processos a cada quantum, evita que uma tarefa longa como a Wishlist bloqueie as outras por tanto tempo.
