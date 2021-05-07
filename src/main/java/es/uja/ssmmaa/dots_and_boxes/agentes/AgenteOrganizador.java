/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.agentes;

import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.TIME_OUT;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.ESPERA;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskResponserPropose_Organizador;
import es.uja.ssmmaa.dots_and_boxes.project.Constantes;
import es.uja.ssmmaa.dots_and_boxes.tareas.TareaSubscripcionDF;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.interfaces.SubscripcionDF;
import es.uja.ssmmaa.dots_and_boxes.interfaces.MessageInform;
import es.uja.ssmmaa.dots_and_boxes.gui.ConsolaJFrame;
import es.uja.ssmmaa.dots_and_boxes.interfaces.SendMessagesInform;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskSendPropose_Organizador;
import es.uja.ssmmaa.dots_and_boxes.interfaces.TasksOrganizadorSub;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskIniciatorSubscription_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskResponderSubscription_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskSendNotifications_Organizador;
import es.uja.ssmmaa.dots_and_boxes.util.MensajeConsola;
import es.uja.ssmmaa.dots_and_boxes.util.MessageSubscription;

import es.uja.ssmmaa.ontologia.Vocabulario;
import es.uja.ssmmaa.ontologia.Vocabulario.TipoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.CompletarPartida;
import es.uja.ssmmaa.ontologia.juegoTablero.InfoJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.ontologia.juegoTablero.Partida;
import es.uja.ssmmaa.ontologia.juegoTablero.ProponerJuego;
import es.uja.ssmmaa.ontologia.juegoTablero.SubInform;

import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.BeanOntologyException;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <
 *
 * AgenteOrganizador
 *
 * Este es el agente encargado de organizar un juego específico para uno de los
 * dos tipos de juego disponibles. Es decir, solo sabe organizar partidas para
 * El Encerrado o Tuberías. Sus tareas principales serán:
 *
 *      - Aceptar la organización de un juego propuesto por un AgenteMonitor.
 *      Al menos debe aceptar organizar 3 juegos simultáneos.
 *
 *      - Generar las partidas necesarias para los juegos que esté organizando.
 *
 *      - Localizar a AgenteTablero a los que pueda proponer realizar una
 *      partida.
 *
 *      - Obtener los resultados de las partidas para poder completar el juego
 *      al que corresponden.
 *
 *      - Informar del resultado del juego al AgenteMonitor que le solicitó su
 *      organización.
 * >
 *
 * @author nono_
 */
public class AgenteOrganizador extends Agent implements SubscripcionDF, TasksOrganizadorSub, SendMessagesInform<MessageSubscription> {

    private GestorSubscripciones gestor;

    // Para la generación y obtención del contenido de los mensages
    private ContentManager manager;
    // El lenguaje utilizado por el agente para la comunicación es SL 
    private final Codec codec = new SLCodec();
    // Las ontología que utilizará el agente
    private Ontology ontology;
    // Agente consola
    private ConsolaJFrame UI_consola;

    // Deberia ser Map<AID, Arraylist<Product>>, pero a json no le gusta AID como clave :(
    public AID agente_organizador_AID;
    public AID agente_monitor_AID;
    public HashMap<Vocabulario.TipoServicio, ArrayList<AID>> agents;
    public HashMap<String, TaskIniciatorSubscription_Organizador> agentsSubscriptions;

    private ArrayList<MessageSubscription> messagesInformToProcess;
    private ArrayList<MensajeConsola> messagesConsoleToProcess;

    public AgenteOrganizador() {
        this.agents = new HashMap<>();
        this.agentsSubscriptions = new HashMap<>();
        this.messagesInformToProcess = new ArrayList<>();
        this.messagesConsoleToProcess = new ArrayList<>();
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
        sd.setName(Vocabulario.TipoJuego.ENCERRADO.name());
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
        for (Map.Entry<String, TaskIniciatorSubscription_Organizador> entry : this.agentsSubscriptions.entrySet()) {

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
    }

    public void init() {
        //Registro de la Ontología
        this.manager = new ContentManager();
        try {
            this.ontology = Vocabulario.getOntology(TipoJuego.ENCERRADO);
            this.manager = (ContentManager) getContentManager();
            this.manager.registerLanguage(this.codec);
            this.manager.registerOntology(this.ontology);
        } catch (BeanOntologyException ex) {
            this.addMsgConsole("Error al registrar la ontología \n" + ex);
//            consola.addMensaje("Error al registrar la ontología \n" + ex);
            this.doDelete();
        }

        // Suscripción al servicio de páginas amarillas
        // Para localiar a los agentes 
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(Vocabulario.TipoServicio.ORGANIZADOR.name());
        template.addServices(templateSd);
        addBehaviour(new TareaSubscripcionDF(this, template));

        // Plantilla del mensaje de suscripción
        MessageTemplate plantilla;
        plantilla = MessageTemplate.and(
                MessageTemplate.not(
                        MessageTemplate.or(
                                MessageTemplate.MatchSender(this.getDefaultDF()),
                                MessageTemplate.MatchSender(this.getAMS())
                        )
                ),
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)
        );
        addBehaviour(new TaskResponderSubscription_Organizador(this, plantilla, this.gestor));

        // Plantilla para responder mensajes FIPA_PROPOSE
        MessageTemplate template_RP = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)
        );
        addBehaviour(new TaskResponserPropose_Organizador(this, template_RP));

        // Tarea de envio de mensajes inform a los subscriptores
        addBehaviour(new TaskSendNotifications_Organizador(this, ESPERA));

    }

    @Override
    public void requestSubscription(Vocabulario.TipoServicio servicio, AID agent) {
        this.addMsgConsole("createSubscription: " + agent.getLocalName());
        //Creamos el mensaje para lanzar el protocolo Subscribe
        ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
        msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));
        msg.setSender(this.getAID());
        msg.addReceiver(agent);
        // Añadimos la tarea de suscripción
        TaskIniciatorSubscription_Organizador sub = new TaskIniciatorSubscription_Organizador(this, msg);
        addBehaviour(sub);
    }

    @Override
    public void cancelSubscription(AID agente) {
        this.addMsgConsole("cancelSubscription: " + agente.getLocalName());
        TaskIniciatorSubscription_Organizador sub = this.agentsSubscriptions.remove(agente.getLocalName());
        if (sub != null) {
            sub.cancel(agente, true);
            this.addMsgConsole("SUSBCRIPCIÓN CANCELADA PARA\n" + agente);
        }
    }

    @Override
    public void addAgent(AID agente, Vocabulario.TipoServicio servicio) {
        this.addMsgConsole("addAgent  AgentID: " + agente.getLocalName());
        this.addMsgConsole("addAgent Servicio: " + servicio.name());

        ArrayList<AID> lista = this.agents.getOrDefault(servicio, new ArrayList<>());

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
                requestSubscription(servicio, agente);
                lista.add(agente);
                break;
        }

        this.agents.put(servicio, lista);
    }

    @Override
    public boolean removeAgent(AID agente, Vocabulario.TipoServicio servicio) {
        boolean to_return = false;
        ArrayList<AID> lista = this.agents.getOrDefault(servicio, new ArrayList<>());

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
        this.agents.put(servicio, lista);

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
    public Ontology getOntology() {
        return this.ontology;
    }

    @Override
    public ContentManager getManager() {
        return this.manager;
    }

    /* 
     * LOGS
     * ========================================================================
     */
    @Override
    public void addMsgConsole(String msg) {
        this.UI_consola.addMensaje(msg);
    }

    @Override
    public void addMessagesInform(MessageSubscription message) {
        this.messagesInformToProcess.add(message);
    }

    // =============
    // Subscriptions
    // =============
    @Override
    public void addSubcription(String nameAgent, SubscriptionInitiator sub) {
        this.agentsSubscriptions.put(nameAgent, (TaskIniciatorSubscription_Organizador) sub);
    }

    @Override
    public void setResultado(AID agenteOrganizador, ContentElement resultado) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<MessageSubscription> getMessagesInform() {
        return messagesInformToProcess;
    }


    /*
     * ========================================================================
     */
    private void completarPartida(Partida partida, List listaJugadores) {
        // Contenido del mensaje representado en la ontología
        CompletarPartida completarPartida = new CompletarPartida();
        completarPartida.setPartida(partida);
        // TODO
//        completarPartida.setListaJugadores(listaJugadores);

//        String idJuego = tipoJuego.name() + "-" + diaJuego + "-" + numJuego;
//        juego = new Juego(idJuego, tipoJuego);
        // Creamos el mensaje a enviar
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        msg.setSender(getAID());

        msg.setLanguage(codec.getName());
        msg.setOntology(ontology.getName());
        msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

        Action ac = new Action(this.getAID(), completarPartida);

        try {
            // Completamos en contenido del mensajes
            manager.fillContent(msg, ac);
        } catch (Codec.CodecException | OntologyException ex) {
            this.addMsgConsole("Error en la construcción del mensaje en Proponer Juego \n" + ex);
//            consola.addMensaje("Error en la construcción del mensaje en Proponer Juego \n" + ex);
        }

        TaskSendPropose_Organizador task = new TaskSendPropose_Organizador(this, msg, completarPartida);
        addBehaviour(task);
    }

    private void proponerPartidaATablero(Juego juego, Vocabulario.Modo modo, InfoJuego infoJuego) {
        // Contenido del mensaje representado en la ontología
        ProponerJuego proponerJuego = new ProponerJuego();
        proponerJuego.setJuego(juego);
        proponerJuego.setModo(modo);
        proponerJuego.setInfoJuego(infoJuego);

        // Creamos el mensaje a enviar
        ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
        msg.setSender(getAID());

        msg.setLanguage(this.codec.getName());
        msg.setOntology(this.ontology.getName());
        msg.setReplyByDate(new Date(System.currentTimeMillis() + TIME_OUT));

        Action ac = new Action(this.getAID(), proponerJuego);

        try {
            // Completamos en contenido del mensajes
            manager.fillContent(msg, ac);
        } catch (Codec.CodecException | OntologyException ex) {
            this.addMsgConsole("Error en la construcción del mensaje en Proponer Juego \n" + ex);
//            consola.addMensaje("Error en la construcción del mensaje en Proponer Juego \n" + ex);
        }

        TaskSendPropose_Organizador task = new TaskSendPropose_Organizador(this, msg, proponerJuego);
        addBehaviour(task);
    }
}
