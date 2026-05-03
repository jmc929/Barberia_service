import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { Scissors, LogOut } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const navItem = (to: string, label: string) => (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `px-3 py-2 rounded-md text-sm font-medium transition-colors ${
          isActive
            ? "bg-accent text-accent-foreground"
            : "text-foreground/70 hover:text-foreground hover:bg-secondary"
        }`
      }
    >
      {label}
    </NavLink>
  );

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <header className="border-b border-border bg-card/80 backdrop-blur sticky top-0 z-10">
        <div className="container flex items-center justify-between h-16">
          <Link to="/" className="flex items-center gap-2 font-display text-xl">
            <Scissors className="text-accent" size={22} />
            <span>Barbería</span>
          </Link>
          <nav className="hidden md:flex items-center gap-1">
            {navItem("/", "Inicio")}
            {navItem("/servicios", "Servicios")}
            {user && navItem("/agendar", "Agendar")}
            {user && navItem("/mis-citas", "Mis citas")}
          </nav>
          <div className="flex items-center gap-2">
            {user ? (
              <>
                <span className="hidden sm:inline text-sm text-muted-foreground">
                  Hola, {user.nombrePersona.split(" ")[0]}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    logout();
                    navigate("/login");
                  }}
                >
                  <LogOut size={16} className="mr-1" /> Salir
                </Button>
              </>
            ) : (
              <>
                <Button asChild variant="ghost" size="sm">
                  <Link to="/login">Ingresar</Link>
                </Button>
                <Button asChild size="sm">
                  <Link to="/registro">Registrarse</Link>
                </Button>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="flex-1 container py-8">
        <Outlet />
      </main>
      <footer className="border-t border-border py-6 text-center text-sm text-muted-foreground">
        © {new Date().getFullYear()} Barbería · Hecho con cariño
      </footer>
    </div>
  );
}