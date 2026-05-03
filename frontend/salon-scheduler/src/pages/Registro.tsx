import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "@/hooks/use-toast";
import { api, ApiError } from "@/lib/api";

export default function Registro() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    numeroDocumento: "",
    numeroCelular: "",
    email: "",
    nombrePersona: "",
    contrasena: "",
    confirmarContrasena: "",
  });
  const [loading, setLoading] = useState(false);

  const upd = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value });

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (form.contrasena !== form.confirmarContrasena) {
      toast({ title: "Las contrasenas no coinciden", variant: "destructive" });
      return;
    }
    setLoading(true);
    try {
      await api.personas.registrar({
        numeroDocumento: form.numeroDocumento,
        numeroCelular: form.numeroCelular,
        email: form.email,
        nombrePersona: form.nombrePersona,
        contrasena: form.contrasena,
        confirmarContrasena: form.confirmarContrasena,
      });
      toast({ title: "Registro exitoso", description: "Ahora puedes iniciar sesion." });
      navigate("/login");
    } catch (err) {
      toast({
        title: "No se pudo registrar",
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
        <h1 className="font-display text-3xl mb-2">Crear cuenta</h1>
        <p className="text-muted-foreground mb-6 text-sm">Registrate para reservar tus citas.</p>
        <form onSubmit={onSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label>Nombre completo</Label>
            <Input required value={form.nombrePersona} onChange={upd("nombrePersona")} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-2">
              <Label>Documento</Label>
              <Input required value={form.numeroDocumento} onChange={upd("numeroDocumento")} />
            </div>
            <div className="space-y-2">
              <Label>Celular</Label>
              <Input required value={form.numeroCelular} onChange={upd("numeroCelular")} />
            </div>
          </div>
          <div className="space-y-2">
            <Label>Correo</Label>
            <Input type="email" required value={form.email} onChange={upd("email")} />
          </div>
          <div className="space-y-2">
            <Label>Contrasena</Label>
            <Input type="password" required value={form.contrasena} onChange={upd("contrasena")} />
          </div>
          <div className="space-y-2">
            <Label>Confirmar contrasena</Label>
            <Input type="password" required value={form.confirmarContrasena} onChange={upd("confirmarContrasena")} />
          </div>
          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Registrando..." : "Registrarme"}
          </Button>
        </form>
        <p className="text-sm text-muted-foreground mt-6 text-center">
          Ya tienes cuenta?{" "}
          <Link to="/login" className="text-accent font-medium hover:underline">Inicia sesion</Link>
        </p>
      </div>
    </div>
  );
}