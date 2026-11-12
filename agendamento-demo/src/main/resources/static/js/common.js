// Nome que aparece no topo da página do cliente. Troque pelo nome da barbearia.
const BARBEARIA = { nome: "Barbearia Demo" };

const DIAS_SEMANA = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];

/** Lê o token CSRF que o servidor grava em cookie (necessário para POST, PUT, PATCH e DELETE). */
function tokenCsrf() {
  const par = document.cookie.split("; ").find((c) => c.startsWith("XSRF-TOKEN="));
  return par ? decodeURIComponent(par.split("=")[1]) : "";
}

async function api(caminho, opcoes = {}) {
  const metodo = (opcoes.method || "GET").toUpperCase();
  const headers = { "Content-Type": "application/json" };
  if (metodo !== "GET") {
    const token = tokenCsrf();
    if (token) headers["X-XSRF-TOKEN"] = token;
  }

  let resp;
  try {
    resp = await fetch("/api" + caminho, {
      ...opcoes,
      headers,
      body: opcoes.body ? JSON.stringify(opcoes.body) : undefined,
    });
  } catch (_) {
    throw new Error("Sem conexão com o servidor. Verifique a internet e tente de novo.");
  }

  if (resp.status === 401 && location.pathname.startsWith("/admin")) {
    location.href = "/login.html";
    throw new Error("Sessão expirada. Entre novamente.");
  }
  if (resp.status === 204) return null;

  let dados = null;
  try { dados = await resp.json(); } catch (_) { /* resposta sem corpo */ }

  if (!resp.ok) {
    const erro = new Error(mensagemDeErro(dados));
    erro.status = resp.status;
    throw erro;
  }
  return dados;
}

function mensagemDeErro(dados) {
  if (dados && dados.erros) return Object.values(dados.erros).join(". ");
  if (dados && dados.detail && dados.status && dados.status < 500) return dados.detail;
  return "Não foi possível concluir. Tente de novo em instantes.";
}

function esc(texto) {
  return String(texto ?? "").replace(/[&<>"']/g, (c) =>
    ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
}

function moeda(valor) {
  return Number(valor).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

/** "2026-11-10T14:30:00" -> "14:30" (sem conversão de fuso: o servidor já usa horário local) */
function hora(iso) { return iso.slice(11, 16); }

function dataISO(d) {
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${mes}-${dia}`;
}

function paraData(iso) {
  const [a, m, d] = iso.slice(0, 10).split("-").map(Number);
  return new Date(a, m - 1, d);
}

function dataLonga(iso) {
  return paraData(iso).toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" });
}

function formatarTelefone(digitos) {
  const d = String(digitos).replace(/\D/g, "").slice(0, 11);
  if (d.length <= 2) return d ? `(${d}` : "";
  if (d.length <= 6) return `(${d.slice(0, 2)}) ${d.slice(2)}`;
  if (d.length <= 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
}
