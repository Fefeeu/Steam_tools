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

---

## Escalonamento: Round Robin

Os pacotes `org.scheduling.rr` e `org.tools` escalonam as tarefas da aplicação usando o algoritmo **Round Robin**. Cada tarefa entra na **Fila de Prontos (Ready Queue)** e recebe a CPU por uma fatia de tempo fixa, o **quantum**. Quando o quantum acaba, a tarefa devolve a CPU e volta para o fim da fila, e a próxima tarefa da fila é despachada (algoritmo **preemptivo**). Isso se repete até todas as tarefas terminarem.

Cada tarefa roda na sua própria thread, mas só a tarefa que está com a CPU pode trabalhar. As outras ficam bloqueadas esperando a sua vez. A troca de turno é feita com o monitor do Java (`synchronized`, `wait` e `notifyAll`).

Para poderem ser interrompidas, as tarefas foram divididas em **passos**. O escalonador confere o quantum entre um passo e outro, então a preempção é **cooperativa**: um passo que já começou sempre termina, e por isso uma fatia pode passar um pouco do quantum.

As quatro tarefas chegam na fila nesta ordem e são executadas **5 vezes** cada uma, como no Sequencial e no Paralelo:

| Ordem | Tarefa | O que é 1 passo | Passos (5 repetições) |
|:-----:|--------|-----------------|:---------------------:|
| 1 | Wishlist | 1 requisição à API: a busca da lista ou a busca de 1 jogo da lista | 145 (5 × (1 + 28 jogos)) |
| 2 | Biblioteca | Buscar a biblioteca de 1 usuário | 10 (5 × 2 usuários) |
| 3 | Jogadores | Consultar os jogadores online de 1 jogo | 20 (5 × 4 jogos) |
| 4 | DLC | Processar 1 jogo do JSON local | 25 (5 × 5 jogos) |

Se a busca de um jogo da Wishlist falhar, a tarefa **cede a CPU** antes do fim do quantum e volta para o fim da fila. Ela tenta o mesmo jogo de novo na próxima fatia, até 3 vezes.

| Classe | Responsabilidade |
|--------|------------------|
| `TarefaRR` | Interface de uma tarefa dividida em passos: `executarPasso()` devolve `true` enquanto ainda falta trabalho, e `querCederCpu()` indica se a tarefa quer devolver a CPU antes do fim do quantum. |
| `EscalonadorRR` | Mantém a Ready Queue, cria uma thread por tarefa, controla de quem é a vez e gera o relatório com quanta, passos, tempo de espera, turnaround e trocas de contexto. |
| `TarefaWishlistRR`, `TarefaBibliotecaRR`, `TarefaJogadoresRR`, `TarefaDLCRR` | As tarefas da Steam divididas em passos. |
| `RoundRobin` | Classe principal: coloca as tarefas da Steam na fila e executa o escalonador. O quantum é passado como argumento (padrão 1000 ms). |

### Como executar

```bash
mvn compile exec:java -Dexec.mainClass="org.tools.RoundRobin" -Dexec.args="1000"
```

O argumento é o quantum em milissegundos. Durante a execução, o programa imprime uma linha para cada fatia (quanto tempo a tarefa rodou, quantos passos executou e se voltou para a fila ou terminou). Ao final, imprime para cada tarefa a quantidade de quanta e de passos, o tempo de espera e o turnaround, além do tempo médio de espera, do turnaround médio, das trocas de contexto e do tempo total.

### Relatório de execução

O escalonador foi executado **5 vezes** com **quantum = 1000 ms** e a Biblioteca usando a API real (`USE_MOCK = false`). Em todas as execuções a ordem de conclusão foi a mesma: **DLC → Biblioteca → Jogadores → Wishlist**. Todos os tempos estão em milissegundos.

Nas execuções 4 e 5, a Steam Web API passou a recusar parte das requisições da Wishlist por excesso de requisições (*rate limit*). Por isso os resultados foram separados em dois cenários.

| Execução | Tempo médio de espera | Turnaround médio | Trocas de contexto | Tempo total | Rate limit |
|:--------:|----------------------:|-----------------:|-------------------:|------------:|:----------:|
| 1 | 19545,30 | 40496,80 | 29 | 83854 | Não |
| 2 | 20756,00 | 41361,50 | 29 | 82518 | Não |
| 3 | 23158,80 | 43942,30 | 30 | 83297 | Não |
| 4 | 19328,30 | 49944,30 | 29 | 122516 | Sim |
| 5 | 12790,30 | 35598,80 | 18 | 91290 | Sim |

### Cenário 1: sem rate limit (execuções 1 a 3)

#### Médias por tarefa

| Tarefa | Quanta | Passos | Fatia média | Tempo de espera | Turnaround | % do tempo de CPU |
|--------|-------:|-------:|------------:|----------------:|-----------:|------------------:|
| Wishlist | 48,7 | 145 | 1182,9 | 25669,7 | 83198,7 | 69,2% |
| Biblioteca | 6,0 | 10 | 1560,6 | 18155,3 | 27519,0 | 11,3% |
| Jogadores | 10,0 | 20 | 1284,4 | 25790,0 | 38634,3 | 15,5% |
| DLC | 3,3 | 25 | 1008,6 | 14998,3 | 18382,0 | 4,1% |

#### Médias gerais

| Métrica | Valor |
|---------|------:|
| Tempo médio de espera | 21153,37 |
| Turnaround médio | 41933,53 |
| Trocas de contexto | 29,3 |
| Tempo total | 83223,0 |

#### Linha do tempo (gráfico de Gantt, início da execução 1)

```
|  Wishlist  |  Biblioteca  |  Jogadores  |  DLC  |  Wishlist  |  Biblioteca  |  Jogadores  |  DLC  | ...
0            1845           3055          4391    5442         6646           8322          9700    10750 ms
```

> As tarefas se revezam na CPU a cada quantum. A DLC termina em cerca de 18 s, a Biblioteca em cerca de 28 s e os Jogadores em cerca de 39 s. Depois disso, a Wishlist fica sozinha na fila e roda até o fim, em cerca de 83 s.

### Cenário 2: com rate limit (execuções 4 e 5)

| Execução | Falhas na Wishlist (cedeu a CPU) | Jogos desistidos após 3 tentativas | Quanta da Wishlist | Passos da Wishlist | Turnaround da Wishlist |
|:--------:|---------------------------------:|-----------------------------------:|-------------------:|-------------------:|-----------------------:|
| 4 | 97 | 48 | 126 | 242 | 122507 |
| 5 | 4 | 2 | 59 | 149 | 91276 |

Cada falha conta como um passo e encerra a fatia da Wishlist antes do fim do quantum, então a Wishlist usa mais quanta e mais passos. As outras tarefas não dependem dessas requisições e mantiveram resultados próximos aos do cenário 1: na execução 4, a DLC, a Biblioteca e os Jogadores terminaram em 15477, 25525 e 36268 ms.

### Conclusões

- **Divisão justa da CPU:** nenhuma tarefa fica com a CPU por mais de uma fatia seguida enquanto há outras na fila. As tarefas curtas não ficam presas atrás da Wishlist: a DLC, que precisa de cerca de 3,4 s de CPU, termina em cerca de 18 s, mesmo com a Wishlist precisando de cerca de 57,5 s.
- **Ordem de conclusão pela duração:** apesar de a Wishlist chegar primeiro, as tarefas terminaram na ordem DLC, Biblioteca, Jogadores e Wishlist em todas as execuções, ou seja, da que precisa de menos CPU para a que precisa de mais.
- **O tempo total não diminui:** como só uma tarefa usa a CPU por vez, o tempo total (cerca de 83 s) é a soma do tempo de todas as tarefas. O ganho do Round Robin está no tempo de resposta das tarefas curtas, não no tempo total.
- **Preempção cooperativa:** a fatia média ficou entre 1008,6 e 1560,6 ms com quantum de 1000 ms. A fatia passa do quantum porque o escalonador só confere o tempo entre um passo e outro, e um passo que começou sempre termina.
- **Ceder a CPU:** quando a API recusou as requisições da Wishlist (execuções 4 e 5), ela devolveu a CPU antes do fim do quantum, e as outras tarefas continuaram sendo executadas normalmente.
