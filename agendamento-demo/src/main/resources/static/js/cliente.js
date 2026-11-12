(function () {
  const PASSOS = ["Serviço", "Barbeiro", "Horário", "Seus dados"];
  const NOMES_DIAS = { MONDAY: "seg", TUESDAY: "ter", WEDNESDAY: "qua", THURSDAY: "qui", FRIDAY: "sex", SATURDAY: "sáb", SUNDAY: "dom" };
  const ORDEM_DIAS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

  const estado = { servicos: [], barbeiros: [], servico: null, barbeiro: null, data: null, horario: null };

  const tela = document.getElementById("tela");
  const poste = document.getElementById("poste");
  const preenchido = document.getElementById("poste-preenchido");
  const rotulo = document.getElementById("passo-rotulo");

  document.getElementById("nome-barbearia").textContent = BARBEARIA.nome;
  document.title = "Agendar horário - " + BARBEARIA.nome;

  function setPasso(n) {
    const total = PASSOS.length;
    poste.setAttribute("aria-valuenow", Math.min(n, total));
    preenchido.style.width = Math.min(n / total, 1) * 100 + "%";
    rotulo.textContent = n <= total ? `Passo ${n} de ${total}: ${PASSOS[n - 1]}` : "Agendamento confirmado";
  }

  function montar(html) {
    tela.innerHTML = html;
    const titulo = tela.querySelector("h2");
    if (titulo) { titulo.tabIndex = -1; titulo.focus(); }
    window.scrollTo({ top: 0 });
  }

  const botaoVoltar = (texto = "Voltar") =>
    `<button type="button" class="botao-texto voltar" data-voltar>${texto}</button>`;

  function resumoDias(dias) {
    return ORDEM_DIAS.filter((d) => dias.includes(d)).map((d) => NOMES_DIAS[d]).join(", ");
  }

  /* ---------- passo 1: serviço ---------- */
  function telaServico() {
    setPasso(1);
    if (!estado.servicos.length) {
      montar(`<h2>Agendar horário</h2><p class="vazio">A barbearia ainda não cadastrou serviços.</p>`);
      return;
    }
    montar(`
      <h2>O que vamos fazer hoje?</h2>
      <div class="lista">
        ${estado.servicos.map((s) => `
          <button type="button" class="linha" data-id="${s.id}">
            <span class="linha-texto"><strong>${esc(s.nome)}</strong><small>${s.duracaoMinutos} min</small></span>
            <span class="linha-preco">${moeda(s.preco)}</span>
          </button>`).join("")}
      </div>`);
    tela.querySelectorAll(".linha").forEach((b) => b.onclick = () => {
      estado.servico = estado.servicos.find((s) => String(s.id) === b.dataset.id);
      estado.horario = null;
      telaBarbeiro();
    });
  }

  /* ---------- passo 2: barbeiro ---------- */
  function telaBarbeiro() {
    setPasso(2);
    if (!estado.barbeiros.length) {
      montar(`${botaoVoltar()}<h2>Com quem?</h2><p class="vazio">A barbearia ainda não cadastrou barbeiros.</p>`);
      tela.querySelector("[data-voltar]").onclick = telaServico;
      return;
    }
    montar(`
      ${botaoVoltar()}
      <h2>Com quem?</h2>
      <div class="lista">
        ${estado.barbeiros.map((b) => `
          <button type="button" class="linha" data-id="${b.id}">
            <span class="linha-texto"><strong>${esc(b.nome)}</strong>
              <small>Atende ${resumoDias(b.diasTrabalho)}, das ${b.inicioExpediente.slice(0, 5)} às ${b.fimExpediente.slice(0, 5)}</small>
            </span>
          </button>`).join("")}
      </div>`);
    tela.querySelector("[data-voltar]").onclick = telaServico;
    tela.querySelectorAll(".linha").forEach((b) => b.onclick = () => {
      estado.barbeiro = estado.barbeiros.find((x) => String(x.id) === b.dataset.id);
      estado.data = null;
      estado.horario = null;
      telaHorario();
    });
  }

  /* ---------- passo 3: dia e horário ---------- */
  function proximosDias(qtd) {
    const dias = [];
    const hoje = new Date();
    for (let i = 0; i < qtd; i++) {
      const d = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate() + i);
      dias.push({ d, iso: dataISO(d), atende: estado.barbeiro.diasTrabalho.includes(DIAS_SEMANA[d.getDay()]) });
    }
    return dias;
  }

  function telaHorario(aviso) {
    setPasso(3);
    const dias = proximosDias(21);
    const atual = dias.find((x) => x.iso === estado.data && x.atende);
    if (!atual) estado.data = (dias.find((x) => x.atende) || {}).iso || null;

    montar(`
      ${botaoVoltar()}
      <h2>Escolha o dia e o horário</h2>
      <p><small>${esc(estado.servico.nome)} com ${esc(estado.barbeiro.nome)}</small></p>
      ${aviso ? `<p class="aviso" role="alert">${esc(aviso)}</p>` : ""}
      <div class="dias" role="group" aria-label="Dias disponíveis">
        ${dias.map((x) => `
          <button type="button" class="dia" data-iso="${x.iso}" aria-pressed="${x.iso === estado.data}"
                  aria-label="${esc(dataLonga(x.iso))}" ${x.atende ? "" : "disabled"}>
            <span>${x.d.toLocaleDateString("pt-BR", { weekday: "short" }).replace(".", "")}</span>
            <strong>${x.d.getDate()}</strong>
          </button>`).join("")}
      </div>
      <p class="data-escolhida" id="data-escolhida"></p>
      <div id="horarios"></div>`);
    tela.querySelector("[data-voltar]").onclick = telaBarbeiro;
    tela.querySelectorAll(".dia").forEach((b) => b.onclick = () => {
      estado.data = b.dataset.iso;
      tela.querySelectorAll(".dia").forEach((o) => o.setAttribute("aria-pressed", o === b));
      carregarHorarios();
    });
    carregarHorarios();
  }

  async function carregarHorarios() {
    const area = document.getElementById("horarios");
    const rotuloData = document.getElementById("data-escolhida");
    if (!estado.data) {
      rotuloData.textContent = "";
      area.innerHTML = `<p class="vazio">${esc(estado.barbeiro.nome)} não tem dias de atendimento nas próximas semanas.</p>`;
      return;
    }
    const dataAlvo = estado.data;
    rotuloData.textContent = dataLonga(dataAlvo).replace(/^./, (c) => c.toUpperCase());
    area.innerHTML = `<p class="vazio">Buscando horários...</p>`;
    try {
      const livres = await api(`/barbeiros/${estado.barbeiro.id}/disponibilidade?servicoId=${estado.servico.id}&data=${dataAlvo}`);
      if (dataAlvo !== estado.data) return; // o cliente já trocou de dia
      if (!livres.length) {
        area.innerHTML = `<p class="vazio">Sem horários livres nesse dia. Escolha outro dia.</p>`;
        return;
      }
      area.innerHTML = `<div class="horarios">${livres.map((h) =>
        `<button type="button" class="horario" data-iso="${h}" aria-pressed="false">${hora(h)}</button>`).join("")}</div>`;
      area.querySelectorAll(".horario").forEach((b) => b.onclick = () => {
        estado.horario = b.dataset.iso;
        telaDados();
      });
    } catch (e) {
      area.innerHTML = `<p class="erro" role="alert">${esc(e.message)}</p>`;
    }
  }

  /* ---------- passo 4: dados do cliente ---------- */
  function lembrado(chave) {
    try { return localStorage.getItem(chave) || ""; } catch (_) { return ""; }
  }
  function lembrar(chave, valor) {
    try { localStorage.setItem(chave, valor); } catch (_) { /* navegação privada */ }
  }

  function telaDados() {
    setPasso(4);
    montar(`
      ${botaoVoltar("Voltar e trocar o horário")}
      <h2>Quase lá</h2>
      <ul class="resumo">
        <li><span>Serviço</span><span>${esc(estado.servico.nome)}</span></li>
        <li><span>Barbeiro</span><span>${esc(estado.barbeiro.nome)}</span></li>
        <li><span>Quando</span><span>${esc(dataLonga(estado.horario))}, ${hora(estado.horario)}</span></li>
        <li><span>Valor</span><span>${moeda(estado.servico.preco)}</span></li>
      </ul>
      <form id="form-dados" novalidate>
        <div class="campo">
          <label for="nome">Seu nome</label>
          <input id="nome" name="nome" autocomplete="name" required value="${esc(lembrado("cliente.nome"))}">
        </div>
        <div class="campo">
          <label for="telefone">Celular com DDD</label>
          <input id="telefone" name="telefone" type="tel" inputmode="tel" autocomplete="tel-national"
                 placeholder="(31) 99999-9999" required value="${esc(formatarTelefone(lembrado("cliente.telefone")))}">
          <small>Usamos só para falar com você sobre o horário.</small>
        </div>
        <p class="erro" id="erro-dados" role="alert" hidden></p>
        <button type="submit" class="botao" id="btn-confirmar" style="width:100%">Confirmar agendamento</button>
      </form>`);
    tela.querySelector("[data-voltar]").onclick = () => telaHorario();

    const campoTel = document.getElementById("telefone");
    campoTel.addEventListener("input", () => { campoTel.value = formatarTelefone(campoTel.value); });
    document.getElementById("form-dados").onsubmit = confirmar;
  }

  async function confirmar(ev) {
    ev.preventDefault();
    const nome = document.getElementById("nome").value.trim();
    const telefone = document.getElementById("telefone").value.replace(/\D/g, "");
    const erroEl = document.getElementById("erro-dados");
    const botao = document.getElementById("btn-confirmar");

    erroEl.hidden = true;
    if (!nome) return mostrarErro("Informe seu nome.");
    if (telefone.length < 10) return mostrarErro("Informe o celular com DDD.");

    botao.disabled = true;
    botao.textContent = "Agendando...";
    try {
      const ag = await api("/agendamentos/publico", {
        method: "POST",
        body: { nome, telefone, servicoId: estado.servico.id, barbeiroId: estado.barbeiro.id, inicio: estado.horario },
      });
      lembrar("cliente.nome", nome);
      lembrar("cliente.telefone", telefone);
      telaConfirmacao(ag);
    } catch (e) {
      if (e.status === 409) {
        telaHorario("Esse horário acabou de ser ocupado. Escolha outro.");
      } else {
        mostrarErro(e.message);
        botao.disabled = false;
        botao.textContent = "Confirmar agendamento";
      }
    }

    function mostrarErro(msg) { erroEl.textContent = msg; erroEl.hidden = false; }
  }

  /* ---------- confirmação ---------- */
  function telaConfirmacao(ag) {
    setPasso(PASSOS.length + 1);
    montar(`
      <div class="ticket">
        <h2>Horário reservado</h2>
        <ul class="resumo">
          <li><span>Serviço</span><span>${esc(ag.servico)}</span></li>
          <li><span>Barbeiro</span><span>${esc(ag.barbeiro)}</span></li>
          <li><span>Dia</span><span>${esc(dataLonga(ag.inicio))}</span></li>
          <li><span>Hora</span><span>${hora(ag.inicio)}</span></li>
          <li><span>Valor</span><span>${moeda(ag.valor)}</span></li>
          <li><span>Código</span><span>#${ag.id}</span></li>
        </ul>
        <p>Esta é uma versão de demonstração: o agendamento é fictício e o pagamento seria feito na barbearia.</p>
      </div>
      <button type="button" class="botao-sec" id="novo">Fazer outro agendamento</button>`);
    document.getElementById("novo").onclick = () => {
      Object.assign(estado, { servico: null, barbeiro: null, data: null, horario: null });
      telaServico();
    };
  }

  /* ---------- início ---------- */
  async function iniciar() {
    setPasso(0);
    tela.innerHTML = `<p class="vazio">Carregando...</p>`;
    try {
      [estado.servicos, estado.barbeiros] = await Promise.all([api("/publico/servicos"), api("/publico/barbeiros")]);
      telaServico();
    } catch (e) {
      tela.innerHTML = `<p class="erro" role="alert">${esc(e.message)}</p>
        <button type="button" class="botao-sec" id="tentar">Tentar de novo</button>`;
      document.getElementById("tentar").onclick = iniciar;
    }
  }

  iniciar();
})();
