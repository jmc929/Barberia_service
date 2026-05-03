import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { api, tokenStorage, userStorage, UsuarioDTO } from "@/lib/api";

interface AuthContextValue {
  user: UsuarioDTO | null;
  loading: boolean;
  login: (email: string, contraseña: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UsuarioDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = userStorage.get<UsuarioDTO>();
    if (stored && tokenStorage.get()) setUser(stored);
    setLoading(false);
  }, []);

  const login = async (email: string, contraseña: string) => {
    const data = await api.auth.login(email, contraseña);
    tokenStorage.set(data.token);
    userStorage.set(data.usuario);
    setUser(data.usuario);
  };

  const logout = () => {
    tokenStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth debe usarse dentro de AuthProvider");
  return ctx;
}