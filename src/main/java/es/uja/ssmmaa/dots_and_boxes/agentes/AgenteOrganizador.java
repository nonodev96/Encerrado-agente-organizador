/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.agentes;

import es.uja.ssmmaa.ontologia.Vocabulario;
import es.uja.ssmmaa.ontologia.Vocabulario.TipoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.CompletarPartida;
import es.uja.ssmmaa.ontologia.juegoTablero.InfoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.Partida;
import es.uja.ssmmaa.ontologia.juegoTablero.ProponerJuego;

import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.TIME_OUT_1;
import es.uja.ssmmaa.dots_and_boxes.gui.ConsolaJFrame;
import es.uja.ssmmaa.dots_and_boxes.interfaces.TasksOrganizadorSub;
import es.uja.ssmmaa.dots_and_boxes.interfaces.SubscripcionDF;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.MY_GAME;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskResponderSubscription_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskIniciatorSubscription_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskSendNotifications_Organizador_InformarResultado;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskResponserPropose_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskSendPropose_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TareaSubscripcionDF;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.project.Partida_Organizador;

import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import jade.util.leap.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * <h2> AgenteOrganizador</h2>
 *
 * <p>
 * Este es el agente encargado de organizar un juego específico para uno de los
 * dos tipos de juego disponibles.
 * </p>
 *
 * <pre>
 * Es decir, solo sabe organizar partidas para El Encerrado o Tuberías.
 * Sus tareas principales serán:
 *
 *  - Aceptar la organización de un juego propuesto por un AgenteMonitor.
 *  Al menos debe aceptar organizar 3 juegos simultáneos.
 *
 *  - Generar las partidas necesarias para los juegos que esté organizando.
 *
 *  - Localizar a AgenteTablero a los que pueda proponer realizar una partida.
 *
 *  - Obtener los resultados de las partidas para poder completar el juego al
 * que corresponden.
 *
 *  - Informar del resultado del juego al AgenteMonitor que le solicitó su
 *  organización.
 *
 * </pre>
 *
 * @author nono_
 */
public class AgenteOrganizador extends Agent implements SubscripcionDF, TasksOrganizadorSub {

    private GestorSubscripciones gestor;

    // Para la generación y obtención del contenido de los mensages
    private ContentManager manager;
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private Codec codec;
    // Las ontología que utilizará el agente
    private Ontology ontology;
    // Agente consola
    private ConsolaJFrame UI_consola;

    // Deberia ser Map<AID, Arraylist<Product>>, pero a json no le gusta AID como clave :(
    public AID agente_organizador_AID;
    public AID agente_monitor_AID;
    private Map<String, Deque<AID>> agentesConocidos;
    private Map<String, TaskIniciatorSubscription_Organizador> subActivas;
    private Map<String, Partida_Organizador> partidasMap;

    public AgenteOrganizador() {
        this.agentesConocidos = new HashMap<>();
        this.subActivas = new HashMap<>();
        this.partidasMap = new HashMap<>();
    }

    @Override
    protected void setup() {
        // Inicialización de las variables del agente
        this.gestor = new GestorSubscripciones();
        this.agente_organizador_AID = getAID();
        this.UI_consola = new ConsolaJFrame(this);

        // Registro del agente en las Páginas Amarrillas
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Vocabulario.TipoServicio.ORGANIZADOR.name());
        sd.setName(MY_GAME.name());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        init();
    }

    @Override
    protected void takeDown() {
        // Cancelamos las subscripciones
        for (Map.Entry<String, TaskIniciatorSubscription_Organizador> entry : this.subActivas.entrySet()) {

        }
        // Eliminar registro del agente en las Páginas Amarillas
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // Despedida
        this.UI_consola.addMensaje("Finaliza la ejecución del agente: " + this.getName());

        // Liberación de recursos, incluido el GUI
        this.UI_consola.dispose();

        MicroRuntime.stopJADE();
    }

    public void init() {
        //Registro de la Ontología
        this.manager = new ContentManager();
        this.codec = new SLCodec();
        try {
            this.ontology = Vocabulario.getOntology(MY_GAME);
            this.manager = (ContentManager) getContentManager();
            this.manager.registerLanguage(this.codec);
            this.manager.registerOntology(this.ontology);
        } catch (BeanOntologyException ex) {
            this.addMsgConsole("Error al registrar la ontología \n" + ex);
            this.doDelete();
            MicroRuntime.stopJADE();
        }

        // Suscripción al servicio de páginas amarillas
        // Para localiar a los agentes 
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        template.addServices(templateSd);
        addBehaviour(new TareaSubscripcionDF(this, template));

        // Plantilla para mensajes de FIPA_SUBSCRIBE
        MessageTemplate template_SUBS = MessageTemplate.and(
                MessageTemplate.not(
                        MessageTemplate.or(
                                MessageTemplate.MatchSender(this.getDefaultDF()),
                                MessageTemplate.MatchSender(this.getAMS())
                        )
                ),
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)
        );
        addBehaviour(new TaskResponderSubscription_Organizador(this, template_SUBS, this.getGestor()));

        // Plantilla para responder mensajes FIPA_PROPOSE
        MessageTemplate template_RP = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)
        );
        addBehaviour(new TaskResponserPropose_Organizador(this, template_RP));

        // Tarea de envio de mensajes inform a los subscriptores
        // TODO
        //addBehaviour(new TaskSendNotifications_Organizador(this, idPartida));
    }

    @Override
    public void addAgent(AID agente, Vocabulario.TipoJuego juego, Vocabulario.TipoServicio servicio) {
        this.addMsgConsole("=============================================");
        this.addMsgConsole("addAgent  AgentID: " + agente.getLocalName());
        this.addMsgConsole("addAgent    juego: " + juego.name());
        this.addMsgConsole("addAgent Servicio: " + servicio.name());
        this.addMsgConsole("=============================================");

        Deque<AID> lista = this.agentesConocidos.getOrDefault(servicio.name() + juego.name(), new LinkedList<>());

        switch (servicio) {
            case JUGADOR:

                lista.add(agente);
                break;
//            case MONITOR:
//                lista.add(agente);
//                break;
            case ORGANIZADOR:

                break;
            case TABLERO:
                ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
                msg.setSender(this.getAID());
                msg.setLanguage(codec.getName());
                msg.setOntology(ontology.getName());
                // AID Tablero
                msg.addReceiver(agente);
                msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT_1));

                TaskIniciatorSubscription_Organizador task = new TaskIniciatorSubscription_Organizador(this, msg);
                addBehaviour(task);

                lista.add(agente);
                break;
        }
        this.addMsgConsole(String.format("Se ha añadido a agentesConocidos (%s) = %s", servicio.name() + juego.name(), "la lista de " + Arrays.toString(lista.toArray())));
        this.agentesConocidos.put(servicio.name() + juego.name(), lista);
    }

    @Override
    public boolean removeAgent(AID agente, Vocabulario.TipoJuego juego, Vocabulario.TipoServicio servicio) {
        boolean to_return = false;
        Deque<AID> lista = this.agentesConocidos.getOrDefault(servicio.name() + juego.name(), new LinkedList<>());

        // Seleccionamos el que vamos a borrar
        ArrayList<AID> list_to_delete = new ArrayList<>();
        for (AID aid : lista) {
            if (aid.getLocalName().equals(agente.getLocalName())) {
                list_to_delete.add(aid);
                to_return = true;
            }
        }

        this.cancelSubscription(agente);

        // Actualizamos
        lista.removeAll(list_to_delete);
        this.agentesConocidos.put(servicio.name() + juego.name(), lista);

        return to_return;
    }

    /*
     * Metodos de las interfaces
     * ========================================================================
     */
    @Override
    public GestorSubscripciones getGestor() {
        return this.gestor;
    }

    @Override
    public ContentManager getManager() {
        return this.manager;
    }

    @Override
    public Ontology getOntology() {
        return this.ontology;
    }

    @Override
    public Codec getCodec() {
        return codec;
    }

    /* 
     * LOGS
     * ========================================================================
     */
    @Override
    public void addMsgConsole(String msg) {
        System.out.println(msg);
        this.UI_consola.addMensaje(msg);
    }

    // =============
    // Subscriptions
    // =============
    @Override
    public void addSubscription(AID agent, SubscriptionInitiator sub) {
        this.addMsgConsole("addSubscription: " + agent.getLocalName());
        this.subActivas.put(agent.getLocalName(), (TaskIniciatorSubscription_Organizador) sub);
    }

    @Override
    public void cancelSubscription(AID agente) {
        this.addMsgConsole("cancelSubscription: " + agente.getLocalName());
        TaskIniciatorSubscription_Organizador sub = this.subActivas.remove(agente.getLocalName());

        if (sub != null) {
            sub.cancel(agente, true);
            this.addMsgConsole("SUSBCRIPCIÓN CANCELADA PARA\n" + agente);
        }
    }

    public Map<String, Partida_Organizador> getPartidas() {
        return partidasMap;
    }

    @Override
    public Partida_Organizador getPartida(String idPartida) {
        return this.partidasMap.getOrDefault(idPartida, null);
    }

    /**
     * <pre>
     * El AgenteOrganizador se encarga de ir generando las rondas necesarias
     * para completar el juego.El número de rondas dependerá del atributo
     * Modo del elemento CompletarJuego. En cada ronda se generan un número de
     * partidas que deben ser completadas por el AgenteTablero.
     * </pre>
     *
     * @param infoJuego
     * @param partida
     * @param listaJugadores
     */
    public void Propose_CompletarPartida(InfoJuego infoJuego, Partida partida, List listaJugadores) {
        // Contenido del mensaje representado en la ontología
        CompletarPartida completarPartida = new CompletarPartida();
        // TODO
        completarPartida.setPartida(partida);
        completarPartida.setListaJugadores(listaJugadores);
        completarPartida.setInfoJuego(infoJuego);

        // Seleccionamos el tablero del encerrado para completar partida
        AID tableroEncerrado = this.agentesConocidos.get(Vocabulario.TipoServicio.TABLERO.name() + Vocabulario.TipoJuego.ENCERRADO.name()).getFirst();
        this.addMsgConsole("Se envia a " + tableroEncerrado.getLocalName());

        // Creamos el mensaje a enviar
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        msg.setSender(getAID());
        msg.addReceiver(tableroEncerrado);
        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT_1));

        Action ac = new Action(this.getAID(), completarPartida);

        try {
            // Completamos en contenido del mensajes
            manager.fillContent(msg, ac);
        } catch (Codec.CodecException | OntologyException ex) {
            this.addMsgConsole("Error en la construcción del mensaje en Completar Partida");
            this.addMsgConsole(ex.toString());
        }
        this.addMsgConsole("Envio la propuesta de completar partida al tablero");
        TaskSendPropose_Organizador task = new TaskSendPropose_Organizador(this, msg);
        addBehaviour(task);
    }

    /**
     * <p>
     * Tu modificas tu partida con getPartida(idPartida) Modificas lo que pase y
     * llamas a este método y se envia el mensaje.
     * </p>
     *
     * @param idPartida
     */
    public void Inform_ClasificacionJuego_o_IncidenciaJuego(String idPartida) {

        addBehaviour(new TaskSendNotifications_Organizador_InformarResultado(this, idPartida));
    }
}
