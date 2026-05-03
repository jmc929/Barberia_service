import { useEffect, useState } from "react";
import { api, ApiError, ServicioDTO } from "@/lib/api";
import { toast } from "@/hooks/use-toast";
import { Scissors } from "lucide-react";

export default function Servicios() {
  const [items, setItems] = useState<ServicioDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.servicios
      .todos()
      .then((data) => setItems(data || []))
      .catch((err) =>
        toast({
          title: "Error cargando servicios",
          description: err instanceof ApiError ? err.message : "",
          variant: "destructive",
        })
      )
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <h1 className="font-display text-4xl mb-2">Servicios</h1>
      <p className="text-muted-foreground mb-8">Descubre lo que tenemos para ti.</p>
      {loading ? (
        <p className="text-muted-foreground">Cargando...</p>
      ) : items.length === 0 ? (
        <p className="text-muted-foreground">No hay servicios disponibles.</p>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((s) => (
            <div key={s.idServicio} className="bg-card border border-border rounded-lg p-6 hover:border-accent transition-colors">
              <Scissors className="text-accent mb-3" size={20} />
              <h3 className="font-display text-xl mb-1">{s.nombreServicio}</h3>
              <p className="text-sm text-muted-foreground mb-4">{s.descripcion}</p>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">{s.duracion} min</span>
                <span className="font-semibold text-accent">${s.costo}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
