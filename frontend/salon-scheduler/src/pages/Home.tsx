import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Scissors, Calendar, Clock } from "lucide-react";

export default function Home() {
  return (
    <div className="space-y-16">
      <section className="text-center max-w-3xl mx-auto pt-8">
        <p className="text-accent uppercase tracking-widest text-xs mb-4">
          Estilo · Precisión · Tradición
        </p>
        <h1 className="font-display text-5xl md:text-6xl mb-6 leading-tight">
          Tu mejor versión empieza con un buen corte
        </h1>
        <p className="text-muted-foreground text-lg mb-8">
          Reserva tu cita en línea, elige tu barbero y disfruta de un servicio
          profesional pensado para ti.
        </p>
        <div className="flex gap-3 justify-center">
          <Button asChild size="lg">
            <Link to="/agendar">Agendar cita</Link>
          </Button>
          <Button asChild variant="outline" size="lg">
            <Link to="/servicios">Ver servicios</Link>
          </Button>
        </div>
      </section>

      <section className="grid md:grid-cols-3 gap-6">
        {[
          { icon: Scissors, title: "Cortes clásicos y modernos", desc: "Barberos expertos que entienden tu estilo." },
          { icon: Calendar, title: "Reserva online", desc: "Agenda tu cita en segundos, sin llamadas." },
          { icon: Clock, title: "Horarios flexibles", desc: "Consulta nuestra disponibilidad en tiempo real." },
        ].map(({ icon: Icon, title, desc }) => (
          <div key={title} className="bg-card border border-border rounded-lg p-6">
            <Icon className="text-accent mb-3" />
            <h3 className="font-display text-xl mb-2">{title}</h3>
            <p className="text-muted-foreground text-sm">{desc}</p>
          </div>
        ))}
      </section>
    </div>
  );
}