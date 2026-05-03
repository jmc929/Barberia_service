import { useEffect, useState } from "react";
import { api, ApiError, CitaDTO, formatLocalTime } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { Button } from "@/components/ui/button";
import { toast } from "@/hooks/use-toast";
import { Calendar, X, Check } from "lucide-react";

export default function MisCitas() {
  const { user } = useAuth();
  const [citas, setCitas] = useState<CitaDTO[]>([]);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    api.citas
      .todas()
      .then((data) => {
        const mias = (data || []).filter(
          (c) => c.numeroDocumentoCliente === user?.numeroDocumento
        );
        setCitas(mias as CitaDTO[]);
      })
      .catch((err) =>
        toast({
          title: "Error cargando citas",
          description: err instanceof ApiError ? err.message : "",
          variant: "destructive",
        })
      )
      .finally(() => setLoading(false));
  };

  useEffect(load, [user]);

  const cancelar = async (no: number) => {
    try {
      await api.agendamiento.cancelar(no);
      toast({ title: "Cita cancelada" });
      load();
    } catch (err) {
      toast({
        title: "No se pudo cancelar",
        description: err instanceof ApiError ? err.message : "",
        variant: "destructive",
      });
    }
  };

  const confirmar = async (no: number) => {
    try {
      await api.agendamiento.confirmar(no);
      toast({ title: "Cita confirmada" });
      load();
    } catch (err) {
      toast({
        title: "No se pudo confirmar",
        description: err instanceof ApiError ? err.message : "",
        variant: "destructive",
      });
    }
  };

  return (
    <div>
      <h1 className="font-display text-4xl mb-2">Mis citas</h1>
      <p className="text-muted-foreground mb-8">Gestiona tus reservas: confirma, cancela o agenda nuevas.</p>
      {loading ? (
        <p className="text-muted-foreground">Cargando...</p>
      ) : citas.length === 0 ? (
        <div className="bg-card border border-border rounded-lg p-8 text-center">
          <Calendar className="mx-auto text-accent mb-3" />
          <p className="text-muted-foreground">Aún no tienes citas agendadas.</p>
        </div>
      ) : (
        <div className="grid gap-3">
          {citas.map((c) => (
            <div key={c.noCita} className="bg-card border border-border rounded-lg p-5 flex flex-wrap items-center gap-4 justify-between">
              <div>
                <p className="font-display text-lg">Cita #{c.noCita}</p>
                <p className="text-sm text-muted-foreground">
                  {c.fechaCita} · {formatLocalTime(c.horaInicioCita)} - {formatLocalTime(c.horaFinCita)}
                </p>
                <p className="text-xs text-muted-foreground mt-1">
                  Barbero: {c.numeroDocumentoPeluquero} · Servicio #{c.idServicio}
                </p>
              </div>
              <div className="flex gap-2">
                <Button size="sm" variant="outline" onClick={() => confirmar(c.noCita)}>
                  <Check size={14} className="mr-1" /> Confirmar
                </Button>
                <Button size="sm" variant="destructive" onClick={() => cancelar(c.noCita)}>
                  <X size={14} className="mr-1" /> Cancelar
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
