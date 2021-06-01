Agente Organizador
==================

# Organizador - Encerrado

Este agente solo se encarga de organizar una partida entre el monitor y los jugadores.
Y recibe la respuesta con el `ResultadoPartida` e `IncidenciaJuego`


El incidencia juego no he conseguido saber a quien se lo envio, es decir un organizador recibe del tablero una incidencia juego, y yo como organizador he de enviarselo a mi monitor, es decir con el que he empezado la partida, pero en `IncidenciaJuego` no tengo el idPartida, y por tanto no sé quien es mi monitor, podria enviarselo a todos los monitores, pero no es lo adecuado.

Para modificar esto, solo habria que descomentar la linea `130` en `TaskIniciatorSubscription_Organizador` y se enviaria a todos, pero creo que no es lo correcto

## Dudas

* En CompletarJuego por que el `completarJuego.setAgenteJuego()` por que recibe un `InfoJuego`, no deberia ser un `AgenteJuego`?