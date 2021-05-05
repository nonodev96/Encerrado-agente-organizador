/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.ClasificacionJuego;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.IncidenciaJuego;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.Justificacion;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import static jade.lang.acl.ACLMessage.AGREE;
import static jade.lang.acl.ACLMessage.FAILURE;
import static jade.lang.acl.ACLMessage.NOT_UNDERSTOOD;
import static jade.lang.acl.ACLMessage.REFUSE;
import jade.proto.SubscriptionInitiator;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author nono_
 */
public class TaskInform extends SubscriptionInitiator {

    private final TasksOrganizadorSub agente;

    public TaskInform(Agent a, ACLMessage msg) {
        super(a, msg);
        this.agente = (TasksOrganizadorSub) a;
    }

    @Override
    protected void handleOutOfSequence(ACLMessage msg) {
        // Ha llegado un mensaje fuera de la secuencia del protocolo
        agente.addMsgConsola("ERROR en Informar Juego___________________\n" + msg);
    }

    @Override
    protected void handleAllResponses(Vector responses) {
        ContentManager manager;
        Justificacion justificacion = null;
        Iterator it = responses.iterator();

        while (it.hasNext()) {
            ACLMessage msg = (ACLMessage) it.next();
            AID emisor = msg.getSender();
            manager = agente.getManager();

            if (manager != null) {
                try {
                    justificacion = (Justificacion) manager.extractContent(msg);

                    switch (msg.getPerformative()) {
                        case NOT_UNDERSTOOD:
//                            agente.addMsgConsola("El agente " + emisor + " no entiende la suscripción\n" + justificacion);
                            break;
                        case REFUSE:
//                            agente.addMsgConsola("El agente " + emisor + " rechaza la suscripción\n" + justificacion);
                            break;
                        case FAILURE:
//                            agente.addMsgConsola("El agente " + emisor + " no ha completado la suscripción\n" + justificacion);
                            break;
                        case AGREE:
//                            agente.addSubcription(emisor.getLocalName(), this);
//                            agente.addMsgConsola("El agente " + emisor + " ha aceptado la suscripción\n" + justificacion);
                            break;
                        default:
                            agente.addMsgConsola("El agente " + emisor + " envía un mensaje desconocido\n"
                                    + msg);
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    agente.addMsgConsola(emisor.getLocalName()
                            + " El contenido del mensaje es incorrecto\n\t"
                            + ex);
                }
            } else {
                agente.addMsgConsola("NO SE ENTIENDE EL MENSAJE\n" + msg);
            }
        }

        if (responses.isEmpty()) {
            agente.addMsgConsola("EL ORGANIZADOR NO RESPONDE A LA SUSCRIPCIÓN");
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
                agente.addMsgConsola("CLASIFICACION\n" + (ClasificacionJuego) contenido);
            } else {
                // El juego no ha finalizado
                agente.addMsgConsola("INCIDENCIA\n" + (IncidenciaJuego) contenido);
            }

            agente.setResultado(inform.getSender(), contenido);
        } catch (Codec.CodecException | OntologyException ex) {
            agente.addMsgConsola("Error en el formato del mensaje del agente "
                    + inform.getSender().getLocalName());
        }
    }

}
