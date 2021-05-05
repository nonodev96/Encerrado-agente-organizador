/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.agentes;

import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.NombreServicio.ORGANIZADOR;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.TipoServicio.SISTEMA;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.TIME_OUT;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskReceivePropose_Organizador;
import es.uja.ssmmaa.dots_and_boxes.project.Constantes;
import es.uja.ssmmaa.dots_and_boxes.tareas.TareaSubscripcionDF;
import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;
import es.uja.ssmmaa.dots_and_boxes.tareas.SubscripcionDF;

import com.google.gson.Gson;

import es.uja.ssmmaa.curso1920.ontologia.Vocabulario;
import es.uja.ssmmaa.curso1920.ontologia.Vocabulario.TipoJuego;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.CompletarPartida;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.InfoJuego;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.Juego;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.Partida;
import es.uja.ssmmaa.curso1920.ontologia.juegoTablero.ProponerJuego;
import es.uja.ssmmaa.dots_and_boxes.gui.ConsolaJFrame;
import es.uja.ssmmaa.dots_and_boxes.tareas.TaskSendPropose_Organizador;
import es.uja.ssmmaa.dots_and_boxes.tareas.TasksOrganizadorSub;
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
import jade.util.leap.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
public class AgenteOrganizador extends Agent implements SubscripcionDF, TasksOrganizadorSub {

    private GestorSubscripciones gestor;
    private Gson gson;

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
    public HashMap<Constantes.NombreServicio, ArrayList<AID>> agentes;
    public HashMap<Constantes.NombreServicio, ArrayList<AID>> agentes_subscritos;

    public AgenteOrganizador() {
        this.gson = new Gson();
        this.agentes = new HashMap<>();
        this.agentes_subscritos = new HashMap<>();
    }

    @Override
    protected void setup() {
        // Inicialización de las variables del agente
        this.gestor = new GestorSubscripciones();
        this.agente_organizador_AID = getAID();
        this.UI_consola = new ConsolaJFrame(this);

        // Registro del agente en las Páginas Amarrillas
        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());
        ServiceDescription templateSD = new ServiceDescription();

        templateSD.setType(Vocabulario.TipoServicio.ORGANIZADOR.name());
        templateSD.setName(Vocabulario.TipoJuego.TRES_EN_RAYA.name());

        template.addServices(templateSD);
        try {
            DFService.register(this, template);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        init();
    }

    @Override
    protected void takeDown() {
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
            this.ontology = Vocabulario.getOntology(TipoJuego.TRES_EN_RAYA);
            this.manager = (ContentManager) getContentManager();
            this.manager.registerLanguage(this.codec);
            this.manager.registerOntology(this.ontology);
        } catch (BeanOntologyException ex) {
            this.addMsgConsola("Error al registrar la ontología \n" + ex);
//            consola.addMensaje("Error al registrar la ontología \n" + ex);
            this.doDelete();
        }

        // Suscripción al servicio de páginas amarillas
        // Para localiar a los agentes 
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription templateSd = new ServiceDescription();
        templateSd.setType(SISTEMA.name());
        template.addServices(templateSd);
        addBehaviour(new TareaSubscripcionDF(this, template));

        // Plantilla para responder mensajes FIPA_PROPOSE
        MessageTemplate template_RP = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE),
                MessageTemplate.MatchPerformative(ACLMessage.PROPOSE)
        );
        addBehaviour(new TaskReceivePropose_Organizador(this, template_RP));
    }

    private void crearSubscripcion(Constantes.NombreServicio servicio, AID agente) {
        this.addMsgConsola("crearSubscripcion: " + agente.getLocalName());
        ArrayList<AID> lista_subscritos = this.agentes_subscritos.getOrDefault(servicio, new ArrayList<>());
        lista_subscritos.add(agente);
        this.agentes_subscritos.put(servicio, lista_subscritos);
    }

    private void cancelarSubscripcion(Constantes.NombreServicio servicio, AID agente) {
        this.addMsgConsola("cancelarSubscripcion: " + agente.getLocalName());

        ArrayList<AID> lista_subscritos = this.agentes_subscritos.getOrDefault(servicio, new ArrayList<>());
        ArrayList<AID> list_to_delete_subs = new ArrayList<>();
        for (AID aid : lista_subscritos) {
            if (aid.getLocalName().equals(agente.getLocalName())) {
                list_to_delete_subs.add(aid);
            }
        }
        lista_subscritos.removeAll(list_to_delete_subs);
        this.agentes.put(servicio, lista_subscritos);
    }

    @Override
    public void addAgent(AID agente, Constantes.NombreServicio servicio) {
        this.addMsgConsola("addAgent  AgentID: " + agente.getLocalName());
        this.addMsgConsola("addAgent Servicio: " + servicio.name());

        ArrayList<AID> lista = this.agentes.getOrDefault(servicio, new ArrayList<>());

        switch (servicio) {
            case JUGADOR:

                lista.add(agente);
                break;
            case MONITOR:
                lista.add(agente);
                break;
            case ORGANIZADOR:

                break;
            case TABLERO:
                crearSubscripcion(servicio, agente);
                lista.add(agente);
                break;
        }

        this.agentes.put(servicio, lista);
    }

    @Override
    public boolean removeAgent(AID agente, Constantes.NombreServicio servicio) {
        boolean to_return = false;
        ArrayList<AID> lista = this.agentes.getOrDefault(servicio, new ArrayList<>());

        // Seleccionamos el que vamos a borrar
        ArrayList<AID> list_to_delete = new ArrayList<>();
        for (AID aid : lista) {
            if (aid.getLocalName().equals(agente.getLocalName())) {
                list_to_delete.add(aid);

                to_return = true;
            }
        }

        this.cancelarSubscripcion(servicio, agente);

        // Actualizamos
        lista.removeAll(list_to_delete);
        this.agentes.put(servicio, lista);

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
    public void addMsgConsola(String msg) {
        this.UI_consola.addMensaje(msg);
    }

    // =============
    // Subscriptions
    // =============
    @Override
    public void addSubcription(String nameAgente, SubscriptionInitiator sub) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setResultado(AID agenteOrganizador, ContentElement resultado) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*
     * ========================================================================
     */
    private void completarPartida(Partida partida, List listaJugadores) {
        // Contenido del mensaje representado en la ontología
        CompletarPartida completarPartida = new CompletarPartida();
        completarPartida.setPartida(partida);
        completarPartida.setListaJugadores(listaJugadores);

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
            this.addMsgConsola("Error en la construcción del mensaje en Proponer Juego \n" + ex);
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
            this.addMsgConsola("Error en la construcción del mensaje en Proponer Juego \n" + ex);
//            consola.addMensaje("Error en la construcción del mensaje en Proponer Juego \n" + ex);
        }

        TaskSendPropose_Organizador task = new TaskSendPropose_Organizador(this, msg, proponerJuego);
        addBehaviour(task);
    }
}
