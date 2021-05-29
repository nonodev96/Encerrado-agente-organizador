/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.interfaces.TasksOrganizador;
import es.uja.ssmmaa.ontologia.Vocabulario.Motivo;
import es.uja.ssmmaa.ontologia.juegoTablero.AgenteJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.JuegoAceptado;
import es.uja.ssmmaa.ontologia.juegoTablero.Justificacion;

import static jade.lang.acl.ACLMessage.ACCEPT_PROPOSAL;
import static jade.lang.acl.ACLMessage.REJECT_PROPOSAL;
import jade.content.onto.OntologyException;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.proto.ProposeInitiator;
import jade.content.AgentAction;
import jade.lang.acl.ACLMessage;
import jade.content.lang.Codec;
import java.util.Iterator;
import java.util.Vector;
import jade.core.Agent;
import jade.core.AID;

/**
 *
 * @author nono_
 */
public class TaskSendPropose_Organizador extends ProposeInitiator {

    private final TasksOrganizador agente;
    private final ContentManager manager;

    public TaskSendPropose_Organizador(Agent a, ACLMessage msg, AgentAction agentAction) {
        super(a, msg);
        this.agente = (TasksOrganizador) a;
        this.manager = agente.getManager();
    }

    @Override
    protected void handleOutOfSequence(ACLMessage msg) {
        System.out.println("ERROR en Proponer Juego_________________\n" + msg);
    }

    @Override
    protected void handleAllResponses(Vector responses) {
        AID agenteJuego = null;
        Iterator it = responses.iterator();

        while (it.hasNext()) {
            ACLMessage msg = (ACLMessage) it.next();

            agenteJuego = msg.getSender();
            try {
                ContentElement respuesta = manager.extractContent(msg);

                switch (msg.getPerformative()) {
                    case ACCEPT_PROPOSAL:
                        // Envio CompletarPartida A Tablero
                        // Trato la respuesta con <JuegoAceptado>
                        // =========================
                        if (respuesta instanceof JuegoAceptado) {
                            JuegoAceptado juegoAceptado = (JuegoAceptado) respuesta;
                            this.agente.addMsgConsole("Juego aceptado " + juegoAceptado);

                            process_JuegoAceptado(juegoAceptado);
                        }
                        break;
                    case REJECT_PROPOSAL:
                        // Envio CompletarPartida A Tablero
                        // Trato la respuesta con <Justificación>
                        // =========================
                        if (respuesta instanceof Justificacion) {
                            Justificacion justificacion = (Justificacion) respuesta;
                            this.agente.addMsgConsole("justificacion " + justificacion);

                            process_Justificacion(justificacion);
                        }
                        break;
                    default:
                }
            } catch (Codec.CodecException | OntologyException ex) {
                System.out.println("ERROR en la construcción del mensaje de\n"
                        + agenteJuego.getLocalName() + "\n" + msg + "\n" + ex);
            }
        }
    }

    private void process_JuegoAceptado(JuegoAceptado juegoAceptado) {
        AgenteJuego agenteJuego = juegoAceptado.getAgenteJuego();
        Juego juego = juegoAceptado.getJuego();

    }

    private void process_Justificacion(Justificacion justificacion) {
       Motivo motivo = justificacion.getDetalle();
       justificacion.getJuego();
    }

}
