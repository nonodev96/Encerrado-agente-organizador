/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.project.Partida_Organizador;
import es.uja.ssmmaa.ontologia.juegoTablero.ClasificacionJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.IncidenciaJuego;

import jade.proto.SubscriptionResponder.Subscription;
import jade.lang.acl.ACLMessage;
import jade.content.onto.OntologyException;
import jade.content.lang.Codec;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.Agent;
import jade.core.AID;

/**
 *
 * @author nono_
 */
public class TaskSend_Inform_IncidenciaJuego extends OneShotBehaviour {

    private final AgenteOrganizador myAgent_Organizador;
    private final AID agente_monitor_AID;
    private final IncidenciaJuego incidenciaJuego;

    public TaskSend_Inform_IncidenciaJuego(Agent a, AID agente_monitor_AID, IncidenciaJuego incidenciaJuego) {
        super(a);
        this.myAgent_Organizador = (AgenteOrganizador) a;
        this.agente_monitor_AID = agente_monitor_AID;
        this.incidenciaJuego = incidenciaJuego;
    }

    @Override
    public void action() {
        GestorSubscripciones gestor = this.myAgent_Organizador.getGestor();
        Subscription sub = gestor.getSubscripcion(agente_monitor_AID.getName());
        send(sub, agente_monitor_AID);
    }

    private void send(Subscription subscription, AID sender) {
        this.myAgent_Organizador.addMsgConsole("Enviando la notificación a " + sender.getLocalName());

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setLanguage(this.myAgent_Organizador.getCodec().getName());
        msg.setOntology(this.myAgent_Organizador.getOntology().getName());
        msg.addReceiver(sender);

        // TODO
        if (incidenciaJuego != null) {
            try {
                this.myAgent_Organizador.getManager().fillContent(msg, incidenciaJuego);
            } catch (Codec.CodecException | OntologyException ex) {
                this.myAgent_Organizador.addMsgConsole("Error al enviar IncidenciaJuego de Organizador a " + sender.getLocalName());
            }
        }

        // Enviamos el mensaje al suscriptor Monitor
        subscription.notify(msg);
    }
}
