/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.project.Constantes.NombreServicio;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.SERVICIOS;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFSubscriber;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.Iterator;

/**
 * Tarea para la suscripción al servicio de páginas amarillas para obtener los
 * agentes que prestan un tipo de servicio establecido en la plantilla de
 * suscripción.
 *
 * @author pedroj
 */
public class TareaSubscripcionDF extends DFSubscriber {

    private SubscripcionDF agente;
    private final String tipoServicio;

    public TareaSubscripcionDF(Agent a, DFAgentDescription template) {
        super(a, template);
        this.agente = (SubscripcionDF) a;
        this.tipoServicio = ((ServiceDescription) template.getAllServices().next()).getType();
    }

    /**
     * Cuando un agente se subscribe al servicio de páginas amarillas se activa
     * este método y se obtendrá el AID y su NombreServicio asociado que
     * corresponda al TipoServicio del template de la tarea. Al agente se le
     * comunica el AID y NombreServicio encontrado.
     *
     * @param dfad DFAgentDescription del agente que se ha subscrito al servicio
     * de páginas amarillas.
     */
    @Override
    public void onRegister(DFAgentDescription dfad) {
        Iterator it = dfad.getAllServices();
        while (it.hasNext()) {
            ServiceDescription sd = (ServiceDescription) it.next();
            for (NombreServicio servicio : SERVICIOS) {
                if (sd.getName().equals(servicio.name())
                        && sd.getType().equals(tipoServicio)) {

                    this.agente.addAgent(dfad.getName(), servicio);

                    // Para depurar el funcionamiento de la tarea
                    System.out.println(
                            "El agente: " + myAgent.getName()
                            + "ha encontrado a:\n\t"
                            + dfad.getName());
                }
            }
        }
    }

    /**
     * Cuando un agente se elimina del servicio de páginas amarillas se comunica
     * su AID a este método. Se le suministrará al agente el AID y los
     * TipoServicio conocidos para que sea eliminado de su registro.
     *
     * @param dfad DFAgentDescription que solo contiene el AID del agente que se
     * ha eliminado del servicio de páginas amarillas.
     */
    @Override
    public void onDeregister(DFAgentDescription dfad) {
        AID agente = dfad.getName();

        for (NombreServicio servicio : SERVICIOS) {
            if (this.agente.removeAgent(agente, servicio)) {
                System.out.println(
                        "El agente: " + agente.getName()
                        + " ha sido eliminado de la lista de "
                        + myAgent.getName());
            }
        }
    }
}
