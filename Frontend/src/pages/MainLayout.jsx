import React, { useState, useEffect } from "react";
import { Outlet, Navigate } from "react-router-dom";
import { useUserStore } from "../stores/UserStore"; // Importante para a reatividade
import Sidebar from "../components/Shared/Sidebar.jsx";
import Header from "../components/Shared/Header.jsx";
import Footer from "../components/Shared/Footer.jsx";

const MainLayout = () => {
  // 1. SINCRONIZAÇÃO COM A STORE (REATIVIDADE):
  // Ao extrairmos o 'isAuthenticated', o MainLayout vai "ouvir" a Store.
  // Mal o handleLogout fizer 'clearUser()', este componente re-renderiza e expulsa o user.
  const isAuthenticated = useUserStore((state) => state.isAuthenticated);

  // 2. CORREÇÃO DE STORAGE:
  // coincidir com o teu apiRequest.js
  const token = sessionStorage.getItem("token");

  const [isOpen, setIsOpen] = useState(true);
  const toggleSidebar = () => setIsOpen(!isOpen);

  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < 768) setIsOpen(false);
      else setIsOpen(true);
    };
    window.addEventListener("resize", handleResize);
    handleResize();
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  /** * GUARDA DE ROTA DINÂMICA:
   * Se o token sumir do storage OU a Store disser que não está autenticado,
   * o utilizador é enviado para o login imediatamente.
   */
  if (!token || !isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
      <div className="d-flex flex-column" style={{ minHeight: "100vh" }}>
        <Header onToggleMenu={toggleSidebar} />
        <div className="d-flex flex-grow-1" style={{ marginTop: "56px" }}>
          <Sidebar isOpen={isOpen} />
          <div className="flex-grow-1 d-flex flex-column bg-light" style={{ minWidth: 0 }}>
            <main className="p-4 flex-grow-1">
              <Outlet />
            </main>
            <Footer />
          </div>
        </div>
      </div>
  );
};

export default MainLayout;