/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import es.uja.ssmmaa.dots_and_boxes.interfaces.TasksOrganizadorSub;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.ontologia.Vocabulario;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.Justificacion;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;

import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.proto.SubscriptionResponder;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLMessage;
import jade.core.Agent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nono_
 */
public class TaskResponderSubscription_Organizador extends SubscriptionResponder {

    // Interfaz
    private final AgenteOrganizador myAgent_Organizador;
    private final String myLocalName;

    public TaskResponderSubscription_Organizador(Agent a, MessageTemplate mt, SubscriptionManager sm) {
        super(a, mt, sm);
        this.myAgent_Organizador = (AgenteOrganizador) a;
        this.myLocalName = this.myAgent_Organizador.getLocalName();
    }


    @Override
    protected ACLMessage handleSubscription(ACLMessage subscription) throws NotUnderstoodException, RefuseException {
        String nombreAgente = subscription.getSender().getName();
        ACLMessage reply = subscription.createReply();
        Justificacion justificacion = new Justificacion();
        Juego juego = new Juego();
        // DUDA Lo dejamos por defecto?
        juego.setIdJuego("");
        juego.setTipoJuego(Vocabulario.TipoJuego.ENCERRADO);
        justificacion.setJuego(juego);

        // Responde afirmativamente a la suscripción siempre
        reply.setPerformative(ACLMessage.AGREE);
        reply.setLanguage(this.myAgent_Organizador.getCodec().getName());
        reply.setOntology(this.myAgent_Organizador.getOntology().getName());

        // Registra la suscripción si no hay una previa
        if (!this.myAgent_Organizador.getGestor().haySubscripcion(nombreAgente)) {
            this.myAgent_Organizador.addMsgConsole("Subscripción creada en " + myLocalName + " para " + nombreAgente);
            Subscription sub = createSubscription(subscription);
            this.mySubscriptionManager.register(sub);

            justificacion.setDetalle(Vocabulario.Motivo.SUBSCRIPCION_ACEPTADA);
        } 


        try {
            this.myAgent_Organizador.getManager().fillContent(reply, justificacion);
        } catch (Codec.CodecException | OntologyException ex) {
//            ex.printStackTrace();
            this.myAgent_Organizador.addMsgConsole("Error al registrar la subscripción en " + myLocalName);
        }
        return reply;
    }

    @Override
    protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
        // Eliminamos la suscripción del agente jugador
        ACLMessage replyCancel = cancel.createReply();
        String nombreAgente = cancel.getSender().getName();
        Justificacion justificacion = new Justificacion();

        Subscription sub = this.myAgent_Organizador.getGestor().getSubscripcion(nombreAgente);
        try {
            this.mySubscriptionManager.deregister(sub);
        } catch (FailureException e) {
            this.myAgent_Organizador.addMsgConsole("Error al cancelar la subscripción para " + nombreAgente);
            justificacion.setDetalle(Vocabulario.Motivo.ERROR_CANCELACION);
        }

        // Mensaje de cancelación
        this.myAgent_Organizador.addMsgConsole("Subscripción cancelada en " + myLocalName + " para " + nombreAgente);
        replyCancel.setPerformative(ACLMessage.INFORM);
        replyCancel.setSender(this.myAgent_Organizador.getAID());

        try {
            this.myAgent_Organizador.getManager().fillContent(replyCancel, justificacion);
        } catch (Codec.CodecException | OntologyException ex) {
            this.myAgent_Organizador.addMsgConsole("Error al cancelar la subscripcion de " + cancel.getSender().getLocalName() + "en " + myLocalName);
        }
        return replyCancel;
    }
}
