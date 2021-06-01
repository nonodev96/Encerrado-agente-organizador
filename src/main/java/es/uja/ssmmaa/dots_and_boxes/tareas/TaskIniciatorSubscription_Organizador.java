/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.ontologia.juegoTablero.ClasificacionJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.IncidenciaJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Justificacion;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import es.uja.ssmmaa.dots_and_boxes.project.Partida_Organizador;
import es.uja.ssmmaa.ontologia.Vocabulario;
import es.uja.ssmmaa.ontologia.Vocabulario.Incidencia;
import es.uja.ssmmaa.ontologia.Vocabulario.Motivo;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.ResultadoPartida;

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

    private final AgenteOrganizador myAgent_Organizador;

    public TaskIniciatorSubscription_Organizador(Agent a, ACLMessage msg) {
        super(a, msg);
        this.myAgent_Organizador = (AgenteOrganizador) a;
    }

    @Override
    protected void handleOutOfSequence(ACLMessage msg) {
        // Ha llegado un mensaje fuera de la secuencia del protocolo
        myAgent_Organizador.addMsgConsole("ERROR en Informar Juego___________________\n" + msg);
    }

    @Override
    protected void handleAllResponses(Vector responses) {
        ContentManager manager;
        Justificacion justificacion;
        Iterator it = responses.iterator();

        if (responses.isEmpty()) {
            myAgent_Organizador.addMsgConsole("EL ORGANIZADOR NO RESPONDE A LA SUSCRIPCIÓN");
        }

        while (it.hasNext()) {

            ACLMessage msg = (ACLMessage) it.next();
            AID emisor = msg.getSender();
            manager = myAgent_Organizador.getManager();
            if (manager == null) {
                myAgent_Organizador.addMsgConsole("NO SE ENTIENDE EL MENSAJE\n" + msg);
                throw new NullPointerException("manager error");
            }
            try {
                justificacion = (Justificacion) manager.extractContent(msg);
                switch (msg.getPerformative()) {
                    case NOT_UNDERSTOOD:
                        myAgent_Organizador.addMsgConsole("El agente " + emisor + " no entiende la suscripción\n" + justificacion);
                        break;
                    case REFUSE:
                        myAgent_Organizador.addMsgConsole("El agente " + emisor + " rechaza la suscripción\n" + justificacion);
                        break;
                    case FAILURE:
                        myAgent_Organizador.addMsgConsole("El agente " + emisor + " no ha completado la suscripción\n" + justificacion);
                        break;
                    case AGREE:
                        myAgent_Organizador.addSubscription(emisor, this);
                        myAgent_Organizador.addMsgConsole("El agente " + emisor + " ha aceptado la suscripción\n" + justificacion);
                        break;
                    default:
                        myAgent_Organizador.addMsgConsole("El agente " + emisor + " envía un mensaje desconocido\n" + msg);
                }
            } catch (Codec.CodecException | OntologyException ex) {
                myAgent_Organizador.addMsgConsole(emisor.getLocalName() + " El contenido del mensaje es incorrecto\n\t" + ex);
            }

        }
    }

    @Override
    protected void handleInform(ACLMessage inform) {
        ContentElement contenido;
        ContentManager manager = myAgent_Organizador.getManager();

        try {
            contenido = manager.extractContent(inform);

            if (contenido instanceof ResultadoPartida) {
                // Finalización correcta del juego
                ResultadoPartida resultadoPartida = (ResultadoPartida) contenido;
                myAgent_Organizador.addMsgConsole("ResultadoPartida: " + resultadoPartida);
                String idPartida = resultadoPartida.getPartida().getIdPartida();
                Partida_Organizador partida_Organizador = this.myAgent_Organizador.getPartida(idPartida);

                resultadoPartida.getGanador();
                Juego juego = resultadoPartida.getPartida().getJuego();

                partida_Organizador.clasificacionJuego = new ClasificacionJuego(juego, partida_Organizador.listaJugadores, null);

                this.myAgent_Organizador.Inform_ClasificacionJuego_o_IncidenciaJuego(idPartida);
            } else if (contenido instanceof IncidenciaJuego) {
                // El juego no ha finalizado
                IncidenciaJuego incidenciaJuego = (IncidenciaJuego) contenido;
                myAgent_Organizador.addMsgConsole("IncidenciaJuego: " + incidenciaJuego);
                Juego juego = incidenciaJuego.getJuego();
                Incidencia incidencia = incidenciaJuego.getDetalle();
//                                partida_Organizador. = new ClasificacionJuego(juego, partida_Organizador.listaJugadores, null);
                IncidenciaJuego incidenciaJuego_to_send = new IncidenciaJuego(juego, incidencia);
                
                // A quien se lo envio si no se quien lo envia?
//                this.myAgent_Organizador.Inform_IncidenciaJuego(incidenciaJuego_to_send);
            }

        } catch (Codec.CodecException | OntologyException ex) {
            myAgent_Organizador.addMsgConsole("Error en el formato del mensaje del agente " + inform.getSender().getLocalName());
        }
    }

}
