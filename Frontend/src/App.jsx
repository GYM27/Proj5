import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import MainLayout from "./pages/MainLayout";
import Dashboard from "./pages/Dashboard";
import Clients from "./pages/Clients";
import NewLeads from "./components/Leads/NewLeads";
import "bootstrap/dist/css/bootstrap.min.css";
import LeadDetails from "./components/Leads/LeadDetails";
import EditLeadForm from "./components/Leads/EditLeadForm";
import LeadsKanban from "./pages/LeadsKanban";
import Users from "./pages/Users";
import Profile from "./pages/Profile";
import NewClient from "./components/Clients/NewClient";

function App() {
  const isDevelopment = import.meta.env.DEV;

  return (
    <BrowserRouter basename={isDevelopment ? "/" : "/LuisF-proj5"}>
      <Routes>
        <Route path="/" element={<Login />} />
        {/* Rotas Públicas (Sem Header/Sidebar) */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route element={<MainLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/leads" element={<LeadsKanban />} />
          <Route path="/leads/new" element={<NewLeads />} />
          <Route path="/leads/edit/:id" element={<EditLeadForm />} />
          <Route path="/leads/:id" element={<LeadDetails />} />
          <Route path="/users" element={<Users />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/clients" element={<Clients />} />
          <Route path="/clients/new" element={<NewClient />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
