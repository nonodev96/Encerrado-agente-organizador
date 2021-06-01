/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.tareas;

import es.uja.ssmmaa.dots_and_boxes.agentes.AgenteOrganizador;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.MAX_JUGADORES_PARTIDA;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.MAX_PARTIDAS;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.MY_GAME;
import es.uja.ssmmaa.dots_and_boxes.project.Partida_Organizador;
import es.uja.ssmmaa.ontologia.Vocabulario;
import es.uja.ssmmaa.ontologia.Vocabulario.Modo;
import es.uja.ssmmaa.ontologia.Vocabulario.TipoJuego;
import es.uja.ssmmaa.ontologia.encerrado.Encerrado;
import es.uja.ssmmaa.ontologia.juegoTablero.AgenteJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.CompletarJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.InfoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.JuegoAceptado;
import es.uja.ssmmaa.ontologia.juegoTablero.Justificacion;
import es.uja.ssmmaa.ontologia.juegoTablero.Monitor;
import es.uja.ssmmaa.ontologia.juegoTablero.Organizador;
import es.uja.ssmmaa.ontologia.juegoTablero.Partida;

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
    private int idPartida;

    public TaskResponserPropose_Organizador(Agent a, MessageTemplate mt) {
        super(a, mt);
        this.myAgent_organizador = (AgenteOrganizador) a;
        this.idPartida = 0;
        this.myAgent_organizador.addMsgConsole("--> ProposeResponder(Agent a, MessageTemplate mt)");
    }

    /**
     * <pre>
     * Tan solo le llega una proposición del monitor con el objeto de
     * "Completar juego"
     * </pre>
     *
     * @param propose
     * @return
     * @throws NotUnderstoodException
     * @throws RefuseException
     */
    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {
        this.myAgent_organizador.addMsgConsole("    --> prepareResponse CompletarJuego ");
        ACLMessage reply = propose.createReply();
        CompletarJuego completarJuego = new CompletarJuego();
        Action action = null;
        try {
            action = (Action) this.myAgent_organizador.getManager().extractContent(propose);
        } catch (Codec.CodecException | OntologyException ex) {
            this.myAgent_organizador.addMsgConsole("Error al extraer la información de CompletarJuego de monitor");
            this.myAgent_organizador.addMsgConsole(ex.toString());
        }
        if (action == null) {
            throw new Error("");
        }

        completarJuego = (CompletarJuego) action.getAction();

        // ================
        Encerrado infoJuego = (Encerrado) completarJuego.getAgenteJuego();

        this.myAgent_organizador.addMsgConsole("InfoJuego: " + infoJuego);

        Juego juego = completarJuego.getJuego();
        String idJuego = juego.getIdJuego();
        TipoJuego tipoJuego = juego.getTipoJuego();
        List listaJugadores = completarJuego.getListaJugadores();
        Modo modo = completarJuego.getModo();

        // ================
        int errores = 0;
        Justificacion justificacion = new Justificacion();
        justificacion.setJuego(juego);

        if (tipoJuego != MY_GAME) {
            justificacion.setDetalle(Vocabulario.Motivo.TIPO_JUEGO_NO_IMPLEMENTADO);
            this.myAgent_organizador.addMsgConsole("Error TIPO_JUEGO_NO_IMPLEMENTADO juego");
            errores++;
        }
        if (modo != Modo.UNICO) {
            justificacion.setDetalle(Vocabulario.Motivo.TIPO_JUEGO_NO_IMPLEMENTADO);
            this.myAgent_organizador.addMsgConsole("Error TIPO_JUEGO_NO_IMPLEMENTADO modo");
            errores++;
        }
        if (listaJugadores.size() > 5) {
            justificacion.setDetalle(Vocabulario.Motivo.PARTICIPACION_EN_JUEGOS_SUPERADA);
            this.myAgent_organizador.addMsgConsole("Error PARTICIPACION_EN_JUEGOS_SUPERADA");
            errores++;
        }

        Map<String, Partida_Organizador> partidas = this.myAgent_organizador.getPartidas();
        if (partidas.size() > MAX_PARTIDAS) {
            justificacion.setDetalle(Vocabulario.Motivo.SUPERADO_LIMITE_PARTIDAS);
            this.myAgent_organizador.addMsgConsole("Error SUPERADO_LIMITE_PARTIDAS");
            errores++;
        }
        if (partidas.get(idJuego) != null) {
            justificacion.setDetalle(Vocabulario.Motivo.JUEGOS_ACTIVOS_SUPERADOS);
            this.myAgent_organizador.addMsgConsole("Error JUEGOS_ACTIVOS_SUPERADOS");
            errores++;
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

            this.myAgent_organizador.addMsgConsole("Propose Completar Partida");
            // Completar Partida
            Partida partida = new Partida();
            partida.setJuego(juego);
            partida.setRonda(0);
            partida.setMaxRondas(10);
            partida.setIdPartida("ID_PARTIDA_" + this.idPartida);
            this.idPartida++;
            this.myAgent_organizador.Propose_CompletarPartida(infoJuego, partida, listaJugadores);
        }

        return reply;
    }

}
