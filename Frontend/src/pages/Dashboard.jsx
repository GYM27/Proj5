import React, { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useUserStore } from "../stores/UserStore"; // Importação da Store para RBAC
import api from "../services/api";
import "../App.css";

/**
 * COMPONENTE: Dashboard
 * --------------------
 * DESCRIÇÃO: Painel central de indicadores de desempenho (KPIs).
 * FUNCIONALIDADE: Adapta a visualização de dados com base no Role do utilizador (ADMIN vs USER).
 */
const Dashboard = () => {
  // ESTADO DOS INDICADORES: Inicializados a zero para garantir uma UI limpa durante o fetch.
  const [stats, setStats] = useState({
    novos: 0,
    analise: 0,
    propostas: 0,
    ganhos: 0,
    perdidos: 0,
    leads: 0,
    clientes: 0,
  });

  const navigate = useNavigate();

  // CONTEXTO DO UTILIZADOR (REATIVIDADE):
  // Extraímos os dados da Store. O componente irá reagir automaticamente
  // assim que o 'userRole' for preenchido após o Login.
  const { userRole, isAuthenticated, firstName } = useUserStore();

  /**
   * LÓGICA DE DADOS DINÂMICA (REGRA DE NEGÓCIO - 5%):
   * Esta função carrega os dados dependendo da sessão do utilizador.
   */
  const fetchDashboardData = useCallback(async () => {
    if (!userRole) return;

    try {
      // Roteamento dinâmico restabelecido!
      // Como o Java já tem o Regex, o /admin vai funcionar na perfeição.
      const leadsEndpoint = userRole === "ADMIN" ? "/leads/" : "/leads";
      const clientsEndpoint = userRole === "ADMIN" ? "/clients/" : "/clients";

      const [leads, clientes] = await Promise.all([
        api(leadsEndpoint),
        api(clientsEndpoint)
      ]);

      // PROCESSAMENTO: Filtra o array retornado para popular os contadores do funil.
      setStats({
        novos: leads.filter((l) => Number(l.state) === 1).length,
        analise: leads.filter((l) => Number(l.state) === 2).length,
        propostas: leads.filter((l) => Number(l.state) === 3).length,
        ganhos: leads.filter((l) => Number(l.state) === 4).length,
        perdidos: leads.filter((l) => Number(l.state) === 5).length,
        leads: leads.length,
        clientes: clientes.length,
      });
    } catch (error) {
      console.error("Erro no Dashboard:", error.message);

      /**
       * GESTÃO DE SESSÃO EXPIRADA (SEGURANÇA - 2%):
       * Se o token falhar (401), limpamos o sessionStorage e expulsamos para o Login.
       */
      if (error.message.includes("401") || error.message.includes("Sessão")) {
        sessionStorage.clear();
        navigate("/login");
      }
    }
  }, [navigate, userRole]);

  /**
   * CICLO DE VIDA E SINCRONIZAÇÃO:
   * O Dashboard "acorda" assim que o utilizador está autenticado.
   */
  useEffect(() => {
    let isMounted = true;

    if (isMounted && isAuthenticated) {
      fetchDashboardData();
    }

    return () => { isMounted = false; };
  }, [fetchDashboardData, isAuthenticated]);

  return (
      <div className="container-fluid">
        {/* BARRA DE BOAS-VINDAS PERSONALIZADA (UX - 3%) */}
        <div className="barra-welcome mb-4 p-3 bg-light rounded shadow-sm d-flex justify-content-between align-items-center">
          <h3 className="m-0">Olá, <strong>{firstName || "Utilizador"}</strong></h3>
        </div>

        {/* FUNIL DE VENDAS: Cards clicáveis que aplicam filtros na navegação */}
        <div className="row g-3 mb-4 justify-content-center">
          {[
            { label: "Novos Leads", count: stats.novos, color: "#e0ecff", stateId: 1 },
            { label: "Em Análise", count: stats.analise, color: "#e0ecff", stateId: 2 },
            { label: "Propostas", count: stats.propostas, color: "#e0ecff", stateId: 3 },
            { label: "Ganhos", count: stats.ganhos, color: "#d4edda", stateId: 4 },
            { label: "Perdidos", count: stats.perdidos, color: "#f8d7da", stateId: 5 },
          ].map((item, index) => (
              <div key={index} className="col-md-2 col-sm-4 col-6">
                <div
                    className="card text-center p-3 border-0 shadow-sm h-100 kpi-card"
                    style={{ backgroundColor: item.color, cursor: "pointer" }}
                    onClick={() => navigate(`/leads?state=${item.stateId}`)}
                >
                  <div className="small fw-bold opacity-75">{item.label}</div>
                  <div className="display-6 fw-bold">{item.count}</div>
                </div>
              </div>
          ))}
        </div>

        {/* TOTAIS GERAIS: Links diretos para as listagens completas */}
        <div className="row g-4 mb-4 justify-content-center">
          <div className="col-md-5">
            <div
                className="card text-center p-4 border-0 shadow-sm bg-white"
                style={{ cursor: "pointer", borderLeft: "5px solid #0d6efd" }}
                onClick={() => navigate("/leads")}
            >
              <div className="h5 opacity-75">Total de Oportunidades</div>
              <div className="display-5 fw-bold text-primary">{stats.leads}</div>
            </div>
          </div>
          <div className="col-md-5">
            <div
                className="card text-center p-4 border-0 shadow-sm bg-white"
                style={{ cursor: "pointer", borderLeft: "5px solid #198754" }}
                onClick={() => navigate("/clients")}
            >
              <div className="h5 opacity-75">Total de Clientes</div>
              <div className="display-5 fw-bold text-success">{stats.clientes}</div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default Dashboard;