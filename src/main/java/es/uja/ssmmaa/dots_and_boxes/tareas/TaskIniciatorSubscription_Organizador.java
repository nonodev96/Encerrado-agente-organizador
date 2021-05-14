/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import es.uja.ssmmaa.dots_and_boxes.interfaces.TasksOrganizadorSub;
import es.uja.ssmmaa.ontologia.juegoTablero.ClasificacionJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.IncidenciaJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Justificacion;

import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.NOT_UNDERSTOOD;
import static jade.lang.acl.ACLMessage.REFUSE;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author nono_
 */
public class TaskIniciatorSubscription_Organizador extends SubscriptionInitiator {

    private final AgenteOrganizador agente;

    public TaskIniciatorSubscription_Organizador(Agent a, ACLMessage msg) {
        super(a, msg);
        this.agente = (AgenteOrganizador) a;
    }

    @Override
    protected void handleOutOfSequence(ACLMessage msg) {
        // Ha llegado un mensaje fuera de la secuencia del protocolo
        agente.addMsgConsole("ERROR en Informar Juego___________________\n" + msg);
    }

    @Override
    protected void handleAllResponses(Vector responses) {
        ContentManager manager;
        Justificacion justificacion;
        Iterator it = responses.iterator();

        while (it.hasNext()) {
            ACLMessage msg = (ACLMessage) it.next();
            AID emisor = msg.getSender();
            manager = agente.getManager();

            if (manager == null) {
                agente.addMsgConsole("NO SE ENTIENDE EL MENSAJE\n" + msg);
                return;
            }
            try {
                justificacion = (Justificacion) manager.extractContent(msg);

                switch (msg.getPerformative()) {
                    case NOT_UNDERSTOOD:
                        agente.addMsgConsole("El agente " + emisor + " no entiende la suscripción\n" + justificacion);
                        break;
                    case REFUSE:
                        agente.addMsgConsole("El agente " + emisor + " rechaza la suscripción\n" + justificacion);
                        break;
                    case FAILURE:
                        agente.addMsgConsole("El agente " + emisor + " no ha completado la suscripción\n" + justificacion);
                        break;
                    case AGREE:
                        agente.addSubscription(emisor, this);
                        agente.addMsgConsole("El agente " + emisor + " ha aceptado la suscripción\n" + justificacion);
                        break;
                    default:
                        agente.addMsgConsole("El agente " + emisor + " envía un mensaje desconocido\n"
                                + msg);
                }
            } catch (Codec.CodecException | OntologyException ex) {
                agente.addMsgConsole(emisor.getLocalName()
                        + " El contenido del mensaje es incorrecto\n\t"
                        + ex);
            }

        }

        if (responses.isEmpty()) {
            agente.addMsgConsole("EL ORGANIZADOR NO RESPONDE A LA SUSCRIPCIÓN");
        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        ContentElement contenido;
        ContentManager manager = agente.getManager();

        try {
            contenido = manager.extractContent(inform);

            if (contenido instanceof ClasificacionJuego) {
                // Finalización correcta del juego
                agente.addMsgConsole("CLASIFICACION\n" + (ClasificacionJuego) contenido);
            } else {
                // El juego no ha finalizado
                agente.addMsgConsole("INCIDENCIA\n" + (IncidenciaJuego) contenido);
            }

            agente.setResultado(inform.getSender(), contenido);
        } catch (Codec.CodecException | OntologyException ex) {
            agente.addMsgConsole("Error en el formato del mensaje del agente "
                    + inform.getSender().getLocalName());
        }
    }

}
