/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uja.ssmmaa.dots_and_boxes.util;

/**
 *
 * @author pedroj
 */
public class Response {

    private String content_1;
    private String content_2;

    public Response() {
        this.content_1 = null;
        this.content_2 = null;
    }

    public Response(String contenido_1) {
        this.content_1 = contenido_1;
        this.content_2 = "";
    }

    public Response(String contenido_1, String contenido_2) {
        this.content_1 = contenido_1;
        this.content_2 = contenido_2;
    }

    public String getContenido() {
        return content_1;
    }

    public String getContenido_2() {
        return content_2;
    }

    public void setContenido(String contenido_1) {
        this.content_1 = contenido_1;
    }

    public void setContenido_2(String contenido_2) {
        this.content_2 = contenido_2;
    }

    @Override
    public String toString() {
        return "{ name: 'Result'"
                + ", content: '" + content_1 + "'"
                + ", content_2: '" + content_2 + "'}";
    }
}
