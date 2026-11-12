(function () {
  const form = document.getElementById("form-login");
  const erroEl = document.getElementById("erro-login");
  const botao = document.getElementById("btn-entrar");

  document.getElementById("nome-barbearia").textContent = BARBEARIA.nome;
  document.title = "Login do dono - " + BARBEARIA.nome;

  function mostrarErro(msg) { erroEl.textContent = msg; erroEl.hidden = false; }

  function entrou() {
    const hoje = dataISO(new Date());
    document.querySelector("main").innerHTML = `
      <h2 style="margin-bottom:16px">Login realizado</h2>
      <p>A sessão está ativa neste navegador. Agora você pode abrir as rotas protegidas da API:</p>
      <ul>
        <li><a href="/api/clientes">/api/clientes</a></li>
        <li><a href="/api/barbeiros">/api/barbeiros</a></li>
        <li><a href="/api/barbeiros/1/agenda?data=${hoje}">/api/barbeiros/1/agenda?data=${hoje}</a></li>
      </ul>
      <p style="margin-top:20px">
        <a href="/" class="botao-texto">Voltar para a página de agendamento</a>
        <button type="button" class="botao-sec pequeno" id="sair">Sair</button>
      </p>`;
    document.getElementById("sair").onclick = async () => {
      try {
        await fetch("/logout", { method: "POST", headers: { "X-XSRF-TOKEN": tokenCsrf() } });
      } finally {
        location.reload();
      }
    };
  }

  form.onsubmit = async (ev) => {
    ev.preventDefault();
    erroEl.hidden = true;

    const username = document.getElementById("usuario").value.trim();
    const password = document.getElementById("senha").value;
    if (!username || !password) return mostrarErro("Informe usuário e senha.");

    botao.disabled = true;
    botao.textContent = "Entrando...";
    try {
      const resp = await fetch("/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
          "X-XSRF-TOKEN": tokenCsrf(),
        },
        body: new URLSearchParams({ username, password }),
      });
      if (resp.status === 204) { entrou(); return; }
      if (resp.status === 401) mostrarErro("Usuário ou senha incorretos.");
      else mostrarErro("Não foi possível entrar. Recarregue a página e tente de novo.");
    } catch (_) {
      mostrarErro("Sem conexão com o servidor. Tente de novo.");
    }
    botao.disabled = false;
    botao.textContent = "Entrar";
  };
})();
