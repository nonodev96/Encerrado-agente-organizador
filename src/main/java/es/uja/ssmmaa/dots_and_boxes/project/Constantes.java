/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.project;

import java.util.Random;

/**
 *
 * @author pedroj
 */
public interface Constantes {

    public static final Random aleatorio = new Random();
    public static final long ESPERA = 1000; // 1 segundo
    public static final long TIME_OUT = 2000; // 2 segundos;
    public static final int NO_ENCONTRADO = -1;
    public static final int ACEPTAR = 85; // 85% de aceptación para la operación 
    public static final int PRIMERO = 0;
    public static final int SEGUNDO = 1;
    public static final int MONEY = 250;
    
    public static final long TIME_PER_BID_ROUND_SECONDS = 40;
    public static final long TIME_IN_WHICH_THE_BID_IS_REFRESHED__SECONDS = 1;

    public enum Status {
        RET_CANCEL(0), RET_OK(1);

        private int statusValue;

        private Status(int statusValue) {
            this.statusValue = statusValue;
        }
    }

    public enum TipoServicio {
        GUI, UTILIDAD, SISTEMA;
    }

    public static final TipoServicio[] TIPOS = TipoServicio.values();

    public enum NombreServicio {
        ORGANIZADOR, JUGADOR, TABLERO, MONITOR;
    }

    public static final NombreServicio[] SERVICIOS = NombreServicio.values();

}
