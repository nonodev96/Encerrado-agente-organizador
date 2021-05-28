/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.MAX_JUGADORES_PARTIDA;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.MAX_PARTIDAS;
import es.uja.ssmmaa.dots_and_boxes.project.Partida_Organizador;
import es.uja.ssmmaa.ontologia.Vocabulario;
import es.uja.ssmmaa.ontologia.Vocabulario.Modo;
import es.uja.ssmmaa.ontologia.Vocabulario.TipoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.AgenteJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.CompletarJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.InfoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.JuegoAceptado;
import es.uja.ssmmaa.ontologia.juegoTablero.Justificacion;
import es.uja.ssmmaa.ontologia.juegoTablero.Monitor;
import es.uja.ssmmaa.ontologia.juegoTablero.Organizador;

import jade.content.Concept;
import jade.content.lang.Codec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;
import jade.util.leap.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nono_
 */
public class TaskResponserPropose_Organizador extends ProposeResponder {

    private final AgenteOrganizador myAgent_organizador;

    public TaskResponserPropose_Organizador(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.myAgent_organizador = (AgenteOrganizador) a;
        this.myAgent_organizador.addMsgConsole("--> ProposeResponder(Agent a, MessageTemplate mt)");
    }

    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
        this.myAgent_organizador.addMsgConsole("    --> prepareResponse");
        ACLMessage reply = propose.createReply();
        CompletarJuego completarJuego = null;
        
        try {
            Action action = (Action) this.myAgent_organizador.getManager().extractContent(propose);
            completarJuego = (CompletarJuego) action.getAction();
        } catch (Codec.CodecException | OntologyException ex) {
            this.myAgent_organizador.addMsgConsole("Error al extraer la información de CompletarJuego de monitor");
        }

        InfoJuego infoJuego = completarJuego.getAgenteJuego();
        // ================

        Juego juego = completarJuego.getJuego();
        String idJuego = juego.getIdJuego();
        TipoJuego tipoJuego = juego.getTipoJuego();
        List listaJugadores = completarJuego.getListaJugadores();
        Modo modo = completarJuego.getModo();

        // ================
        int errores = 0;
        Justificacion justificacion = new Justificacion();
        justificacion.setJuego(juego);

        if (tipoJuego != TipoJuego.ENCERRADO) {
            justificacion.setDetalle(Vocabulario.Motivo.TIPO_JUEGO_NO_IMPLEMENTADO);
            errores++;
        }
        if (modo != Modo.UNICO) {
            justificacion.setDetalle(Vocabulario.Motivo.TIPO_JUEGO_NO_IMPLEMENTADO);
            errores++;
        }
        if (listaJugadores.size() > MAX_JUGADORES_PARTIDA) {
            justificacion.setDetalle(Vocabulario.Motivo.PARTICIPACION_EN_JUEGOS_SUPERADA);
            errores++;
        }

        Map<String, Partida_Organizador> partidas = this.myAgent_organizador.getPartidas();
        if (partidas.size() > MAX_PARTIDAS) {
            justificacion.setDetalle(Vocabulario.Motivo.SUPERADO_LIMITE_PARTIDAS);
        }
        if (partidas.get(idJuego) != null) {
            justificacion.setDetalle(Vocabulario.Motivo.JUEGOS_ACTIVOS_SUPERADOS);
        }

        if (errores != 0) {
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
            try {
                this.myAgent_organizador.getManager().fillContent(reply, justificacion);
            } catch (Codec.CodecException | OntologyException ex) {
                this.myAgent_organizador.addMsgConsole("Error al justificar el motivo del fallo al completar un juego");
            }

        } else {
            JuegoAceptado juegoAceptado = new JuegoAceptado();
            
            // TODO AgenteJuego es abstracto e implementa:
            //      Monitor|Organizador|Jugador
//            Monitor agenteJuego_m = new Monitor();
//            agenteJuego_m.setAgenteMonitor(propose.getSender());
//            agenteJuego_m.setNombre(propose.getSender().getName());

            Organizador agenteJuego_o = new Organizador();
            agenteJuego_o.setAgenteOrganizador(this.myAgent_organizador.getAID());
            // DUDAS
            agenteJuego_o.setNombre(this.myAgent_organizador.getName());

            juegoAceptado.setJuego(juego);
            juegoAceptado.setAgenteJuego(agenteJuego_o);

            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            try {
                this.myAgent_organizador.getManager().fillContent(reply, juegoAceptado);
            } catch (Codec.CodecException | OntologyException ex) {
                this.myAgent_organizador.addMsgConsole("Error al justificar el motivo del fallo al completar un juego");
            }
        }

        return reply;
    }

}
