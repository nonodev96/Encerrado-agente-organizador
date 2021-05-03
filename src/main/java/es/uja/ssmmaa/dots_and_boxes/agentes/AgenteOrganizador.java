/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.agentes;

import es.uja.ssmmaa.dots_and_boxes.tareas.TaskRP_Organizador;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.NombreServicio.ORGANIZADOR;
import static es.uja.ssmmaa.dots_and_boxes.project.Constantes.TipoServicio.SISTEMA;
import es.uja.ssmmaa.dots_and_boxes.project.Constantes;
import es.uja.ssmmaa.dots_and_boxes.tareas.TareaSubscripcionDF;
import es.uja.ssmmaa.dots_and_boxes.tareas.SubscripcionDF;

import es.uja.ssmmaa.dots_and_boxes.util.GestorSubscripciones;

import com.google.gson.Gson;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
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
public class AgenteOrganizador extends Agent implements SubscripcionDF {

    private GestorSubscripciones gestor;
    private Gson gson;

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

        // Registro del agente en las Páginas Amarrillas
        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());
        ServiceDescription templateSD = new ServiceDescription();
        templateSD.setType(SISTEMA.name());
        templateSD.setName(ORGANIZADOR.name());
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
        // Liberación de recursos, incluido el GUI

        // Despedida
        System.out.println("Finaliza la ejecución del agente: " + this.getName());
    }

    public void init() {
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
        addBehaviour(new TaskRP_Organizador(this, template_RP));
    }

    private void crearSubscripcion(Constantes.NombreServicio servicio, AID agente) {
        System.out.println("crearSubscripcion: " + agente.getLocalName());
        ArrayList<AID> lista_subscritos = this.agentes_subscritos.getOrDefault(servicio, new ArrayList<>());
        lista_subscritos.add(agente);
        this.agentes_subscritos.put(servicio, lista_subscritos);
    }

    private void cancelarSubscripcion(Constantes.NombreServicio servicio, AID agente) {
        System.out.println("cancelarSubscripcion: " + agente.getLocalName());

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
        System.out.println("addAgent  AgentID: " + agente.getLocalName());
        System.out.println("addAgent Servicio: " + servicio.name());

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

    public void log(String content) {

    }

    /**
     * ========================================================================
     */
}
