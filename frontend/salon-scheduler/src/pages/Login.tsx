import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/hooks/use-toast";
import { ApiError } from "@/lib/api";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [contraseña, setContraseña] = useState("");
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, contraseña);
      toast({ title: "Bienvenido", description: "Sesión iniciada correctamente." });
      navigate("/mis-citas");
    } catch (err) {
      toast({
        title: "No se pudo iniciar sesión",
        description: err instanceof ApiError ? err.message : "Error desconocido",
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto">
      <div className="bg-card border border-border rounded-lg p-8">
        <h1 className="font-display text-3xl mb-2">Iniciar sesión</h1>
        <p className="text-muted-foreground mb-6 text-sm">
          Ingresa con tu correo para gestionar tus citas.
        </p>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email">Correo</Label>
            <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="pwd">Contraseña</Label>
            <Input id="pwd" type="password" required value={contraseña} onChange={(e) => setContraseña(e.target.value)} />
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Ingresando..." : "Ingresar"}
          </Button>
        </form>
        <p className="text-sm text-muted-foreground mt-6 text-center">
          ¿No tienes cuenta?{" "}
          <Link to="/registro" className="text-accent font-medium hover:underline">Regístrate</Link>
        </p>
      </div>
    </div>
  );
}