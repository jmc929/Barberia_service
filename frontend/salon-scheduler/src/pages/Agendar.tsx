import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiError, ServicioDTO, UsuarioDTO } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "@/hooks/use-toast";

function addMinutes(time: string, minutes: number): string {
  const [h, m] = time.split(":").map(Number);
  const total = h * 60 + m + minutes;
  const hh = String(Math.floor(total / 60) % 24).padStart(2, "0");
  const mm = String(total % 60).padStart(2, "0");
  return `${hh}:${mm}:00`;
}

export default function Agendar() {
  const navigate = useNavigate();
  const [servicios, setServicios] = useState<ServicioDTO[]>([]);
  const [barberos, setBarberos] = useState<UsuarioDTO[]>([]);
  const [form, setForm] = useState({
    idServicio: "",
    numeroDocumentoPeluquero: "",
    fechaCita: "",
    horaInicioCita: "",
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    Promise.all([api.servicios.todos(), api.personas.barberos()])
      .then(([s, b]) => {
        setServicios(s || []);
        setBarberos(b || []);
      })
      .catch((err) =>
        toast({
          title: "Error cargando datos",
          description: err instanceof ApiError ? err.message : "",
          variant: "destructive",
        })
      );
  }, []);

  const buildPayload = () => {
    const servicio = servicios.find((s) => String(s.idServicio) === form.idServicio);
    if (!servicio) throw new Error("Selecciona un servicio");
    const horaInicio = `${form.horaInicioCita}:00`;
    const horaFin = addMinutes(form.horaInicioCita, servicio.duracion);
    return {
      idServicio: Number(form.idServicio),
      numeroDocumentoPeluquero: form.numeroDocumentoPeluquero,
      fechaCita: form.fechaCita,
      horaInicioCita: horaInicio,
      horaFinCita: horaFin,
    };
  };

  const checkDisponibilidad = async () => {
    setLoading(true);
    try {
      const payload = buildPayload();
      const res = await api.agendamiento.disponibilidad(payload);
      toast({
        title: res.disponible ? "¡Hay disponibilidad!" : "No disponible",
        description: res.mensaje,
        variant: res.disponible ? "default" : "destructive",
      });
    } catch (err) {
      toast({
        title: "Error",
        description: err instanceof ApiError ? err.message : (err as Error).message,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = buildPayload();
      await api.agendamiento.agendar(payload);
      toast({ title: "Cita agendada", description: "Tu reserva fue creada." });
      navigate("/mis-citas");
    } catch (err) {
      toast({
        title: "No se pudo agendar",
        description: err instanceof ApiError ? err.message : (err as Error).message,
        variant: "destructive",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-xl mx-auto">
      <h1 className="font-display text-4xl mb-2">Agendar cita</h1>
      <p className="text-muted-foreground mb-8">Elige servicio, barbero, fecha y hora.</p>
      <form onSubmit={onSubmit} className="bg-card border border-border rounded-lg p-6 space-y-4">
        <div className="space-y-2">
          <Label>Servicio</Label>
          <Select value={form.idServicio} onValueChange={(v) => setForm({ ...form, idServicio: v })}>
            <SelectTrigger><SelectValue placeholder="Selecciona un servicio" /></SelectTrigger>
            <SelectContent>
              {servicios.map((s) => (
                <SelectItem key={s.idServicio} value={String(s.idServicio)}>
                  {s.nombreServicio} · {s.duracion} min · ${s.costo}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="space-y-2">
          <Label>Barbero</Label>
          <Select value={form.numeroDocumentoPeluquero} onValueChange={(v) => setForm({ ...form, numeroDocumentoPeluquero: v })}>
            <SelectTrigger><SelectValue placeholder="Selecciona un barbero" /></SelectTrigger>
            <SelectContent>
              {barberos.map((b) => (
                <SelectItem key={b.numeroDocumento} value={b.numeroDocumento}>
                  {b.nombrePersona}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="space-y-2">
            <Label>Fecha</Label>
            <Input type="date" required value={form.fechaCita} onChange={(e) => setForm({ ...form, fechaCita: e.target.value })} />
          </div>
          <div className="space-y-2">
            <Label>Hora inicio</Label>
            <Input type="time" required value={form.horaInicioCita} onChange={(e) => setForm({ ...form, horaInicioCita: e.target.value })} />
          </div>
        </div>
        <div className="flex gap-2 pt-2">
          <Button type="button" variant="outline" onClick={checkDisponibilidad} disabled={loading} className="flex-1">
            Consultar disponibilidad
          </Button>
          <Button type="submit" disabled={loading} className="flex-1">
            {loading ? "Procesando..." : "Agendar"}
          </Button>
        </div>
      </form>
    </div>
  );
}
